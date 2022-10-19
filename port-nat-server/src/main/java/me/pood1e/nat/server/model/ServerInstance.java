package me.pood1e.nat.server.model;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseInstance;
import me.pood1e.nat.common.model.ProxyMessage;
import me.pood1e.nat.common.model.ProxyMessageType;
import me.pood1e.nat.common.util.SslUtils;
import me.pood1e.nat.server.handler.ServerOutBoundInitializer;
import me.pood1e.nat.server.handler.ServerProxyInitializer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author pood1e
 */
@Slf4j
public class ServerInstance extends BaseInstance implements ServerChannelFunction, ServerProxyFunction {

	private static final String LOCAL_IP = "127.0.0.1";
	private final Map<String, Condition> channelCondition = new HashMap<>();
	private final Lock lock = new ReentrantLock();
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final Server server;
	private final ServerBootstrap outServer;
	private final ChannelFuture proxyFuture;
	private List<ChannelFuture> outFutures;

	public ServerInstance(Server server) {
		super(server.isSslEnable() ? SslUtils.getServerSsl() : null);
		this.server = server;
		bossGroup = new NioEventLoopGroup(2 * server.getMaxConnection());
		workerGroup = new NioEventLoopGroup(2 * server.getMaxConnection());
		outServer = defaultServerBootstrap();
		outServer.childHandler(new ServerOutBoundInitializer(this));
		ServerBootstrap proxyServer = defaultServerBootstrap();
		proxyServer.childHandler(new ServerProxyInitializer(this, server));
		proxyServer.childHandler(new ServerProxyInitializer(this, server));
		proxyFuture = proxyServer.bind(server.getPort()).addListener(future -> {
			if (future.isSuccess()) {
				log.info("open proxy {}", server.getPort());
			}
		});
	}

	private ServerBootstrap defaultServerBootstrap() {
		return new ServerBootstrap().group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, true);
	}

	public synchronized Optional<Map.Entry<Channel, Map<String, Channel>>> bestProxy() {
		return proxyMap.entrySet().stream().min(Comparator.comparingInt(o -> o.getValue().size()));
	}

	public void close() {
		closeListen(proxyFuture);
		proxyMap.forEach((channel, stringChannelMap) -> {
			channel.close();
			stringChannelMap.values().forEach(ChannelOutboundInvoker::close);
		});
	}

	@Override
	public synchronized void addProxy(Channel proxy) {
		checkProxy(true);
		super.addProxy(proxy);
	}

	@Override
	public synchronized void removeProxy(Channel proxy) {
		super.removeProxy(proxy);
		checkProxy(false);
	}


	@Override
	public synchronized void active(Channel ch) {
		Optional<Map.Entry<Channel, Map<String, Channel>>> entryOptional = bestProxy();
		if (entryOptional.isPresent()) {
			String key = NanoIdUtils.randomNanoId();
			channelKey.put(ch, key);
			entryOptional.get().getValue().put(key, ch);
			entryOptional.get().getKey().writeAndFlush(
					new ProxyMessage.ConnectionRequest(ProxyMessageType.OPEN_CONNECTION,
							key,
							(short) ((InetSocketAddress) ch.localAddress()).getPort()));
		} else {
			ch.close();
		}
	}

	@Override
	public void waitOpen(Channel ch, OpenResult openResult) {
		lock.lock();
		try {
			if (channelProxy.get(ch) == null) {
				Condition condition = lock.newCondition();
				channelCondition.put(channelKey.get(ch), condition);
				if (condition.await(server.getTimeout(), TimeUnit.MILLISECONDS)) {
					openResult.success();
				} else {
					openResult.failed();
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void openSuccess(Channel proxy, String key) {
		lock.lock();
		try {
			channelProxy.put(proxyMap.get(proxy).get(key), proxy);
			Condition condition = channelCondition.remove(key);
			if (condition != null) {
				condition.signalAll();
			}
		} finally {
			lock.unlock();
		}
	}


	@Override
	public synchronized void inactive(Channel ch) {
		String key = channelKey.get(ch);
		channelCondition.remove(key);
		super.inactive(ch);
	}

	private synchronized void checkProxy(boolean on) {
		if (proxyMap.isEmpty()) {
			if (on) {
				outFutures = server.outPorts.entrySet().stream().map(entry -> entry.getValue() ?
						outServer.bind(LOCAL_IP, entry.getKey()) :
						outServer.bind(entry.getKey())).collect(Collectors.toList());
			} else {
				closeListen(outFutures);
			}
		}
	}

	private synchronized void closeListen(ChannelFuture future) {
		if (future != null) {
			if (future.isSuccess()) {
				future.channel().close();
			}
			if (!future.isDone()) {
				future.cancel(true);
			}
		}
	}

	private synchronized void closeListen(List<ChannelFuture> futures) {
		futures.forEach(this::closeListen);
	}
}

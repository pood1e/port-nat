package me.pood1e.nat.client.model;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.client.handler.ClientInBoundInitializer;
import me.pood1e.nat.client.handler.ClientProxyInitializer;
import me.pood1e.nat.common.model.BaseChannelFunction;
import me.pood1e.nat.common.model.BaseInstance;
import me.pood1e.nat.common.model.ProxyMessage;
import me.pood1e.nat.common.model.ProxyMessageType;
import me.pood1e.nat.common.util.SslUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author pood1e
 */
@Slf4j
public class ClientInstance extends BaseInstance implements ClientProxyFunction, BaseChannelFunction {

	private final Client client;

	private final NioEventLoopGroup loop;

	public ClientInstance(Client client) {
		super(client.isSslEnable() ? SslUtils.getClientSsl() : null);
		loop = new NioEventLoopGroup(client.getMaxConnection() * 2);
		this.client = client;
		for (int i = 0; i < client.getMaxConnection(); i++) {
			connectProxy();
		}
	}

	private Bootstrap defaultBootstrap() {
		return new Bootstrap().group(loop).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	@Override
	public void connectProxy() {
		Bootstrap bootstrap = defaultBootstrap();
		bootstrap.handler(new ClientProxyInitializer(this, client));
		bootstrap.connect(client.address, client.getPort())
				.addListener((ChannelFutureListener) future -> {
					if (!future.isSuccess()) {
						reconnect();
					}
				});
	}

	@Override
	public void reconnect() {
		loop.schedule(this::connectProxy, client.retryDelay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void openChannel(Channel proxy, ProxyMessage.ConnectionRequest request) {
		ClientAddress address = client.outMapper.get((int) request.getPort());
		defaultBootstrap().handler(new ClientInBoundInitializer(this))
				.connect(address.getAddress(), address.getPort())
				.addListener((ChannelFutureListener) future -> {
					proxy.writeAndFlush(
							new ProxyMessage.ConnectionResponse(
									future.isSuccess() ?
											ProxyMessageType.OPEN_CONNECTION_SUCCESS :
											ProxyMessageType.CLOSE_CONNECTION, request.getKey()));
					if (future.isSuccess()) {
						channelKey.put(future.channel(), request.getKey());
						channelProxy.put(future.channel(), proxy);
						proxyMap.get(proxy).put(request.getKey(), future.channel());
					}
				});
	}
}

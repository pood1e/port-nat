package me.pood1e.nat.common.model;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.ssl.SslContext;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pood1e
 */
public abstract class BaseInstance implements BaseChannelFunction, BaseProxyFunction {

	@Getter
	protected final SslContext sslContext;
	protected final Map<Channel, Map<String, Channel>> proxyMap = new HashMap<>();
	protected final Map<Channel, String> channelKey = new HashMap<>();
	protected final Map<Channel, Channel> channelProxy = new HashMap<>();

	protected BaseInstance(SslContext sslContext) {
		this.sslContext = sslContext;
	}


	@Override
	public synchronized void addProxy(Channel proxy) {
		proxyMap.put(proxy, new HashMap<>());
	}

	@Override
	public synchronized void removeProxy(Channel proxy) {
		proxyMap.remove(proxy).values().forEach(ChannelOutboundInvoker::close);
	}

	@Override
	public void proxyWrite(Channel proxy, ProxyMessage.TransferData transferData) {
		Channel channel = proxyMap.get(proxy).get(transferData.getConnectionKey());
		if (channel != null) {
			channel.writeAndFlush(Unpooled.wrappedBuffer(transferData.getData()));
		}
	}

	@Override
	public void closeChannel(Channel proxy, String key) {
		Channel channel = proxyMap.get(proxy).remove(key);
		if (channel != null) {
			channel.close();
		}
	}

	@Override
	public synchronized void channelWrite(Channel ch, ByteBuf buf) {
		String key = channelKey.get(ch);
		Channel proxy = channelProxy.get(ch);
		byte[] data = new byte[buf.readableBytes()];
		buf.readBytes(data);
		ProxyMessage.TransferData transferData = new ProxyMessage.TransferData(key, data);
		proxy.writeAndFlush(transferData);
	}

	@Override
	public synchronized void inactive(Channel ch) {
		String key = channelKey.remove(ch);
		if (key != null) {
			Channel proxy = channelProxy.remove(ch);
			if (proxy != null) {
				if (proxyMap.get(proxy).remove(key) != null) {
					proxy.writeAndFlush(new ProxyMessage.ConnectionResponse(ProxyMessageType.CLOSE_CONNECTION, key));
				}
			}
		}
	}
}

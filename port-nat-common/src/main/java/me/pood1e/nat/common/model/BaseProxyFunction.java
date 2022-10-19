package me.pood1e.nat.common.model;

import io.netty.channel.Channel;

/**
 * @author pood1e
 */
public interface BaseProxyFunction extends SslProvider {

	void addProxy(Channel proxy);

	void removeProxy(Channel proxy);

	void proxyWrite(Channel proxy, ProxyMessage.TransferData transferData);

	void closeChannel(Channel proxy, String key);

}

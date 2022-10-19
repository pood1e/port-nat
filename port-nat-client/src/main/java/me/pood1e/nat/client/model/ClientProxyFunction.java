package me.pood1e.nat.client.model;

import io.netty.channel.Channel;
import me.pood1e.nat.common.model.BaseProxyFunction;
import me.pood1e.nat.common.model.ProxyMessage;

/**
 * @author pood1e
 */
public interface ClientProxyFunction extends BaseProxyFunction {

	void connectProxy();

	void reconnect();

	void openChannel(Channel proxy, ProxyMessage.ConnectionRequest request);
}

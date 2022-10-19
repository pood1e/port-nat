package me.pood1e.nat.server.model;

import io.netty.channel.Channel;
import me.pood1e.nat.common.model.BaseProxyFunction;

/**
 * @author pood1e
 */
public interface ServerProxyFunction extends BaseProxyFunction {
	void openSuccess(Channel proxy, String key);
}

package me.pood1e.nat.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.util.ChannelUtils;
import me.pood1e.nat.server.model.ServerProxyFunction;

/**
 * @author pood1e
 */
@Slf4j
public class ServerProxyInitializer extends ChannelInitializer<Channel> {

	private final ServerProxyFunction instance;
	private final BaseConfig config;

	public ServerProxyInitializer(ServerProxyFunction instance, BaseConfig config) {
		this.instance = instance;
		this.config = config;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelUtils.initial(ch, config, instance);
		ch.pipeline().addLast(new ServerEventHandler(instance, config));
	}
}

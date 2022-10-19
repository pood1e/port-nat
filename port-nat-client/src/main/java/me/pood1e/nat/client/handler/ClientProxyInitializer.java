package me.pood1e.nat.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.client.model.ClientProxyFunction;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.util.ChannelUtils;

/**
 * @author pood1e
 */
@Slf4j
@AllArgsConstructor
public class ClientProxyInitializer extends ChannelInitializer<Channel> {

	private final ClientProxyFunction instance;
	private final BaseConfig config;


	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelUtils.initial(ch, config, instance);
		ch.pipeline().addLast(new ClientEventHandler(instance, config));
	}
}

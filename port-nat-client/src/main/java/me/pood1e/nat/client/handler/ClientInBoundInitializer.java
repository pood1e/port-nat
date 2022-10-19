package me.pood1e.nat.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseChannelFunction;

/**
 * @author pood1e
 */
@Slf4j
public class ClientInBoundInitializer extends ChannelInitializer<Channel> {

	private final BaseChannelFunction instance;

	public ClientInBoundInitializer(BaseChannelFunction instance) {
		this.instance = instance;
	}

	@Override
	protected void initChannel(Channel ch) {
		ch.pipeline()
				.addLast(new LoggingHandler(LogLevel.DEBUG))
				.addLast(new ClientInBoundHandler(instance));
	}
}

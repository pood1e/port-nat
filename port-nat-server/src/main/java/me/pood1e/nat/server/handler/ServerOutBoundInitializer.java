package me.pood1e.nat.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.server.model.ServerChannelFunction;


/**
 * @author pood1e
 */
@Slf4j
public class ServerOutBoundInitializer extends ChannelInitializer<Channel> {

	private final ServerChannelFunction instance;

	public ServerOutBoundInitializer(ServerChannelFunction instance) {
		this.instance = instance;
	}

	@Override
	protected void initChannel(Channel ch) {
		ch.pipeline()
				.addLast(new LoggingHandler(LogLevel.DEBUG))
				.addLast(new ServerOutBoundHandler(instance));
	}
}

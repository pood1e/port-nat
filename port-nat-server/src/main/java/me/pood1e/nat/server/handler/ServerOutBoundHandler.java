package me.pood1e.nat.server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.handler.RealBoundHandler;
import me.pood1e.nat.server.model.ServerChannelFunction;

/**
 * @author pood1e
 */
@Slf4j
public class ServerOutBoundHandler extends RealBoundHandler {

	private final ServerChannelFunction function;

	public ServerOutBoundHandler(ServerChannelFunction function) {
		super(function);
		this.function = function;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		function.active(ctx.channel());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		function.waitOpen(ctx.channel(), new ServerChannelFunction.OpenResult() {
			@Override
			public void success() {
				ctx.fireChannelActive();
			}

			@Override
			public void failed() {
				ctx.channel().close();
			}
		});
	}
}

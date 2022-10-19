package me.pood1e.nat.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseChannelFunction;

/**
 * @author pood1e
 */
@Slf4j
public class RealBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {

	private final BaseChannelFunction function;

	public RealBoundHandler(BaseChannelFunction function) {
		this.function = function;
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		function.inactive(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.warn(cause.getMessage());
		ctx.channel().close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
		function.channelWrite(ctx.channel(), msg);
	}
}

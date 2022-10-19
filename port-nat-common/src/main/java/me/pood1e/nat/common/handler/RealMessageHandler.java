package me.pood1e.nat.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author pood1e
 */
@Slf4j
public class RealMessageHandler implements ChannelHandler {

	private final static String ENCODER = "ENCODER";
	private final static String DECODER = "DECODER";

	private final int limit;

	public RealMessageHandler(int limit) {
		this.limit = limit;
	}


	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().addAfter(ctx.name(), ctx.name() + ENCODER, new Encoder(limit));
		ctx.pipeline().addAfter(ctx.name(), ctx.name() + DECODER, new Decoder(limit));
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		List<String> names = ctx.pipeline().names();
		removeHandler(ctx, names, ctx.name() + ENCODER);
		removeHandler(ctx, names, ctx.name() + DECODER);
	}

	private void removeHandler(ChannelHandlerContext ctx, List<String> names, String name) {
		if (names.contains(name)) {
			ctx.pipeline().remove(name);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

	}

	private static final class Encoder extends ChannelOutboundHandlerAdapter {

		private final int limit;

		private Encoder(int limit) {
			this.limit = limit;
		}


		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			if (msg instanceof ByteBuf buf) {
				if (buf.readableBytes() > limit) {
					log.warn("exceed limit:{} ,drop message", limit);
					return;
				}
				ByteBuf ret = Unpooled.buffer(buf.readableBytes() + 4);
				ret.writeInt(buf.readableBytes());
				ret.writeBytes(buf);
				buf.release();
				super.write(ctx, ret, promise);
			}
		}
	}

	private static final class Decoder extends ReplayingDecoder<Decoder.State> {
		private final int limit;
		private int length;

		public Decoder(int limit) {
			super(State.DATA_HEAD);
			this.limit = limit;
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
			switch (state()) {
				case DATA_HEAD -> {
					length = in.readInt();
					if (length > limit) {
						log.warn("exceed limit:{} ,drop message", limit);
						return;
					}
					checkpoint(State.DATA_BODY);
				}
				case DATA_BODY -> {
					out.add(in.readBytes(length));
					checkpoint(State.DATA_HEAD);
				}
			}
		}

		private enum State {
			DATA_HEAD, DATA_BODY
		}

	}
}

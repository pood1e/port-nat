package me.pood1e.nat.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.util.AesUtils;
import org.apache.commons.codec.binary.Base64;

/**
 * @author pood1e
 */
@Slf4j
public class AesHandler extends ChannelHandlerAdapter {

	public final String INBOUND_AES_HANDLER = "INBOUND_AES_HANDLER";
	public final String OUTBOUND_AES_HANDLER = "OUTBOUND_AES_HANDLER";
	private final byte[] key;

	public AesHandler(String key) {
		this.key = Base64.decodeBase64(key);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().replace(this, INBOUND_AES_HANDLER, new AESInBoundHandler(key));
		ctx.pipeline().addAfter(INBOUND_AES_HANDLER, OUTBOUND_AES_HANDLER, new AESOutBoundHandler(key));
	}

	private static final class AESInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
		private final byte[] key;

		public AESInBoundHandler(byte[] key) {
			this.key = key;
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
			byte[] buffer = new byte[msg.readableBytes()];
			msg.readBytes(buffer);
			byte[] data = AesUtils.decode(key, buffer);
			ctx.fireChannelRead(Unpooled.wrappedBuffer(data));
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			super.exceptionCaught(ctx, cause);
			log.error(cause.getMessage());
			cause.printStackTrace();
		}
	}

	private static final class AESOutBoundHandler extends ChannelOutboundHandlerAdapter {
		private final byte[] key;

		public AESOutBoundHandler(byte[] key) {
			this.key = key;
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			if (msg instanceof ByteBuf buf) {
				byte[] buffer = new byte[buf.readableBytes()];
				buf.readBytes(buffer);
				buf.release();
				byte[] data = AesUtils.encode(key, buffer);
				super.write(ctx, Unpooled.wrappedBuffer(data), promise);
			}
		}
	}
}

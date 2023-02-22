package me.pood1e.nat.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.util.RsaUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author pood1e
 */
@Slf4j
public class RsaHandler extends ChannelHandlerAdapter {

	public final String INBOUND_AES_HANDLER = "INBOUND_RSA_HANDLER";
	public final String OUTBOUND_AES_HANDLER = "OUTBOUND_RSA_HANDLER";
	private final RSAPublicKey pubKey;
	private final RSAPrivateKey priKey;

	public RsaHandler(String pubKey, String priKey) throws Exception {
		this.pubKey = RsaUtils.getPublicKey(pubKey);
		this.priKey = RsaUtils.getPrivateKey(priKey);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().replace(this, INBOUND_AES_HANDLER, new RsaHandler.RSAInBoundHandler(priKey));
		ctx.pipeline().addAfter(INBOUND_AES_HANDLER, OUTBOUND_AES_HANDLER, new RsaHandler.RSAOutBoundHandler(pubKey));
	}

	private static final class RSAInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
		private final RSAPrivateKey priKey;

		public RSAInBoundHandler(RSAPrivateKey priKey) {
			this.priKey = priKey;
		}

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
			byte[] buffer = new byte[msg.readableBytes()];
			msg.readBytes(buffer);
			byte[] data = RsaUtils.decryptByPrivateKey(buffer, priKey);
			ctx.fireChannelRead(Unpooled.wrappedBuffer(data));
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			super.exceptionCaught(ctx, cause);
			log.error(cause.getMessage());
			cause.printStackTrace();
		}
	}

	private static final class RSAOutBoundHandler extends ChannelOutboundHandlerAdapter {
		private final RSAPublicKey pubKey;

		public RSAOutBoundHandler(RSAPublicKey pubKey) {
			this.pubKey = pubKey;
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			if (msg instanceof ByteBuf buf) {
				byte[] buffer = new byte[buf.readableBytes()];
				buf.readBytes(buffer);
				buf.release();
				byte[] data = RsaUtils.encryptByPublicKey(buffer, pubKey);
				super.write(ctx, Unpooled.wrappedBuffer(data), promise);
			}
		}
	}
}

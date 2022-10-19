package me.pood1e.nat.common.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.ProxyMessage;
import me.pood1e.nat.common.model.ProxyMessageType;

import java.util.List;

/**
 * @author pood1e
 */
@Slf4j
public class ProxyMessageHandler implements ChannelHandler {

	private final static String ENCODER = "ENCODER";
	private final static String DECODER = "DECODER";

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().addAfter(ctx.name(), ctx.name() + ENCODER, new ProxyMessageEncoder());
		ctx.pipeline().addAfter(ctx.name(), ctx.name() + DECODER, new ProxyMessageDecoder());
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


	/**
	 * @author pood1e
	 */
	private static final class ProxyMessageDecoder extends ReplayingDecoder<ProxyMessageDecoder.ProxyMessageState> {

		private ProxyMessageType current;
		private int length;

		public ProxyMessageDecoder() {
			super(ProxyMessageState.DATA_TYPE);
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
			switch (state()) {
				case DATA_TYPE -> {
					current = ProxyMessageType.valueOf(in.readByte());
					log.debug("read message type: {}", current);
					switch (current) {
						case PING, PONG, PROXY_READY, AUTH_FAILED, AUTH_TIMEOUT -> out.add(new ProxyMessage(current));
						case OPEN_CONNECTION, OPEN_CONNECTION_SUCCESS, CLOSE_CONNECTION, AUTH_SALT ->
								checkpoint(ProxyMessageState.DATA_BODY);
						case TRANSFER_DATA, AUTH -> checkpoint(ProxyMessageState.DATA_LENGTH);
					}
				}
				case DATA_LENGTH -> {
					length = in.readInt();
					checkpoint(ProxyMessageState.DATA_BODY);
				}
				case DATA_BODY -> {
					switch (current) {
						case OPEN_CONNECTION -> {
							ByteBuf buf = in.readBytes(ProxyMessage.ConnectionRequest.SIZE);
							out.add(ProxyMessage.ConnectionRequest.decode(current, buf));
							buf.release();
						}
						case CLOSE_CONNECTION, OPEN_CONNECTION_SUCCESS -> {
							ByteBuf buf = in.readBytes(ProxyMessage.ConnectionResponse.SIZE);
							out.add(ProxyMessage.ConnectionResponse.decode(current, buf));
							buf.release();
						}
						case AUTH_SALT -> {
							ByteBuf buf = in.readBytes(ProxyMessage.AuthSalt.SIZE);
							out.add(ProxyMessage.AuthSalt.decode(buf));
							buf.release();
						}
						case TRANSFER_DATA -> {
							ByteBuf buf = in.readBytes(length);
							out.add(ProxyMessage.TransferData.decode(buf, length));
							buf.release();
						}
						case AUTH -> {
							ByteBuf buf = in.readBytes(length);
							out.add(ProxyMessage.Auth.decode(buf));
							buf.release();
						}
					}
					checkpoint(ProxyMessageState.DATA_TYPE);
				}
			}
		}

		/**
		 * @author pood1e
		 */
		public enum ProxyMessageState {
			DATA_TYPE, DATA_LENGTH, DATA_BODY
		}
	}

	private static final class ProxyMessageEncoder extends ChannelOutboundHandlerAdapter {
		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			if (msg instanceof ProxyMessage proxyMessage) {
				log.debug("write message type: {}", proxyMessage.getType());
				super.write(ctx, proxyMessage.encode(), promise);
			}
		}
	}
}

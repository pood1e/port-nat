package me.pood1e.nat.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.client.model.ClientProxyFunction;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.model.ProxyEvent;
import me.pood1e.nat.common.model.ProxyMessage;
import me.pood1e.nat.common.model.ProxyMessageType;
import me.pood1e.nat.common.util.ChannelUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author pood1e
 */
@Slf4j
public class ClientEventHandler extends SimpleChannelInboundHandler<ProxyMessage> {

	private final ClientProxyFunction instance;
	private final BaseConfig config;

	private boolean ready = false;

	public ClientEventHandler(ClientProxyFunction instance, BaseConfig config) {
		this.instance = instance;
		this.config = config;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().addBefore(ctx.name(), "HEART_BEAT",
				new IdleStateHandler(config.getHeart(), 0, 0, TimeUnit.MILLISECONDS));
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) {
		switch (msg.getType()) {
			case AUTH_SALT ->
					ctx.channel().writeAndFlush(new ProxyMessage.Auth(config.getPass() + ((ProxyMessage.AuthSalt) msg).getSalt()));
			case AUTH_FAILED, AUTH_TIMEOUT -> ctx.channel().close();
			case PROXY_READY -> ctx.pipeline().fireUserEventTriggered(ProxyEvent.PROXY_READY);
			case PING -> ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.PONG));
			case OPEN_CONNECTION -> instance.openChannel(ctx.channel(), (ProxyMessage.ConnectionRequest) msg);
			case TRANSFER_DATA -> instance.proxyWrite(ctx.channel(), (ProxyMessage.TransferData) msg);
			case CLOSE_CONNECTION ->
					instance.closeChannel(ctx.channel(), ((ProxyMessage.ConnectionResponse) msg).getKey());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		ctx.pipeline().fireUserEventTriggered(ProxyEvent.ERROR_OCCUR);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(cause.getMessage());
		ctx.channel().close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (evt instanceof IdleStateEvent event && event.state().equals(IdleState.READER_IDLE) && !event.isFirst()) {
			ctx.channel().close();
		}
		if (evt instanceof ProxyEvent event) {
			switch (event) {
				case ERROR_OCCUR -> {
					if (ready) {
						instance.removeProxy(ctx.channel());
					}
					instance.reconnect();
				}
				case PROXY_READY -> {
					ready = true;
					ChannelUtils.addRealHandler(ctx);
					instance.addProxy(ctx.channel());
				}
			}
		}
	}
}

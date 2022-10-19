package me.pood1e.nat.server.handler;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.model.ProxyEvent;
import me.pood1e.nat.common.model.ProxyMessage;
import me.pood1e.nat.common.model.ProxyMessageType;
import me.pood1e.nat.common.util.ChannelUtils;
import me.pood1e.nat.server.model.ServerProxyFunction;

import java.util.concurrent.TimeUnit;

/**
 * @author pood1e
 */
@Slf4j
public class ServerEventHandler extends SimpleChannelInboundHandler<ProxyMessage> {

	private final ServerProxyFunction instance;
	private final BaseConfig config;
	private final String key = NanoIdUtils.randomNanoId();
	private boolean authed;
	private boolean ready = false;

	public ServerEventHandler(ServerProxyFunction instance, BaseConfig config) {
		this.instance = instance;
		this.config = config;
		authed = !this.config.isAuth();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(cause.getMessage());
		ctx.channel().close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		super.userEventTriggered(ctx, ProxyEvent.OPEN_SUCCESS);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		ctx.pipeline().addBefore(ctx.name(), "HEART_BEAT",
				new IdleStateHandler(config.getHeart(), config.getHeart(), 0, TimeUnit.MILLISECONDS));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		ctx.pipeline().fireUserEventTriggered(ProxyEvent.ERROR_OCCUR);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (evt instanceof SslHandshakeCompletionEvent event) {
			if (event.isSuccess()) {
				ctx.pipeline().fireUserEventTriggered(ProxyEvent.SSL_SUCCESS);
			} else {
				ctx.channel().close();
			}
		}
		if (evt instanceof IdleStateEvent event) {
			switch (event.state()) {
				case WRITER_IDLE -> {
					if (ready) {
						ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.PING));
					}
				}
				case READER_IDLE -> {
					if (!event.isFirst()) {
						if (!authed) {
							ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.AUTH_TIMEOUT));
						}
					}
				}
			}
		}
		if (evt instanceof ProxyEvent event) {
			switch (event) {
				case ERROR_OCCUR -> {
					if (ready) {
						instance.removeProxy(ctx.channel());
					}
				}
				case OPEN_SUCCESS -> {
					if (!config.isSslEnable() && !config.isAuth()) {
						ctx.pipeline().fireUserEventTriggered(ProxyEvent.PROXY_READY);
					} else if (config.isAuth()) {
						auth(ctx);
					}
				}
				case SSL_SUCCESS -> {
					if (!config.isAuth()) {
						ctx.pipeline().fireUserEventTriggered(ProxyEvent.PROXY_READY);
					} else {
						auth(ctx);
					}
				}
				case AUTH_SUCCESS -> {
					authed = true;
					ctx.pipeline().fireUserEventTriggered(ProxyEvent.PROXY_READY);
				}
				case AUTH_TIMEOUT -> {
					ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.AUTH_TIMEOUT));
					ctx.channel().close();
				}
				case AUTH_FAILED -> {
					ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.AUTH_FAILED));
					ctx.channel().close();
				}
				case PROXY_READY -> {
					ready = true;
					ctx.channel().writeAndFlush(new ProxyMessage(ProxyMessageType.PROXY_READY))
							.addListener((ChannelFutureListener) future -> ChannelUtils.addRealHandler(ctx));
					instance.addProxy(ctx.channel());
				}
			}
		}
	}

	private void auth(ChannelHandlerContext ctx) {
		log.debug("send salt:{}", key);
		ctx.channel().writeAndFlush(new ProxyMessage.AuthSalt(key));
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage msg) {
		if (!authed) {
			ctx.pipeline().fireUserEventTriggered(msg instanceof ProxyMessage.Auth auth &&
					auth.getKey().equals(config.getPass() + key) ? ProxyEvent.AUTH_SUCCESS : ProxyEvent.AUTH_FAILED);
		} else {
			switch (msg.getType()) {
				case CLOSE_CONNECTION ->
						instance.closeChannel(ctx.channel(), ((ProxyMessage.ConnectionResponse) msg).getKey());
				case OPEN_CONNECTION_SUCCESS ->
						instance.openSuccess(ctx.channel(), ((ProxyMessage.ConnectionResponse) msg).getKey());
				case TRANSFER_DATA -> instance.proxyWrite(ctx.channel(), (ProxyMessage.TransferData) msg);
			}
		}
	}
}

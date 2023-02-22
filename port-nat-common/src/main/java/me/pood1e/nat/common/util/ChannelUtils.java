package me.pood1e.nat.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.pood1e.nat.common.handler.AesHandler;
import me.pood1e.nat.common.handler.ProxyMessageHandler;
import me.pood1e.nat.common.handler.RealMessageHandler;
import me.pood1e.nat.common.handler.RsaHandler;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.model.SslProvider;

/**
 * @author pood1e
 */
public class ChannelUtils {

	private static final String SSL_HANDLER = "SSL";
	private static final String AES_HANDLER = "AES";

	private static final String RSA_HANDLER = "RSA";

	private static final String REAL_HANDLER = "REAL_HANDLER";

	public static void initial(Channel ch, BaseConfig baseConfig, SslProvider provider) throws Exception {
		if (baseConfig.isAesEnable()) {
			ch.pipeline().addFirst(AES_HANDLER, new AesHandler(baseConfig.getAesKey()));
		}
		if (baseConfig.isRsaEnable()) {
			ch.pipeline().addFirst(RSA_HANDLER, new RsaHandler(baseConfig.getRsaPubKey(), baseConfig.getRsaPriKey()));
		}
		if (baseConfig.isSslEnable()) {
			ch.pipeline().addFirst(SSL_HANDLER, provider.getSslContext().newHandler(ch.alloc()));
		}
		ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
		ch.pipeline().addLast(new ProxyMessageHandler());
	}

	public static void addRealHandler(ChannelHandlerContext ctx) {
		ChannelHandler handler = new RealMessageHandler(10 * 1024 * 1024);
		if (ctx.pipeline().get(SSL_HANDLER) != null) {
			ctx.pipeline().addAfter(SSL_HANDLER, REAL_HANDLER, handler);
		} else {
			ctx.pipeline().addFirst(handler);
		}
	}

}

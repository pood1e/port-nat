package me.pood1e.nat.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import me.pood1e.nat.common.handler.AESHandler;
import me.pood1e.nat.common.handler.ProxyMessageHandler;
import me.pood1e.nat.common.handler.RSAHandler;
import me.pood1e.nat.common.handler.RealMessageHandler;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.common.model.SslProvider;

/**
 * @author pood1e
 */
public class ChannelUtils {
	public static void initial(Channel ch, BaseConfig baseConfig, SslProvider provider) throws Exception {
		if (baseConfig.isAesEnable()) {
			ch.pipeline().addFirst("AES", new AESHandler(baseConfig.getAesKey()));
		}
		if (baseConfig.isRsaEnable()) {
			ch.pipeline().addFirst("RSA", new RSAHandler(baseConfig.getRsaPubKey(), baseConfig.getRsaPriKey()));
		}
		if (baseConfig.isSslEnable()) {
			ch.pipeline().addFirst("SSL", provider.getSslContext().newHandler(ch.alloc()));
		}
		ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
		ch.pipeline().addLast(new ProxyMessageHandler());
	}

	public static void addRealHandler(ChannelHandlerContext ctx) {
		ChannelHandler handler = new RealMessageHandler(10 * 1024 * 1024);
		if (ctx.pipeline().get("SSL") != null) {
			ctx.pipeline().addAfter("SSL", "REAL_HANDLER", handler);
		} else {
			ctx.pipeline().addFirst(handler);
		}
	}

}

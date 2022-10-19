package me.pood1e.nat.common.model;

import io.netty.handler.ssl.SslContext;

/**
 * @author pood1e
 */
public interface SslProvider {

	SslContext getSslContext();
}

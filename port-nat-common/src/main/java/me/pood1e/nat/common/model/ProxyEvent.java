package me.pood1e.nat.common.model;

/**
 * @author pood1e
 */
public enum ProxyEvent {
	OPEN_SUCCESS,
	SSL_SUCCESS,
	AUTH_SUCCESS, AUTH_TIMEOUT, AUTH_FAILED,
	PROXY_READY,
	ERROR_OCCUR
}

package me.pood1e.nat.common.model;

import lombok.Getter;

/**
 * @author pood1e
 */
@Getter
public enum ProxyMessageType {

	PING(1), PONG(2),
	AUTH(3), AUTH_SALT(4), PROXY_READY(5), AUTH_FAILED(6), AUTH_TIMEOUT(7),
	OPEN_CONNECTION(10), CLOSE_CONNECTION(11), OPEN_CONNECTION_SUCCESS(12),
	TRANSFER_DATA(13);


	private final byte cmd;

	ProxyMessageType(int cmd) {
		this.cmd = (byte) cmd;
	}

	public static ProxyMessageType valueOf(int cmd) {
		for (ProxyMessageType type : ProxyMessageType.values()) {
			if (cmd == type.cmd) {
				return type;
			}
		}
		throw new RuntimeException();
	}
}

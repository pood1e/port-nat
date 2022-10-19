package me.pood1e.nat.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author pood1e
 */
@Getter
@Setter
public class BaseConfig {
	protected long heart = 15000;
	protected int maxConnection = 25;
	protected int port;
	protected boolean sslEnable;
	protected boolean aesEnable;
	protected String aesKey;
	protected boolean rsaEnable;
	protected String rsaPubKey;
	protected String rsaPriKey;
	protected boolean auth;
	protected String pass;
}

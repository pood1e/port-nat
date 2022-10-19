package me.pood1e.nat.client.model;

import lombok.Getter;
import lombok.Setter;
import me.pood1e.nat.common.model.BaseConfig;

import java.util.Map;

/**
 * @author pood1e
 */
@Getter
@Setter
public class Client extends BaseConfig {
	protected long retryDelay = 10000;
	protected String address;
	protected Map<Integer, ClientAddress> outMapper;
}

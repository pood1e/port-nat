package me.pood1e.nat.server.model;

import lombok.Getter;
import lombok.Setter;
import me.pood1e.nat.common.model.BaseConfig;

import java.util.Map;


/**
 * @author pood1e
 */
@Getter
@Setter
public class Server extends BaseConfig {
	protected Map<Integer, Boolean> outPorts;
	protected long timeout = 5000;
}

package me.pood1e.nat.server.service;

import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.common.model.BaseConfig;
import me.pood1e.nat.server.PresetProxy;
import me.pood1e.nat.server.model.Server;
import me.pood1e.nat.server.model.ServerInstance;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pood1e
 */
@Slf4j
@Component
public class InMemoryProxyImpl implements InMemoryService {

	private final Map<BaseConfig, ServerInstance> map = new HashMap<>();

	public InMemoryProxyImpl(PresetProxy presetProxy) {
		presetProxy.getProxies().forEach(this::open);
	}

	@Override
	public void open(Server server) {
		map.put(server, new ServerInstance(server));
	}

	@Override
	public void close(Server server) {
		ServerInstance instance = map.get(server);
		if (instance != null) {
			instance.close();
		}
	}

}

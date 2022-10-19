package me.pood1e.nat.client;

import lombok.extern.slf4j.Slf4j;
import me.pood1e.nat.client.model.ClientInstance;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author pood1e
 */
@Slf4j
@Component
public class ClientProxyRunner implements ApplicationRunner {

	private final ClientProxyConfig config;

	public ClientProxyRunner(ClientProxyConfig config) {
		this.config = config;
	}


	@Override
	public void run(ApplicationArguments args) {
		config.getClients().forEach(ClientInstance::new);
	}
}

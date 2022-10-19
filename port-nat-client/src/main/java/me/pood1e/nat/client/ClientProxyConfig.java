package me.pood1e.nat.client;

import lombok.Getter;
import lombok.Setter;
import me.pood1e.nat.client.model.Client;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author pood1e
 */
@ConfigurationProperties(prefix = "proxy")
@Component
@Getter
@Setter
public class ClientProxyConfig {

	private List<Client> clients;

}

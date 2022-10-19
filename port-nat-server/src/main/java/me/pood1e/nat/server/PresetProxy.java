package me.pood1e.nat.server;

import lombok.Getter;
import lombok.Setter;
import me.pood1e.nat.server.model.Server;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pood1e
 */
@ConfigurationProperties(prefix = "proxy.preset")
@Component
@Getter
@Setter
public class PresetProxy {

	private List<Server> proxies = new ArrayList<>();

}

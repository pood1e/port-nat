package me.pood1e.nat.server.service;

import me.pood1e.nat.server.model.Server;

/**
 * @author pood1e
 */
public interface InMemoryService {

	void open(Server proxy);

	void close(Server proxy);

}

package me.pood1e.nat.server.model;

import io.netty.channel.Channel;
import me.pood1e.nat.common.model.BaseChannelFunction;

/**
 * @author pood1e
 */
public interface ServerChannelFunction extends BaseChannelFunction {

	void active(Channel ch);

	void waitOpen(Channel ch, OpenResult openResult);

	interface OpenResult {
		void success();

		void failed();
	}

}

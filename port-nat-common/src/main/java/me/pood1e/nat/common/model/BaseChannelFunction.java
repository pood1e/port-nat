package me.pood1e.nat.common.model;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author pood1e
 */
public interface BaseChannelFunction {

	void channelWrite(Channel ch, ByteBuf buf);

	void inactive(Channel ch);

}

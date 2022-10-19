package me.pood1e.nat.common.model;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * @author pood1e
 */
@Getter
public class ProxyMessage {

	protected final ProxyMessageType type;

	public ProxyMessage(ProxyMessageType type) {
		this.type = type;
	}

	public ByteBuf encode() {
		return Unpooled.wrappedBuffer(new byte[]{type.getCmd()});
	}

	@Getter
	public static class Auth extends ProxyMessage {

		private final String key;

		public Auth(String key) {
			super(ProxyMessageType.AUTH);
			this.key = key;
		}

		public static Auth decode(ByteBuf buf) {
			return new Auth(buf.toString(StandardCharsets.UTF_8));
		}

		@Override
		public ByteBuf encode() {
			byte[] data = key.getBytes(StandardCharsets.UTF_8);
			ByteBuf buf = Unpooled.buffer(5 + data.length);
			return buf.writeByte(type.getCmd()).writeInt(data.length).writeBytes(data);
		}
	}

	@Getter
	public static class ConnectionRequest extends ProxyMessage {

		public static final int SIZE = 2 + NanoIdUtils.DEFAULT_SIZE;

		private final String key;
		private final short port;

		public ConnectionRequest(ProxyMessageType type, String key, short port) {
			super(type);
			this.key = key;
			this.port = port;
		}

		public static ConnectionRequest decode(ProxyMessageType type, ByteBuf buf) {
			String key = buf.toString(0, NanoIdUtils.DEFAULT_SIZE, StandardCharsets.UTF_8);
			short port = buf.getShort(NanoIdUtils.DEFAULT_SIZE);
			return new ConnectionRequest(type, key, port);
		}

		@Override
		public ByteBuf encode() {
			ByteBuf buf = Unpooled.buffer(SIZE + 1);
			return buf.writeByte(type.getCmd()).writeBytes(key.getBytes(StandardCharsets.UTF_8)).writeShort(port);
		}
	}

	@Getter
	public static class ConnectionResponse extends ProxyMessage {

		public static final int SIZE = NanoIdUtils.DEFAULT_SIZE;

		private final String key;

		public ConnectionResponse(ProxyMessageType type, String key) {
			super(type);
			this.key = key;
		}

		public static ConnectionResponse decode(ProxyMessageType type, ByteBuf buf) {
			return new ConnectionResponse(type, buf.toString(StandardCharsets.UTF_8));
		}

		@Override
		public ByteBuf encode() {
			ByteBuf buf = Unpooled.buffer(SIZE + 1);
			return buf.writeByte(type.getCmd()).writeBytes(key.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Getter
	public static class AuthSalt extends ProxyMessage {

		public static final int SIZE = NanoIdUtils.DEFAULT_SIZE;

		private final String salt;

		public AuthSalt(String key) {
			super(ProxyMessageType.AUTH_SALT);
			this.salt = key;
		}

		public static AuthSalt decode(ByteBuf buf) {
			return new AuthSalt(buf.toString(StandardCharsets.UTF_8));
		}

		@Override
		public ByteBuf encode() {
			ByteBuf buf = Unpooled.buffer(SIZE + 1);
			return buf.writeByte(type.getCmd()).writeBytes(salt.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Getter
	public static class TransferData extends ProxyMessage {

		private static final int SIZE = NanoIdUtils.DEFAULT_SIZE;

		private final String connectionKey;
		private final byte[] data;

		public TransferData(String connectionKey, byte[] data) {
			super(ProxyMessageType.TRANSFER_DATA);
			this.connectionKey = connectionKey;
			this.data = data;
		}

		public static TransferData decode(ByteBuf buf, int length) {
			String connectionKey = buf.toString(0, NanoIdUtils.DEFAULT_SIZE, StandardCharsets.UTF_8);
			byte[] data = new byte[length - SIZE];
			buf.getBytes(SIZE, data);
			return new TransferData(connectionKey, data);
		}

		@Override
		public ByteBuf encode() {
			int dataLength = SIZE + data.length;
			ByteBuf buf = Unpooled.buffer(5 + dataLength);
			return buf.writeByte(type.getCmd())
					.writeInt(dataLength)
					.writeBytes(connectionKey.getBytes(StandardCharsets.UTF_8))
					.writeBytes(data);
		}
	}

}

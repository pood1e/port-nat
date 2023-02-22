package me.pood1e.nat.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author pood1e
 */
public class AesUtils {

	private static final String ALGORITHM = "AES";
	private static final String MODE = "AES/ECB/PKCS5Padding";

	public static String generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		keyGenerator.init(new SecureRandom());
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] byteKey = secretKey.getEncoded();
		return Base64.encodeBase64String(byteKey);
	}


	public static byte[] encode(byte[] thisKey, byte[] plaintext) throws Exception {
		Key key = new SecretKeySpec(thisKey, ALGORITHM);
		Cipher cipher = Cipher.getInstance(MODE);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(plaintext);
	}


	public static byte[] decode(byte[] thisKey, byte[] encrypted) throws Exception {
		Key key = new SecretKeySpec(thisKey, ALGORITHM);
		Cipher cipher = Cipher.getInstance(MODE);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(encrypted);
	}
}

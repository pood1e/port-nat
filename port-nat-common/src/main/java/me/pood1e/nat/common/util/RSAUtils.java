package me.pood1e.nat.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author pood1e
 */
public class RSAUtils {

	private final static String ALGORITHM_RSA = "RSA";
	private final static int MAX_DECRYPT_BLOCK = 256;
	private final static int MAX_ENCRYPT_BLOCK = 245;

	public static KeyPair getRsaKeypair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
		keyPairGen.initialize(2048);
		return keyPairGen.generateKeyPair();
	}

	public static RSAPublicKey getPublicKey(String publicKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
		return (RSAPublicKey) keyFactory.generatePublic(x509EncodedKeySpec);
	}


	public static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
		return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
	}

	private static Cipher cipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
		return Cipher.getInstance(keyFactory.getAlgorithm());
	}

	public static byte[] encryptByPublicKey(byte[] data, PublicKey publicKey) throws Exception {
		Cipher cipher = cipher();
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return doFinal(data, cipher, MAX_ENCRYPT_BLOCK);
	}

	public static byte[] decryptByPrivateKey(byte[] data, PrivateKey privateKey) throws Exception {
		Cipher cipher = cipher();
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return doFinal(data, cipher, MAX_DECRYPT_BLOCK);
	}

	private static byte[] doFinal(byte[] data, Cipher cipher, int maxBlock) throws IOException, IllegalBlockSizeException, BadPaddingException {
		int inputLen = data.length;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			for (int offSet = 0; inputLen - offSet > 0; offSet += maxBlock) {
				byte[] cache;
				if (inputLen - offSet > maxBlock) {
					cache = cipher.doFinal(data, offSet, maxBlock);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
			}
			return out.toByteArray();
		}
	}


}

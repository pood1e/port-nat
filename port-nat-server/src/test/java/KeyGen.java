import me.pood1e.nat.common.util.AESUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author pood1e
 */
public class KeyGen {
	public static void main(String[] args) throws NoSuchAlgorithmException {
		System.out.println(AESUtils.generateKey());
		System.out.println(AESUtils.generateKey());
//		Pair<String, String> keys = RSAUtils.getRSAKeyString();
//		System.out.println(keys.getFirst());
//		System.out.println(keys.getSecond());
//		Pair<String, String> keys2 = RSAUtils.getRSAKeyString();
//		System.out.println(keys2.getFirst());
//		System.out.println(keys2.getSecond());
	}
}

import me.pood1e.nat.common.util.AesUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author pood1e
 */
public class KeyGen {
	public static void main(String[] args) throws NoSuchAlgorithmException {
		System.out.println(AesUtils.generateKey());
		System.out.println(AesUtils.generateKey());
//		Pair<String, String> keys = RsaUtils.getRSAKeyString();
//		System.out.println(keys.getFirst());
//		System.out.println(keys.getSecond());
//		Pair<String, String> keys2 = RsaUtils.getRSAKeyString();
//		System.out.println(keys2.getFirst());
//		System.out.println(keys2.getSecond());
	}
}

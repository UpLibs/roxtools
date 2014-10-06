package roxtools.crypto;

import java.security.Key;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
	
	public enum Algorithm {
		AES("AES"),
		RSA("RSA");
		
		private final String name ;
		
		private Algorithm(String name) {
			this.name = name ;
		}
		
		public String getName() {
			return name;
		}
	}

	static private final Random rand = new Random();
	
	static public byte[] createInitializationVector() {
		byte[] iv = new byte[16];
		rand.nextBytes(iv);
		return iv;
	}
	

	public static byte[] encodeKey(Key key) {
		return key.getEncoded();
	}

	public static Key decodeKey(byte[] bytes, Algorithm algorithm) {
		return new SecretKeySpec(bytes, algorithm.getName());
	}
	
}

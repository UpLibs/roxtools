package roxtools.crypto;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
	
	static public byte[] createInitializationVector(long seed) {
		Random rand = new Random(seed) ;
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
	
	public static PublicKey decodePublicKey(byte[] bytes, Algorithm algorithm) {
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

		try {
			KeyFactory factory = KeyFactory.getInstance( algorithm.getName() );
			return factory.generatePublic(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	public static PrivateKey decodePrivateKey(byte[] bytes, Algorithm algorithm) {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		
		try {
			KeyFactory factory = KeyFactory.getInstance( algorithm.getName() );
			return factory.generatePrivate(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	
}

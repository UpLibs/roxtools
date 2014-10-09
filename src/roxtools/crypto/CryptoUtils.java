package roxtools.crypto;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
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
	
	static public byte[] createMask(long seed, int maskSize) {
		byte[] mask = new byte[maskSize] ;
		Random random = new Random(seed) ;
		random.nextBytes(mask);
		return mask ;
	}
	
	static public byte[] mergeMasks(byte[] m1, byte[] m2) {
		int sz = Math.max( m1.length , m2.length ) ;
		byte[] m = new byte[sz] ;
		
		for (int i = 0; i < m.length; i++) {
			m[i] = (byte) ((m1[i%m1.length] & 0xFF) ^ (m2[i%m2.length] & 0xFF)) ;
		}
		
		return m ;
	}
	
	static public byte[] encodeKey(Key key) {
		return key.getEncoded();
	}

	static public Key decodeKey(byte[] bytes, Algorithm algorithm) {
		return new SecretKeySpec(bytes, algorithm.getName());
	}
	
	static public PublicKey decodePublicKey(byte[] bytes, Algorithm algorithm) {
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

		try {
			KeyFactory factory = KeyFactory.getInstance( algorithm.getName() );
			return factory.generatePublic(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	static public PrivateKey decodePrivateKey(byte[] bytes, Algorithm algorithm) {
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		
		try {
			KeyFactory factory = KeyFactory.getInstance( algorithm.getName() );
			return factory.generatePrivate(spec);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}

	static public long calcHashcode( byte[] data ) {
		if (data == null) return 0;

        int resultA = 1;
        int resultB = 1;
        
        int size = data.length ;
        int sizeM1 = size-1 ;
        		
        for (int i = 0; i < size; i++) {
        	byte elem1 = data[i] ;
        	byte elem2 = data[sizeM1-i] ;
        	
        	resultA = 31 * resultA + elem1 ;
        	resultB = 31 * resultB + elem2 ;
		}
        
        long result = ( ( (long)resultA) << 32) + resultB ;
        
        return result ;
	}
	
}

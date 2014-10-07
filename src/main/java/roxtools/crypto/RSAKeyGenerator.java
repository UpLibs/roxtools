package roxtools.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKeyGenerator {

	public enum RSAMode {
		RSA_1024(1024) ,
		RSA_2048(2048)
		;
		
		private final int bits ;
		
		private RSAMode(int bits) {
			this.bits = bits ;
		}
		
		public int getBits() {
			return bits;
		}
	}
	
	////////////////////////////////////////////////////
	
	RSAMode mode ;

	public RSAKeyGenerator() {
		this( RSAMode.RSA_1024 ) ;
	}
	
	public RSAKeyGenerator(RSAMode mode) {
		this.mode = mode;
	}
	
	public RSAMode getMode() {
		return mode;
	}

	public KeyPair createKeyPair() {
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e) ;
		}
		generator.initialize(mode.getBits());
		return generator.generateKeyPair();
	}
	
}

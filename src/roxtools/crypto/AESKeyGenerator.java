package roxtools.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

public class AESKeyGenerator {

	public enum AESMode {
		AES_128(128) ,
		AES_192(192) ,
		AES_256(256)
		;
		
		private final int bits ;
		
		private AESMode(int bits) {
			this.bits = bits ;
		}
		
		public int getBits() {
			return bits;
		}
	}
	

	/////////////////////////////////////////
	
	private AESMode mode ;
	
	public AESKeyGenerator() {
		this(AESMode.AES_128) ;
	}
	
	public AESKeyGenerator(AESMode mode) {
		this.mode = mode ;
	}
	
	public AESMode getMode() {
		return mode;
	}
	
	public Key createKey() {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e) ;
		}
		
		kgen.init( mode.getBits() );
		return kgen.generateKey();
	}

	public Key createKey(String passwrod, byte[] salt) {
		return createKey(passwrod.toCharArray(), salt) ;
	}
	
	public Key createKey(char[] passwrod, byte[] salt) {
		byte[] seed = new byte[8] ;
		
		for (int i = 0; i < passwrod.length; i++) {
			int c = passwrod[i];
			int s = salt[ i % salt.length ] ;
			
			seed[ i % seed.length ] ^= (byte) (c ^ s) ;
		}
		
		return createKey(seed);
	}

	public Key createKey(long seed) {
		return createKey( long2bytes(seed) ) ;
	}
	
	public Key createKey(byte[] seed) {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e) ;
		}

		SecureRandom sr = new SecureRandom(seed);

		kgen.init(mode.getBits() , sr);

		return kgen.generateKey();
	}
	
	static private byte[] long2bytes(long v) {
		byte[] buffer = new byte[8] ;
		
		buffer[0] = (byte) (v >>> 56);
		buffer[1] = (byte) (v >>> 48);
		buffer[2] = (byte) (v >>> 40);
		buffer[3] = (byte) (v >>> 32);
		buffer[4] = (byte) (v >>> 24);
		buffer[5] = (byte) (v >>> 16);
		buffer[6] = (byte) (v >>> 8);
		buffer[7] = (byte) (v);
		
		return buffer ;
	}

}

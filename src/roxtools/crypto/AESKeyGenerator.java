package roxtools.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

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
	
	static private class MySecureRandom extends SecureRandom {
		private static final long serialVersionUID = -8963928592262368019L;
		
		final private Random rand ;
		
		public MySecureRandom(long seed) {
			this.rand = new Random(seed) ;
		}
		
		@Override
		public synchronized void setSeed(byte[] seed) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public boolean nextBoolean() {
			return rand.nextBoolean();
		}
		
		@Override
		public synchronized void nextBytes(byte[] bytes) {
			rand.nextBytes(bytes);
		}
		
		@Override
		public double nextDouble() {
			return rand.nextDouble();
		}
		
		@Override
		public float nextFloat() {
			return rand.nextFloat();
		}
		
		@Override
		public synchronized double nextGaussian() {
			return rand.nextGaussian();
		}
		
		@Override
		public int nextInt() {
			return rand.nextInt();
		}
		
		@Override
		public int nextInt(int n) {
			return rand.nextInt(n);
		}
		
		@Override
		public long nextLong() {
			return rand.nextLong();
		}
		
	}
	
	public Key createKey(long seed) {
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e) ;
		}

		SecureRandom sr = new MySecureRandom(seed);

		kgen.init(mode.getBits() , sr);

		return kgen.generateKey();
	}
	
	public Key createKey(byte[] seed) {
		return createKey( bytes2long(seed) ) ;
	}

	static public long bytes2long(byte[] buffer) {
		return (((long) buffer[0] << 56) + ((long) (buffer[1] & 255) << 48) + ((long) (buffer[2] & 255) << 40) + ((long) (buffer[3] & 255) << 32) +
				((long) (buffer[4] & 255) << 24) + ((buffer[5] & 255) << 16) + ((buffer[6] & 255) << 8) + ((buffer[7] & 255) ));
	}
	

}

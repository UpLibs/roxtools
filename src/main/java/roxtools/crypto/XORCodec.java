package roxtools.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class XORCodec extends CryptoCodec {

	static public class Mask implements Key {
		private static final long serialVersionUID = -3589990229821231558L;
		
		private final byte[] mask ;
		
		public Mask(Mask m1, Mask m2) {
			this( CryptoUtils.mergeMasks(m1.mask , m2.mask) ) ;
		}
		
		public Mask(long seed, int maskSize) {
			this.mask = CryptoUtils.createMask(seed, maskSize) ;
		}
		
		public Mask(byte[] mask) {
			this.mask = mask;
		}

		public Mask merge( byte[] mask ) {
			return new Mask( CryptoUtils.mergeMasks(this.mask, mask) ) ;
		}
		
		public Mask merge( Mask mask ) {
			return merge( mask.mask ) ;
		}
		
		@Override
		public String getAlgorithm() {
			return "XOR" ;
		}

		@Override
		public String getFormat() {
			return "XORMask";
		}

		@Override
		public byte[] getEncoded() {
			return mask ;
		}
		
	}

	///////////////////////////////////////////////////////////
	
	public XORCodec(Mask key) {
		super(key);
	}
	
	@Override
	public Mask getKey() {
		return (Mask) super.getKey();
	}
	
	@Override
	protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		throw new UnsupportedOperationException("XOR is not a JDK Cipher!") ;
	}
	
	public byte[] encrypt(byte[] data, byte[] iv) {
		byte[] result = new byte[ data.length ] ;
		
		byte[] mask = getKeyEncoded() ;
		
		long ivIndexSeed = CryptoUtils.calcHashcode(iv) ;
		Random ivIndexRand = new Random(ivIndexSeed) ;
		
		int ivSz = iv.length;
		
		byte[] iv2 = new byte[ivSz] ;
		ivIndexRand.nextBytes(iv2) ;
		
		int prevMasked = 0 ;
		
		for (int i = 0; i < result.length; i++) {
			int m = mask[i % mask.length] & 0xFF ;
			
			int ivIdx = ivIndexRand.nextInt(ivSz) ;
			int iv2Idx = ivIndexRand.nextInt(ivSz) ;
			int v1 = iv[ivIdx % ivSz] & 0xFF ;
			int v2 = iv2[iv2Idx % ivSz] & 0xFF ;
			int v = v1 ^ v2 ;
			
			m = (m ^ v) ^ prevMasked ;
			
			int masked = (data[i] & 0xFF) ^ m ;
			
			result[i] = (byte) masked ;
			
			prevMasked = masked ;
		}
		
		return result ;
	}
	
	public byte[] decrypt(byte[] data, byte[] iv) {
		byte[] result = new byte[ data.length ] ;
		
		byte[] mask = getKeyEncoded() ;
		
		long ivIndexSeed = CryptoUtils.calcHashcode(iv) ;
		Random ivIndexRand = new Random(ivIndexSeed) ;
		
		int ivSz = iv.length;
		
		byte[] iv2 = new byte[ivSz] ;
		ivIndexRand.nextBytes(iv2) ;
		
		int prevMasked = 0 ;
		
		for (int i = 0; i < result.length; i++) {
			int m = mask[i % mask.length] & 0xFF ;
			
			int ivIdx = ivIndexRand.nextInt(ivSz) ;
			int iv2Idx = ivIndexRand.nextInt(ivSz) ;
			int v1 = iv[ivIdx % ivSz] & 0xFF ;
			int v2 = iv2[iv2Idx % ivSz] & 0xFF ;
			int v = v1 ^ v2 ;
			
			m = (m ^ v) ^ prevMasked ;
			
			int masked = (data[i] & 0xFF) ;
			
			result[i] = (byte) (masked ^ m) ;
			
			prevMasked = masked ;
		}
		
		return result ;
	}
	
	public static void main(String[] args) {
		
		Mask mask = new Mask(123, 10) ;
		
		XORCodec xorCodec = new XORCodec(mask) ;
		
		byte[] data = "DATA skdfuy2374691387hskdfhakufiq874 XXXXXX".getBytes() ;
		
		System.out.println("decrypt> "+ new String(data) );
		
		byte[] iv = CryptoUtils.createInitializationVector(4573) ;
		
		byte[] encrypt = xorCodec.encrypt(data, iv) ;
		
		System.out.println("encrypt> "+ new String(encrypt) );
		
		byte[] decrypt = xorCodec.decrypt(encrypt, iv) ;
		
		System.out.println("decrypt> "+ new String(decrypt) );
	}
	
}

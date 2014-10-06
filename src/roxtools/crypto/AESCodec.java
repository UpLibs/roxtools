package roxtools.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import roxtools.crypto.CryptoUtils.Algorithm;

final public class AESCodec extends CryptoCodec {
	
	public AESCodec() {
		this( new AESKeyGenerator().createKey() ) ;
	}
	
	public AESCodec(byte[] keyEncoded) {
		this( CryptoUtils.decodeKey(keyEncoded, Algorithm.AES) ) ;
	}
	
	public AESCodec(Key key) {
		super(key) ;
	}
	
	@Override
	protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		return Cipher.getInstance("AES/CBC/PKCS5Padding");
	}
	
	public byte[] encrypt(byte[] data, byte[] iv) {
		try {
			Cipher cipher = createCipher();
			cipher.init(Cipher.ENCRYPT_MODE, getKey() , new IvParameterSpec(iv));
			return cipher.doFinal(data);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}

	public byte[] decrypt(byte[] encData, byte[] iv) {
		try {
			Cipher cipher = createCipher();
			cipher.init(Cipher.DECRYPT_MODE, getKey() , new IvParameterSpec(iv));
			return cipher.doFinal(encData);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
	}

}

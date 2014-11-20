package roxtools.crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

abstract public class CryptoCodec {
	
	private Key key ;

	public CryptoCodec(Key key) {
		this.key = key;
	}
	
	public Key getKey() {
		return key;
	}
	
	public byte[] getKeyEncoded() {
		return CryptoUtils.encodeKey(key) ;
	}
	
	
	abstract protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException ;
	
	
}

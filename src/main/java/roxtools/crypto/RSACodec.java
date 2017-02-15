package roxtools.crypto;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import roxtools.crypto.CryptoUtils.Algorithm;

public class RSACodec extends CryptoCodec {
	
	public RSACodec(byte[] keyEncoded, boolean privateKey) {
		this( 
				privateKey
				?
				CryptoUtils.decodePrivateKey(keyEncoded, Algorithm.RSA)
				:
				CryptoUtils.decodePublicKey(keyEncoded, Algorithm.RSA)
		) ;
	}
	
	public RSACodec(Key key) {
		super(key) ;
	}
	
	@Override
	protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		return cipher;
	}

	public byte[] encrypt(byte[] data) throws IOException {
		try {
			Cipher cipher = createCipher();
			cipher.init(Cipher.ENCRYPT_MODE, getKey() );
			return cipher.doFinal(data);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public byte[] decrypt(byte[] data) throws IOException {
		try {
			Cipher cipher = createCipher();
			cipher.init(Cipher.DECRYPT_MODE, getKey() );
			return cipher.doFinal(data);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	
}

package roxtools.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import roxtools.crypto.CryptoUtils.Algorithm;

public class RSACodec extends CryptoCodec {

	private Key key ;
	
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
		Cipher cipher;
		try {
			cipher = createCipher();
			cipher.init(Cipher.ENCRYPT_MODE, key);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream( data.length*2 );

		for (int i = 0; i < data.length; i += 117) {
			int lenght = data.length - i ;
			if (lenght > 117) lenght = 117;
			
			try {
				byte[] enc = cipher.doFinal(data, i, lenght);
				buffer.write(enc);
			}
			catch (Exception e) {
				throw new IOException(e) ;
			}
		}

		return buffer.toByteArray();

	}

	public byte[] decrypt(byte[] data) throws IOException {
		Cipher cipher;
		try {
			cipher = createCipher();
			cipher.init(Cipher.DECRYPT_MODE, key);
		}
		catch (Exception e) {
			throw new IllegalStateException(e) ;
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream() ;
		
		for (int i = 0; i < data.length; i+=128) {
			int lenght = data.length - i ;
			if (lenght > 128) lenght = 128 ;
			
			byte[] dec;
			try {
				dec = cipher.doFinal(data , i , lenght);
				buffer.write(dec) ;
			}
			catch (Exception e) {
				throw new IOException(e) ;
			}
		}

		return buffer.toByteArray() ;
	}

	
}

package roxtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final public class DigestMD5 {

	final private MessageDigest digestMD5 ;
	{
		try {
			digestMD5 = MessageDigest.getInstance("MD5");
			
			synchronized (digestMD5) {
				digestMD5.reset() ;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Can't find MD5 digest!") ;
		}
	}
	
	public String calcMD5Hex(byte[] data) {
		BigInteger bigInteger = new BigInteger(1, calcMD5(data) ) ;
		return bigInteger.toString(16) ;
	}
	
	public byte[] calcMD5(byte[] data) {
		synchronized (digestMD5) {
			digestMD5.update(data) ;
			byte[] digest = digestMD5.digest() ;
			digestMD5.reset() ;
			return digest ;
		}
	}
	
	public byte[] calcMD5(File file) throws IOException {
		FileInputStream fin = new FileInputStream(file) ;
		
		try {
			return calcMD5(fin) ;			
		}
		finally {
			fin.close();
		}
	}
	
	public byte[] calcMD5(InputStream in) throws IOException {
		byte[] buff = new byte[1024*8] ;
		int r ;
		
		synchronized (digestMD5) {
			while ( (r = in.read(buff)) >= 0 ) {
				digestMD5.update(buff, 0, r);
			}
			
			byte[] digest = digestMD5.digest() ;
			digestMD5.reset() ;
			return digest ;
		}
		
	}
	
}

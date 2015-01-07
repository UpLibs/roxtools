package roxtools;

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
	
}

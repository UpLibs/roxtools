package roxtools;

import java.nio.charset.Charset;
import java.util.Arrays;

final public class StringUtils {

	static final public Charset CHARSET_LATIN1 = Charset.forName("iso-8859-1") ;
	static final public Charset CHARSET_UTF8 = Charset.forName("UTF-8") ;
	
	//return (int)(value ^ (value >>> 32));
	
	static public int[] toInts(long[] masks) {
		int[] masks2 = new int[masks.length*2] ;
		int masks2Sz = 0 ;
		
		for (int i = 0; i < masks.length; i++) {
			long n = masks[i];
			
			int n1 = (int) (n & 0xFFFFFFFF) ;
			int n2 = (int) ((n >>> 32) & 0xFFFFFFFF) ;
			
			masks2[masks2Sz++] = n1 ;
			masks2[masks2Sz++] = n2 ;
		}
		
		return masks2 ;
	}
	
	static public int[] toIntsMask(long[] masks) {
		int[] masks2 = toInts(masks) ;
		
		// ensure that mask doesn't have much zeros:
		
		int fixMask = 1236843025 ;
		
		for (int i = 0; i < masks2.length; i++) {
			masks2[i] = masks2[i] ^ fixMask ;
			fixMask *= 31 ;
		}
		
		return masks2 ;
	}
	
	static public String maskString(String str, long... masks) {
		int[] masks2 = toIntsMask(masks) ;
		return maskString(str, masks2) ;
	}
	
	static public String maskString(String str, int... masks) {
		
		masks = masks.clone() ;
		
		byte[] bs = str.getBytes(CHARSET_LATIN1) ;
		
		int bsOriginalSize = bs.length ;
		
		if (bsOriginalSize % 4 != 0) {
			int mod4 = bsOriginalSize % 4 ;
			int szMod4 = mod4 > 0 ? bsOriginalSize + (4-mod4) : bsOriginalSize ;
			bs = Arrays.copyOf(bs, szMod4) ;
		}
		
		int bsOff = 0 ;
		
		byte[] bs2 = new byte[bs.length] ;
		int bs2Sz = 0 ;
		
		int maskIdx = -1 ;
		
		for (int i = 0; i < bs.length; i+= 4) {
			int n = SerializationUtils.readInt(bs, bsOff) ;
			bsOff += 4 ;
			
			maskIdx = (++maskIdx) % masks.length ; 
			
			int mask = masks[maskIdx] ;
			
			masks[maskIdx] = mask * 31 ;
			
			int n2 = n ^ mask ;
			
			mask = mask * 31 ;
			
			SerializationUtils.writeInt(n2, bs2, bs2Sz) ;
			bs2Sz += 4 ;
		}
		
		if (bs2.length != bsOriginalSize) {
			return new String(bs2,0,bsOriginalSize, CHARSET_LATIN1) ;
		}
		else {
			return new String(bs2, CHARSET_LATIN1) ;
		}
		
	}
	
	/////////////////////////////////////
	
	static public class SplitResult {
		final public String[] result ;
		final public int size ;
		
		public SplitResult(String[] result, int size) {
			this.result = result;
			this.size = size;
		}
		
		@Override
		public String toString() {
			return Arrays.toString( Arrays.copyOf(result, size) ) ;
		}
	}
	
	static final private SplitResult splitResultDummy = new SplitResult(new String[0], 0) ;
	
	static public SplitResult split(final String str, final char separatorChar) {
		return split(str, separatorChar, false, null) ;
	}
	
	static public SplitResult split(final String str, final char separatorChar, String[] preAllocatedResult) {
		return split(str, separatorChar, false, preAllocatedResult) ;
	}
	
	static public SplitResult split(final String str, final char separatorChar, final boolean preserveAllTokens) {
		return split(str, separatorChar, preserveAllTokens, null) ;
	}
	
	static public SplitResult split(final String str, final char separatorChar, final boolean preserveAllTokens, String[] preAllocatedResult) {
		if (str == null) return splitResultDummy ;
		
		final int len = str.length();
		
		if (len == 0) return splitResultDummy ;
		
		if (preAllocatedResult == null || preAllocatedResult.length == 0) preAllocatedResult = new String[8] ;
		int resultSize = 0 ;
		
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;
		
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match || preserveAllTokens) {
					if ( resultSize == preAllocatedResult.length ) preAllocatedResult = Arrays.copyOf(preAllocatedResult, Math.min(resultSize*2 , len) ) ;
					preAllocatedResult[ resultSize++ ] = str.substring(start, i) ;
					match = false;
					lastMatch = true;
				}
				start = ++i;
				continue;
			}
			lastMatch = false;
			match = true;
			i++;
		}
		
		if (match || preserveAllTokens && lastMatch) {
			if ( resultSize == preAllocatedResult.length ) preAllocatedResult = Arrays.copyOf(preAllocatedResult, Math.min(resultSize*2 , len) ) ;
			preAllocatedResult[ resultSize++ ] = str.substring(start, i) ;
		}
		
		return new SplitResult(preAllocatedResult, resultSize) ;
    }
	
}

package roxtools;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.HashMap;

final public class BytesPool {
	
	static private BytesPool defaultInstance ;
	
	static public BytesPool getDefaultInstance() {
		if (defaultInstance == null) {
			synchronized ( BytesPool.class ) {
				if (defaultInstance == null) defaultInstance = new BytesPool() ;
			}
		}
		
		return defaultInstance ;
	}
	
	//////////////////////////////////////////////////////////////////

	final private HashMap<Integer, ArrayDeque<SoftReference<byte[]>>> cacheTable = new HashMap<Integer, ArrayDeque<SoftReference<byte[]>>>() ;
	
	public void clear() {
		
		synchronized (cacheTable) {
			
			for (ArrayDeque<SoftReference<byte[]>> cachedBytes : cacheTable.values()) {
				
				synchronized (cachedBytes) {
					cachedBytes.clear() ;
				}
				
			}
			
			cacheTable.clear() ;
			
		}
		
	}
	
	public byte[] catchBytes(int size) {
		Integer sizeKey = new Integer(size) ;
		
		ArrayDeque<SoftReference<byte[]>> cachedBytes ;
		synchronized (cacheTable) {
			cachedBytes = cacheTable.get(sizeKey) ;
			
			if (cachedBytes == null) {
				cacheTable.put(sizeKey, cachedBytes = new ArrayDeque<SoftReference<byte[]>>() ) ;
			}
		}
		
		synchronized (cachedBytes) {
			
			while (true) {
				SoftReference<byte[]> ref = cachedBytes.pollLast() ;
				if (ref == null) break ;
				
				byte[] bs = ref.get() ;
				if (bs != null) return bs ;
			}
			 
		}
		
		return new byte[size] ;
	}
	
	public void releaseBytes(byte[] bs) {
		SoftReference<byte[]> ref = new SoftReference<byte[]>(bs) ;
		
		Integer sizeKey = new Integer(bs.length) ;
		
		ArrayDeque<SoftReference<byte[]>> cachedBytes ;
		synchronized (cacheTable) {
			cachedBytes = cacheTable.get(sizeKey) ;
			
			if (cachedBytes == null) {
				cacheTable.put(sizeKey, cachedBytes = new ArrayDeque<SoftReference<byte[]>>() ) ;
			}
		}
		
		synchronized (cachedBytes) {
			cachedBytes.addLast(ref) ;
		}
		
	}
	
}

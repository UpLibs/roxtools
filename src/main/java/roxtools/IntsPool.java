package roxtools;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.HashMap;

final public class IntsPool {
	
	static private IntsPool defaultInstance ;
	
	static public IntsPool getDefaultInstance() {
		if (defaultInstance == null) {
			synchronized ( IntsPool.class ) {
				if (defaultInstance == null) defaultInstance = new IntsPool() ;
			}
		}
		
		return defaultInstance ;
	}
	
	//////////////////////////////////////////////////////////////////

	final private HashMap<Integer, ArrayDeque<SoftReference<int[]>>> cacheTable = new HashMap<Integer, ArrayDeque<SoftReference<int[]>>>() ;
	
	public void clear() {
		
		synchronized (cacheTable) {
			
			for (ArrayDeque<SoftReference<int[]>> cachedBytes : cacheTable.values()) {
				
				synchronized (cachedBytes) {
					cachedBytes.clear() ;
				}
				
			}
			
			cacheTable.clear() ;
			
		}
		
	}
	
	public int[] catchInts(int size) {
		Integer sizeKey = new Integer(size) ;
		
		ArrayDeque<SoftReference<int[]>> cached ;
		synchronized (cacheTable) {
			cached = cacheTable.get(sizeKey) ;
		}
		
		if (cached != null) {
			synchronized (cached) {
				
				while (true) {
					SoftReference<int[]> ref = cached.pollLast() ;
					if (ref == null) break ;
					
					int[] ns = ref.get() ;
					if (ns != null) return ns ;
				}
				 
			}
		}
		
		return new int[size] ;
	}
	
	public void releaseInts(int[] ns) {
		SoftReference<int[]> ref = new SoftReference<int[]>(ns) ;
		
		Integer sizeKey = new Integer(ns.length) ;
		
		ArrayDeque<SoftReference<int[]>> cached ;
		synchronized (cacheTable) {
			cached = cacheTable.get(sizeKey) ;
			
			if (cached == null) {
				cacheTable.put(sizeKey, cached = new ArrayDeque<SoftReference<int[]>>(4) ) ;
			}
		}
		
		synchronized (cached) {
			cached.addLast(ref) ;
		}
		
	}
	
}

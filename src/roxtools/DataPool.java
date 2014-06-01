package roxtools;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.HashMap;

abstract public class DataPool<T , K extends DataPoolSizeKey> {
	
	final private HashMap<K, ArrayDeque<SoftReference<T>>> cacheTable = new HashMap<K, ArrayDeque<SoftReference<T>>>() ;
	
	final public void clear() {
		
		synchronized (cacheTable) {
			
			for (ArrayDeque<SoftReference<T>> cachedData : cacheTable.values()) {
				
				synchronized (cachedData) {
					cachedData.clear() ;
				}
				
			}
			
			cacheTable.clear() ;
			
		}
		
	}
	
	
	abstract public T createData(K dataSizeKey) ;
	
	abstract public K getDataSizeKey(T data) ;
	
	final public T catchData(K dataSizeKey) {
		
		ArrayDeque<SoftReference<T>> cachedData ;
		synchronized (cacheTable) {
			cachedData = cacheTable.get(dataSizeKey) ;
			
			if (cachedData == null) {
				cacheTable.put(dataSizeKey, cachedData = new ArrayDeque<SoftReference<T>>() ) ;
			}
		}
		
		synchronized (cachedData) {
			
			while (true) {
				SoftReference<T> ref = cachedData.pollLast() ;
				if (ref == null) break ;
				
				T data = ref.get() ;
				if (data != null) return data ;
			}
			 
		}
		
		return createData(dataSizeKey) ;
	}
	
	final public void releaseData(T data) {
		K dataSizeKey = getDataSizeKey(data) ;
		releaseData(data, dataSizeKey) ;
	}
	
	final public void releaseData(T data, K dataSizeKey) {
		SoftReference<T> ref = new SoftReference<T>(data) ;
		
		ArrayDeque<SoftReference<T>> cachedData ;
		synchronized (cacheTable) {
			cachedData = cacheTable.get(dataSizeKey) ;
			
			if (cachedData == null) {
				cacheTable.put(dataSizeKey, cachedData = new ArrayDeque<SoftReference<T>>() ) ;
			}
		}
		
		synchronized (cachedData) {
			cachedData.addLast(ref) ;
		}
		
	}
	
}

package roxtools;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * A pool of {@link BigHashMapPool}. Since construction of {@link BigHashMapPool} is slow,
 * this can be handy when many instance need to be created and discarded in a code.  
 * 
 * @author Graciliano M. P. (gracilianomp@gmail.com)
 *
 * @param <K>
 * @param <V>
 */

final public class BigHashMapPool<K, V> {
	
	final private RoxDeque<SoftReference<BigHashMap<K, V>>> pool = new RoxDeque<SoftReference<BigHashMap<K,V>>>(100) ;

	final private ReferenceQueue<BigHashMap<K, V>> refQueue = new ReferenceQueue<BigHashMap<K, V>>() ;
	
	final private int maxSize ;
	
	public BigHashMapPool() {
		this(50) ;
	}
	
	public BigHashMapPool(int maxSize) {
		this.maxSize = maxSize ;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	private void clearReferenceQueue() {
		
		Reference<? extends BigHashMap<K, V>> ref ;
		
		while ( ( ref = refQueue.poll() ) != null ) {
			pool.remove(ref) ;
		}
		
	}
	
	public BigHashMap<K, V> create(int initialGroups, int groupSizeAverage, int memoryBlockSize) {
		int maxInitialGroups = initialGroups*3 ;
		
		synchronized (pool) {
			int forSz = pool.size() ;
			
			for (int i = 0; i < forSz;) {
				SoftReference<BigHashMap<K, V>> ref = pool.get(i) ;
				
				BigHashMap<K, V> hash = ref.get() ;
				
				if (hash == null) {
					ref.clear() ;
					pool.remove(i) ;
					--forSz ;
					continue ;
				}
				
				int groupsTableSize = hash.groupsTableSize() ;
				
				if ( groupsTableSize >= initialGroups && groupsTableSize <= maxInitialGroups && hash.getGroupSizeAverage() == groupSizeAverage && hash.getMemoryBlockSize() == memoryBlockSize ) {
					ref.clear() ;
					pool.remove(i) ;
					return hash ;
				}
				
				++i ;
			}
		
			clearReferenceQueue() ;
		}
		
		
		BigHashMap<K, V> hash = new BigHashMap<K, V>(initialGroups, groupSizeAverage, memoryBlockSize) ;
		
		return hash ;
	}
	
	public void release(BigHashMap<K, V> hash) {
		hash.clear();
		
		SoftReference<BigHashMap<K, V>> ref = new SoftReference<BigHashMap<K,V>>(hash, refQueue) ;
		
		synchronized (pool) {
			if (pool.size() < this.maxSize) {
				pool.addFirst(ref) ;
			}
		
			clearReferenceQueue() ;
		}
			
	}
	
}

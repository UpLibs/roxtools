package roxtools;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Same implementation of {@link BigHashMap} but for Integer values.
 * 
 * @see BigHashMap
 * 
 * @author Graciliano M. P. <gracilianomp@gmail.com>
 *
 * @param <K>
 * @param <V>
 */
final public class BigHashMapInteger<K> implements Iterable<K> , Map<K, Integer> {
	
	Object[][] memKeys ;
	int[][] memVals ;
	int[][] memNext ;
	
	/**
	 * Used to optimize reHash, avoiding call of {@code Object.hashcode()}.
	 */
	int[][] memHash ;
	
	int memSize ;
	int memCapacity ;
	
	private void ensureMemoryCapacity(int capacity) {
		
		while (memCapacity < capacity) {
			int prevMemBlocks = memKeys.length ;
			int newMemBlocks = prevMemBlocks + 1 ;
			
			Object[][] keys2 = new Object[newMemBlocks][] ;
			System.arraycopy(memKeys, 0, keys2, 0, prevMemBlocks);
			keys2[prevMemBlocks] = new Object[memoryBlockSize] ;
			memKeys = keys2;
			
			int[][] vals2 = new int[newMemBlocks][] ;
			System.arraycopy(memVals, 0, vals2, 0, prevMemBlocks);
			vals2[prevMemBlocks] = new int[memoryBlockSize] ;
			memVals = vals2;
			
			int[][] next2 = new int[newMemBlocks][] ;
			System.arraycopy(memNext, 0, next2, 0, prevMemBlocks);
			next2[prevMemBlocks] = new int[memoryBlockSize] ;
			memNext = next2;
			
			int[][] hash2 = new int[newMemBlocks][] ;
			System.arraycopy(memHash, 0, hash2, 0, prevMemBlocks);
			hash2[prevMemBlocks] = new int[memoryBlockSize] ;
			memHash = hash2;

			memCapacity = newMemBlocks * memoryBlockSize ;
			
			//System.out.println("MEM BLKS> "+ newMemBlocks +" > "+ memoryBlockSize);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	
	/* TABLE GROUP ABSTRATION BEGIN */
	
	private int idxInit ;
	private int idxEnd ;
	private int idxTotal ;
	private int idxMinHash ;
	private int idxMaxHash ;
	
	protected void groupSetInternals(int gI) {
		int groupInit = gI * GROUPS_ATTRS ;
		
		//System.out.println(gI);
		
		idxInit = groupInit ;
		idxEnd = groupInit + 1 ;
		idxTotal = groupInit + 2 ;
		idxMinHash = groupInit + 3 ;
		idxMaxHash = groupInit + 4 ;
	}
	
	protected int groupPositionOf(int hashcode, Object key) {
		if ( this.groupsMem[idxTotal] == 0 ) return -1 ;
		if ( hashcode < this.groupsMem[idxMinHash] || hashcode > this.groupsMem[idxMaxHash] ) return -1 ;
		
		int cursor = this.groupsMem[idxInit] ;
		
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			if ( memHash[blk][blkI] == hashcode && memKeys[blk][blkI].equals(key) ) return cursor ;
			cursor = memNext[blk][blkI] ;
		}
		
		return -1 ;
	}
	
	protected int groupGetMinHashcode() {
		int cursor = this.groupsMem[idxInit] ;
		
		int minHashcode = 0 ;
		int count = 0 ;
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			int hc = memHash[blk][blkI] ;
			if (++count == 1 || hc < minHashcode) minHashcode = hc ;
			cursor = memNext[blk][blkI] ;
		}
		
		return minHashcode ;
	}
	

	protected int groupGetMaxHashcode() {
		int cursor = this.groupsMem[idxInit] ;
		
		int maxHashcode = 0 ;
		int count = 0 ;
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			int hc = memHash[blk][blkI] ;
			if (++count == 1 || hc > maxHashcode) maxHashcode = hc ;
			cursor = memNext[blk][blkI] ;
		}
		
		return maxHashcode ;
	}
	
	protected String groupToString(int gI) {
		int groupInit = gI * GROUPS_ATTRS ;
		
		int idxInit = groupInit ;
		//int idxEnd = groupInit + 1 ;
		//int idxTotal = groupInit + 2 ;
		int idxMinHash = groupInit + 3 ;
		int idxMaxHash = groupInit + 4 ;
		
		StringBuffer strBuf = new StringBuffer() ;
		
		strBuf.append( this.groupsMem[idxMinHash] +".."+ this.groupsMem[idxMaxHash] + "[") ;
		
		int cursor = this.groupsMem[idxInit] ;
		
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			strBuf.append("(") ;
			strBuf.append( memKeys[blk][blkI] ) ;
			strBuf.append(" , ") ;
			strBuf.append( memVals[blk][blkI] ) ;
			strBuf.append(")") ;
			
			cursor = memNext[blk][blkI] ;
		}
		
		strBuf.append("]") ;
		
		return strBuf.toString();
	}
	
	protected int[] groupLinkOf(int hashcode, Object key) {
		int prev = -1 ;
		int cursor = this.groupsMem[idxInit] ;
		
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			if ( memHash[blk][blkI] == hashcode && memKeys[blk][blkI].equals(key) ) {
				return new int[] { prev , cursor , memNext[blk][blkI] } ;
			}
			prev = cursor ;
			cursor = memNext[blk][blkI] ;
		}
		
		return null ;
	}
	
	protected Integer groupGet(int hashcode, Object key) {
		int pos = groupPositionOf(hashcode , key) ;
		if (pos < 0) return null ;
		int blk = pos/memoryBlockSize ;
		return (Integer) memVals[blk][ pos-(blk*memoryBlockSize) ] ;
	}
	
	
	protected Integer groupPut(int hashcode, K key, Integer val) {
		int pos = groupPositionOf(hashcode, key) ;
		
		if (pos >= 0) {
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;

			Integer prevVal = (Integer) memVals[blk][blkI] ;
			
			memKeys[blk][blkI] = key ;
			memVals[blk][blkI] = val ;
			
			return prevVal ;
		}
		else {
			if (memSize >= threshold) {
				reHash( groupsTotal*2 ) ;
				groupSetInternals( (hashcode & 0x7FFFFFFF) % groupsTotal ) ;
			}
			
			ensureMemoryCapacity(memSize+1) ;
			
			pos = memSize++ ;
			
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;
			
			memKeys[blk][blkI] = key ;
			memVals[blk][blkI] = val ;
			memHash[blk][blkI] = hashcode ;
			memNext[blk][blkI] = -1 ;
			
			this.groupsMem[idxTotal]++ ;
			
			if ( this.groupsMem[idxTotal] == 1) {
				this.groupsMem[idxInit] = pos ;
				
				this.groupsMem[idxMinHash] = this.groupsMem[idxMaxHash] = hashcode ;
			}
			else {
				blk = this.groupsMem[idxEnd]/memoryBlockSize ;
				blkI = this.groupsMem[idxEnd]-(blk*memoryBlockSize) ;
				memNext[blk][blkI] = pos ;
				
				if (hashcode > this.groupsMem[idxMaxHash]) this.groupsMem[idxMaxHash] = hashcode ; 
				if (hashcode < this.groupsMem[idxMinHash]) this.groupsMem[idxMinHash] = hashcode ;
			}
			
			this.groupsMem[idxEnd] = pos ;
			size++ ;
			
			return null ;
		}
	}
	
	protected void groupPutReHash(int hashcode, Object key, int val, int pos) {
		int blk = pos/memoryBlockSize ;
		int blkI = pos-(blk*memoryBlockSize) ;
		
		memKeys[blk][blkI] = key ;
		memVals[blk][blkI] = val ;
		memHash[blk][blkI] = hashcode ;
		memNext[blk][blkI] = -1 ;
		
		if (this.groupsMem[idxTotal] > 0) {
			blk = this.groupsMem[idxEnd]/memoryBlockSize ;
			blkI = this.groupsMem[idxEnd]-(blk*memoryBlockSize) ;
			memNext[blk][blkI] = pos ;
			
			if (hashcode > this.groupsMem[idxMaxHash]) this.groupsMem[idxMaxHash] = hashcode ; 
			if (hashcode < this.groupsMem[idxMinHash]) this.groupsMem[idxMinHash] = hashcode ;
		}
		else {
			this.groupsMem[idxInit] = pos ;
			this.groupsMem[idxMinHash] = this.groupsMem[idxMaxHash] = hashcode ;
		}
		
		this.groupsMem[idxEnd] = pos ;
		this.groupsMem[idxTotal]++ ;
	}
	
	protected Integer groupRemove(int hashcode , Object key, boolean allowRehash) {
		int[] link = groupLinkOf(hashcode , key) ;
		if (link == null) return null ;
				
		int prev = link[0] ;
		int pos = link[1] ;
		int next = link[2] ; 
			
		int blk = pos/memoryBlockSize ;
		int blkI = pos-(blk*memoryBlockSize) ;
		
		Integer prevVal = (Integer) memVals[blk][blkI] ;
		
		memKeys[blk][blkI] = null ;
		memVals[blk][blkI] = 0 ;
		memNext[blk][blkI] = -1 ;
		
		if (prev >= 0) {
			blk = prev/memoryBlockSize ;
			blkI = prev-(blk*memoryBlockSize) ;
			memNext[blk][blkI] = next ;
		}
		
		this.groupsMem[idxTotal]-- ;
		
		if (this.groupsMem[idxTotal] == 0) {
			this.groupsMem[idxInit] = -1 ;
			this.groupsMem[idxEnd] = -1 ;
		}
		else {
			if ( this.groupsMem[idxInit] == pos ) this.groupsMem[idxInit] = next ;
			if ( this.groupsMem[idxEnd] == pos ) this.groupsMem[idxEnd] = prev ;
			
			if ( this.groupsMem[idxMinHash] == hashcode ) this.groupsMem[idxMinHash] = groupGetMinHashcode() ;  
			if ( this.groupsMem[idxMaxHash] == hashcode ) this.groupsMem[idxMaxHash] = groupGetMaxHashcode() ;
		}
		
		this.size-- ;
		
		if (pos == memSize-1) {
			--memSize ;
		}
		else {
			removeCount++ ;
		
			if (removeCount >= this.memSize/2 && allowRehash) {
				int groupsSize =  this.size % this.groupSizeAverage == 0 ? this.size/this.groupSizeAverage : this.size/this.groupSizeAverage+1 ; 
				reHash(groupsSize) ;
			}
		}
		
		return prevVal ;
	}

	/* TABLE GROUP ABSTRATION END */
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	int groupsTotal ;
	int[] groupsMem ;
	
	int size ;
	int removeCount ;
	
	int threshold ;
	final int groupSizeAverage ;
	final int memoryBlockSize ;
	
	static final private int GROUPS_ATTRS = 5 ;
	
	static final public int DEFAULT_INITIAL_GROUPS = 100 ;
	static final public int DEFAULT_GROUP_SIZE_AVERAGE = 10 ;
	static public final int DEFAULT_MEMORY_BLOCK_SIZE = 100000 ;
	
	public BigHashMapInteger() {
		this(DEFAULT_INITIAL_GROUPS) ;
	}
	
	public BigHashMapInteger(int initialGroups) {
		this(initialGroups, DEFAULT_GROUP_SIZE_AVERAGE) ;
	}
	
	public BigHashMapInteger(int initialGroups, int groupSizeAverage) {
		this(initialGroups, groupSizeAverage, DEFAULT_MEMORY_BLOCK_SIZE) ;
	}

	///////////////////////////////////////////////////////////////////////////////
	
	static private Object[][] createObjArray(int a , int b) {
		Object[][] arr = new Object[a][] ;
		
		for (int i = 0; i < a; i++) {
			arr[i] = new Object[b] ;
		}
		
		return arr ;
	}
	
	static private int[][] createIntArray(int a , int b) {
		int[][] arr = new int[a][] ;
		
		for (int i = 0; i < a; i++) {
			arr[i] = new int[b] ;
		}
		
		return arr ;
	}
	
	/**
	 * 
	 * @param initialGroups The initial number of groups. This is useful to avoid reHash calls when the number of elements to be inserted is already known and you are populating a new {@code BigHashMapInteger} instance. (Default: 100)
	 * @param groupSizeAverage The group size average to be maintained. A low number will increase reHash calls, and a high number will make search of elements slower. (Default: 10)
	 * @param memoryBlockSize The size of each block of memory used to represent the internal linked lists. Is recommended to use a range proportional to the maximal number of elements to store. (Default: 100000)
	 */
	public BigHashMapInteger(int initialGroups, int groupSizeAverage, int memoryBlockSize) {
		int initialMemory = initialGroups * groupSizeAverage ;
		
		int blocks = initialMemory / memoryBlockSize ;
		if ( initialMemory % memoryBlockSize != 0 ) blocks++ ;
		
		this.memKeys = createObjArray(blocks, memoryBlockSize) ;
		this.memVals = createIntArray(blocks, memoryBlockSize) ;
		this.memHash = createIntArray(blocks, memoryBlockSize) ;
		this.memNext = createIntArray(blocks, memoryBlockSize) ;
		
		this.memCapacity = blocks * memoryBlockSize ;
		this.memSize = 0 ;
		
		this.groupsMem = new int[initialGroups*GROUPS_ATTRS] ;
		
		int forSz = this.groupsMem.length ;
		for (int i = 0; i < forSz ; i += GROUPS_ATTRS) {
			this.groupsMem[i] = -1 ;
		}
		
		this.groupsTotal = initialGroups ;
		
		this.groupSizeAverage = groupSizeAverage ;
		this.memoryBlockSize = memoryBlockSize ;
		
		this.threshold = initialGroups * groupSizeAverage ;
	}
	
	/**
	 * The number of elements in this Hashtable.
	 * @return
	 */
	public int size() {
		return size ;
	}
	
	protected void reHash(int totalGroups) {

		//System.out.println("*reHash> "+ totalGroups +" >> "+ toString());
		
		this.groupsMem = new int[totalGroups*GROUPS_ATTRS] ;
		for (int i = 0; i < totalGroups; i++) {
			int groupInit = i * GROUPS_ATTRS ;
			this.groupsMem[groupInit] = -1 ;
			this.groupsMem[groupInit+2] = 0 ;
		}
		this.groupsTotal = totalGroups ;
		
		if (size > 0) {				
			int prevMemSize = memSize ;
			
			memSize-- ;
			final int blkLimit = memSize/memoryBlockSize ;
			final int blkLimitI = (memSize-(blkLimit*memoryBlockSize)) +1 ;
			
			memSize = 0 ;
			int skip = 0 ;
			
			for (int i = 0; i <= blkLimit; i++) {
				int blkEnd = i == blkLimit ? blkLimitI : memoryBlockSize ; 
				for (int j = 0; j < blkEnd; j++) {
					Object key = memKeys[i][j] ;
					if (key == null) {
						++skip ;
						continue ;
					}
					
					int val = memVals[i][j] ;
					int hc = memHash[i][j] ;
					
					groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
					
					groupPutReHash( hc , key , val , memSize++ ) ;
				}
			}
			
			for (int i = prevMemSize-skip; i < prevMemSize; i++) {
				int blk = i/memoryBlockSize ;
				int blkI = i-(blk*memoryBlockSize) ;
				memKeys[blk][blkI] = null ;
				memVals[blk][blkI] = 0 ;
			}
			
			size = memSize ;
		}
		
		int neededMemBlks = memSize / memoryBlockSize + 1 ;
		
		if ( neededMemBlks < memKeys.length ) {
			Object[][] keys2 = new Object[neededMemBlks][] ;
			System.arraycopy(memKeys, 0, keys2, 0, neededMemBlks);
			memKeys = keys2;
			
			int[][] vals2 = new int[neededMemBlks][] ;
			System.arraycopy(memVals, 0, vals2, 0, neededMemBlks);
			memVals = vals2;
			
			int[][] next2 = new int[neededMemBlks][] ;
			System.arraycopy(memNext, 0, next2, 0, neededMemBlks);
			memNext = next2;
			
			int[][] hash2 = new int[neededMemBlks][] ;
			System.arraycopy(memHash, 0, hash2, 0, neededMemBlks);
			memHash = hash2;

			memCapacity = neededMemBlks * memoryBlockSize ;
		}
		
		this.threshold = groupsTotal * groupSizeAverage ;
		this.removeCount = 0 ;
	}
	
	/**
	 * /**
	 * Insert a pair of key and value in this {@code BigHashMapInteger}.
	 * 
	 * @param key
	 * @param val
	 */
	public Integer put(K key, Integer val) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
		
		return groupPut( hc , key , val ) ;
	}

	public Integer get(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
		
		return groupGet( hc , key ) ;
	}
	
	/**
	 * Remove a key from this {@code BigHashMapInteger}.
	 * 
	 * @param key
	 * @return The previous value of the key.
	 */
	public Integer remove(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
		
		return groupRemove( hc , key , true ) ;
	}
	
	private Integer removeNoRehash(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
		
		return groupRemove( hc , key , false ) ;
	}
	
	/**
	 * Clear all the elements.
	 */
	public void clear(){
		for (int i = 0; i < groupsTotal; i++) {
			int groupInit = i * GROUPS_ATTRS ;
			this.groupsMem[groupInit] = -1 ;
			this.groupsMem[groupInit+2] = 0 ;
		}
		
		{
			Object[] tmpObj = new Object[ Math.min(1000, memoryBlockSize)] ;
			int[] tmpInt = new int[tmpObj.length] ;
			
			int wrote = 0 ;
			while (wrote < memoryBlockSize) {
				int len = Math.min( tmpObj.length , memoryBlockSize-wrote) ;
				System.arraycopy(tmpObj, 0, memKeys[0], wrote, len) ;
				System.arraycopy(tmpInt, 0, memVals[0], wrote, len) ;
				System.arraycopy(tmpInt, 0, memNext[0], wrote, len) ;
				System.arraycopy(tmpInt, 0, memHash[0], wrote, len) ;
				wrote += len ;
			}
			
			for (int i = 1; i < memKeys.length; i++) {
				System.arraycopy(memKeys[0], 0, memKeys[i], 0, memoryBlockSize) ;
				System.arraycopy(memVals[0], 0, memVals[i], 0, memoryBlockSize) ;
				System.arraycopy(memNext[0], 0, memNext[i], 0, memoryBlockSize) ;
				System.arraycopy(memHash[0], 0, memHash[i], 0, memoryBlockSize) ;
			}
			
		}
		
		this.size = 0 ;
		this.memSize = 0 ;
		this.removeCount = 0 ;
	}
	
	public void compact() {
		int groupsSize =  this.size % this.groupSizeAverage == 0 ? this.size/this.groupSizeAverage : this.size/this.groupSizeAverage+1 ;
		
		if ( this.size == this.memSize && this.groupsTotal == groupsSize ) return ;
				
		reHash(groupsSize) ;
	}
	
	/**
	 * @param key The key to search.
	 * @return True if the key exists in this {@code BigHashMapInteger}.
	 */
	public boolean containsKey(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & 0x7FFFFFFF) % groupsTotal ) ;
		
		return groupPositionOf(hc, key) >= 0 ;	
	}
	
	public boolean containsValue(Object value) {
		Iterator<Integer> iteratorValues = iteratorValues() ;
		
		while ( iteratorValues.hasNext() ) {
			if ( iteratorValues.next().equals(value) ) return true ;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer() ;
		
		strBuf.append( this.groupsTotal ) ;
		strBuf.append(";") ;
		strBuf.append( this.memCapacity ) ;
		
		strBuf.append("{\n") ;
		
		for (int i = 0; i < groupsTotal ; i++) {
			strBuf.append( groupToString(i) ) ;
			strBuf.append(" ,\n") ;
		}
		
		strBuf.append("}") ;
		
		return strBuf.toString();
	}

	///////////////////////////////////////////////////////////////////
	
	abstract private class MyIteratorAbstract<T> implements Iterator<T> {
		int blk = 0 ;
		int blkI = -1 ;
		int count = 0 ;
		
		public boolean hasNext() {
			if (count >= size) return false ;
			
			while ( true ) {
				++blkI ;
				if (blkI >= memoryBlockSize) {
					if ( ++blk >= memKeys.length ) return false ;
					blkI = 0 ;
				}
					
				if ( memKeys[blk][blkI] != null ) {
					count++ ;
					return true ;
				}
				
			}
		}
	}
	
	private class MyIterator extends MyIteratorAbstract<K> {
		@SuppressWarnings("unchecked")
		public K next() {
			return (K) memKeys[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMapInteger.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a @{code Iterator} of keys. The keys order of this
	 * @{code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<K> iterator() {
		return new MyIterator() ;
	}
	

	private class MyIteratorValues extends MyIteratorAbstract<Integer> {
		public Integer next() {
			return (Integer) memVals[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMapInteger.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a @{code Iterator} of values. The values order of this
	 * @{code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<Integer> iteratorValues() {
		return new MyIteratorValues() ;
	}

	private class MyIteratorEntries extends MyIteratorAbstract<Entry<K, Integer>> {
		@SuppressWarnings("unchecked")
		public Entry<K, Integer> next() {
			return new MyIteratorEntry( memKeys[blk][blkI] , memVals[blk][blkI] )  ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMapInteger.this.removeNoRehash(key) ;
		}
	}
	
	@SuppressWarnings("rawtypes")
	static private class MyIteratorEntry implements Entry {
		Object key ;
		Object val ;
		
		public MyIteratorEntry(Object key, Object val) {
			this.key = key;
			this.val = val;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return val;
		}

		public Object setValue(Object value) {
			Object prevVal = val ;
			val = value ;
			return prevVal ;
		}
		
	}
	
	/**
	 * Returns a @{code Iterator} of pair of keys and values ({@code Entry<K, Integer>}).
	 * The entries order of this @{code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<Entry<K, Integer>> iteratorEntries() {
		return new MyIteratorEntries() ;
	}

	public Set<K> keySet() {
		return new KeySet();
	}

	private class KeySet extends AbstractSet<K> {
		public Iterator<K> iterator() {
			return BigHashMapInteger.this.iterator() ;
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return BigHashMapInteger.this.removeNoRehash(o) != null;
		}

		public void clear() {
			BigHashMapInteger.this.clear();
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, Integer>> entrySet() {
		return new EntrySet() ;
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, Integer>> {
		public Iterator<Map.Entry<K, Integer>> iterator() {
			return iteratorEntries() ;
		}

		public boolean add(Map.Entry<K, Integer> o) {
			return super.add(o);
		}

		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			
			Map.Entry<K,Integer> entry = (Map.Entry<K,Integer>) o;
			Object key = entry.getKey();
			
			return containsKey(key) ;
		}

		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			
			Map.Entry<K,Integer> entry = (Map.Entry<K,Integer>) o;
			Object key = entry.getKey();
			
			return BigHashMapInteger.this.removeNoRehash(key) != null ;
		}

		public int size() {
			return size;
		}

		public void clear() {
			BigHashMapInteger.this.clear();
		}
	}
	
	public Collection<Integer> values() {
		return new ValueCollection();
	}

	private class ValueCollection extends AbstractCollection<Integer> {
		public Iterator<Integer> iterator() {
			return BigHashMapInteger.this.iteratorValues();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}

		public void clear() {
			BigHashMapInteger.this.clear();
		}
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0 ;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends Integer> m) {
		 for (Map.Entry<? extends K, ? extends Integer> e : m.entrySet()) {
	            put(e.getKey(), e.getValue());		
		 }
	}

	
}

package roxtools;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 
 * The class {@code BigHashMap} implements a high performance {@link java.util.HashMap}, that reduces
 * some issues with the standard {@link java.util.HashMap}:
 * 
 * <ul>
 * 
 * <li>
 * The standard {@link java.util.HashMap} creates an object {@code Hashtable.Entry} for each
 * entry stored. This slow down the reHash algorithm, since a reHash will
 * need to re-allocate a new {@code Hashtable.Entry} for each element.
 * </li>
 * <li>
 * The use of a {@code Hashtable.Entry} for each element in the HashMap will increase the
 * number of objects in the VM, what creates more fragmentation and slow down the GC, since the
 * number of objects to track will be increased.
 * </li>
 * <li>
 * Due the nature of the linked list, is impossible to know the insertion order of the keys
 * in the {@link HashMap} (unless an extra data structure is used). 
 * </li>
 * </ul>
 * <p>
 * {@code BigHashMap} doesn't allocate new objects for each element to store them, what reduces
 * memory usage, speed up reHash algorithm, and increases GC speed. The internal abstraction
 * of {@code BigHashMap} is similar to {@code java.util.HashMap}, based in groups of
 * linked lists, but in {@code BigHashMap} the linked list is not represented with dynamic allocation
 * like on {@code Hashtable.Entry}.
 * <p>
 * The internal data structure of {@code BigHashMap} is bases in two parts, table of groups and
 * the memory used to represent the linked lists of the groups. All of them based in
 * arrays, reducing dynamic memory allocation (See source code to understand better the internal
 * data structure).
 * <p>
 * Due the internal structure of {@code BigHashMap}, is possible to know the order of insertion
 * of each key. The ability to know this order is a collateral effect of the internal structure,
 * and not a purpose of the implementation.    
 * <p>
 * Another extra optimization on {@code BigHashMap} is to avoid search of objects not stored
 * on a group when {@code put(K,V)} is called. A {@link HashMap} need to know if a {@code key}
 * is already stored when inserting it. To avoid search inside the linked list, each
 * group has the minimal and maximal {@code hashcode} stored on it. This will allow to know the
 * range of {@code hashcode} stored in the group, reducing search and speeding up insertion. 
 * 
 * @author Graciliano M. P. (gracilianomp@gmail.com)
 *
 * @param <K>
 * @param <V>
 */
final public class BigHashMap<K,V> implements Iterable<K> , Map<K, V> {
	
	private static final int MASK_REMOVE_NEGATIVE_SIGN = 0x7FFFFFFF;
	
	Object[][] memKeys ;
	Object[][] memVals ;
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
			
			Object[][] vals2 = new Object[newMemBlocks][] ;
			System.arraycopy(memVals, 0, vals2, 0, prevMemBlocks);
			vals2[prevMemBlocks] = new Object[memoryBlockSize] ;
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
	
	/*
	 * This section of the code represents the table of groups. A simple way to understand this
	 * abstraction is to think that this is a optimized representation of a class like that:
	 * 
	 * <code>
	 * 
	 *   private class Group {
	 *   	private int init ; // init key position on memKeys[].
	 *   	private int end ; // end key position on memKeys[].
	 *   	private int total ; // total elements in this group.
	 *   	private int minHash ; // minimal hashcode in the group.
	 *   	private int maxHash ; // maximal hashcode in the group.
	 *   }
	 * 
	 * </code>
	 * 
	 * A Hashtable that stores too much elements will have too much Groups, what creates a memory
	 * hot spot. To minimize dynamic allocation and reduce memory, the class above is represented
	 * over <code> this.groupsMem </code> and <code> this.groupsTotal </code>. Where each attribute
	 * in the above class is an element in the array <code> this.groupsMem </code>. So, the total
	 * attribute of the 1st group should be at <code> this.groupsMem[2] </code>, and for the 2nd
	 * group at <code> this.groupsMem[7] </code>.
	 * <p>
	 * To represent all the table groups over a single array and work with them using the group methods below,
	 * <code> groupSetInternals() </code> should be called before, defining the pointer attributes below with
	 * the position in the array <code> this.groupsMem </code>. So, to access the attribute total, will
	 * be with <code> this.groupsMem[idxTotal] </code>. A simple way to understand the data structure is the
	 * algorithm below, that counts the total of elements in all the groups:
	 * 
	 *  <code>
	 *  
	 *    int totalInAllGroups = 0 ;
	 *    
	 *    for (int i = 0 ; i < this.groupsTotal ; i++) {
	 *      groupSetInternals(i) ;
	 *      totalInAllGroups += this.groupsMem[idxTotal] ;
	 *    }
	 *  
	 *  </code>
	 * 
	 * And here is the equivalent code if we had used normal Object orientation, like the class Group above:
	 * 
	 * <code>
	 * 
	 *    // groups are stored at array:  Group[] groups ;
	 * 
	 * 	  int totalInAllGroups = 0 ;
	 *    
	 *    for (int i = 0 ; i < this.groupsTotal ; i++) {
	 *      Group group = groups[i] ;
	 *      totalInAllGroups += group.total ;
	 *    }
	 * 
	 * </code>
	 * 
	 */
	
	private int idxInit ;
	private int idxEnd ;
	private int idxTotal ;
	private int idxMinHash ;
	private int idxMaxHash ;
	
	/**
	 * Should be called before work with a specific group, preparing internal attribute
	 * to act like the desired group.
	 * @param gI The index of the group.
	 */
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
	
	@SuppressWarnings("unchecked")
	protected V groupGet(int hashcode, Object key) {
		int pos = groupPositionOf(hashcode , key) ;
		if (pos < 0) return null ;
		int blk = pos/memoryBlockSize ;
		return (V) memVals[blk][ pos-(blk*memoryBlockSize) ] ;
	}
	
	
	protected V groupPut(int hashcode, K key, V val) {
		int pos = groupPositionOf(hashcode, key) ;
		
		if (pos >= 0) {
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;

			@SuppressWarnings("unchecked")
			V prevVal = (V) memVals[blk][blkI] ;
			
			memKeys[blk][blkI] = key ;
			memVals[blk][blkI] = val ;
			
			return prevVal ;
		}
		else {
			if (memSize >= threshold) {
				reHash( groupsTotal*2 ) ;
				groupSetInternals( (hashcode & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
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
	
	protected V groupPutIfExists(int hashcode, K key, V val) {
		int pos = groupPositionOf(hashcode, key) ;
		if (pos < 0) return null ;
		
		int blk = pos/memoryBlockSize ;
		int blkI = pos-(blk*memoryBlockSize) ;

		@SuppressWarnings("unchecked")
		V prevVal = (V) memVals[blk][blkI] ;
		
		memKeys[blk][blkI] = key ;
		memVals[blk][blkI] = val ;
		
		return prevVal ;
	}
	
	protected void groupPutReHash(int hashcode, Object key, Object val, int pos) {
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
	
	protected V groupRemove(int hashcode , Object key, boolean allowRehash) {
		int[] link = groupLinkOf(hashcode , key) ;
		if (link == null) return null ;
				
		int prev = link[0] ;
		int pos = link[1] ;
		int next = link[2] ; 
			
		int blk = pos/memoryBlockSize ;
		int blkI = pos-(blk*memoryBlockSize) ;
		
		@SuppressWarnings("unchecked")
		V prevVal = (V) memVals[blk][blkI] ;
		
		memKeys[blk][blkI] = null ;
		memVals[blk][blkI] = null ;
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
				reHash(groupsSize > 0 ? groupsSize : 1) ;
			}
		}
		
		return prevVal ;
	}


	protected V groupGetReorder(int hashcode , Object key) {
		int[] link = groupLinkOf(hashcode , key) ;
		if (link == null) return null ;
				
		int prev = link[0] ;
		int pos = link[1] ;
		int next = link[2] ; 
			
		int blk = pos/memoryBlockSize ;
		int blkI = pos-(blk*memoryBlockSize) ;
		
		@SuppressWarnings("unchecked")
		V prevVal = (V) memVals[blk][blkI] ;
		
		int endPos = this.groupsMem[idxEnd] ;
		
		if (endPos != pos) {
			assert( this.groupsMem[idxTotal] > 1 ) ;
			
			memNext[blk][blkI] = -1 ;
			
			if (prev >= 0) {
				blk = prev/memoryBlockSize ;
				blkI = prev-(blk*memoryBlockSize) ;
				memNext[blk][blkI] = next ;
			}
			else {
				assert( this.groupsMem[idxInit] == pos ) ;
				this.groupsMem[idxInit] = next ;
			}
			
			blk = endPos/memoryBlockSize ;
			blkI = endPos-(blk*memoryBlockSize) ;
			memNext[blk][blkI] = pos ;
			
			this.groupsMem[idxEnd] = pos ;
		}
		
		return prevVal ;
	}
	
	protected V groupPutReorder(int hashcode, K key, V val) {
		int[] link = groupLinkOf(hashcode , key) ;
		
		if (link != null) {
			int prev = link[0] ;
			int pos = link[1] ;
			int next = link[2] ; 
				
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;
			
			@SuppressWarnings("unchecked")
			V prevVal = (V) memVals[blk][blkI] ;
			memNext[blk][blkI] = -1 ;
			
			memKeys[blk][blkI] = key ;
			memVals[blk][blkI] = val ;
			
			if (prev >= 0) {
				blk = prev/memoryBlockSize ;
				blkI = prev-(blk*memoryBlockSize) ;
				memNext[blk][blkI] = next ;
			}
			
			if (this.groupsMem[idxTotal] > 1) {
				blk = this.groupsMem[idxEnd]/memoryBlockSize ;
				blkI = this.groupsMem[idxEnd]-(blk*memoryBlockSize) ;
				memNext[blk][blkI] = pos ;
				
				if ( this.groupsMem[idxInit] == pos ) this.groupsMem[idxInit] = next ;
				this.groupsMem[idxEnd] = pos ;
			}
			
			return prevVal ;
		}
		else {
			if (memSize >= threshold) {
				reHash( groupsTotal*2 ) ;
				groupSetInternals( (hashcode & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
			}
			
			ensureMemoryCapacity(memSize+1) ;
			
			int pos = memSize++ ;
			
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
	
	public BigHashMap() {
		this(DEFAULT_INITIAL_GROUPS) ;
	}
	
	public BigHashMap(int initialGroups) {
		this(initialGroups, DEFAULT_GROUP_SIZE_AVERAGE) ;
	}
	
	public BigHashMap(int initialGroups, int groupSizeAverage) {
		this(initialGroups, groupSizeAverage, DEFAULT_MEMORY_BLOCK_SIZE) ;
	}

	///////////////////////////////////////////////////////////////////////////////
	
	/*
	 * For some reason this two methods are faster than this:
	 * 
	 *   // this is slower:
	 *   Object[][] mem = new Object[a][b] ;
	 *   // and this is faster: 
	 *   Object[][] mem = createObjArray(a,b) ;
	 *   
	 * I think that this is because JVM tries to allocate consecutive blocks of memory,
	 * and the methods below doesn't care about this.
	 * 
	 */

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
	 * @param initialGroups The initial number of groups. This is useful to avoid reHash calls when the number of elements to be inserted is already known and you are populating a new {@code BigHashMap} instance. (Default: 100)
	 * @param groupSizeAverage The group size average to be maintained. A low number will increase reHash calls, and a high number will make search of elements slower. (Default: 10)
	 * @param memoryBlockSize The size of each block of memory used to represent the internal linked lists. Is recommended to use a range proportional to the maximal number of elements to store. (Default: 100000)
	 */
	public BigHashMap(int initialGroups, int groupSizeAverage, int memoryBlockSize) {
		if (initialGroups <= 0) initialGroups = 1 ;
		if (groupSizeAverage <= 0) groupSizeAverage = 1 ;
		
		int initialMemory = initialGroups * groupSizeAverage ;
		
		int blocks = initialMemory / memoryBlockSize ;
		if ( initialMemory % memoryBlockSize != 0 ) blocks++ ;
		
		this.memKeys = createObjArray(blocks, memoryBlockSize) ;
		this.memVals = createObjArray(blocks, memoryBlockSize) ;
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
	
	public int groupsTableSize() {
		return this.groupsTotal ;
	}
	
	public int getGroupSizeAverage() {
		return groupSizeAverage;
	}
	
	public int getMemoryBlockSize() {
		return memoryBlockSize;
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
					
					Object val = memVals[i][j] ;
					int hc = memHash[i][j] ;
					
					groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
					
					groupPutReHash( hc , key , val , memSize++ ) ;
				}
			}
			
			for (int i = prevMemSize-skip; i < prevMemSize; i++) {
				int blk = i/memoryBlockSize ;
				int blkI = i-(blk*memoryBlockSize) ;
				memKeys[blk][blkI] = null ;
				memVals[blk][blkI] = null ;
			}
			
			size = memSize ;
		}
		
		int neededMemBlks = memSize / memoryBlockSize + 1 ;
		
		if ( neededMemBlks < memKeys.length ) {
			Object[][] keys2 = new Object[neededMemBlks][] ;
			System.arraycopy(memKeys, 0, keys2, 0, neededMemBlks);
			memKeys = keys2;
			
			Object[][] vals2 = new Object[neededMemBlks][] ;
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
	 * Insert a pair of key and value in this {@code BigHashMap}.
	 * 
	 * @param key
	 * @param val
	 */
	public V put(K key, V val) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupPut( hc , key , val ) ;
	}

	/**
	 * Same as put(). but only if the key already exists.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putIfExists(K key, V val) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupPutIfExists( hc , key , val ) ;
	}

	
	/**
	 * Same as put(). but if key exists will be reordered by access time.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putReorder(K key, V val) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupPutReorder( hc , key , val ) ;
	}

	public V get(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupGet( hc , key ) ;
	}
	
	/**
	 * Same as get(), but reorder the internal keys by access time.
	 * @param key
	 * @return
	 */
	public V getReorder(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupGetReorder( hc , key ) ;
	}
	
	/**
	 * Remove a key from this {@code BigHashMap}.
	 * 
	 * @param key
	 * @return The previous value of the key.
	 */
	public V remove(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupRemove( hc , key , true ) ;
	}
	
	public V removeNoRehash(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
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
				System.arraycopy(tmpObj, 0, memVals[0], wrote, len) ;
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
	
	///////////////////////////////////////////////////////////////////
	
	public int indexOfKey(K key) {
		if (key == null) return -1 ;
		
		int hashcode = key.hashCode() ;
		
		int count = 0 ;
		int blk = 0 ;
		int blkI = -1 ;
		
		while ( count < size ) {
			++blkI ;
			if (blkI >= memoryBlockSize) {
				if ( ++blk >= memKeys.length ) return -1 ;
				blkI = 0 ;
			}
			
			if ( memKeys[blk][blkI] != null ) {
				if ( memHash[blk][blkI] == hashcode && memKeys[blk][blkI].equals(key) ) return count ; 
				count++ ;
			}
		}
		
		return -1 ;
	}
	
	public Entry<K, V> getEntry(int idx) {
		if (idx > memSize/2) {
			return getEntryDescendent(idx) ;
		}
		else {
			return getEntryAscendent(idx) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Entry<K, V> getEntryAscendent(int idx) {
		if (idx >= size) return null ;
		
		System.out.println("get>> "+ idx);
		
		
		int count = 0 ;
		int blk = 0 ;
		int blkI = -1 ;
		
		while ( count < size ) {
			++blkI ;
			if (blkI >= memoryBlockSize) {
				if ( ++blk >= memKeys.length ) return null ;
				blkI = 0 ;
			}
				
			if ( memKeys[blk][blkI] != null ) {
				if ( idx == count ) {
					K k = (K) memKeys[blk][blkI] ;
					V v = (V) memVals[blk][blkI] ;
					
					return new MyEntry<K,V>(k,v) ; 
				}
				else if ( count > idx ) {
					break ;
				}
				
				count++ ;
			}
		}
		
		return null ;
	}
	
	@SuppressWarnings("unchecked")
	public Entry<K, V> getEntryDescendent(int idx) {
		if (idx >= size) return null ;
		
		System.out.println("get<< "+ idx);
		
		int count = size-1 ;
		int blk = memSize/memoryBlockSize ;
		int blkI = (memSize-(blk*memoryBlockSize))+1 ;
		
		while ( count >= 0 ) {
			--blkI ;
			if (blkI < 0) {
				if ( --blk < 0 ) return null ;
				blkI = memoryBlockSize-1 ;
			}
				
			if ( memKeys[blk][blkI] != null ) {
				if ( idx == count ) {
					K k = (K) memKeys[blk][blkI] ;
					V v = (V) memVals[blk][blkI] ;
					
					return new MyEntry<K,V>(k,v) ; 
				}
				else if ( count < idx ) {
					break ;
				}
				
				count-- ;
			}
		}
		
		return null ;
	}
	
	public Entry<K, V> removeEntry(int idx) {
		java.util.Map.Entry<K, V> entry = getEntry(idx) ;
		
		if (entry != null) remove( entry.getKey() ) ;
		
		return entry ;
	}
	
	///////////////////////////////////////////////////////////////////
	
	
	static private class MyEntry<K,V> implements Entry<K, V> {

		final private K key ;
		final private V value ;
		
		protected MyEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public String toString() {
			return "["+key+" , "+value+"]" ;
		}
	}
	
	
	public void compact() {
		int groupsSize =  this.size % this.groupSizeAverage == 0 ? this.size/this.groupSizeAverage : this.size/this.groupSizeAverage+1 ;
		
		if ( this.size == this.memSize && this.groupsTotal == groupsSize ) return ;
				
		reHash(groupsSize > 0 ? groupsSize : 1) ;
	}
	
	/**
	 * @param key The key to search.
	 * @return True if the key exists in this {@code BigHashMap}.
	 */
	public boolean containsKey(Object key) {
		int hc =  key.hashCode() ;
		
		groupSetInternals( (hc & MASK_REMOVE_NEGATIVE_SIGN) % groupsTotal ) ;
		
		return groupPositionOf(hc, key) >= 0 ;	
	}
	
	public boolean containsValue(Object value) {
		Iterator<V> iteratorValues = iteratorValues() ;
		
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
	
	static abstract public class WalkerEntry<K,V> {
		public WalkerEntry() {
		}
		
		abstract public boolean step(K key , V value) ;
	}
	
	static abstract public class Walker<T> {
		public Walker() {
		}
		
		abstract public boolean step(T key) ;
	}
	
	@SuppressWarnings("unchecked")
	public void walkEntries(WalkerEntry<K, V> walker) {
		
		int count = 0 ;
		INITLOOP: for (int blk = 0; blk < memKeys.length; blk++) {
			for (int blkI = 0; blkI < memoryBlockSize; blkI++) {
				Object key = memKeys[blk][blkI] ;
				if (key != null) {
					if (!walker.step( (K)key , (V)memVals[blk][blkI] )) break INITLOOP ;
					if (++count >= size) break INITLOOP ;
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void walkEntriesKey(Walker<K> walker) {
		
		int count = 0 ;
		INITLOOP: for (int blk = 0; blk < memKeys.length; blk++) {
			for (int blkI = 0; blkI < memoryBlockSize; blkI++) {
				Object key = memKeys[blk][blkI] ;
				if (key != null) {
					if(!walker.step( (K)key )) break INITLOOP ;
					if (++count >= size) break INITLOOP ;
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void walkEntriesValue(Walker<V> walker) {
		
		int count = 0 ;
		INITLOOP: for (int blk = 0; blk < memKeys.length; blk++) {
			for (int blkI = 0; blkI < memoryBlockSize; blkI++) {
				Object key = memKeys[blk][blkI] ;
				if (key != null) {
					if(!walker.step( (V)memVals[blk][blkI] )) break INITLOOP ;
					if (++count >= size) break INITLOOP ;
				}
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void walkRandomEntries(WalkerEntry<K, V> walker) {
		boolean[] memIndexes = new boolean[memSize] ;
		Random rand = new Random() ;
		
		INITLOOP: for (int count = 0; count < size;) {
			int idx = rand.nextInt(memSize) ;
			
			if ( memIndexes[idx] ) {
				int end = Math.max( memSize-idx , idx ) ;
				for (int i = 1; i < end; i++) {
					int x = idx+i ;
					int y = idx-i ;
					if ( x < memSize && !memIndexes[x] ) {
						idx = x ;
						break ;
					}
					else if ( y >= 0 && !memIndexes[y] ) {
						idx = y ;
						break ;
					}
				}
				
				assert( !memIndexes[idx] ) ;
			}

			memIndexes[idx] = true ;
			
			int blk = idx/memoryBlockSize ;
			int blkI = idx-(blk*memoryBlockSize) ;
			
			Object key = memKeys[blk][blkI] ;
			if (key != null) {
				if(!walker.step( (K)key , (V)memVals[blk][blkI] )) break INITLOOP ;
				++count ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void walkRandomEntriesKey(Walker<K> walker) {
		boolean[] memIndexes = new boolean[memSize] ;
		Random rand = new Random() ;
		
		INITLOOP: for (int count = 0; count < size;) {
			int idx = rand.nextInt(memSize) ;
			
			if ( memIndexes[idx] ) {
				int end = Math.max( memSize-idx , idx ) ;
				for (int i = 1; i < end; i++) {
					int x = idx+i ;
					int y = idx-i ;
					if ( x < memSize && !memIndexes[x] ) {
						idx = x ;
						break ;
					}
					else if ( y >= 0 && !memIndexes[y] ) {
						idx = y ;
						break ;
					}
				}
				
				assert( !memIndexes[idx] ) ;
			}

			memIndexes[idx] = true ;
			
			int blk = idx/memoryBlockSize ;
			int blkI = idx-(blk*memoryBlockSize) ;
			
			Object key = memKeys[blk][blkI] ;
			if (key != null) {
				if(!walker.step( (K)key )) break INITLOOP ;
				++count ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void walkRandomEntriesValue(Walker<V> walker) {
		boolean[] memIndexes = new boolean[memSize] ;
		Random rand = new Random() ;
		
		INITLOOP: for (int count = 0; count < size;) {
			int idx = rand.nextInt(memSize) ;
			
			if ( memIndexes[idx] ) {
				int end = Math.max( memSize-idx , idx ) ;
				for (int i = 1; i < end; i++) {
					int x = idx+i ;
					int y = idx-i ;
					if ( x < memSize && !memIndexes[x] ) {
						idx = x ;
						break ;
					}
					else if ( y >= 0 && !memIndexes[y] ) {
						idx = y ;
						break ;
					}
				}
				
				assert( !memIndexes[idx] ) ;
			}

			memIndexes[idx] = true ;
			
			int blk = idx/memoryBlockSize ;
			int blkI = idx-(blk*memoryBlockSize) ;
			
			Object key = memKeys[blk][blkI] ;
			if (key != null) {
				if (!walker.step( (V)memVals[blk][blkI] )) break INITLOOP ;
				++count ;
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////
	
	abstract private class MyRandomIteratorAbstract<T> implements Iterator<T> {
		int count = 0 ;
		int blk = 0 ;
		int blkI = -1 ;
		
		final private boolean[] memIndexes ;
		final private int memSize ;
		
		protected MyRandomIteratorAbstract() {
			this.memSize = BigHashMap.this.memSize ;
			this.memIndexes = new boolean[memSize] ;
		}
		

		Random rand = new Random() ;
		
		public boolean hasNext() {
			while (count < memSize) {
				int idx = rand.nextInt(memSize) ;
				
				if ( this.memIndexes[idx] ) {
					int end = Math.max( memSize-idx , idx ) ;
					for (int i = 1; i < end; i++) {
						int x = idx+i ;
						int y = idx-i ;
						if ( x < memSize && !this.memIndexes[x] ) {
							idx = x ;
							break ;
						}
						else if ( y >= 0 && !this.memIndexes[y] ) {
							idx = y ;
							break ;
						}
					}
					
					assert( !this.memIndexes[idx] ) ;
				}
				
				this.memIndexes[idx] = true ;
				++count ;
				
				blk = idx/memoryBlockSize ;
				blkI = idx-(blk*memoryBlockSize) ;
				
				if ( memKeys[blk][blkI] != null ) {
					return true ;
				}
			}
			
			return false ;
		}
	}
	
	private class MyRandomIterator extends MyRandomIteratorAbstract<K> {
		@SuppressWarnings("unchecked")
		public K next() {
			return (K) memKeys[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of keys. The keys order of this
	 * {@code Iterator} is random. 
	 */
	public Iterator<K> iteratorRandom() {
		return new MyRandomIterator() ;
	}
	

	private class MyRandomIteratorValues extends MyRandomIteratorAbstract<V> {
		@SuppressWarnings("unchecked")
		public V next() {
			return (V) memVals[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of values. The values order of this
	 * {@code Iterator} is random. 
	 */
	public Iterator<V> iteratorRandomValues() {
		return new MyRandomIteratorValues() ;
	}

	private class MyRandomIteratorEntries extends MyRandomIteratorAbstract<Entry<K, V>> {
		@SuppressWarnings("unchecked")
		public Entry<K, V> next() {
			return new MyIteratorEntry( memKeys[blk][blkI] , memVals[blk][blkI] )  ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
		
	/**
	 * Returns a {@code Iterator} of pair of keys and values ({@code Entry<K, V>}).
	 * The entries order of this {@code Iterator} is random. 
	 */
	public Iterator<Entry<K, V>> iteratorRandomEntries() {
		return new MyRandomIteratorEntries() ;
	}

	////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unchecked")
	public void walkTableGroupKeys(int groupIdx, Walker<K> walker) {
		int groupInit = groupIdx * GROUPS_ATTRS ;
		
		int groupIdxInit = groupInit ;
		
		int cursor = groupsMem[groupIdxInit] ;
		
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			
			cursor = memNext[blk][blkI] ;
			
			if ( !walker.step( (K) memKeys[blk][blkI] ) ) break ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void walkTableGroupValues(int groupIdx, Walker<V> walker) {
		int groupInit = groupIdx * GROUPS_ATTRS ;
		
		int groupIdxInit = groupInit ;
		
		int cursor = groupsMem[groupIdxInit] ;
		
		while (cursor >= 0) {
			int blk = cursor/memoryBlockSize ;
			int blkI = cursor-(blk*memoryBlockSize) ;
			
			cursor = memNext[blk][blkI] ;
			
			if ( !walker.step( (V) memVals[blk][blkI] ) ) break ;
		}
	}
	
	abstract private class MyTableGroupIteratorAbstract<T> implements Iterator<T> {
		int cursor ;
		
		int blk = 0 ;
		int blkI = -1 ;
		
		public MyTableGroupIteratorAbstract(int groupIdx) {
			int groupInit = groupIdx * GROUPS_ATTRS ;
			
			int groupIdxInit = groupInit ;
			
			this.cursor = groupsMem[groupIdxInit] ;
				
		}
		
		public boolean hasNext() {
			if (cursor < 0) return false ;
			
			blk = cursor/memoryBlockSize ;
			blkI = cursor-(blk*memoryBlockSize) ;
			
			cursor = memNext[blk][blkI] ;
			
			return true ;
		}
	}
	

	private class MyTableGroupIterator extends MyTableGroupIteratorAbstract<K> {
		
		public MyTableGroupIterator(int groupIdx) {
			super(groupIdx);
		}

		@SuppressWarnings("unchecked")
		public K next() {
			return (K) memKeys[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of keys. The keys order of this
	 * {@code Iterator} is the same order of insertion of the keys in this table group. 
	 */
	public Iterator<K> iteratorTableGroup(int groupIdx) {
		return new MyTableGroupIterator(groupIdx) ;
	}
	
	abstract private class MyTableGroupIteratorReverseAbstract<T> implements Iterator<T> {
		int[] groupPositions ;
		int cursor ;
		
		public MyTableGroupIteratorReverseAbstract(int groupIdx) {
			int groupInit = groupIdx * GROUPS_ATTRS ;
			
			int groupIdxInit = groupInit ;
			int groupIdxTotal = groupInit+2 ;
			
			int total = groupsMem[groupIdxTotal] ;
			
			int[] groupPositions = new int[total] ;
			int groupPositionsSz = 0 ;
			
			int cursor = groupsMem[groupIdxInit] ;
			
			while (cursor >= 0) {
				groupPositions[groupPositionsSz++] = cursor ;
				
				int blk = cursor/memoryBlockSize ;
				int blkI = cursor-(blk*memoryBlockSize) ;
				
				cursor = memNext[blk][blkI] ;
			}
			
			assert(groupPositionsSz == groupPositions.length) ;
			
			this.groupPositions = groupPositions ;
			this.cursor = groupPositionsSz ;
		}
		
		public boolean hasNext() {
			return --cursor >= 0 ;
		}
	}
	

	private class MyTableGroupIteratorReverse extends MyTableGroupIteratorReverseAbstract<K> {
		
		public MyTableGroupIteratorReverse(int groupIdx) {
			super(groupIdx);
		}

		@SuppressWarnings("unchecked")
		public K next() {
			int pos = groupPositions[this.cursor] ;
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;
			
			return (K) memKeys[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			int pos = groupPositions[this.cursor] ;
			int blk = pos/memoryBlockSize ;
			int blkI = pos-(blk*memoryBlockSize) ;
			
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of reversed keys. The keys order of this
	 * {@code Iterator} is the same order of insertion of the keys in this table group. 
	 */
	public Iterator<K> iteratorTableGroupReverse(int groupIdx) {
		return new MyTableGroupIteratorReverse(groupIdx) ;
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	abstract protected class MyIteratorAbstract<T> implements Iterator<T> {
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
	
	protected class MyIterator extends MyIteratorAbstract<K> {
		@SuppressWarnings("unchecked")
		public K next() {
			return (K) memKeys[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of keys. The keys order of this
	 * {@code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<K> iterator() {
		return new MyIterator() ;
	}
	

	protected class MyIteratorValues extends MyIteratorAbstract<V> {
		@SuppressWarnings("unchecked")
		public V next() {
			return (V) memVals[blk][blkI] ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
		}
	}
	
	/**
	 * Returns a {@code Iterator} of values. The values order of this
	 * {@code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<V> iteratorValues() {
		return new MyIteratorValues() ;
	}

	protected class MyIteratorEntries extends MyIteratorAbstract<Entry<K, V>> {
		@SuppressWarnings("unchecked")
		public Entry<K, V> next() {
			return new MyIteratorEntry( memKeys[blk][blkI] , memVals[blk][blkI] )  ;
		}

		@SuppressWarnings("unchecked")
		public void remove() {
			K key = (K) memKeys[blk][blkI] ;
			if (key != null) BigHashMap.this.removeNoRehash(key) ;
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
	 * Returns a {@code Iterator} of pair of keys and values ({@code Entry<K, V>}).
	 * The entries order of this {@code Iterator} is the same order of insertion of the keys. 
	 */
	public Iterator<Entry<K, V>> iteratorEntries() {
		return new MyIteratorEntries() ;
	}

	public Set<K> keySet() {
		return new KeySet();
	}

	private class KeySet extends AbstractSet<K> {
		public Iterator<K> iterator() {
			return BigHashMap.this.iterator() ;
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return BigHashMap.this.removeNoRehash(o) != null;
		}

		public void clear() {
			BigHashMap.this.clear();
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new EntrySet() ;
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		public Iterator<Map.Entry<K, V>> iterator() {
			return iteratorEntries() ;
		}

		public boolean add(Map.Entry<K, V> o) {
			return super.add(o);
		}

		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			
			Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
			Object key = entry.getKey();
			
			return containsKey(key) ;
		}

		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			
			Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
			Object key = entry.getKey();
			
			return BigHashMap.this.removeNoRehash(key) != null ;
		}

		public int size() {
			return size;
		}

		public void clear() {
			BigHashMap.this.clear();
		}
	}
	
	public Collection<V> values() {
		return new ValueCollection();
	}

	private class ValueCollection extends AbstractCollection<V> {
		public Iterator<V> iterator() {
			return BigHashMap.this.iteratorValues();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}

		public void clear() {
			BigHashMap.this.clear();
		}
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0 ;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		 for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
	            put(e.getKey(), e.getValue());		
		 }
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(groupsMem);
		result = prime * result + groupsTotal;
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		@SuppressWarnings("rawtypes")
		BigHashMap other = (BigHashMap) obj;
		
		if (size != other.size) return false;
		
		if (!Arrays.equals(groupsMem, other.groupsMem)) return false;
		if (groupsTotal != other.groupsTotal) return false;
		
		if (memSize != other.memSize) return false;
		
		if (!Arrays.deepEquals(memHash, other.memHash)) return false;
		if (!Arrays.deepEquals(memKeys, other.memKeys)) return false;
		if (!Arrays.deepEquals(memVals, other.memVals)) return false;
		
		return true;
	}
	
	
	
}

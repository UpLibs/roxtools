package roxtools;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

@SuppressWarnings("unchecked")
final public class BigLinkedIntListPool {
	
	private static final Logger LOG = getLogger(BigLinkedIntListPool.class);
	
	final protected int blockSize ;
	
	private int poolCapacity ;
	
	private int[][] links ;
	private int[][] linksReversed ;
	private int[][] data ;
	
	public BigLinkedIntListPool() {
		this(1, 1024*64) ;
	}
	
	public BigLinkedIntListPool(int initalBlocks, int blockSize) {
		this.blockSize = blockSize ;
		
		this.links = new int[initalBlocks][blockSize] ;
		this.linksReversed = new int[initalBlocks][blockSize] ;
		
		int[][] data = new int[initalBlocks][blockSize] ;
		
		poolCapacity = (initalBlocks * blockSize) -1 ;
		
		this.data = data ;
	}
	
	public int capacity() {
		return poolCapacity ;
	}
	
	public int size() {
		return poolSize ;
	}
	
	private void addBlock() {
		
		synchronized (freeIndex_MUTEX) {
			int prevSize = links.length ;
			int newSize = prevSize+1 ;
			
			int[][] links2 = new int[newSize][] ;
			int[][] linksReversed2 = new int[newSize][] ;
			int[][] data2 = new int[newSize][] ;
			
			System.arraycopy(links, 0, links2, 0, prevSize);
			System.arraycopy(linksReversed, 0, linksReversed2, 0, prevSize);
			System.arraycopy(data, 0, data2, 0, prevSize);
			
			links2[prevSize] = new int[blockSize] ;
			linksReversed2[prevSize] = new int[blockSize] ;
			data2[prevSize] = new int[blockSize] ;
			
			this.links = links2 ;
			this.linksReversed = linksReversed2 ;
			this.data = data2 ;
			
			poolCapacity += blockSize ;
		}
		
		LOG.debug("ADDED BLOCK> size/capacity: {} / {} ; memory: {}KB", this.poolSize , this.poolCapacity , (getUsedMemory()/1024) );
		
	}
	
	final private Mutex freeIndex_MUTEX = new Mutex() ;
	
	private int poolSize = 0 ;
	private int freeIndex = 1 ;
	
	protected int nextFreeIndex() {
		
		synchronized (freeIndex_MUTEX) {
			if (poolSize == poolCapacity) {
				addBlock();
			}
		
			int freeIndex = this.freeIndex ;
			int nextFreeIndex = getLink(freeIndex) ;
			
			if (nextFreeIndex == 0) {
				this.freeIndex = freeIndex +1 ;	
			}
			else {
				releasedIndexesSize-- ;
				this.freeIndex = nextFreeIndex ;
			}
			
			poolSize++ ;
			
			return freeIndex ;
		}
	}
	
	private int releaseIndexCount = 0 ;
	private int releasedIndexesSize = 0 ;
	
	protected void releaseIndex(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		
		synchronized (freeIndex_MUTEX) {
			this.links[blockIdx][innerIdx] = this.freeIndex ;
			this.freeIndex = idx ;
			
			releaseIndexCount++ ;
			releasedIndexesSize++ ;
			
			this.poolSize-- ;
		}
	}
	
	protected void setData(int idx , int elem) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		this.data[blockIdx][innerIdx] = elem ;
	}
	
	protected int getData(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		return this.data[blockIdx][innerIdx] ;
	}
	
	protected void setLink(int idx , int link) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		this.links[blockIdx][innerIdx] = link ;
		
		blockIdx = link / blockSize ;
		innerIdx = link - (blockIdx*blockSize) ;
		
		this.linksReversed[blockIdx][innerIdx] = idx ;
	}
	
	protected int getLink(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		return this.links[blockIdx][innerIdx] ;
	}
	
	protected int getLinkReversed(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		return this.linksReversed[blockIdx][innerIdx] ;
	}
	
	public BigLinkedIntList createLinkedList() {
		clearUnreferencedLists() ;
		
		BigLinkedIntList linkedList = createLinkedListInstace() ;
		return linkedList ;
	}
	
	public BigLinkedIntList createLinkedListUnreferenced() {
		BigLinkedIntList linkedList = createLinkedListInstaceUnreferenced() ;
		return linkedList ;
	}
	
	protected BigLinkedIntList createLinkedListInstace() {
		BigLinkedIntList linkedList = new BigLinkedIntListReferenced(this) ;
		return linkedList ;
	}

	protected BigLinkedIntList createLinkedListInstaceUnreferenced() {
		BigLinkedIntList linkedList = new BigLinkedIntListUnreferenced(this) ;
		return linkedList ;
	}
	
	
	final private ReferenceQueue<BigLinkedIntList> referenceQueue = new ReferenceQueue<BigLinkedIntList>() ;
	
	private ReferenceQueue<BigLinkedIntList> getReferenceQueue() {
		return referenceQueue;
	}
	
	static private class ReferencesTable {
		private ArrayList<BigLinkedIntListReference>[] table ;
		private int size ;
		private int thresholdMin ;
		private int thresholdMax ;
		
		public ReferencesTable(int groupsSize) {
			this.table = createTable(groupsSize) ;
			this.size = 0 ;
			updateThreshold();
			
		}
		
		private ArrayList<BigLinkedIntListReference>[] createTable(int groupsSize) {
			ArrayList<BigLinkedIntListReference>[] referencesTable = new ArrayList[groupsSize] ;
			
			for (int i = 0; i < referencesTable.length; i++) {
				referencesTable[i] = new ArrayList<BigLinkedIntListPool.BigLinkedIntListReference>() ;
			}
			
			return referencesTable ;
		}
		
		private void rebuildTable(int groupsSize) {
			if (groupsSize < 8) {
				groupsSize = 8 ;
			}
			
			synchronized (this) {
				if (groupsSize == table.length) return ;
				
				LOG.debug("Rebuilding ReferenceTable. From size {} to {}.", groupsSize , table.length);
				
				ArrayList<BigLinkedIntListReference>[] referencesTable2 = createTable(groupsSize) ;
			
				for (int i = 0; i < table.length; i++) {
					ArrayList<BigLinkedIntListReference> prevGroup = table[i] ;
					
					for (BigLinkedIntListReference ref : prevGroup) {
						int idx2 = calcTableGroupIndex(ref, referencesTable2.length) ;
						referencesTable2[idx2].add(ref) ;
					}
				}
				
				this.table = referencesTable2 ;	
				updateThreshold();
			}
			
		}

		private int calcTableGroupIndex(BigLinkedIntListReference ref, int length) {
			int idx = ref.hashCode() & (length-1);
			return idx;
		}
		
		private void updateThreshold() {
			synchronized (this) {
				this.thresholdMax = calcTableThreshold(this.table.length) ;
				this.thresholdMin = calcTableThreshold( (int)(this.table.length * 0.40) ) ;
			}
		}
		
		private int calcTableThreshold(int size) {
			int threshold = size * 100 ;
			if (threshold < 100) threshold = 100;
			return threshold ;
		}
		
		private ArrayList<BigLinkedIntListReference> getTableGroup(BigLinkedIntListReference ref) {
			synchronized (this) {
				int idx = calcTableGroupIndex(ref, table.length);
				return table[idx] ;	
			}
		}
		
		public int getListsSize() {
			synchronized (this) {
				int total = 0 ;
				
				for (ArrayList<BigLinkedIntListReference> refs : table) {
					synchronized (refs) {
						total += refs.size() ;
					}	
				}
			
				assert(total == size) ;
				
				return total ;
			}
		}
		
		public void registerReference(BigLinkedIntListReference ref) {
			synchronized (this) {
				if ( this.size >= this.thresholdMax ) {
					rebuildTable( this.table.length * 2 ) ;
				}
				
				ArrayList<BigLinkedIntListReference> group = getTableGroup(ref) ;
				group.add(ref) ;	
				size++ ;
			}
		}
		
		public void unregisterReference(BigLinkedIntListReference ref) {
			synchronized (this) {
				if ( this.size <= this.thresholdMin ) {
					rebuildTable( this.table.length/2 ) ;
				}
				
				ArrayList<BigLinkedIntListReference> group = getTableGroup(ref) ;
				group.remove(ref) ;
				size-- ;
			}
		}
		
	}
	
	private ReferencesTable referencesTable = new ReferencesTable(8) ;
		
	public int getReferencedListsSize() {
		return referencesTable.getListsSize() ;
	}
	
	private void registerReference(BigLinkedIntListReference ref) {
		referencesTable.registerReference(ref);
	}
	
	private void unregisterReference(BigLinkedIntListReference ref) {
		referencesTable.unregisterReference(ref);
	}
	
	public int clearUnreferencedLists() {
		
		int clearCount = 0 ;
		
		while ( true ) {
			
			BigLinkedIntListReference ref = (BigLinkedIntListReference) referenceQueue.poll();
			
			if (ref == null) break ;
			
			ref.clear();
			
			unregisterReference(ref);
			
			clearCount++ ;
		}
		
		return clearCount ;
	}

	////////////////////////////////////////////////
	
	static public interface BigLinkedIntList extends Iterable<Integer> {
		
		public BigLinkedIntListPool getPool() ;

		public Object getMUTEX() ;

		public int size() ;

		public boolean isEmpty() ;

		public void add(int elem) ;

		public void addAll(int[] elems) ;

		public void addAll(int[] elems, int off, int length) ;

		public void addAll(List<Integer> elems) ;

		public void addAll(List<Integer> elems, int off, int length) ;

		public void addAll(Iterable<Integer> elems) ;

		public Integer removeFirst() ;

		public Integer removeLast() ;

		public Integer remove(int idx) ;

		public void clear() ;

		public Integer getFirst() ;

		public Integer getLast() ;

		public Integer get(int idx) ;

		public Integer getFromHead(int idx) ;

		public Integer getFromTail(int idx) ;

		public void setAll(List<Integer> elems) ;

		public void setAll(Integer... elems) ;

		public void setAll(int... elems) ;

		public Integer set(int idx, Integer elem) ;

		public Integer setFromHead(int idx, Integer elem) ;

		public Integer setFromTail(int idx, Integer elem) ;

		public List<Integer> toList() ;

		public Integer[] toArray() ;

		public int[] toIntArray() ;

		public void copyIntoArray(Integer[] a, int off) ;

		public void copyIntoArray(Integer[] a, int off, int length) ;

		public void copyIntoArray(int[] a, int off) ;

		public void copyIntoArray(int[] a, int off, int length) ;

		public Iterator<Integer> iterator() ;

		public String toString() ;
	}
	
	static public class BigLinkedIntListReferenced implements BigLinkedIntList {
		final private BigLinkedIntListReference ref ;
		
		public BigLinkedIntListReferenced(BigLinkedIntListPool pool) {
			ref = new BigLinkedIntListReference(pool, this) ;
			pool.registerReference(ref);
		}
		
		public BigLinkedIntListPool getPool() {
			return ref.getPool();
		}

		public Object getMUTEX() {
			return ref.getMUTEX();
		}

		public int size() {
			return ref.size();
		}

		public boolean isEmpty() {
			return ref.isEmpty();
		}

		public void add(int elem) {
			ref.add(elem);
		}

		public void addAll(int[] elems) {
			ref.addAll(elems);
		}

		public void addAll(int[] elems, int off, int length) {
			ref.addAll(elems, off, length);
		}

		public void addAll(List<Integer> elems) {
			ref.addAll(elems);
		}

		public void addAll(List<Integer> elems, int off, int length) {
			ref.addAll(elems, off, length);
		}

		public void addAll(Iterable<Integer> elems) {
			ref.addAll(elems);
		}

		public Integer removeFirst() {
			return ref.removeFirst();
		}

		public Integer removeLast() {
			return ref.removeLast();
		}

		public Integer remove(int idx) {
			return ref.remove(idx);
		}

		public void clear() {
			ref.clear();
		}

		public Integer getFirst() {
			return ref.getFirst();
		}

		public Integer getLast() {
			return ref.getLast();
		}

		public Integer get(int idx) {
			return ref.get(idx);
		}

		public Integer getFromHead(int idx) {
			return ref.getFromHead(idx);
		}

		public Integer getFromTail(int idx) {
			return ref.getFromTail(idx);
		}

		public void setAll(List<Integer> elems) {
			ref.setAll(elems);
		}

		public void setAll(Integer... elems) {
			ref.setAll(elems);
		}

		public void setAll(int... elems) {
			ref.setAll(elems);
		}

		public Integer set(int idx, Integer elem) {
			return ref.set(idx, elem);
		}

		public Integer setFromHead(int idx, Integer elem) {
			return ref.setFromHead(idx, elem);
		}

		public Integer setFromTail(int idx, Integer elem) {
			return ref.setFromTail(idx, elem);
		}

		public List<Integer> toList() {
			return ref.toList();
		}

		public Integer[] toArray() {
			return ref.toArray();
		}

		public int[] toIntArray() {
			return ref.toIntArray();
		}

		public void copyIntoArray(Integer[] a, int off) {
			ref.copyIntoArray(a, off);
		}

		public void copyIntoArray(Integer[] a, int off, int length) {
			ref.copyIntoArray(a, off, length);
		}

		public void copyIntoArray(int[] a, int off) {
			ref.copyIntoArray(a, off);
		}

		public void copyIntoArray(int[] a, int off, int length) {
			ref.copyIntoArray(a, off, length);
		}

		public Iterator<Integer> iterator() {
			return ref.iterator();
		}

		public String toString() {
			return ref.toString();
		}
		
	}
	
	final static private class BigLinkedIntListReference extends WeakReference<BigLinkedIntList> implements BigLinkedIntList {
		final private BigLinkedIntListPool pool ;
		private int headLinkIdx ;
		private int tailLinkIdx ;
		private int size ;

		public BigLinkedIntListReference(BigLinkedIntListPool pool) {
			super(null) ;
			
			this.pool = pool ;
			this.headLinkIdx = this.tailLinkIdx = 0 ;
			this.size = 0 ;
		}
		
		public BigLinkedIntListReference(BigLinkedIntListPool pool, BigLinkedIntList list) {
			super(list, pool.getReferenceQueue()) ;
			
			this.pool = pool ;
			this.headLinkIdx = this.tailLinkIdx = 0 ;
			this.size = 0 ;
		}
		
		public BigLinkedIntListPool getPool() {
			return pool;
		}
		
		public Object getMUTEX() {
			return pool;
		}
		
		public int size() {
			return size ;
		}
		
		public boolean isEmpty() {
			return size == 0 ;
		}
		
		public void add(int elem) {
			int idx = pool.nextFreeIndex() ;
			
			if (this.size == 0) {
				this.headLinkIdx = this.tailLinkIdx = idx ;
			}
			else {
				pool.setLink(this.tailLinkIdx, idx);
				this.tailLinkIdx = idx ;
			}
			
			pool.setData(tailLinkIdx, elem);
			
			this.size++ ;
		}
		
		private void addSlot() {
			int idx = pool.nextFreeIndex() ;
			
			if (this.size == 0) {
				this.headLinkIdx = this.tailLinkIdx = idx ;
			}
			else {
				pool.setLink(this.tailLinkIdx, idx);
				this.tailLinkIdx = idx ;
			}
			
			this.size++ ;
		}
		
		public void addAll(int[] elems) {
			addAll(elems, 0, elems.length);
		}
		
		public void addAll(int[] elems, int off, int length) {
			int limit = off+length ;
			
			for (int i = off; i < limit; i++) {
				int elem = elems[i] ;
				add(elem);
			}
		}
		
		public void addAll(List<Integer> elems) {
			addAll(elems, 0 , elems.size());
		}
		
		public void addAll(List<Integer> elems, int off, int length) {
			int limit = off+length ;
			
			for (int i = off; i < limit; i++) {
				Integer elem = elems.get(i) ;
				add(elem);
			}
		}
		
		public void addAll(Iterable<Integer> elems) {
			for (Integer e : elems) {
				add(e);
			}
		}
		
		public Integer removeFirst() {
			if (this.size == 0) return null ;
			
			int prev = pool.getData(headLinkIdx);
			
			int nextLink = pool.getLink(headLinkIdx) ;
			
			pool.releaseIndex(headLinkIdx);
			
			this.headLinkIdx = nextLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public Integer removeLast() {
			if (this.size == 0) return null ;
			
			int prev = pool.getData(tailLinkIdx);
			
			int prevLink = pool.getLinkReversed(tailLinkIdx) ;
			
			pool.releaseIndex(tailLinkIdx);
			
			this.tailLinkIdx = prevLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public Integer remove(int idx) {
			if (idx >= size) return null ;
			
			if (idx == 0) {
				return removeFirst() ;
			}
			else if (idx == size-1) {
				return removeLast() ;
			}
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			int prevCursor = cursor ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				prevCursor = cursor ;
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			int linkNext = pool.links[blockIdx][innerIdx] ;
			
			pool.setLink(prevCursor, linkNext);
			
			pool.releaseIndex(cursor);
			
			this.size-- ;
			
			return prevData ;
		}
		
		public void clear() {
			while (size > 0) {
				pool.setData(tailLinkIdx, 0);
				
				int prevLink = pool.getLinkReversed(tailLinkIdx) ;
				
				pool.releaseIndex(tailLinkIdx);
				
				this.tailLinkIdx = prevLink ;
				
				this.size-- ;
			}
		}

		public Integer getFirst() {
			if (size == 0) return null ;
			return pool.getData(headLinkIdx) ;
		}
		
		public Integer getLast() {
			if (size == 0) return null ;
			return pool.getData(tailLinkIdx) ;
		}
		
		public Integer get(int idx) {
			if ( idx < (size >>> 1) ) {
				return getFromHead(idx) ;
			}
			else {
				return getFromTail(idx) ;
			}
		}
		
		public Integer getFromHead(int idx) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			return pool.data[blockIdx][innerIdx] ;
		}
		
		public Integer getFromTail(int idx) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] linksReversed = pool.linksReversed;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.tailLinkIdx ;
			
			for (int i = (size-idx)-2 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				
				cursor = linksReversed[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			return pool.data[blockIdx][innerIdx] ;
		}
		
		public void setAll(List<Integer> elems) {
			int elemsSz = elems.size() ;
			if ( size > elemsSz ) {
				do {
					removeLast() ;
				}
				while ( size > elemsSz ) ;
			}
			else if ( size < elemsSz ) {
				do {
					addSlot();
				}
				while ( size < elemsSz ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems.get(setSz) ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		public void setAll(Integer... elems) {
			
			if ( size > elems.length ) {
				do {
					removeLast() ;
				}
				while ( size > elems.length ) ;
			}
			else if ( size < elems.length ) {
				do {
					addSlot();
				}
				while ( size < elems.length ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems[setSz] ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		public void setAll(int... elems) {
			
			if ( size > elems.length ) {
				do {
					removeLast() ;
				}
				while ( size > elems.length ) ;
			}
			else if ( size < elems.length ) {
				do {
					addSlot();
				}
				while ( size < elems.length ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems[setSz] ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		
		public Integer set(int idx, Integer elem) {
			if (idx == size) {
				add(elem);
				return null ;
			}
			else if ( idx < (size >>> 1) ) {
				return setFromHead(idx, elem) ;
			}
			else {
				return setFromTail(idx, elem) ;
			}
		}
		
		public Integer setFromHead(int idx, Integer elem) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			pool.data[blockIdx][innerIdx] = elem ;
			
			return prevData ;
		}
		
		public Integer setFromTail(int idx, Integer elem) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] linksReversed = pool.linksReversed;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.tailLinkIdx ;
			
			for (int i = (size-idx)-2 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				
				cursor = linksReversed[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			pool.data[blockIdx][innerIdx] = elem ;
			
			return prevData ;
		}
		
		@SuppressWarnings("unused")
		protected int[] getLinks() {
			int[] links = new int[size] ;
			int linksSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( linksSz < size ) {
				links[linksSz++] = cursor ;
				cursor = pool.getLink(cursor);
			}
			
			return links ;
		}
		
		public List<Integer> toList() {
			ArrayList<Integer> list = new ArrayList<Integer>(size) ;
			Collections.addAll(list, toArray()) ;
			return list ;
		}
		
		public Integer[] toArray() {
			Integer[] a = new Integer[size] ;
			int aSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( aSz < size ) {
				a[aSz++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
			
			return a ;
		}
		
		public int[] toIntArray() {
			int[] a = new int[size] ;
			int aSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( aSz < size ) {
				a[aSz++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
			
			return a ;
		}
		
		public void copyIntoArray(Integer[] a, int off) {
			copyIntoArray(a, off, size-off);
		}
		
		public void copyIntoArray(Integer[] a, int off, int length) {
			int cursor = this.headLinkIdx ;
			
			int copy = 0 ;
			while ( copy < length ) {
				a[off++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
		}
		
		public void copyIntoArray(int[] a, int off) {
			copyIntoArray(a, off, size-off);
		}
		
		public void copyIntoArray(int[] a, int off, int length) {
			int cursor = this.headLinkIdx ;
			
			int copy = 0 ;
			while ( copy < length ) {
				a[off++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
		}
		
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {

				int consumeCount = 0 ;
				int cursor = headLinkIdx ;
				
				@Override
				public boolean hasNext() {
					return consumeCount < size ;
				}

				@Override
				public Integer next() {
					int data = pool.getData(cursor) ;
					int next = pool.getLink(cursor) ;
					cursor = next ;
					consumeCount++ ;
					return data;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException() ;
				}
			};
		}
		
		@Override
		public String toString() {
			return "[ head:"+ headLinkIdx +" ... tail:"+ tailLinkIdx +" ; size: "+ size +"]" ;
		}
	}
	

	final static private class BigLinkedIntListUnreferenced implements BigLinkedIntList {
		final private BigLinkedIntListPool pool ;
		private int headLinkIdx ;
		private int tailLinkIdx ;
		private int size ;

		public BigLinkedIntListUnreferenced(BigLinkedIntListPool pool) {
			this.pool = pool ;
			this.headLinkIdx = this.tailLinkIdx = 0 ;
			this.size = 0 ;
		}
		
		public BigLinkedIntListPool getPool() {
			return pool;
		}
		
		public Object getMUTEX() {
			return pool;
		}
		
		public int size() {
			return size ;
		}
		
		public boolean isEmpty() {
			return size == 0 ;
		}
		
		public void add(int elem) {
			int idx = pool.nextFreeIndex() ;
			
			if (this.size == 0) {
				this.headLinkIdx = this.tailLinkIdx = idx ;
			}
			else {
				pool.setLink(this.tailLinkIdx, idx);
				this.tailLinkIdx = idx ;
			}
			
			pool.setData(tailLinkIdx, elem);
			
			this.size++ ;
		}
		
		private void addSlot() {
			int idx = pool.nextFreeIndex() ;
			
			if (this.size == 0) {
				this.headLinkIdx = this.tailLinkIdx = idx ;
			}
			else {
				pool.setLink(this.tailLinkIdx, idx);
				this.tailLinkIdx = idx ;
			}
			
			this.size++ ;
		}
		
		public void addAll(int[] elems) {
			addAll(elems, 0, elems.length);
		}
		
		public void addAll(int[] elems, int off, int length) {
			int limit = off+length ;
			
			for (int i = off; i < limit; i++) {
				int elem = elems[i] ;
				add(elem);
			}
		}
		
		public void addAll(List<Integer> elems) {
			addAll(elems, 0 , elems.size());
		}
		
		public void addAll(List<Integer> elems, int off, int length) {
			int limit = off+length ;
			
			for (int i = off; i < limit; i++) {
				Integer elem = elems.get(i) ;
				add(elem);
			}
		}
		
		public void addAll(Iterable<Integer> elems) {
			for (Integer e : elems) {
				add(e);
			}
		}
		
		public Integer removeFirst() {
			if (this.size == 0) return null ;
			
			int prev = pool.getData(headLinkIdx);
			
			int nextLink = pool.getLink(headLinkIdx) ;
			
			pool.releaseIndex(headLinkIdx);
			
			this.headLinkIdx = nextLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public Integer removeLast() {
			if (this.size == 0) return null ;
			
			int prev = pool.getData(tailLinkIdx);
			
			int prevLink = pool.getLinkReversed(tailLinkIdx) ;
			
			pool.releaseIndex(tailLinkIdx);
			
			this.tailLinkIdx = prevLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public Integer remove(int idx) {
			if (idx >= size) return null ;
			
			if (idx == 0) {
				return removeFirst() ;
			}
			else if (idx == size-1) {
				return removeLast() ;
			}
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			int prevCursor = cursor ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				prevCursor = cursor ;
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			int linkNext = pool.links[blockIdx][innerIdx] ;
			
			pool.setLink(prevCursor, linkNext);
			
			pool.releaseIndex(cursor);
			
			this.size-- ;
			
			return prevData ;
		}
		
		public void clear() {
			while (size > 0) {
				pool.setData(tailLinkIdx, 0);
				
				int prevLink = pool.getLinkReversed(tailLinkIdx) ;
				
				pool.releaseIndex(tailLinkIdx);
				
				this.tailLinkIdx = prevLink ;
				
				this.size-- ;
			}
		}

		public Integer getFirst() {
			if (size == 0) return null ;
			return pool.getData(headLinkIdx) ;
		}
		
		public Integer getLast() {
			if (size == 0) return null ;
			return pool.getData(tailLinkIdx) ;
		}
		
		public Integer get(int idx) {
			if ( idx < (size >>> 1) ) {
				return getFromHead(idx) ;
			}
			else {
				return getFromTail(idx) ;
			}
		}
		
		public Integer getFromHead(int idx) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			return pool.data[blockIdx][innerIdx] ;
		}
		
		public Integer getFromTail(int idx) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] linksReversed = pool.linksReversed;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.tailLinkIdx ;
			
			for (int i = (size-idx)-2 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				
				cursor = linksReversed[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			return pool.data[blockIdx][innerIdx] ;
		}
		
		public void setAll(List<Integer> elems) {
			int elemsSz = elems.size() ;
			if ( size > elemsSz ) {
				do {
					removeLast() ;
				}
				while ( size > elemsSz ) ;
			}
			else if ( size < elemsSz ) {
				do {
					addSlot();
				}
				while ( size < elemsSz ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems.get(setSz) ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		public void setAll(Integer... elems) {
			
			if ( size > elems.length ) {
				do {
					removeLast() ;
				}
				while ( size > elems.length ) ;
			}
			else if ( size < elems.length ) {
				do {
					addSlot();
				}
				while ( size < elems.length ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems[setSz] ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		public void setAll(int... elems) {
			
			if ( size > elems.length ) {
				do {
					removeLast() ;
				}
				while ( size > elems.length ) ;
			}
			else if ( size < elems.length ) {
				do {
					addSlot();
				}
				while ( size < elems.length ) ;
			}
			
			int blockSize = pool.blockSize ;
			
			int cursor = this.headLinkIdx ;
			int setSz = 0 ;
			
			int blockIdx ;
			int innerIdx ;
			
			while ( setSz < size ) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor - (blockIdx*blockSize) ;
				
				pool.data[blockIdx][innerIdx] = elems[setSz] ;
				cursor = pool.links[blockIdx][innerIdx] ;
				
				setSz++ ;
			}
			
		}
		
		
		public Integer set(int idx, Integer elem) {
			if (idx == size) {
				add(elem);
				return null ;
			}
			else if ( idx < (size >>> 1) ) {
				return setFromHead(idx, elem) ;
			}
			else {
				return setFromTail(idx, elem) ;
			}
		}
		
		public Integer setFromHead(int idx, Integer elem) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] links = pool.links ;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.headLinkIdx ;
			
			for (int i = idx-1 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				cursor = links[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			pool.data[blockIdx][innerIdx] = elem ;
			
			return prevData ;
		}
		
		public Integer setFromTail(int idx, Integer elem) {
			if (idx >= size) return null ;
			
			int blockSize = pool.blockSize ;
			int[][] linksReversed = pool.linksReversed;
			
			int blockIdx ;
			int innerIdx ;
			
			int cursor = this.tailLinkIdx ;
			
			for (int i = (size-idx)-2 ; i >= 0 ; i--) {
				blockIdx = cursor / blockSize ;
				innerIdx = cursor % blockSize ;
				
				
				cursor = linksReversed[blockIdx][innerIdx] ;
			}
			
			blockIdx = cursor / blockSize ;
			innerIdx = cursor % blockSize ;
			
			int prevData = pool.data[blockIdx][innerIdx] ;
			
			pool.data[blockIdx][innerIdx] = elem ;
			
			return prevData ;
		}
		
		@SuppressWarnings("unused")
		protected int[] getLinks() {
			int[] links = new int[size] ;
			int linksSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( linksSz < size ) {
				links[linksSz++] = cursor ;
				cursor = pool.getLink(cursor);
			}
			
			return links ;
		}
		
		public List<Integer> toList() {
			ArrayList<Integer> list = new ArrayList<Integer>(size) ;
			Collections.addAll(list, toArray()) ;
			return list ;
		}
		
		public Integer[] toArray() {
			Integer[] a = new Integer[size] ;
			int aSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( aSz < size ) {
				a[aSz++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
			
			return a ;
		}
		
		public int[] toIntArray() {
			int[] a = new int[size] ;
			int aSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( aSz < size ) {
				a[aSz++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
			
			return a ;
		}
		
		public void copyIntoArray(Integer[] a, int off) {
			copyIntoArray(a, off, size-off);
		}
		
		public void copyIntoArray(Integer[] a, int off, int length) {
			int cursor = this.headLinkIdx ;
			
			int copy = 0 ;
			while ( copy < length ) {
				a[off++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
		}
		
		public void copyIntoArray(int[] a, int off) {
			copyIntoArray(a, off, size-off);
		}
		
		public void copyIntoArray(int[] a, int off, int length) {
			int cursor = this.headLinkIdx ;
			
			int copy = 0 ;
			while ( copy < length ) {
				a[off++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
		}
		
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {

				int consumeCount = 0 ;
				int cursor = headLinkIdx ;
				
				@Override
				public boolean hasNext() {
					return consumeCount < size ;
				}

				@Override
				public Integer next() {
					int data = pool.getData(cursor) ;
					int next = pool.getLink(cursor) ;
					cursor = next ;
					consumeCount++ ;
					return data;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException() ;
				}
			};
		}
		
		@Override
		public String toString() {
			return "[ head:"+ headLinkIdx +" ... tail:"+ tailLinkIdx +" ; size: "+ size +"]" ;
		}
	}
	
	public long getUsedMemory() {
		long total = 0 ;
		
		total += this.links.length * (blockSize * 4L) ;
		total += this.linksReversed.length * (blockSize * 4L) ;
		total += this.data.length * (blockSize * 8L) ;
		
		return total ;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() +"[size: "+ poolSize +" ; capacity: "+ poolCapacity +" ; releaseIndexCount: "+ releasedIndexesSize +" / "+ releaseIndexCount+" ; memory: "+ (getUsedMemory()/1024) +"KB]";
	}

}

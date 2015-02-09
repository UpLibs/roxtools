package roxtools;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

final public class BigLinkedListPool<E> {
	final protected Class<E> type ;
	final protected Class<E[]> typeMulti ;
	final protected int blockSize ;
	
	private int poolCapacity ;
	
	private int[][] links ;
	private int[][] linksReversed ;
	private E[][] data ;
	
	public BigLinkedListPool(Class<E> type) {
		this(type, 1, 1024*64) ;
	}
	
	@SuppressWarnings("unchecked")
	public BigLinkedListPool(Class<E> type, int initalBlocks, int blockSize) {
		this.type = type ;
		this.blockSize = blockSize ;
		
		this.links = new int[initalBlocks][blockSize] ;
		this.linksReversed = new int[initalBlocks][blockSize] ;
		
		this.typeMulti = (Class<E[]>) Array.newInstance(type, 1).getClass() ;
		
		E[][] data = (E[][]) Array.newInstance(typeMulti, initalBlocks) ;
		
		for (int i = 0; i < data.length; i++) {
			data[i] = (E[]) Array.newInstance(type, blockSize) ;
		}
		
		poolCapacity = (initalBlocks * blockSize) -1 ;
		
		this.data = data ;
	}
	
	public int capacity() {
		return poolCapacity ;
	}
	
	public int size() {
		return poolSize ;
	}
	
	@SuppressWarnings("unchecked")
	private void addBlock() {
		int prevSize = links.length ;
		int newSize = prevSize+1 ;
		
		int[][] links2 = new int[newSize][] ;
		int[][] linksReversed2 = new int[newSize][] ;
		E[][] data2 = (E[][]) Array.newInstance(typeMulti, newSize) ;
		
		System.arraycopy(links, 0, links2, 0, prevSize);
		System.arraycopy(linksReversed, 0, linksReversed2, 0, prevSize);
		System.arraycopy(data, 0, data2, 0, prevSize);
		
		links2[prevSize] = new int[blockSize] ;
		linksReversed2[prevSize] = new int[blockSize] ;
		data2[prevSize] = (E[]) Array.newInstance(type, blockSize) ;
		
		this.links = links2 ;
		this.linksReversed = linksReversed2 ;
		this.data = data2 ;
		
		poolCapacity += blockSize ;
	}
	
	private int poolSize = 0 ;
	private int freeIndex = 1 ;
	
	protected int nextFreeIndex() {
		
		if (poolSize == poolCapacity) {
			addBlock();
		}
		
		int freeIndex = this.freeIndex ;
		int nextFreeIndex = getLink(freeIndex) ;
		
		if (nextFreeIndex == 0) {
			this.freeIndex = freeIndex +1 ;	
		}
		else {
			this.freeIndex = nextFreeIndex ;
		}
		
		poolSize++ ;
		
		return freeIndex ;
	}
	
	protected void releaseIndex(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		
		this.links[blockIdx][innerIdx] = freeIndex ;
		this.freeIndex = idx ;
		
		this.poolSize-- ;
	}
	
	protected void setData(int idx , E elem) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - (blockIdx*blockSize) ;
		this.data[blockIdx][innerIdx] = elem ;
	}
	
	protected E getData(int idx) {
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
	
	public BigLinkedList<E> createLinkedList() {
		BigLinkedList<E> linkedList = createLinkedListInstace() ;
		return linkedList ;
	}
	
	protected BigLinkedList<E> createLinkedListInstace() {
		BigLinkedList<E> linkedList = new BigLinkedList<E>(this) ;
		return linkedList ;
	}

	////////////////////////////////////////////////
	
	static public class BigLinkedList<E> implements Iterable<E> {
		final private BigLinkedListPool<E> pool ;
		private int headLinkIdx ;
		private int tailLinkIdx ;
		private int size ;

		public BigLinkedList(BigLinkedListPool<E> pool) {
			this.pool = pool ;
			this.headLinkIdx = this.tailLinkIdx = 0 ;
			this.size = 0 ;
		}
		
		public int size() {
			return size ;
		}
		
		public boolean isEmpty() {
			return size == 0 ;
		}
		
		public void add(E elem) {
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
		
		public E removeFirst() {
			if (this.size == 0) return null ;
			
			E prev = pool.getData(headLinkIdx);
			pool.setData(headLinkIdx, null);
			
			int nextLink = pool.getLink(headLinkIdx) ;
			
			pool.releaseIndex(headLinkIdx);
			
			this.headLinkIdx = nextLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public E removeLast() {
			if (this.size == 0) return null ;
			
			E prev = pool.getData(tailLinkIdx);
			pool.setData(tailLinkIdx, null);
			
			int prevLink = pool.getLinkReversed(tailLinkIdx) ;
			
			pool.releaseIndex(tailLinkIdx);
			
			this.tailLinkIdx = prevLink ;
			
			this.size-- ;
			
			return prev ;
		}
		
		public E remove(int idx) {
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
			
			E prevData = pool.data[blockIdx][innerIdx] ;
			pool.data[blockIdx][innerIdx] = null ;
			
			int linkNext = pool.links[blockIdx][innerIdx] ;
			
			pool.setLink(prevCursor, linkNext);
			
			pool.releaseIndex(cursor);
			
			this.size-- ;
			
			return prevData ;
		}
		
		public void clear() {
			while (size > 0) {
				pool.setData(tailLinkIdx, null);
				
				int prevLink = pool.getLinkReversed(tailLinkIdx) ;
				
				pool.releaseIndex(tailLinkIdx);
				
				this.tailLinkIdx = prevLink ;
				
				this.size-- ;
			}
		}

		public E getFirst() {
			if (size == 0) return null ;
			return pool.getData(headLinkIdx) ;
		}
		
		public E getLast() {
			if (size == 0) return null ;
			return pool.getData(tailLinkIdx) ;
		}
		
		public E get(int idx) {
			if ( idx < (size >>> 1) ) {
				return getFromHead(idx) ;
			}
			else {
				return getFromTail(idx) ;
			}
		}
		
		public E getFromHead(int idx) {
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
		
		public E getFromTail(int idx) {
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
		
		@SuppressWarnings("unchecked")
		public E[] toArray() {
			E[] a = (E[]) Array.newInstance(pool.type, size) ;
			int aSz = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( aSz < size ) {
				a[aSz++] = pool.getData(cursor);
				cursor = pool.getLink(cursor);
			}
			
			return a ;
		}
		
		public Iterator<E> iterator() {
			return new Iterator<E>() {

				int cursor = headLinkIdx ;
				
				@Override
				public boolean hasNext() {
					return cursor != 0 ;
				}

				@Override
				public E next() {
					E data = pool.getData(cursor) ;
					int next = pool.getLink(cursor) ;
					cursor = next ;
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
		int total = 0 ;
		
		total += this.links.length * blockSize * 4 ;
		total += this.linksReversed.length * blockSize * 4 ;
		total += this.data.length * blockSize * 8 ;
		
		return total ;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() +"[size: "+ poolSize +" ; capacity: "+ poolCapacity +" ; memory: "+ (getUsedMemory()/1024) +"KB]";
	}

}

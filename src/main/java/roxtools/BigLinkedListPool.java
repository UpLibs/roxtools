package roxtools;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

final public class BigLinkedListPool<E> {
	final protected Class<E> type ;
	final protected Class<E[]> typeMulti ;
	final protected int blockSize ;
	
	private int capacity ;
	
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
		
		capacity = initalBlocks * blockSize ;
		
		this.data = data ;
	}
	
	public int capacity() {
		return capacity ;
	}
	
	public int size() {
		return size ;
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
		
		capacity += blockSize ;
	}
	
	private int size = 0 ;
	private int freeIndex = 1 ;
	
	protected int nextFreeIndex() {
		
		if (size == capacity) {
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
		
		size++ ;
		
		return freeIndex ;
	}
	
	protected void releaseIndex(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
		
		this.links[blockIdx][innerIdx] = freeIndex ;
		this.freeIndex = idx ;
	}
	
	protected void setData(int idx , E elem) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
		this.data[blockIdx][innerIdx] = elem ;
	}
	
	protected E getData(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
		return this.data[blockIdx][innerIdx] ;
	}
	
	protected void setLink(int idx , int link) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
		this.links[blockIdx][innerIdx] = link ;
		
		blockIdx = link / blockSize ;
		innerIdx = link - blockIdx ;
		
		this.linksReversed[blockIdx][innerIdx] = idx ;
	}
	
	protected int getLink(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
		return this.links[blockIdx][innerIdx] ;
	}
	
	protected int getLinkReversed(int idx) {
		int blockIdx = idx / blockSize ;
		int innerIdx = idx - blockIdx ;
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
	
	static public class BigLinkedList<E> {
		final private BigLinkedListPool<E> pool ;
		int headLinkIdx ;
		int tailLinkIdx ;
		int size ;

		public BigLinkedList(BigLinkedListPool<E> pool) {
			this.pool = pool ;
			this.headLinkIdx = this.tailLinkIdx = 0 ;
			this.size = 0 ;
		}
		
		public int size() {
			return size ;
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
		

		public void clear() {
			while (size > 0) {
				pool.setData(tailLinkIdx, null);
				
				int prevLink = pool.getLinkReversed(tailLinkIdx) ;
				
				pool.releaseIndex(tailLinkIdx);
				
				this.tailLinkIdx = prevLink ;
				
				this.size-- ;
			}
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
			int i = 0 ;
			
			int cursor = this.headLinkIdx ;
			
			while ( i < size ) {
				if (i == idx) return pool.getData(cursor) ;
				cursor = pool.getLink(cursor);
				i++;
			}
			
			return null ;
		}
		
		public E getFromTail(int idx) {
			int i = size-1 ;
			
			int cursor = this.tailLinkIdx ;
			
			while ( i >= 0 ) {
				if (i == idx) return pool.getData(cursor) ;
				cursor = pool.getLinkReversed(cursor);
				i--;
			}
			
			return null ;
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
			return "["+ headLinkIdx +" ; "+ size +"]"+ Arrays.toString(getLinks()) + Arrays.toString(toArray()) ;
		}
	}

	////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		BigLinkedListPool<String> bigLinkedListPool = new BigLinkedListPool<String>(String.class) ;

		System.out.println( bigLinkedListPool );
		
		BigLinkedList<String> linkedList = bigLinkedListPool.createLinkedList() ;
		
		System.out.println(linkedList);
		
		linkedList.add("a");
		linkedList.add("b");
		linkedList.add("c");
		linkedList.add("d");
		
		System.out.println(linkedList);
		
		System.out.println("----------------------------");
		
		System.out.println("rem> "+ linkedList.removeLast() );
		System.out.println("rem> "+ linkedList.removeLast() );
		System.out.println("rem> "+ linkedList.removeLast() );
		
		linkedList.add("x");
		linkedList.add("y");
		linkedList.add("z");
		
		System.out.println("rem> "+ linkedList.removeLast() );
		
		System.out.println(linkedList);
		
		System.out.println("rem> "+ linkedList.removeLast() );
		System.out.println("rem> "+ linkedList.removeLast() );
		
		System.out.println(linkedList);
		
		linkedList.add("x2");
		linkedList.add("y2");
		
		System.out.println(linkedList);
		
		linkedList.clear();
		
		System.out.println(linkedList);
		
		linkedList.add("z2");
		linkedList.add("a2");
		linkedList.add("b2");
		linkedList.add("c2");
		linkedList.add("d2");
		
		System.out.println(linkedList);
		
		System.out.println("--------------------");
		
		for (int i = 0; i < linkedList.size(); i++) {
			String v0 = linkedList.get(i) ;
			String v1 = linkedList.getFromHead(i) ;
			String v2 = linkedList.getFromTail(i) ;
			System.out.println(i+"> "+ v0 +" ; "+ v1 +" ; "+ v2);
		}
		
		System.out.println("--------------------");
		
		Iterator<String> iterator = linkedList.iterator() ;
		
		while ( iterator.hasNext() ) {
			String val = iterator.next() ;
			System.out.println("> "+ val);
		}
				
		System.out.println("--------------------");

		System.out.println(linkedList);
		
	}
}

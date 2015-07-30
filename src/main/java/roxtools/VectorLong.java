package roxtools;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

final public class VectorLong implements Iterable<Long> , Cloneable {
	
	public long[] elems ;
	public int count = 0 ;

	public VectorLong() {
		this(8) ;
	}

	/**
	 * PS: If {@code initialCapacity} &lt;= 0 it will be set to 1.
	 * @param initialCapacity Initial capacity of internal array.
	 */
	public VectorLong(int initialCapacity) {
		this.elems = new long[ Math.max(initialCapacity, 1) ] ;
	}
	
	public Object clone() {
		try {
			VectorLong v = (VectorLong) super.clone();
			v.elems = new long[count];
			System.arraycopy(elems, 0, v.elems, 0, count);
			return v;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	
	public int size() {
		return count ;
	}
	
	public void ensureCapacity(int capacity) {
		if (elems.length < capacity) {
			long[] elems2 = new long[ (capacity - (capacity%2)) * 2 ];
			System.arraycopy(elems, 0, elems2, 0, count);
			elems = elems2;
		}
	}
	
	public long set(int index, long element) {
		long prevElem = elems[index] ;
		elems[index] = element ;
		return prevElem ;
	}
	
	public void add(long elem) {
		ensureCapacity(count+1) ;
		elems[count++] = elem ;
	}
	
	public void add(int idx , long elem) {
		ensureCapacity(count+1) ;
		System.arraycopy(elems, idx, elems, idx + 1, count - idx);
		elems[idx] = elem ;
		++count ;
	}
	
	public void insertElementAt(long elem, int idx) {
		add(idx , elem) ;
	}
	
	public long get(int idx) {
		return elems[idx];
	}
	
	public long firstElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[0];
	}
	
	public long lastElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[count-1];
	}
	
    public int indexOf(long o) {
    	
    	for (int i = 0; i < count; i++) {
			if ( elems[i] == o ) return i ;
		}
    	
    	return -1;
    }
    
	public boolean removeAll(Collection<Long> c) {
		 boolean remove = false ;
		 
		 for (Long obj : c) {
			 remove |= remove(obj) ;
		}
		
		return remove ;
	}
	
	public boolean remove(long o) {
		int idx = indexOf(o) ;
		if (idx < 0) return false ;
		removeAtIndex(idx) ;
		return true ;
	}
	
	public void removeVoid(int idx) {
		int len = count - idx - 1;
		System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
	}
	
	public void removeRange(int idx, int lng) {
		if ( lng < 0 ) throw new IllegalArgumentException("Invalid range size: "+ lng) ;
		if ( idx+lng > count ) throw new ArrayIndexOutOfBoundsException(idx+"+"+lng) ;
		
		int len = count - idx - lng;
		System.arraycopy(elems, idx + lng, elems, idx, len);
		
		count -= lng ;
	}
	
	public long removeAtIndex(int idx) {
		long prevElem = elems[idx] ;

		int len = count - idx - 1;
		System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
		
		return prevElem ;
	}

	public void clear() {
		this.elems = new long[10];
		count = 0;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	static final private LongComparator longComparator = new LongComparator() ;
	static private class LongComparator {
		public int compare(long thisVal , long anotherVal) {
	    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	    }
	}
	
	static private class LongComparatorWrapper extends LongComparator {
		final Comparator<Long> comparator ;

		public LongComparatorWrapper(Comparator<Long> comparator) {
			this.comparator = comparator;
		}
		
		@Override
		public int compare(long thisVal, long anotherVal) {
			return this.comparator.compare(thisVal , anotherVal) ;
		}
		
	}
	
	public int getInsertSortedIndex(long elem, Comparator<Long> comparator) {
		int low = 0;
		int high = count-1;
		
		LongComparator longComp = comparator != null ? new LongComparatorWrapper(comparator) : longComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
	    	long midVal = elems[mid] ;
		    
		    int cmp = longComp.compare(midVal,elem) ;

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return low ;
	}
	
    static public int compareTo(long thisVal , long anotherVal) {
    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
	
	public int insertSorted(long elem, Comparator<Long> comparator) {
		int idx = getInsertSortedIndex(elem, comparator) ;
		add(idx , elem) ;
		return idx ;
	}
	
	public int insertSortedUnique(long elem, Comparator<Long> comparator) {
		int low = 0;
		int high = count-1;
		
		LongComparator longComp = comparator != null ? new LongComparatorWrapper(comparator) : longComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
    		long midVal = elems[mid] ;
		    
		    int cmp = longComp.compare(midVal,elem) ;

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return -1; // key found
		}
		
		add(low , elem) ;
		return low ;
	}
	
	public int insertSortedCheckEnd(long elem, Comparator<Long> comparator) {
		int cmp;
		if (count > 0) {
			LongComparator longComp = comparator != null ? new LongComparatorWrapper(comparator) : longComparator ;
			
			long endVal = elems[count - 1];
			cmp = longComp.compare(endVal, elem);
		}
		else {
			cmp = 0 ;
		}

		if (cmp <= 0) {
			this.add(elem);
			return count - 1;
		}
		else {
			return insertSorted(elem, comparator);
		}
	}

	public int binarySearch(long elem, Comparator<Long> comparator) {
		int low = 0;
		int high = count-1;
		
		LongComparator longComp = comparator != null ? new LongComparatorWrapper(comparator) : longComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    long midVal = elems[mid] ;
		    
		    int cmp = longComp.compare(midVal,elem);

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return -1 ;
	}
	
	///////////////////////////////////////////
	
	public void reverse() {
		
		for (int i=0, mid=count>>1, j=count-1; i<mid; i++, j--) {
			long obj = elems[i] ; 
			elems[i] = elems[j] ;
			elems[j] = obj ;
		}
		
	}
	
	public Vector<Long> toVector() {
		Vector<Long> vec = new Vector<Long>(this.count) ;
		
		for (int i = 0; i < this.count; i++) {
			vec.add( elems[i] ) ;
		}
		
		return vec ;
	}
	
	///////////////////////////////////////////
	
	private class MyIterator implements Iterator<Long> {
		int cursor = 0 ;
		
		public boolean hasNext() {
			return cursor < count ;
		}

		public Long next() {
			return elems[cursor++] ;
		}

		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	public Iterator<Long> iterator() {
		return new MyIterator() ;
	}
	
	///////////////////////////////////////
	
    public int hashCode() {
    	int hashCode = 1;
    	for (int i = 0; i < this.count; i++) {
    		long value = elems[i] ;
    		int h = (int)(value ^ (value >>> 32)) ;
    		hashCode = 31*hashCode + h ;
		}
    	return hashCode;
    }
    
    public boolean addAll(Collection<Long> c) {
		Iterator<Long> e = c.iterator();
		while (e.hasNext()) {
			add(e.next()) ;
		}
		return true ;
	}
    
    public boolean addAll(VectorLong other) {
    	int sz = other.count ;
    	
    	ensureCapacity(count+sz) ;
    	
    	for (int i = 0; i < sz; i++) {
			long elem = other.elems[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    
    public boolean addAll(long[] other) {
    	return addAll(other, 0, other.length) ;
    }
    
    public boolean addAll(long[] other, int off, int lng) {
    	ensureCapacity(count+lng) ;
    	
    	int limit = off+lng ;
    	for (int i = off; i < limit; i++) {
    		long elem = other[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    
	public boolean contains(long o) {
		for (int i = 0; i < this.count; i++) {
			if ( elems[i] == o ) return true ;
		}
		return false ;
	}
	
    public boolean containsAll(Collection<Long> c) {
    	Iterator<Long> e = c.iterator();
    	while (e.hasNext())
    	    if (!contains(e.next()))
    		return false;
    	return true;
    }
	
	public boolean isEmpty() {
		return count == 0;
	}
	
	public long[] toArray() {
		long[] array = new long[count] ;
		System.arraycopy(elems, 0, array, 0, count) ;
		return array ;
	}
	
	public long[] toArray(long[] array) {
		if (array == null || array.length < count) array = new long[count] ;
		System.arraycopy(elems, 0, array, 0, count) ;
		return array ;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder() ;
		
		str.append("[");
		
		for (int i = 0; i < count; i++) {
			if (i > 0) str.append(", ") ;
			str.append(elems[i]) ;
		}
		
		str.append("]");
		
		return str.toString();
	}
	
	public synchronized void copyInto(long[] anArray) {
    	System.arraycopy(elems, 0, anArray, 0, count);
    }
	
	public synchronized void copyInto(long[] anArray, int anArrayOffset) {
    	System.arraycopy(elems, 0, anArray, anArrayOffset, count) ;
    }
	
	///////////////////////////////////////
	
	public static void main(String[] args) {
		
		VectorLong v = new VectorLong() ;
		
		for (int i = 0; i < 10 ; i++) {
			v.add(i) ;
		}
		
		v.reverse() ;
		
		System.out.println("-------");
		
		System.out.println(v);
		
	}
	

}

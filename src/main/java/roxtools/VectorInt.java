package roxtools;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

final public class VectorInt implements Iterable<Integer> , Cloneable {
	
	public int[] elems ;
	public int count = 0 ;

	public VectorInt() {
		this(8) ;
	}

	/**
	 * PS: If {@code initialCapacity} &lt;= 0 it will be set to 1.
	 * @param initialCapacity Initial capacity of internal array.
	 */
	public VectorInt(int initialCapacity) {
		this.elems = new int[ Math.max(initialCapacity, 1) ] ;
	}
	
	public Object clone() {
		try {
			VectorInt v = (VectorInt) super.clone();
			v.elems = new int[count];
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
			int[] elems2 = new int[ (capacity - (capacity%2)) * 2 ];
			System.arraycopy(elems, 0, elems2, 0, count);
			elems = elems2;
		}
	}
	
	public int set(int index, int element) {
		int prevElem = elems[index] ;
		elems[index] = element ;
		return prevElem ;
	}
	
	public void add(int elem) {
		ensureCapacity(count+1) ;
		elems[count++] = elem ;
	}
	
	public void add(int idx , int elem) {
		ensureCapacity(count+1) ;
		System.arraycopy(elems, idx, elems, idx + 1, count - idx);
		elems[idx] = elem ;
		++count ;
	}
	
	public void insertElementAt(int elem, int idx) {
		add(idx , elem) ;
	}
	
	public int get(int idx) {
		return elems[idx];
	}
	
	public int firstElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[0];
	}
	
	public int lastElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[count-1];
	}
	
    public int indexOf(int o) {
    	
    	for (int i = 0; i < count; i++) {
			if ( elems[i] == o ) return i ;
		}
    	
    	return -1;
    }
    
	public boolean removeAll(Collection<Integer> c) {
		 boolean remove = false ;
		 
		 for (Integer obj : c) {
			 remove |= remove(obj) ;
		}
		
		return remove ;
	}
	
	public boolean remove(int o) {
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
	
	public int removeAtIndex(int idx) {
		int prevElem = elems[idx] ;

		int len = count - idx - 1;
		System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
		
		return prevElem ;
	}

	public void clear() {
		this.elems = new int[10];
		count = 0;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	static final private IntComparator intComparator = new IntComparator() ;
	static private class IntComparator {
		public int compare(int thisVal , int anotherVal) {
	    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	    }
	}
	
	static private class IntComparatorWrapper extends IntComparator {
		final Comparator<Integer> comparator ;

		public IntComparatorWrapper(Comparator<Integer> comparator) {
			this.comparator = comparator;
		}
		
		@Override
		public int compare(int thisVal, int anotherVal) {
			return this.comparator.compare(thisVal , anotherVal) ;
		}
		
	}
	
	public int getInsertSortedIndex(int elem, Comparator<Integer> comparator) {
		int low = 0;
		int high = count-1;
		
		IntComparator intComp = comparator != null ? new IntComparatorWrapper(comparator) : intComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    int midVal = elems[mid] ;
		    
		    int cmp = intComp.compare(midVal,elem) ;

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return low ;
	}
	
    static public int compareTo(int thisVal , int anotherVal) {
    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
	
	public int insertSorted(int elem, Comparator<Integer> comparator) {
		int idx = getInsertSortedIndex(elem, comparator) ;
		add(idx , elem) ;
		return idx ;
	}
	
	public int insertSortedUnique(int elem, Comparator<Integer> comparator) {
		int low = 0;
		int high = count-1;
		
		IntComparator intComp = comparator != null ? new IntComparatorWrapper(comparator) : intComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    int midVal = elems[mid] ;
		    
		    int cmp = intComp.compare(midVal,elem) ;

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
	
	public int insertSortedCheckEnd(int elem, Comparator<Integer> comparator) {
		int cmp;
		if (count > 0) {
			IntComparator intComp = comparator != null ? new IntComparatorWrapper(comparator) : intComparator ;
			
			int endVal = elems[count - 1];
			cmp = intComp.compare(endVal, elem);
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

	public int binarySearch(int elem, Comparator<Integer> comparator) {
		int low = 0;
		int high = count-1;
		
		IntComparator intComp = comparator != null ? new IntComparatorWrapper(comparator) : intComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    int midVal = elems[mid] ;
		    
		    int cmp = intComp.compare(midVal,elem);

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
			int obj = elems[i] ; 
			elems[i] = elems[j] ;
			elems[j] = obj ;
		}
		
	}
	
	public Vector<Integer> toVector() {
		Vector<Integer> vec = new Vector<Integer>(this.count) ;
		
		for (int i = 0; i < this.count; i++) {
			vec.add( elems[i] ) ;
		}
		
		return vec ;
	}
	
	///////////////////////////////////////////
	
	private class MyIterator implements Iterator<Integer> {
		int cursor = 0 ;
		
		public boolean hasNext() {
			return cursor < count ;
		}

		public Integer next() {
			return elems[cursor++] ;
		}

		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	public Iterator<Integer> iterator() {
		return new MyIterator() ;
	}
	
	///////////////////////////////////////
	
	@Override
    public int hashCode() {
    	int hashCode = 1;
    	for (int i = 0; i < this.count; i++) {
    	    hashCode = 31*hashCode + elems[i] ;
		}
    	return hashCode;
    }
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		VectorInt other = (VectorInt) obj;
		
		return equals(other.elems, 0, other.count) ;
	}

	public boolean equals(int[] a, int offset, int length) {
    	if ( this.count != length ) return false ;
    		
    	for (int i = 0; i < length; i++) {
    		if ( a[offset+i] != elems[i] ) return false ;
		}
    	
    	return true ;
    }
    
    public boolean addAll(Collection<Integer> c) {
		Iterator<Integer> e = c.iterator();
		while (e.hasNext()) {
			add(e.next()) ;
		}
		return true ;
	}
    
    public boolean addAll(VectorInt other) {
    	int sz = other.count ;
    	
    	ensureCapacity(count+sz) ;
    	
    	for (int i = 0; i < sz; i++) {
			int elem = other.elems[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    
    public boolean addAll(int[] other) {
    	return addAll(other, 0, other.length) ;
    }
    
    public boolean addAll(int[] other, int off, int lng) {
    	ensureCapacity(count+lng) ;
    	
    	int limit = off+lng ;
    	for (int i = off; i < limit; i++) {
			int elem = other[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    
	public boolean contains(int o) {
		for (int i = 0; i < this.count; i++) {
			if ( elems[i] == o ) return true ;
		}
		return false ;
	}
	
    public boolean containsAll(Collection<Integer> c) {
    	Iterator<Integer> e = c.iterator();
    	while (e.hasNext())
    	    if (!contains(e.next()))
    		return false;
    	return true;
    }
	
	public boolean isEmpty() {
		return count == 0;
	}
	
	public int[] toArray() {
		int[] array = new int[count] ;
		System.arraycopy(elems, 0, array, 0, count) ;
		return array ;
	}
	
	public int[] toArray(int[] array) {
		if (array == null || array.length < count) array = new int[count] ;
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
	
	public synchronized void copyInto(int[] anArray) {
    	System.arraycopy(elems, 0, anArray, 0, count);
    }
	
	public synchronized void copyInto(int[] anArray, int anArrayOffset) {
    	System.arraycopy(elems, 0, anArray, anArrayOffset, count) ;
    }
	
}

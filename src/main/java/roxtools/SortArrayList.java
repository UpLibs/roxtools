package roxtools;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

final public class SortArrayList<E> extends AbstractList<E> implements Iterable<E> , Cloneable {
	
	public Object[] elems ;
	public int count = 0 ;

	public SortArrayList() {
		this(8) ;
	}
	
	/**
	 * PS: If {@code initialCapacity} &lt;= 0 it will be set to 1.
	 * @param initialCapacity Initial capacity of internal array.
	 */
	public SortArrayList(int initialCapacity) {
		this.elems = new Object[ Math.max(initialCapacity, 1) ] ;
	}
	
	public SortArrayList(Object[] elems, boolean forceCopy) {
		this(elems, 0, elems.length, forceCopy) ;
	}
	
	public SortArrayList(Object[] elems, int off, int length, boolean forceCopy) {
		if (off != 0 || elems.length != length) forceCopy = true ;
		
		if (forceCopy) {
			this.elems = new Object[length] ;
			System.arraycopy(elems, off, this.elems, 0, length) ;
			this.count = this.elems.length ;
		}
		else {
			this.elems = elems ;
			this.count = elems.length ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			SortArrayList<E> v = (SortArrayList<E>) super.clone();
			v.elems = new Object[count];
			System.arraycopy(elems, 0, v.elems, 0, count);
			return v;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Object cloneAndAdd(E elem) {
		try {
			SortArrayList<E> v = (SortArrayList<E>) super.clone();
			v.elems = new Object[count+1];
			System.arraycopy(elems, 0, v.elems, 0, count);
			v.elems[count++] = elem ;
			return v;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	
	@Override
	public int size() {
		return count ;
	}
	
	@Override
	public boolean isEmpty() {
		return count == 0 ;
	}
	
	public void ensureCapacity(int capacity) {
		if (elems.length < capacity) {
			Object[] elems2 = new Object[ (capacity - (capacity%2)) * 2 ];
			System.arraycopy(elems, 0, elems2, 0, count);
			elems = elems2;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {
		E prevElem = (E) elems[index] ;
		elems[index] = element ;
		return prevElem ;
	}
	
	@Override
	public boolean add(E elem) {
		ensureCapacity(count+1) ;
		elems[count++] = elem ;
		return true ;
	}
	
	@Override
	public void add(int idx , E elem) {
		ensureCapacity(count+1) ;
		System.arraycopy(elems, idx, elems, idx + 1, count - idx);
		elems[idx] = elem ;
		++count ;
	}
	
	public void insertElementAt(E elem, int idx) {
		add(idx , elem) ;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public E get(int idx) {
		return (E) elems[idx];
	}
	
	@SuppressWarnings("unchecked")
	public E firstElement() {
		if (count == 0) throw new NoSuchElementException();
		return (E)elems[0];
	}
	
	 @SuppressWarnings("unchecked")
	public E lastElement() {
		if (count == 0) throw new NoSuchElementException();
		return (E)elems[count-1];
	 }
	 
	 @Override
	public boolean removeAll(Collection<?> c) {
		 boolean remove = false ;
		 
		 for (Object obj : c) {
			 remove |= remove(obj) ;
		}
		
		return remove ;
	}
	
	@Override
	public boolean remove(Object o) {
		int idx = indexOf(o) ;
		if (idx < 0) return false ;
		return remove(idx) != null ;
	}
	 
	@Override
	@SuppressWarnings("unchecked")
	public E remove(int idx) {
		E prevElem = (E) elems[idx] ;

		int len = count - idx - 1;
		if (len > 0) System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
		elems[count] = null;

		return prevElem;
	}
	
	public void removeVoid(int idx) {
		int len = count - idx - 1;
		if (len > 0) System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
		elems[count] = null;
	}
	
	public void removeRange(int idx, int lng) {
		if ( lng < 0 ) throw new IllegalArgumentException("Invalid range size: "+ lng) ;
		if ( idx+lng > count ) throw new ArrayIndexOutOfBoundsException(idx+"+"+lng) ;
	
		
		int len = count - idx - lng;
		System.arraycopy(elems, idx + lng, elems, idx, len);

		for (int i = 1; i <= lng; i++) {
			elems[count-i] = null;	
		}
		
		count -= lng ;
	}
	
	@Override
	public boolean contains(Object o) {
		return indexOf(o,0) >= 0; 
	}
	
	@Override
	public int indexOf(Object elem) {
		return indexOf(elem, 0);
	}
	
	public int indexOf(Object elem, int index) {
		if (elem == null) {
			for (int i = index; i < count; i++)
				if (elems[i] == null)
					return i;
		} else {
			for (int i = index; i < count; i++)
				if (elem.equals(elems[i]))
					return i;
		}
		return -1;

	}
	
	public int lastIndexOf(Object elem) {
		return lastIndexOf(elem, count - 1);
	}
	
	public int lastIndexOf(Object elem, int index) {
		if (index >= count)
			throw new IndexOutOfBoundsException(index + " >= " + count);
		
		if (elem == null) {
			for (int i = index; i >= 0; i--)
				if (elems[i] == null)
					return i;
		} else {
			for (int i = index; i >= 0; i--)
				if (elem.equals(elems[i]))
					return i;
		}
		
		return -1;
	}

	@Override
	public void clear() {
		this.elems = new Object[10];
		count = 0;
	}
	
	public void clear(int capacity) {
		this.elems = new Object[capacity];
		count = 0;
	}
	
	@SuppressWarnings("unchecked")
	public int getInsertSortedIndex(E elem, Comparator<E> comparator) {
		int low = 0;
		int high = count-1;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    E midVal = (E) elems[mid] ;
		    
		    int cmp = comparator.compare(midVal,elem);

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return low ;
	}
	
	public int insertSorted(E elem, Comparator<E> comparator) {
		int idx = getInsertSortedIndex(elem, comparator) ;
		add(idx , elem) ;
		return idx ;
	}
	
	@SuppressWarnings("unchecked")
	public int insertSortedCheckEnd(E elem, Comparator<E> comparator) {
		int cmp;
		if (count > 0) {
			E endVal = (E) elems[count - 1];
			cmp = comparator.compare(endVal, elem);
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

	@SuppressWarnings("unchecked")
	public int binarySearch(E elem, Comparator<E> comparator) {
		int low = 0;
		int high = count-1;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    E midVal = (E) elems[mid] ;
		    
		    int cmp = comparator.compare(midVal,elem);

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return -(low + 1);  // key not found.
	}
	
	///////////////////////////////////////////
	
	public void reverse() {
		
		for (int i=0, mid=count>>1, j=count-1; i<mid; i++, j--) {
			Object obj = elems[i] ; 
			elems[i] = elems[j] ;
			elems[j] = obj ;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Vector<E> toVector() {
		Vector<E> vec = new Vector<E>(this.count) ;
		
		for (int i = 0; i < this.count; i++) {
			vec.add( (E)elems[i] ) ;
		}
		
		return vec ;
	}
	
    public synchronized void copyInto(Object[] anArray) {
    	System.arraycopy(elems, 0, anArray, 0, count);
    }
	
	///////////////////////////////////////////
	
	private class MyIterator implements Iterator<E> {
		int cursor = 0 ;
		
		public boolean hasNext() {
			return cursor < count ;
		}

		@SuppressWarnings("unchecked")
		public E next() {
			return (E)elems[cursor++] ;
		}

		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		return new MyIterator() ;
	}
	

}

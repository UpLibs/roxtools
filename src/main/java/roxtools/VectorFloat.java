package roxtools;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

final public class VectorFloat implements Iterable<Float> , Cloneable {
	
	public float[] elems ;
	public int count = 0 ;

	public VectorFloat() {
		this(8) ;
	}

	/**
	 * PS: If {@code initialCapacity} &lt;= 0 it will be set to 1.
	 * @param initialCapacity Initial capacity of internal array.
	 */
	public VectorFloat(int initialCapacity) {
		this.elems = new float[ Math.max(initialCapacity, 1) ] ;
	}
	
	public Object clone() {
		try {
			VectorFloat v = (VectorFloat) super.clone();
			v.elems = new float[count];
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
			float[] elems2 = new float[ (capacity - (capacity%2)) * 2 ];
			System.arraycopy(elems, 0, elems2, 0, count);
			elems = elems2;
		}
	}
	
	public float set(int index, float element) {
		float prevElem = elems[index] ;
		elems[index] = element ;
		return prevElem ;
	}
	
	public void add(float elem) {
		ensureCapacity(count+1) ;
		elems[count++] = elem ;
	}
	
	public void addList(float... elem) {
		ensureCapacity(count+elem.length) ;
		
		for (int i = 0; i < elem.length; i++) {
			elems[count++] = elem[i] ;	
		}
	}
	
	public void add(int idx , float elem) {
		ensureCapacity(count+1) ;
		System.arraycopy(elems, idx, elems, idx + 1, count - idx);
		elems[idx] = elem ;
		++count ;
	}
	
	public void insertElementAt(float elem, int idx) {
		add(idx , elem) ;
	}
	
	public float get(int idx) {
		return elems[idx];
	}
	
	public float firstElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[0];
	}
	
	public float lastElement() {
		if (count == 0) throw new NoSuchElementException();
		return elems[count-1];
	}
	
    public int indexOf(float o) {
    	
    	for (int i = 0; i < count; i++) {
			if ( elems[i] == o ) return i ;
		}
    	
    	return -1;
    }
    
	public boolean removeAll(Collection<Float> c) {
		 boolean remove = false ;
		 
		 for (Float obj : c) {
			 remove |= remove(obj) ;
		}
		
		return remove ;
	}
	
	public boolean remove(float o) {
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
	
	public float removeAtIndex(int idx) {
		float prevElem = elems[idx] ;

		int len = count - idx - 1;
		System.arraycopy(elems, idx + 1, elems, idx, len);

		--count;
		
		return prevElem ;
	}

	public void clear() {
		this.elems = new float[10];
		count = 0;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	static final private DoubleComparator doubleComparator = new DoubleComparator() ;
	static private class DoubleComparator {
		public int compare(float thisVal , float anotherVal) {
	    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	    }
	}
	
	static private class DoubleComparatorWrapper extends DoubleComparator {
		final Comparator<Float> comparator ;

		public DoubleComparatorWrapper(Comparator<Float> comparator) {
			this.comparator = comparator;
		}
		
		@Override
		public int compare(float thisVal, float anotherVal) {
			return this.comparator.compare(thisVal , anotherVal) ;
		}
		
	}
	
	public int getInsertSortedIndex(float elem, Comparator<Float> comparator) {
		int low = 0;
		int high = count-1;
		
		DoubleComparator doubleComp = comparator != null ? new DoubleComparatorWrapper(comparator) : doubleComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    float midVal = elems[mid] ;
		    
		    int cmp = doubleComp.compare(midVal,elem) ;

		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return low ;
	}
	
    static public int compareTo(float thisVal , float anotherVal) {
    	return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
    }
	
	public int insertSorted(float elem, Comparator<Float> comparator) {
		int idx = getInsertSortedIndex(elem, comparator) ;
		add(idx , elem) ;
		return idx ;
	}
	
	public int insertSortedUnique(float elem, Comparator<Float> comparator) {
		int low = 0;
		int high = count-1;
		
		DoubleComparator doubleComp = comparator != null ? new DoubleComparatorWrapper(comparator) : doubleComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    float midVal = elems[mid] ;
		    
		    int cmp = doubleComp.compare(midVal,elem) ;

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
	
	public int insertSortedCheckEnd(float elem, Comparator<Float> comparator) {
		int cmp;
		if (count > 0) {
			DoubleComparator doubleComp = comparator != null ? new DoubleComparatorWrapper(comparator) : doubleComparator ;
			
			float endVal = elems[count - 1];
			cmp = doubleComp.compare(endVal, elem);
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

	public int binarySearch(float elem, Comparator<Float> comparator) {
		int low = 0;
		int high = count-1;
		
		DoubleComparator doubleComp = comparator != null ? new DoubleComparatorWrapper(comparator) : doubleComparator ;
		
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    float midVal = elems[mid] ;
		    
		    int cmp = doubleComp.compare(midVal,elem);

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
			float obj = elems[i] ; 
			elems[i] = elems[j] ;
			elems[j] = obj ;
		}
		
	}
	
	public Vector<Float> toVector() {
		Vector<Float> vec = new Vector<Float>(this.count) ;
		
		for (int i = 0; i < this.count; i++) {
			vec.add( elems[i] ) ;
		}
		
		return vec ;
	}
	
	///////////////////////////////////////////
	
	private class MyIterator implements Iterator<Float> {
		int cursor = 0 ;
		
		public boolean hasNext() {
			return cursor < count ;
		}

		public Float next() {
			return elems[cursor++] ;
		}

		public void remove() {
			throw new UnsupportedOperationException() ;
		}
	}
	
	public Iterator<Float> iterator() {
		return new MyIterator() ;
	}
	
	///////////////////////////////////////
	
	public int hashCode(float value) {
		int bits = Float.floatToIntBits(value);
		return bits ;
	}

	
    public int hashCode() {
    	int hashCode = 1;
    	for (int i = 0; i < this.count; i++) {
    	    hashCode = 31*hashCode + hashCode(elems[i]) ;
		}
    	return hashCode;
    }
    
    public boolean addAll(Collection<Float> c) {
		Iterator<Float> e = c.iterator();
		while (e.hasNext()) {
			add(e.next()) ;
		}
		return true ;
	}
    

    public boolean addAll(VectorFloat other) {
    	int sz = other.count ;
    	
    	ensureCapacity(count+sz) ;
    	
    	for (int i = 0; i < sz; i++) {
			float elem = other.elems[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    

    public boolean addAll(float[] other) {
    	return addAll(other, 0, other.length) ;
    }
    
    public boolean addAll(float[] other, int off, int lng) {
    	ensureCapacity(count+lng) ;
    	
    	int limit = off+lng ;
    	for (int i = off; i < limit; i++) {
    		float elem = other[i] ; 
			elems[count++] = elem ;
		}
    	
    	return true ;
    }
    
	public boolean contains(float o) {
		for (int i = 0; i < this.count; i++) {
			if ( elems[i] == o ) return true ;
		}
		return false ;
	}
	
    public boolean containsAll(Collection<Float> c) {
    	Iterator<Float> e = c.iterator();
    	while (e.hasNext())
    	    if (!contains(e.next()))
    		return false;
    	return true;
    }
	
	public boolean isEmpty() {
		return count == 0;
	}
	
	public float[] toArray() {
		float[] array = new float[count] ;
		System.arraycopy(elems, 0, array, 0, count) ;
		return array ;
	}
	
	public double[] toDoubleArray() {
		double[] array = new double[count] ;
		for (int i = 0; i < count; i++) {
			array[i] = elems[i] ;
		}
		return array ;
	}
	
	public float[] toArray(int offset, int length) {
		float[] array = new float[length] ;
		System.arraycopy(elems, offset, array, 0, length) ;
		return array ;
	}
	
	public float[] toArray(float[] array) {
		if (array == null || array.length < count) array = new float[count] ;
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
	
	public synchronized void copyInto(float[] anArray) {
    	System.arraycopy(elems, 0, anArray, 0, count);
    }
	
	public synchronized void copyInto(float[] anArray, int anArrayOffset) {
    	System.arraycopy(elems, 0, anArray, anArrayOffset, count);
    }
	
	///////////////////////////////////////
	
	public static void main(String[] args) {
		
		VectorFloat v = new VectorFloat() ;
		
		for (int i = 0; i < 10 ; i++) {
			v.add(i) ;
		}
		
		v.reverse() ;
		
		System.out.println("-------");
		
		System.out.println(v);
		
	}
	

}

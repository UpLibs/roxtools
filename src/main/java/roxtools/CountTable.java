package roxtools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final public class CountTable<K> {
	
	final static public class Entry<K> {
		final int hash ;
		final K key ;
		int count ;
		
		public Entry(K key, int count, int hash) {
			this.hash = hash;
			this.key = key;
			this.count = count;
		}
		
		public K getKey() {
			return key;
		}
		
		public int getCount() {
			return count;
		}
		
		@Override
		public String toString() {
			return key+"="+count ;
		}
	}
	

	static private final int MAXIMUM_CAPACITY = 1 << 30;
	static private final int DEFAULT_TABLE_GROUP_SIZE = 30 ;
	static private final float DEFAULT_TABLE_LOAD_FACTOR = 0.75f ;
	
	
	private int size = 0 ;
	
	private int threshold ;
	
	private Entry<K>[][] table ;
	private int[] tableSizes ;
	
	public CountTable() {
		this(8) ;
	}
	
	@SuppressWarnings("unchecked")
	public CountTable(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new Entry[capacity][DEFAULT_TABLE_GROUP_SIZE] ;
		this.tableSizes = new int[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
	}
	

	static private int calcThreshold(int capacity) {
		return (int) Math.min(capacity * DEFAULT_TABLE_GROUP_SIZE*DEFAULT_TABLE_LOAD_FACTOR , MAXIMUM_CAPACITY + 1);
	}
	
	
	static int hash(Object k) {
        int h = k.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
	
	static int tableIndexFor(int h, int length) {
        return h & (length-1);
    }
	
	private boolean eq(K a, K b) {
		return a == b || a.equals(b) ;
	}
	
	public int increment(K key) {
		return sum(key, 1);
	}
	
	public int decrement(K key) {
		return sum(key, -1);
	}
	
	public int sum(K key, int amount) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) {
				v.count += amount ;
				return v.count ;
			}
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			
			group = table[tableIdx] ;
			groupSize = tableSizes[tableIdx] ;
		}
		
		if (groupSize == group.length) {
			group = Arrays.copyOf(group, groupSize+DEFAULT_TABLE_GROUP_SIZE) ;
			table[tableIdx] = group ;
		}
		
		group[groupSize++] = new Entry<K>(key,amount,hash) ;
		tableSizes[tableIdx] = groupSize ;
		
		this.size++ ;
		
		return amount ;
	}
	
	public boolean set(K key, int value) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) {
				v.count = value ;
				return false ;
			}
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			
			group = table[tableIdx] ;
			groupSize = tableSizes[tableIdx] ;
		}
		
		if (groupSize == group.length) {
			group = Arrays.copyOf(group, groupSize+DEFAULT_TABLE_GROUP_SIZE) ;
			table[tableIdx] = group ;
		}
		
		group[groupSize++] = new Entry<K>(key,value,hash) ;
		tableSizes[tableIdx] = groupSize ;
		
		this.size++ ;
		
		return true ;
	}
	
	public boolean setMaximum(K key, int value) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) {
				if (value > v.count) {
					v.count = value ;
					return true ;
				}
				else {
					return false ;
				}
			}
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			
			group = table[tableIdx] ;
			groupSize = tableSizes[tableIdx] ;
		}
		
		if (groupSize == group.length) {
			group = Arrays.copyOf(group, groupSize+DEFAULT_TABLE_GROUP_SIZE) ;
			table[tableIdx] = group ;
		}
		
		group[groupSize++] = new Entry<K>(key,value,hash) ;
		tableSizes[tableIdx] = groupSize ;
		
		this.size++ ;
		
		return true ;
	}
	
	public boolean setMinimum(K key, int value) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) {
				if (value < v.count) {
					v.count = value ;
					return true ;
				}
				else {
					return false ;
				}
			}
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			
			group = table[tableIdx] ;
			groupSize = tableSizes[tableIdx] ;
		}
		
		if (groupSize == group.length) {
			group = Arrays.copyOf(group, groupSize+DEFAULT_TABLE_GROUP_SIZE) ;
			table[tableIdx] = group ;
		}
		
		group[groupSize++] = new Entry<K>(key,value,hash) ;
		tableSizes[tableIdx] = groupSize ;
		
		this.size++ ;
		
		return true ;
	}
	
	public Entry<K> remove(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) {
				System.arraycopy(group, i+1, group, i, (groupSize-(i+1))) ;
				tableSizes[tableIdx] = groupSize-1 ;
				
				return v ;
			}
		}
		
		return null ;
	}
	
	
	public int get(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) return v.count ;
		}
		
		return 0 ;
	}
	
	public Entry<K> getEntry(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) return v ;
		}
		
		return null ;
	}
	
	public boolean contains(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		
		Entry<K>[] group = table[tableIdx] ;
		int groupSize = tableSizes[tableIdx] ;
		
		for (int i = groupSize-1; i >= 0; i--) {
			Entry<K> v = group[i] ;
			if (v.hash == hash && eq(v.key,key)) return true ;
		}
		
		return false ;
	}
	
	
	private void reHash(int newTableSize) {
		@SuppressWarnings("unchecked")
		Entry<K>[][] table2 = new Entry[newTableSize][DEFAULT_TABLE_GROUP_SIZE] ;
		int[] table2Sizes = new int[newTableSize] ;
		
		for (int i = 0; i < table.length; i++) {
			Entry<K>[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			for (int j = groupSz-1; j >= 0; j--) {
				Entry<K> v = group[j] ;
				
				int hash = v.hash;
				int tableIdx = tableIndexFor(hash, newTableSize) ;
				
				Entry<K>[] group2 = table2[tableIdx] ;
				int group2sz = table2Sizes[tableIdx] ;
				
				if (group2sz == group2.length) {
					group2 = Arrays.copyOf(group2, group2sz+DEFAULT_TABLE_GROUP_SIZE) ;
					table2[tableIdx] = group2 ;
				}
				
				group2[group2sz++] = v ;
				table2Sizes[tableIdx] = group2sz ;
			}
		}
		
		this.table = table2 ;
		this.tableSizes = table2Sizes ;
		
		this.threshold = calcThreshold( newTableSize ) ;
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0 ;
	}
	
	public void clear() {
		clear(8) ;
	}
	
	@SuppressWarnings("unchecked")
	public void clear(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new Entry[capacity][DEFAULT_TABLE_GROUP_SIZE] ;
		this.tableSizes = new int[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
		
		this.size = 0 ;
	}
	
	public Entry<K>[] getEntries() {
		@SuppressWarnings("unchecked")
		Entry<K>[] entries = new Entry[size] ;
		int entriesSz = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			Entry<K>[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			for (int j = groupSz-1; j >= 0 ; j--) {
				Entry<K> v = group[j] ;
				entries[entriesSz++] = v ;
			}
		}
		
		return entries ;
	}
	
	static public interface FilterEntry<K> {
		public boolean filter( Entry<K> entry ) ;
	}
	
	public Entry<K>[] getEntries( FilterEntry<K> filter ) {
		@SuppressWarnings("unchecked")
		Entry<K>[] entries = new Entry[size] ;
		int entriesSz = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			Entry<K>[] group = table[i] ;
			int groupSz = tableSizes[i] ;
			
			for (int j = groupSz-1; j >= 0 ; j--) {
				Entry<K> v = group[j] ;
				if ( filter.filter(v) ) {
					entries[entriesSz++] = v ;
				}
			}
		}
		
		if (entriesSz < entries.length) {
			return Arrays.copyOf(entries, entriesSz) ;
		}
		else {
			return entries ;
		}
	}
	
	public List<K> getKeys() {
		ArrayList<K> list = new ArrayList<K>(size) ;
		
		for (Entry<K> entry : getEntries()) {
			list.add( entry.getKey() ) ;
		}
		
		return list ;
	}
	
	public List<K> getKeysOrdered() {
		return sortKeys( getKeys() ) ;
	}
	
	public Object[] getKeysArray() {
		Object[] keys = new Object[size] ;
		int keysSz = 0 ;
		
		for (Entry<K> entry : getEntries()) {
			keys[keysSz++] = entry.getKey() ;
		}
		
		return keys ;
	}
	
	@SuppressWarnings("unchecked")
	public K[] getKeysArray(K[] keys) {
		if (keys.length < size) {
			keys = (K[]) Array.newInstance(keys.getClass().getComponentType(), size) ;
		}
		
		int keysSz = 0 ;
		
		for (Entry<K> entry : getEntries()) {
			keys[keysSz++] = entry.getKey() ;
		}
		
		if (keys.length > size) keys[size] = null;
		
		return keys ;
	}
	
	public K[] getKeysArrayOrdered(K[] keys) {
		return sortKeys( getKeysArray(keys) ) ;
	}

	public List<K> sortKeys(List<K> keys) {
		if (keys == null) return null ;
		
		Collections.sort( keys , new Comparator<K>() {
			@Override
			public int compare(K o1, K o2) {
				int c1 = get(o1) ;
				int c2 = get(o2) ;
				return Integer.compare(c1, c2);
			}
		});
		return keys ;
	}
	
	public K[] sortKeys(K[] keys) {
		if (keys == null) return null ;
		
		Arrays.sort( keys , new Comparator<K>() {
			@Override
			public int compare(K o1, K o2) {
				int c1 = get(o1) ;
				int c2 = get(o2) ;
				return Integer.compare(c1, c2);
			}
		});
		return keys ;
	}

	
}

package roxtools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final public class WeakMapRecyclable<K,V> {
	
	static public interface RecycleHandler<V> {
		
		public void recycleValue(V value) ;
		
	}
	
	@SuppressWarnings("rawtypes")
	static final private class RecycleHandlerDummy implements RecycleHandler {
		@Override
		public void recycleValue(Object value) {}
	}
	
	static final private RecycleHandlerDummy DUMMY_RECICLE_HANDLER = new RecycleHandlerDummy() ; 
	
	/////////////////////////////////////////////////////////////////////////
	
	final static public class Entry<K,V> extends WeakReference<K> {
		int hash ;
		V value ;
		
		Entry<K,V> next ;
		
		public Entry(K key, V value, int hash, ReferenceQueue<K> refQueue) {
			super(key, refQueue);
			
			this.hash = hash ;
			this.value = value ;
		}
		
		public V getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return get()+"="+value ;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////

	static private final int MAXIMUM_CAPACITY = 1 << 30;
	static private final int DEFAULT_TABLE_GROUP_SIZE = 30 ;
	static private final float DEFAULT_TABLE_LOAD_FACTOR = 0.75f ;
	
	private int size = 0 ;
	private int threshold ;
	
	private Entry<K,V>[] table ;
	private ReferenceQueue<K> refQueue = new ReferenceQueue<K>() ; 
	
	public WeakMapRecyclable() {
		this(8) ;
	}
	
	@SuppressWarnings("unchecked")
	public WeakMapRecyclable(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new Entry[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
	}
	
	public WeakMapRecyclable(RecycleHandler<V> recycleHandler) {
		this() ;
		this.recycleHandler = recycleHandler ;
	}
	
	public WeakMapRecyclable(int initialCapacity, RecycleHandler<V> recycleHandler) {
		this(initialCapacity);
		this.recycleHandler = recycleHandler ;
	}
	
	@SuppressWarnings("unchecked")
	private RecycleHandler<V> recycleHandler = DUMMY_RECICLE_HANDLER ;
	
	public void setRecycleHandler(RecycleHandler<V> recycleHandler) {
		this.recycleHandler = recycleHandler;
	}
	
	public RecycleHandler<V> getRecycleHandler() {
		return recycleHandler;
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
	
	private void recycleValue(V value) {
		
		try {
			if (value != null) {
				recycleHandler.recycleValue(value) ;
			}
		}
		catch (Throwable e) {
			e.printStackTrace(); 
		}
		
	}
	
	public V put(K key, V value) {
		expungeStaleEntries();
		
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		Entry<K,V> head = table[tableIdx] ;
		
		{
			Entry<K,V> cursor = head ;
			Entry<K,V> prev = null ;
			
			while ( cursor != null ) {
				Entry<K,V> next = cursor.next ;
				
				if ( cursor.hash == hash ) {
					K k = cursor.get() ;
					
					if (k == null) {
						if (prev == null) {
							table[tableIdx] = next ;
						}
						else {
							prev.next = next ;
						}
						
						this.size-- ;
						
						recycleValue( cursor.value );
					}
					else if ( eq(k,key) ) {
						V prevVal = cursor.value ;
						cursor.value = value ;
						
						recycleValue(prevVal) ;
						
						return prevVal ;
					}
				}
				
				prev = cursor ;
				cursor = next ; 
			}
		}
		
		if (size >= threshold) {
			reHash( table.length * 2 ) ;
			
			tableIdx = tableIndexFor(hash, table.length) ;
			head = table[tableIdx] ;
		}
		
		Entry<K,V> entry = new Entry<K,V>(key,value, hash, refQueue) ;
		entry.next = head ;
		
		table[tableIdx] = entry ;
		
		this.size++ ;
		
		return null ;
	}
	
	public Entry<K,V> remove(K key) {
		return removeImplem(key) ;
	}
	
	public void removeAndRecycle(K key) {
		Entry<K, V> entry = removeImplem(key) ;
		
		if (entry != null) {
			recycleValue( entry.value );
		}
	}
	
	private Entry<K,V> removeImplem(K key) {
		expungeStaleEntries();
		
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		Entry<K,V> head = table[tableIdx] ;
		
		{
			Entry<K,V> cursor = head ;
			Entry<K,V> prev = null ;
			
			while ( cursor != null ) {
				Entry<K,V> next = cursor.next ;
				
				if ( cursor.hash == hash ) {
					K k = cursor.get() ;
					
					if (k == null) {
						if (prev == null) {
							table[tableIdx] = next ;
						}
						else {
							prev.next = next ;	
						}
						
						this.size-- ;
						
						recycleValue( cursor.value );
					}
					else if ( eq(k,key) ) {
						if (prev == null) {
							table[tableIdx] = next ;
						}
						else {
							prev.next = next ;	
						}
						
						this.size-- ;
						
						return cursor ;
					}	
				}
				
				prev = cursor ;
				cursor = next ; 
			}
		}
		
		return null ;
	}
	
	
	public V get(K key) {
		expungeStaleEntries();
		
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		Entry<K,V> head = table[tableIdx] ;
	
		Entry<K,V> cursor = head ;
		Entry<K,V> prev = null ;
		
		while ( cursor != null ) {
			Entry<K,V> next = cursor.next ;
			
			if (cursor.hash == hash) {
				K k = cursor.get() ;
				
				if (k == null) {
					if (prev == null) {
						table[tableIdx] = next ;
					}
					else {
						prev.next = next ;	
					}
					
					this.size-- ;
					
					recycleValue( cursor.value ) ;
				}
				else if ( eq(k,key) ) {
					return cursor.value ;
				}
			}
			
			prev = cursor ;
			cursor = next ; 
		}
		
		return null ;
	}
	
	public V getFast(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		Entry<K,V> head = table[tableIdx] ;
	
		Entry<K,V> cursor = head ;
		
		while ( cursor != null ) {
			Entry<K,V> next = cursor.next ;
			
			if (cursor.hash == hash) {
				K k = cursor.get() ;
				
				if (k != null && eq(k,key) ) {
					return cursor.value ;
				}
			}
			
			cursor = next ; 
		}
		
		return null ;
	}
	
	public boolean contains(K key) {
		int hash = hash(key) ;
		
		int tableIdx = tableIndexFor(hash, table.length) ;
		Entry<K,V> head = table[tableIdx] ;
		
		Entry<K,V> cursor = head ;
		Entry<K,V> prev = null ;
		
		while ( cursor != null ) {
			Entry<K,V> next = cursor.next ;
			
			if (cursor.hash == hash) {
				K k = cursor.get() ;
				
				if (k == null) {
					if (prev == null) {
						table[tableIdx] = next ;
					}
					else {
						prev.next = next ;	
					}
					
					this.size-- ;
					
					recycleValue( cursor.value ) ;
				}
				else if ( eq(k,key) ) {
					return true ;
				}
			}
			
			prev = cursor ;
			cursor = next ; 
		}
		
		return false ;
	}
	
	
	private void reHash(int newTableSize) {
		@SuppressWarnings("unchecked")
		Entry<K,V>[] table2 = new Entry[newTableSize] ;
		
		int size2 = 0 ;
		
		for (int i = 0; i < table.length; i++) {
			Entry<K,V> cursor = table[i] ;
			
			while (cursor != null) {
				K k = cursor.get() ;
				
				if (k == null) {
					recycleValue(cursor.value);
					continue ;
				}
				
				Entry<K,V> next = cursor.next ;
				
				int hash = hash(k) ;
				int tableIdx = tableIndexFor(hash, newTableSize) ;
				
				cursor.next = table2[tableIdx] ;
				table2[tableIdx] = cursor ;
				
				size2++ ;
				
				cursor = next ;
			}
			
		}
		
		this.table = table2 ;
		this.size = size2 ;
		
		this.threshold = calcThreshold( newTableSize ) ;
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0 ;
	}
	
	public void expungeStaleEntries() {
		
		for (Object ref ; (ref = refQueue.poll()) != null; ) {
            synchronized (refQueue) {
                @SuppressWarnings("unchecked")
                Entry<K,V> entry = (Entry<K,V>) ref;
                
        		int tableIdx = tableIndexFor(entry.hash, table.length) ;
        		Entry<K,V> head = table[tableIdx] ;
        		
        		Entry<K,V> cursor = head ;
        		Entry<K,V> prev = null ;
        		
        		while ( cursor != null ) {
        			Entry<K,V> next = cursor.next ;
        			
        			if (cursor == entry) {
    					if (prev == null) {
    						table[tableIdx] = next ;
    					}
    					else {
    						prev.next = next ;	
    					}
    					
    					this.size-- ;
    					
    					recycleValue( cursor.value ) ;
    				}
        			
        			prev = cursor ;
        			cursor = next ; 
        		}
            }
		}
		
	}
	
	public void clear() {
		clear(8) ;
	}
	
	@SuppressWarnings("unchecked")
	public void clear(int initialCapacity) {
		if (initialCapacity < 4) initialCapacity = 4 ;
		
		for (Object ref ; (ref = refQueue.poll()) != null; ) {
			Entry<K,V> entry = (Entry<K,V>) ref;
			recycleValue(entry.value);
		}
		
		int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
		
		this.table = new Entry[capacity] ;
		
		this.threshold = calcThreshold(capacity) ;
		
		this.size = 0 ;
	}
	
	public Entry<K,V>[] getEntries() {
		expungeStaleEntries();
		
		@SuppressWarnings("unchecked")
		Entry<K,V>[] entries = new Entry[size] ;
		int entriesSz = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			Entry<K,V> cursor = table[i] ;
			Entry<K,V> prev = null ;
			
			while (cursor != null) {
				K k = cursor.get() ;
			
				Entry<K,V> next = cursor.next ;
				
				if (k == null) {
					if (prev == null) {
						table[i] = next ;
					}
					else {
						prev.next = next ;
					}
					
					this.size-- ;
					
					recycleValue( cursor.value );
				}
				else {
					entries[entriesSz++] = cursor ;	
				}
				
				prev = cursor ;
				cursor = next ;
			}
		}
		
		return entries ;
	}
	
	static public interface FilterEntry<K,V> {
		public boolean filter( Entry<K,V> entry ) ;
	}
	
	public Entry<K,V>[] getEntries( FilterEntry<K,V> filter ) {
		expungeStaleEntries();
		
		@SuppressWarnings("unchecked")
		Entry<K,V>[] entries = new Entry[size] ;
		int entriesSz = 0 ;
		
		for (int i = table.length-1; i >= 0 ; i--) {
			Entry<K,V> cursor = table[i] ;
			Entry<K,V> prev = null ;
			
			while (cursor != null) {
				K k = cursor.get() ;
			
				Entry<K,V> next = cursor.next ;
				
				if (k == null) {
					if (prev == null) {
						table[i] = next ;
					}
					else {
						prev.next = next ;
					}
					
					this.size-- ;
					
					recycleValue( cursor.value );
				}
				else if ( filter.filter(cursor) ) {
					entries[entriesSz++] = cursor ;
				}
				
				prev = cursor ;
				cursor = next ;
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
		
		for (Entry<K,V> entry : getEntries()) {
			K key = entry.get() ;
			if (key != null) list.add(key) ;
		}
		
		return list ;
	}
	
	public Object[] getKeysArray() {
		Object[] keys = new Object[size] ;
		int keysSz = 0 ;
		
		for (Entry<K,V> entry : getEntries()) {
			K key = entry.get() ;
			if (key != null) {
				keys[keysSz++] = key ;
			}
		}
		
		if (keysSz != keys.length) {
			return Arrays.copyOf(keys, keysSz) ;
		}
		else {
			return keys ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public K[] getKeysArray(K[] keys) {
		if (keys.length < size) {
			keys = (K[]) Array.newInstance(keys.getClass().getComponentType(), size) ;
		}
		
		int keysSz = 0 ;
		
		for (Entry<K,V> entry : getEntries()) {
			K key = entry.get() ;
			if (key != null) {
				keys[keysSz++] = key ;
			}
		}
		
		if (keys.length > keysSz) keys[keysSz] = null;
		
		return keys ;
	}
	
}

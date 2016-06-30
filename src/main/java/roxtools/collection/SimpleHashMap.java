package roxtools.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of HashMap that uses less memory and no wrapper object for each entry.
 * 
 * @author gracilianomp@gmail.com
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
final public class SimpleHashMap<K, V> implements Map<K, V>{

	private static final int MASK_REMOVE_NEGATIVE_SIGN = 0x7FFFFFFF;
	
	static private int groupIndex(int objHashcode, int totalGroups) {
		return (objHashcode & MASK_REMOVE_NEGATIVE_SIGN) % totalGroups ;
	}
	
	/////////////////////////////////////////////////////////////
	
	private int size = 0 ;
	
	final private DynamicArrayObject<K> chainKey ;
	final private DynamicArrayObject<V> chainVal ;
	final private DynamicArrayInt chainHash ;
	final private DynamicArrayInt chainNext ;
	
	private int chainRemoved ;
	
	private int[] groups ;
	private int[] groupsSizes ;
	
	public SimpleHashMap() {
		this.chainKey = new DynamicArrayObject<>() ;
		this.chainVal = new DynamicArrayObject<>() ;
		this.chainHash = new DynamicArrayInt() ;
		this.chainNext = new DynamicArrayInt() ;
		
		skipFirstChainPos();
		
		this.groups = new int[8] ;
		this.groupsSizes = new int[groups.length] ;
	}
	
	public SimpleHashMap( Class<K> keyClass, Class<V> valClass ) {
		this.chainKey = new DynamicArrayObject<>(keyClass) ;
		this.chainVal = new DynamicArrayObject<>(valClass) ;
		this.chainHash = new DynamicArrayInt() ;
		this.chainNext = new DynamicArrayInt() ;
		
		skipFirstChainPos();
		
		this.groups = new int[8] ;
		this.groupsSizes = new int[groups.length] ;
	}

	private void skipFirstChainPos() {
		// Skip 1st position, so index 0 will be as null position, or end/stop index.
		this.chainKey.add(null);
		this.chainVal.add(null);
		this.chainHash.addInt(0);
		this.chainNext.addInt(0);
	}
	
	@Override
	public int size() {
		return size ;
	}

	@Override
	public boolean isEmpty() {
		return size == 0 ;
	}

	@Override
	public boolean containsKey(Object key) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				K k = chainKey.get(pos) ;
				if ( key.equals(k) ) {
					return true ;
				}
			}
			
			pos = chainNext.getInt(pos) ;
		}
		
		return false ;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public V get(Object key) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				K k = chainKey.get(pos) ;
				if ( key.equals(k) ) {
					return chainVal.get(pos) ;
				}
			}
			
			pos = chainNext.getInt(pos) ;
		}
		
		return null;
	}

	@Override
	public V put(K key, V value) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int groupPos = groups[groupIdx] ;
		
		int cursor = groupPos ;
		
		while (cursor > 0) {
			int h = chainHash.getInt(cursor) ;
			
			if ( objHash == h ) {
				K k = chainKey.get(cursor) ;
				if ( key.equals(k) ) {
					chainKey.set(cursor, key) ;
					V prev = chainVal.set(cursor, value);
					return prev ;
				}
			}
			
			cursor = chainNext.getInt(cursor) ;
		}
		
		int newPos ;
		
		if (chainRemoved > 0) {
			newPos = chainRemoved ;
			int nextRemoved = chainNext.getInt(newPos) ;
			chainRemoved = nextRemoved ;
			
			chainNext.setInt(newPos, groupPos);
			chainHash.setInt(newPos, objHash);
			chainKey.set(newPos, key);
			chainVal.set(newPos, value);
		}
		else {
			newPos = chainNext.size() ;
			
			chainNext.addInt(groupPos);
			chainHash.addInt(objHash);
			chainKey.add(key);
			chainVal.add(value);
		}
		
		groups[groupIdx] = newPos ;
		groupsSizes[groupIdx]++ ;
		
		this.size++ ;
		
		checkRehashNeeded();
		
		return null;
	}

	@Override
	public V remove(Object key) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int prevPos = 0 ;
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				K k = chainKey.get(pos) ;
				
				if ( key.equals(k) ) {
					chainKey.set(pos, null) ;
					V prev = chainVal.set(pos, null) ;
					
					int next = chainNext.getInt(pos) ;
					
					if (prevPos > 0) {
						chainNext.setInt(prevPos, next) ;
					}
					else {
						groups[groupIdx] = next ;
					}
					
					groupsSizes[groupIdx]-- ;
					
					this.size-- ;
					
					chainNext.setInt(pos, this.chainRemoved) ;
					this.chainRemoved = pos ;
					
					return prev ;
				}
			}
			
			prevPos = pos ;
			pos = chainNext.getInt(pos) ;
		}
		
		return null;
	}
	
	private void checkRehashNeeded() {
		if ( this.size > this.groups.length*10 ) {
			rehash( this.groups.length*2 );
		}
	}

	private void rehash(int totalGroups) {
		if ( this.groups.length == totalGroups ) return ;
		
		int[] groups2 = new int[totalGroups] ;
		int[] groupsSizes2 = new int[totalGroups] ;
		
		int sz = this.size+1 ;
		
		for (int i = 1; i < sz; i++) {
			K key = chainKey.get(i) ;
			if (key == null) continue ;
			
			int objHash = key.hashCode() ;
			int groupIdx = groupIndex(objHash, totalGroups) ;
			
			int prevPos = groups2[groupIdx] ;
			
			groups2[groupIdx] = i ;
			groupsSizes2[groupIdx]++ ;
			
			chainNext.setInt(i, prevPos) ;
		}
		
		this.groups = groups2 ;
		this.groupsSizes = groupsSizes2 ;
		
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			put( entry.getKey() , entry.getValue() ) ;
		}
	}

	@Override
	public void clear() {
		this.size = 0 ;
		
		this.chainKey.clear();
		this.chainVal.clear();
		this.chainHash.clear();
		this.chainNext.clear();
		
		this.groups = new int[8] ;
		this.groupsSizes = new int[8] ;
		
		this.chainRemoved = 0 ;
		
		skipFirstChainPos();
	}
	
	//////////////////////////////////////////////////////////
	
	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				Iterator<K> iterator = chainKey.iterator() ;
				if (iterator.hasNext()) iterator.next() ;
				return iterator ;
			}

			@Override
			public int size() {
				return SimpleHashMap.this.size() ;
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractSet<V>() {
			@Override
			public Iterator<V> iterator() {
				Iterator<V> iterator = chainVal.iterator() ;
				if (iterator.hasNext()) iterator.next() ;
				return iterator ;
			}

			@Override
			public int size() {
				return SimpleHashMap.this.size() ;
			}
		};
	}

	private class MyKeyValIterator implements Iterator<java.util.Map.Entry<K, V>>{

		private int cursor = 1 ;
		private int size = SimpleHashMap.this.size() +1;
		
		@Override
		public boolean hasNext() {
			return cursor < size ;
		}

		@Override
		public java.util.Map.Entry<K, V> next() {
			final int idx = cursor ;

			cursor++ ;
			
			return new Entry<K, V>() {
				@Override
				public K getKey() {
					return chainKey.get(idx) ;
				}

				@Override
				public V getValue() {
					return chainVal.get(idx) ;
				}

				@Override
				public V setValue(V value) {
					return chainVal.set(idx, value) ;
				}
			};
		}
		
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {

		return new AbstractSet<java.util.Map.Entry<K, V>>() {
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new MyKeyValIterator() ;
			}

			@Override
			public int size() {
				return SimpleHashMap.this.size() ;
			}

		};
	}
	
	
	
}

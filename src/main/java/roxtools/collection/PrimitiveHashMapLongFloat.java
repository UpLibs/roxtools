package roxtools.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of SimpleHashMap for primitives Long (key) and Float (value).
 * 
 * @author gracilianomp@gmail.com
 *
 */
final public class PrimitiveHashMapLongFloat implements Map<Long, Float>{

	private static final int MASK_REMOVE_NEGATIVE_SIGN = 0x7FFFFFFF;
	
	static private int groupIndex(int objHashcode, int totalGroups) {
		return (objHashcode & MASK_REMOVE_NEGATIVE_SIGN) % totalGroups ;
	}
	
	/////////////////////////////////////////////////////////////
	
	private int size = 0 ;
	
	final private DynamicArrayLong chainKey ;
	final private DynamicArrayFloat chainVal ;
	final private DynamicArrayInt chainHash ;
	final private DynamicArrayInt chainNext ;
	
	private int chainRemoved ;
	
	private int[] groups ;
	private int[] groupsSizes ;
	
	public PrimitiveHashMapLongFloat() {
		this.chainKey = new DynamicArrayLong() ;
		this.chainVal = new DynamicArrayFloat() ;
		this.chainHash = new DynamicArrayInt() ;
		this.chainNext = new DynamicArrayInt() ;
		
		skipFirstChainPos();
		
		this.groups = new int[8] ;
		this.groupsSizes = new int[groups.length] ;
	}
	
	private void skipFirstChainPos() {
		// Skip 1st position, so index 0 will be as null position, or end/stop index.
		this.chainKey.addLong(0);
		this.chainVal.addLong(0);
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
				Long k = chainKey.getLong(pos) ;
				if ( key.equals(k) ) {
					return true ;
				}
			}
			
			pos = chainNext.getInt(pos) ;
		}
		
		return false ;
	}
	
	public boolean containsKey(long key) {
		int objHash = Long.hashCode(key);
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				Long k = chainKey.getLong(pos) ;
				if ( key == k ) {
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
	public Float get(Object key) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				Long k = chainKey.getLong(pos) ;
				if ( key.equals(k) ) {
					return chainVal.get(pos) ;
				}
			}
			
			pos = chainNext.getInt(pos) ;
		}
		
		return null;
	}
	
	/**
	 * Primitive get.
	 * @param key The key to search.
	 * @return Value or null when not existent key.
	 */
	
	public Float getPrimitive(long key) {
		int objHash = Long.hashCode(key) ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				Long k = chainKey.getLong(pos) ;
				if ( key == k ) {
					return chainVal.getFloat(pos) ;
				}
			}
			
			pos = chainNext.getInt(pos) ;
		}
		
		return null ;
	}

	@Override
	public Float put(Long key, Float value) {
		return putPrimitive(key.longValue(), value.floatValue()) ;
	}
	
	public Float putPrimitive(long key, float value) {
		int objHash = Long.hashCode(key) ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int groupPos = groups[groupIdx] ;
		
		int cursor = groupPos ;
		
		while (cursor > 0) {
			int h = chainHash.getInt(cursor) ;
			
			if ( objHash == h ) {
				long k = chainKey.getLong(cursor) ;
				if ( key == k ) {
					chainKey.setLong(cursor, key) ;
					Float prev = chainVal.setFloat(cursor, value);
					return prev ;
				}
			}
			
			cursor = chainNext.getInt(cursor) ;
		}
		
		int newPos ;
		
		if (chainRemoved > 0) {
			newPos = chainRemoved ;
			int nextRemoved = chainNext.getInt(newPos) ;
			chainRemoved = (-nextRemoved)-1 ;
			
			chainNext.setInt(newPos, groupPos);
			chainHash.setInt(newPos, objHash);
			chainKey.setLong(newPos, key);
			chainVal.setFloat(newPos, value);
		}
		else {
			newPos = chainNext.size() ;
			
			chainNext.addInt(groupPos);
			chainHash.addInt(objHash);
			chainKey.addLong(key);
			chainVal.addFloat(value);
		}
		
		groups[groupIdx] = newPos ;
		groupsSizes[groupIdx]++ ;
		
		this.size++ ;
		
		checkRehashNeeded();
		
		return null;
	}

	@Override
	public Float remove(Object key) {
		int objHash = key.hashCode() ;
		int groupIdx = groupIndex(objHash, groups.length) ;
		
		int prevPos = 0 ;
		int pos = groups[groupIdx] ;
		
		while (pos > 0) {
			int h = chainHash.getInt(pos) ;
			
			if ( objHash == h ) {
				Long k = chainKey.getLong(pos) ;
				
				if ( key.equals(k) ) {
					chainKey.setLong(pos, 0) ;
					Float prev = chainVal.setFloat(pos, 0) ;
					
					int next = chainNext.getInt(pos) ;
					
					if (prevPos > 0) {
						chainNext.setInt(prevPos, next) ;
					}
					else {
						groups[groupIdx] = next ;
					}
					
					groupsSizes[groupIdx]-- ;
					
					this.size-- ;
					
					chainNext.setInt(pos, -(this.chainRemoved+1)) ;
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
			long removed = chainNext.getInt(i) ;
			if (removed < 0) continue ;
			
			Long key = chainKey.get(i) ;
			
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
	public void putAll(Map<? extends Long, ? extends Float> m) {
		for (java.util.Map.Entry<? extends Long, ? extends Float> entry : m.entrySet()) {
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
	public Set<Long> keySet() {
		return new AbstractSet<Long>() {
			@Override
			public Iterator<Long> iterator() {
				Iterator<Long> iterator = chainKey.iterator() ;
				if (iterator.hasNext()) iterator.next() ;
				return iterator ;
			}

			@Override
			public int size() {
				return PrimitiveHashMapLongFloat.this.size() ;
			}
		};
	}

	@Override
	public Collection<Float> values() {
		return new AbstractSet<Float>() {
			@Override
			public Iterator<Float> iterator() {
				Iterator<Float> iterator = chainVal.iterator() ;
				if (iterator.hasNext()) iterator.next() ;
				return iterator ;
			}

			@Override
			public int size() {
				return PrimitiveHashMapLongFloat.this.size() ;
			}
		};
	}

	private class MyKeyValIterator implements Iterator<java.util.Map.Entry<Long, Float>>{

		private int cursor = 1 ;
		private int size = PrimitiveHashMapLongFloat.this.size() +1;
		
		@Override
		public boolean hasNext() {
			return cursor < size ;
		}

		@Override
		public java.util.Map.Entry<Long, Float> next() {
			final int idx = cursor ;

			cursor++ ;
			
			return new Entry<Long, Float>() {
				@Override
				public Long getKey() {
					return chainKey.get(idx) ;
				}

				@Override
				public Float getValue() {
					return chainVal.get(idx) ;
				}

				@Override
				public Float setValue(Float value) {
					return chainVal.set(idx, value) ;
				}
			};
		}
		
	}
	
	@Override
	public Set<java.util.Map.Entry<Long, Float>> entrySet() {

		return new AbstractSet<java.util.Map.Entry<Long, Float>>() {
			@Override
			public Iterator<java.util.Map.Entry<Long, Float>> iterator() {
				return new MyKeyValIterator() ;
			}

			@Override
			public int size() {
				return PrimitiveHashMapLongFloat.this.size() ;
			}

		};
	}
	
	
	
}

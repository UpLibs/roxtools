package roxtools;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Memoize<T> {
	
	static public class MemKey {
		private String[] keyParts ;
		
		public MemKey(String... keyParts) {
			this.keyParts = keyParts ;
		}

		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) {
				final int prime = 31;
				int result = 1;
				result = prime * result + Arrays.hashCode(keyParts);
				hashcode = result ;
			}
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKey other = (MemKey) obj;
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}
	
	static public class MemKeyObjs extends MemKey {
		static private String[] toString(Object[] objs) {
			String[] strs = new String[objs.length] ;
			for (int i = 0; i < strs.length; i++) {
				strs[i] = objs[i].toString() ;
			}
			return strs ;
		}
		
		public MemKeyObjs(Object... objs) {
			super(toString(objs)) ;
		}
	}
	
	static private class MemoryReference<T> extends SoftReference<T> {
		private MemoryReference(T referent) {
			super(referent);
		}
		
		private long time = System.currentTimeMillis() ;
		
		public long getTime() {
			return time;
		}
		
		public void updateTime() {
			time = System.currentTimeMillis() ;
		}
	}
	
	////////////////////////////////////////////////////
	
	public Memoize() {
		this(0) ;
	}
	
	public Memoize(long memoryTimeout) {
		this.memoryTimeout = memoryTimeout ;
	}
	
	private long memoryTimeout ;
	
	public void setMemoryTimeout(long memoryTimeout) {
		this.memoryTimeout = memoryTimeout;
	}
	
	public long getMemoryTimeout() {
		return memoryTimeout;
	}
	
	private HashMap<MemKey, MemoryReference<T>> memories = new HashMap<Memoize.MemKey, MemoryReference<T>>() ;
	
	public void put(MemKey key, T val) {
		synchronized (memories) {
			memories.put(key, new MemoryReference<T>(val)) ;
		}
	}
	
	public T get(MemKey key) {
		return getImplem(key, false, false) ;
	}
	
	public T getUpdateMemoryTime(MemKey key) {
		return getImplem(key, true, false) ;
	}
	
	public T getIgnoreTimeout(MemKey key) {
		return getImplem(key, false, true) ;
	}
	
	private T getImplem(MemKey key, boolean updateTime, boolean ignoreTimeout) {
		synchronized (memories) {
			MemoryReference<T> ref = memories.get(key) ;
			
			if (ref != null) {
				if (!ignoreTimeout && memoryTimeout > 0 && System.currentTimeMillis() - ref.getTime() > memoryTimeout) {
					memories.remove(key) ;
					return null ;
				}
				else {
					T val = ref.get() ;
					
					if (val == null) {
						memories.remove(key) ;	
						return null ;
					}
					else {
						if (updateTime) ref.updateTime();
						return val ;
					}
				}
			}
			else {
				return null ;
			}
		}
	}
	
	public boolean contains(MemKey key) {
		synchronized (memories) {
			return get(key) != null ;
		}
	}
	
	public T remove(MemKey key) {
		synchronized (memories) {
			SoftReference<T> ref = memories.remove(key) ;
			return ref != null ? ref.get() : null ;
		}
	}
	
	public void clear() {
		synchronized (memories) {
			memories.clear();
		}
	}

	public int size() {
		synchronized (memories) {
			return memories.size();
		}
	}

	public boolean isEmpty() {
		synchronized (memories) {
			return memories.isEmpty();
		}
	}

	public Set<MemKey> keySet() {
		synchronized (memories) {
			return memories.keySet();
		}
	}
	
	
	

}

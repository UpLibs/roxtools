package roxtools;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Memoize<T> {
	
	static public class MemKeyInt {
		
		final private int[] keyParts ;
		
		public MemKeyInt(int... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public int[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKeyInt other = (MemKeyInt) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}
	
	static public class MemKeyByte {
		
		final private byte[] keyParts ;
		
		public MemKeyByte(byte... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public byte[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKeyByte other = (MemKeyByte) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}
	
	static public class MemKeyLong {
		
		final private long[] keyParts ;
		
		public MemKeyLong(long... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public long[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKeyLong other = (MemKeyLong) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}

	static public class MemKeyFloat {
		
		final private float[] keyParts ;
		
		public MemKeyFloat(float... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public float[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKeyFloat other = (MemKeyFloat) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}


	static public class MemKeyDouble {
				
		final private double[] keyParts ;
		
		public MemKeyDouble(double... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public double[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKeyDouble other = (MemKeyDouble) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
		}
	}
	
	static public class MemKey {
				
		final private Object[] keyParts ;
		
		public MemKey(Object... keyParts) {
			this.keyParts = keyParts ;
		}
		
		public Object[] getKeyParts() {
			return keyParts;
		}
		
		private int hashcode = 0 ;
		@Override
		public int hashCode() {
			if (hashcode == 0) hashcode = Arrays.hashCode(keyParts) ;
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			
			if (getClass() != obj.getClass()) return false;
			
			MemKey other = (MemKey) obj;
			
			if ( this.hashCode() != other.hashCode() ) return false;
			
			if (!Arrays.equals(keyParts, other.keyParts)) return false;
			return true;
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
	
	private HashMap<Object, MemoryReference<T>> memories = new HashMap<Object, MemoryReference<T>>() ;
	
	public void put(Object key, T val) {
		synchronized (memories) {
			memories.put(key, new MemoryReference<T>(val)) ;
		}
	}
	
	public T get(Object key) {
		return getImplem(key, false, false) ;
	}
	
	public T getUpdateMemoryTime(Object key) {
		return getImplem(key, true, false) ;
	}
	
	public T getIgnoreTimeout(Object key) {
		return getImplem(key, false, true) ;
	}
	
	private T getImplem(Object key, boolean updateTime, boolean ignoreTimeout) {
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

	public Set<Object> keySet() {
		synchronized (memories) {
			return memories.keySet();
		}
	}
	
	
	

}

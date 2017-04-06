package roxtools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;

public class DynamicMutexHandler {
	
	static public interface DynamicMutex extends Comparable<DynamicMutex> {
		public String getID() ;
		
		public boolean isCurrentThreadLocking() ;
		public boolean isSomeThreadLocking() ;
		
		public boolean lock() ;
		public boolean unlock() ;
		
		public int getPhase() ;
		public int setPhase(int phase) ;
		public void waitPhase(int desiredPhase) ;
		public boolean waitPhase(int desiredPhase, long timeout) ;
	}
	
	static private class SimpleMutex implements DynamicMutex {
		final private String id ;

		private SimpleMutex(String id) {
			this.id = id;
		}
		
		@Override
		public String getID() {
			return id ;
		}
		
		private volatile Thread lockingThread ;
		private volatile int lockCount ;
		

		@Override
		public boolean isCurrentThreadLocking() {
			Thread currentThread = Thread.currentThread() ;
			
			synchronized (this) {
				if ( lockingThread == currentThread ) {
					assert(lockCount > 0) ;
					return true ;
				}
				else {
					return false ;
				}
			}
		}
		
		@Override
		public boolean isSomeThreadLocking() {
			synchronized (this) {
				if ( lockingThread != null ) {
					assert(lockCount > 0) ;
					return true ;
				}
				else {
					assert(lockCount == 0) ;
					return false ;
				}
			}
		}
		
		@Override
		public boolean lock() {
			Thread currentThread = Thread.currentThread() ;
			
			synchronized (this) {
				if ( lockingThread == currentThread ) {
					lockCount++ ;
					return false ;
				}
				
				while ( lockingThread != null ) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
				
				lockingThread = currentThread ;
				lockCount = 1 ;
				
				this.notifyAll();
				return true ;
			}
		}
		
		@Override
		public boolean unlock() {
			Thread currentThread = Thread.currentThread() ;
			
			synchronized (this) {
				if ( lockingThread == currentThread ) {
					lockCount-- ;
					
					if (lockCount <= 0) {
						lockingThread = null ;
						lockCount = 0 ;
						this.notifyAll();
					}
					
					return true ;
				}
				else {
					return false ;	
				}
			}
		}
		
		private volatile int phase ;
		
		@Override
		public int getPhase() {
			synchronized (this) {
				return this.phase ;
			}
		}
		
		@Override
		public int setPhase(int phase) {
			synchronized (this) {
				int prev = this.phase ;
				this.phase = phase ;
				this.notifyAll();
				return prev ;
			}
		}
		
		@Override
		public void waitPhase(int desiredPhase) {
			synchronized (this) {
				while (this.phase != desiredPhase) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}
		}
		
		@Override
		public boolean waitPhase(int desiredPhase, long timeout) {
			long init = System.currentTimeMillis() ;
			
			synchronized (this) {
				while (this.phase != desiredPhase) {
					long elapsedTime = System.currentTimeMillis() - init ;
					long timeRemaining = timeout-elapsedTime ;
					
					if (timeRemaining <= 0) return this.phase == desiredPhase ;
							
					try {
						this.wait(timeRemaining);
					} catch (InterruptedException e) {}
				}
				
				return this.phase == desiredPhase ;
			}
		}
		
		@Override
		public int compareTo(DynamicMutex o) {
			if ( o instanceof SimpleMutex ) {
				return this.id.compareTo(o.getID()) ;	
			}
			else {
				return 1 ;
			}
		}
	}
	
	static private class MultiMutex implements DynamicMutex {

		private final SimpleMutex[] mutexes ;
		
		private MultiMutex(SimpleMutex[] mutexes) {
			assert(mutexes.length > 1) ;
			this.mutexes = mutexes;
		}
		
		@Override
		public String getID() {
			StringBuilder str = new StringBuilder() ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				str.append( simpleMutex.getID() ) ;
				str.append(";") ;
			}
			
			return str.toString() ;
		}
		
		@Override
		public boolean isCurrentThreadLocking() {
			boolean allOk = true ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				boolean ok = simpleMutex.isCurrentThreadLocking();
				allOk = allOk && ok ;
			}
			
			return allOk;
		}
		
		@Override
		public boolean isSomeThreadLocking() {
			boolean allOk = true ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				boolean ok = simpleMutex.isSomeThreadLocking();
				allOk = allOk && ok ;
			}
			
			return allOk;
		}

		@Override
		public boolean lock() {
			boolean allOk = true ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				boolean ok = simpleMutex.lock();
				
				allOk = allOk && ok ;
			}
			
			return allOk;
		}

		@Override
		public boolean unlock() {
			boolean allOk = true ;
			
			for (int i = mutexes.length-1; i >= 0; i--) {
				SimpleMutex mutex = mutexes[i] ;
				boolean ok = mutex.unlock();
				
				allOk = allOk && ok ;
			}
			
			return allOk ;
		}
		
		@Override
		public int getPhase() {
			int phase = mutexes[0].getPhase() ;
			
			for (int i = 1; i < mutexes.length; i++) {
				SimpleMutex mutex = mutexes[i];
				int p = mutex.getPhase() ;
				
				if (p != phase) {
					return 0 ;
				}
			}
			
			return phase ;
		}
		
		@Override
		public int setPhase(int phase) {
			int prev = getPhase() ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				simpleMutex.setPhase(phase) ;
			}
			
			return prev ;
		}
		
		@Override
		public void waitPhase(int desiredPhase) {
			for (SimpleMutex simpleMutex : mutexes) {
				simpleMutex.waitPhase(desiredPhase);
			}
		}
		
		@Override
		public boolean waitPhase(int desiredPhase, long timeout) {
			long init = System.currentTimeMillis() ;

			boolean allOk = true ;
			
			for (SimpleMutex simpleMutex : mutexes) {
				long elapsedTime = System.currentTimeMillis() - init ;
				long timeRemaining = timeout-elapsedTime ;
				
				boolean ok = simpleMutex.waitPhase(desiredPhase, timeRemaining);
				if (!ok) {
					return false ;
				}
				
				allOk = allOk && ok ;
			}
			
			return allOk ;
		}

		@Override
		public int compareTo(DynamicMutex o) {
			if ( o instanceof MultiMutex ) {
				return this.getID().compareTo(o.getID());	
			}
			else {
				return -1 ;
			}
		}

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public DynamicMutex getMutex(String id) {
		return getMutexImplem(id) ;
	}
	
	final private ReferenceQueue<SimpleMutex> referenceQueueMutex = new ReferenceQueue<>() ;
	final private HashMap<String, MyReference<String,SimpleMutex>> mutexes = new HashMap<>() ;
	
	private SimpleMutex getMutexImplem(String id) {
		if (id == null) id = "" ;
		
		synchronized (mutexes) {
			MyReference<String, SimpleMutex> ref = mutexes.get(id) ;
			if (ref != null) {
				SimpleMutex mutex = ref.get() ;
				if (mutex != null) {
					return mutex ;
				}
				else {
					cleanReferences(mutexes, referenceQueueMutex);
				}
			}
			
			SimpleMutex mutex = new SimpleMutex(id) ;
			
			mutexes.put(id, new MyReference<String, SimpleMutex>(id, mutex, referenceQueueMutex)) ;
			
			return mutex ;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public DynamicMutex getMultiMutex(String... ids) {
		return getMultiMutexImplem(ids) ;
	}
	
	static public String[] uniqueIDs(String[] ids) {
		Arrays.sort(ids);
		
		int sz = ids.length ;
		
		for (int i = 1; i < sz; i++) {
			String idPrev = ids[i-1] ;
			String id = ids[i] ;
			
			if ( id.equals(idPrev) ) {
				String[] unique = new String[sz-1] ;
				int uniqueSz = 0 ;
				
				for (int j = 0; j < i; j++) {
					id = ids[j] ;
					unique[uniqueSz++] = id ;	
				}
				
				for (int j = i+1; j < sz; j++) {
					idPrev = ids[j-1] ;
					id = ids[j] ;
					
					if (!id.equals(idPrev)) {
						unique[uniqueSz++] = id ;	
					}
				}
				
				if ( uniqueSz < unique.length ) {
					String[] unique2 = new String[uniqueSz];
					System.arraycopy(unique, 0, unique2, 0, uniqueSz);
					return unique2 ;
				}
				else {
					return unique ;
				}
				
			}
		}
		
		return ids ;
	}

	static private class MultiIDs {
		final private String[] ids ;

		protected MultiIDs(String[] ids) {
			this.ids = ids ;
		}

		private int hashcode ;
		
		@Override
		public int hashCode() {
			if (hashcode == 0) {
				hashcode = Arrays.hashCode(ids) ;	
			}
			return hashcode ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			
			MultiIDs other = (MultiIDs) obj;
			if (!Arrays.equals(ids, other.ids)) return false;
			return true;
		}
		
	}

	final private ReferenceQueue<MultiMutex> referenceQueueMultiMutex = new ReferenceQueue<>() ;
	final private HashMap<MultiIDs, MyReference<MultiIDs, MultiMutex>> multiMutexes = new HashMap<>() ;
	
	private DynamicMutex getMultiMutexImplem(String... ids) {
		if (ids == null || ids.length == 0) return getMutexImplem("") ;
		
		ids = uniqueIDs(ids) ;
		
		int idsSz = ids.length ;
		
		if (idsSz == 1) return getMutex( ids[0] ) ;
		
		MultiIDs key = new MultiIDs(ids) ;
		
		synchronized (multiMutexes) {
			MyReference<MultiIDs, MultiMutex> ref = multiMutexes.get(key) ;
			if (ref != null) {
				MultiMutex multiMutex = ref.get();
				if (multiMutex != null) {
					return multiMutex ;
				}
				else {
					cleanReferences(multiMutexes, referenceQueueMultiMutex);
				}
			}
			
			SimpleMutex[] mutexes = new SimpleMutex[idsSz] ;
			
			for (int i = 0; i < idsSz; i++) {
				mutexes[i] = getMutexImplem( ids[i] ) ;
			}
			
			MultiMutex multiMutex = new MultiMutex(mutexes) ;
			
			multiMutexes.put(key, new MyReference<MultiIDs, MultiMutex>(key, multiMutex, referenceQueueMultiMutex)) ;
			
			return multiMutex ;
		}
	}
	
	static private class MyReference<I, M extends DynamicMutex> extends SoftReference<M> {

		final private I id ;
		
		public MyReference(I id, M referent, ReferenceQueue<M> q) {
			super(referent, q);
			this.id = id ;
		}
		
		public I getID() {
			return id ;
		}
		
	}
	
	private <I,M extends DynamicMutex, R extends MyReference<I, M>> void cleanReferences( HashMap<I, R> mutexes , ReferenceQueue<M> referenceQueue ) {
		
		synchronized (mutexes) {
			
			while (true) {
				@SuppressWarnings("unchecked")
				R ref = (R) referenceQueue.poll() ;
				
				if (ref == null) return ;
				
				I id = ref.getID() ;
			
				mutexes.remove(id) ;
			}
			
		}
		
	}
	
}

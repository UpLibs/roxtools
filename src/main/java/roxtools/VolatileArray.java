package roxtools;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

final public class VolatileArray<E> {
	
	final private VolatileArrayInstantiator<E> instantiator ;
	
	final private Object MUTEX = new Object() ;
	
	private Reference<E>[] elements ;
	
	public VolatileArray(VolatileArrayInstantiator<E> instantiator) {
		this(instantiator, 100) ;
	}
	
	@SuppressWarnings("unchecked")
	public VolatileArray(VolatileArrayInstantiator<E> instantiator, int initialCapacity) {
		if (initialCapacity < 100) initialCapacity = 100 ;
		this.instantiator = instantiator;
		this.elements = new SoftReference[initialCapacity] ;
	}
	
	private void ensureCapacity(int index) {
		synchronized (MUTEX) {
			int totalInstances = instantiator.totalInstances() ;
			
			if (index >= totalInstances) throw new IndexOutOfBoundsException("index out of range: "+ index +" / "+ totalInstances) ;
			
			if (index < this.elements.length) return ;
			
			int newSize = index+1 ;
			newSize = ((newSize / 100) + 1) * 100 ;
			
			if (newSize > totalInstances) newSize = totalInstances ;
			
			this.elements = Arrays.copyOf(this.elements, newSize) ;	
		}
	}
	
	public E release(int index) {
		Reference<E>[] elements = this.elements ;
		
		if ( elements.length <= index ) return null ;
		
		Reference<E> ref = elements[index] ;
		
		if (ref == null) return null ;
		
		E elem = ref.get() ;
		
		if (elem != null) {
			this.elements[index] = new WeakReference<E>(elem) ;
		}
		
		return elem ;
	}
	
	@SuppressWarnings("unchecked")
	public void releaseAll() {
		synchronized (MUTEX) {
			this.elements = new SoftReference[100] ;
		}
	}
	
	public boolean isLoaded(int index) {
		return getIfLoaded(index) != null ;
	}
	
	public E getIfLoaded(int index) {
		Reference<E>[] elements = this.elements ;
		
		if ( elements.length <= index ) return null ;
		
		Reference<E> ref = elements[index] ;
		
		if (ref == null) return null ;
		
		E elem = ref.get() ;
		return elem ;
	}
	
	public E get(int index) {
		ensureCapacity(index) ;
		Reference<E>[] elements = this.elements ;
		
		Reference<E> ref = elements[index] ;
		
		if (ref == null) {
			E elem = loadInstance(index) ;
			elements[index] = new SoftReference<E>(elem) ;
			return elem ;
		}
		
		E elem = ref.get() ;
		
		if (elem == null) {
			elem = loadInstance(index) ;
			elements[index] = new SoftReference<E>(elem) ;
			return elem ;
		}
		
		return elem ;
	}
	
	public E getNoHold(int index) {
		E elem = getIfLoaded(index) ;
		if (elem != null) return elem ;
		
		elem = loadInstance(index) ;
		return elem ;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private E loadInstance(int index) {
		LoadMutex<E> loadMutex = loadMutex_catch(index) ;
		
		E elem ;
		synchronized (loadMutex) {
			if (loadMutex.element != null) return loadMutex.element ;
			elem = instantiator.instantiate(index) ;
			loadMutex.element = elem ;
		}
		
		loadMutex_release(index, loadMutex) ;
		
		return elem ;
	}
	
	@SuppressWarnings("unchecked")
	private LoadMutex<E>[] loadMutexes = new LoadMutex[10] ;
	
	private void loadMutex_ensureCapacity(int index) {
		synchronized (MUTEX) {
			int totalInstances = instantiator.totalInstances() ;
			
			if (index >= totalInstances) throw new IndexOutOfBoundsException("index out of range: "+ index +" / "+ totalInstances) ;
			
			if (index < this.loadMutexes.length) return ;
			
			int newSize = index+1 ;
			newSize = ((newSize / 100) + 1) * 100 ;
			
			if (newSize > totalInstances) newSize = totalInstances ;
			
			this.loadMutexes = Arrays.copyOf(this.loadMutexes, newSize) ;
		}
	}
	
	static private class LoadMutex<E> {
		volatile public int catchOunt = 0 ;
		volatile public E element ;
	}
	
	private LoadMutex<E> loadMutex_catch(int index) {
		synchronized (MUTEX) {
			loadMutex_ensureCapacity(index) ;
			LoadMutex<E>[] preLoadMutexes = this.loadMutexes ;
			
			LoadMutex<E> mutex = preLoadMutexes[index] ;
			
			if (mutex == null) preLoadMutexes[index] = mutex = new LoadMutex<E>() ;
			
			mutex.catchOunt++ ;
			
			return mutex ;
		}
	}
	
	private void loadMutex_release(int index, LoadMutex<E> mutex) {
		synchronized (MUTEX) {
			loadMutex_ensureCapacity(index) ;
			LoadMutex<E>[] preLoadMutexes = this.loadMutexes ;
			
			LoadMutex<E> prevMutex = preLoadMutexes[index] ;
			
			if (prevMutex != mutex) throw new IllegalStateException() ;
			
			prevMutex.catchOunt-- ;
			
			if (prevMutex.catchOunt == 0) {
				preLoadMutexes[index] = null ;
			}
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void preLoadAsync(final int initIndex, final int endIndex) {
		
		ExecutorService threadPool = AsyncTask.getDefaultThreadPool() ;
		
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				preLoad(initIndex, endIndex) ;
			}
		}) ;
		
	}
	
	public void preLoadMultiThreadAsync(final int initIndex, final int endIndex, final int maxThreads) {
		
		ExecutorService threadPool = AsyncTask.getDefaultThreadPool() ;
		
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				preLoadMultiThread(initIndex, endIndex, maxThreads) ;
			}
		}) ;
		
	}
	
	public void preLoad(int initIndex, int endIndex) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		ensureCapacity(endIndex) ;
		
		for (int i = initIndex; i <= endIndex; i++) {
			Reference<E> ref = this.elements[i] ;
			if (ref != null && ref.get() != null) continue ;
	
			E elem = loadInstance(i) ;
			this.elements[i] = new SoftReference<E>(elem) ;
		}
	}
	
	public void preLoadMultiThread(int initIndex, int endIndex, int maxThreads) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		int totalPreLoad = (endIndex - initIndex) +1 ; 
		
		if (totalPreLoad < 1) {
			return ;
		}
		else if (totalPreLoad == 1) {
			ensureCapacity(initIndex) ;
			
			Reference<E> ref = this.elements[initIndex] ;
			if (ref != null && ref.get() != null) return ;
	
			E elem = loadInstance(initIndex) ;
			this.elements[initIndex] = new SoftReference<E>(elem) ;
			
			return ;
		}
		
		if (maxThreads < 2) maxThreads = 2 ;
		else if (maxThreads > totalPreLoad) maxThreads = totalPreLoad ;
		
		final Semaphore semaphore = new Semaphore(maxThreads+1) ;
		ExecutorService threadPool = AsyncTask.getDefaultThreadPool() ;
		
		ensureCapacity(endIndex);
		
		for (int i = initIndex; i <= endIndex; i++) {
			Reference<E> ref = this.elements[i] ;
			if (ref != null && ref.get() != null) continue ;
			
			final int iF = i ;
			
			AsyncTask<Object> task = new AsyncTask<Object>() {
				@Override
				public void run() {
					E elem = loadInstance(iF) ;
					VolatileArray.this.elements[iF] = new SoftReference<E>(elem) ;
					
					semaphore.release() ;
					notifyFinished() ;
				}
			};
			
			try {
				semaphore.acquire(2) ;
			} catch (InterruptedException e) {}
			
			threadPool.execute(task) ;
		
			semaphore.release() ;
		}
	}
	
	public int size() {
		return instantiator.totalInstances() ;
	}

	public E[] toArray(int initIndex, int endIndex) {
		return toArray(initIndex, endIndex, null) ;
	}
	
	@SuppressWarnings("unchecked")
	public E[] toArray(int initIndex, int endIndex, E[] a) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		int size = (endIndex-initIndex)+1 ;
		
		E[] array ;
		if ( a == null || a.length < size ) {
			Class<?> eClass = a != null ? a.getClass().getComponentType() : get(initIndex).getClass() ;
			array = (E[]) Array.newInstance(eClass, size);	
		}
		else {
			array = a ;
		}
		
		int arraySz = 0 ;
		
		for (int i = initIndex; i <= endIndex; i++) {
			E e = get(i) ;
			array[arraySz++] = e ;
		}
		
		return array ;
	}
	
	static abstract public class Iteration<E> {
		
		abstract public void iterate(E elem, int index) ;
		
	}
	
	static abstract public class IterationMultiValue<E> {
		
		abstract public void iterateMulti(E[] elems, int initIndex) ;
		
	}
	
	public void iterate(int initIndex, int endIndex, int preLoadBlockSize, Iteration<E> iteration) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		int lastPreLoadIndex = initIndex+preLoadBlockSize ;
		preLoadMultiThreadAsync(initIndex, lastPreLoadIndex, 2) ;
		
		for (int i = initIndex; i <= endIndex; i++) {
			E e = get(i) ;
			iteration.iterate(e, i) ;
			
			if (i == lastPreLoadIndex) {
				int initPreLoad = i+1 ;
				lastPreLoadIndex = initPreLoad+preLoadBlockSize ;
				preLoadMultiThreadAsync(initPreLoad, lastPreLoadIndex, 2) ;
			}
		}
	}
	
	public void iterateMultiThread(int initIndex, int endIndex, int maxThreads, final Iteration<E> iteration) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		if (maxThreads < 2) maxThreads = 2 ;
		
		final Semaphore semaphore = new Semaphore(maxThreads+1) ;
		ExecutorService threadPool = AsyncTask.getDefaultThreadPool() ;
		
		@SuppressWarnings("unchecked")
		AsyncTask<Object>[] tasks = new AsyncTask[ (endIndex-initIndex)+1 ] ;
		int tasksSz = 0 ;
		
		for (int i = initIndex; i <= endIndex; i++) {
			final int iF = i ;
			
			AsyncTask<Object> task = new AsyncTask<Object>() {
				@Override
				public void run() {
					E e = get(iF) ;
					iteration.iterate(e, iF) ;
					
					semaphore.release() ;
					notifyFinished() ;
				}
			};
			
			try {
				semaphore.acquire(2) ;
			} catch (InterruptedException e) {}
			
			tasks[tasksSz++] = task ;
			threadPool.execute(task) ;
		
			semaphore.release() ;
		}
		
		AsyncTask.waitTasks(tasks) ;
		
	}
	
	public void iterateMultiValue(int initIndex, int endIndex, int multiValueBlock, int maxThreads, final IterationMultiValue<E> iteration) {
		int totalInstances = instantiator.totalInstances() ;
		if (endIndex >= totalInstances || endIndex < 0) endIndex = totalInstances-1 ;
		
		if (maxThreads < 2) maxThreads = 2 ;
		
		final Class<?> elemClass ;
		{
			E e0 = get(initIndex) ;
			elemClass = e0.getClass() ;
		}
		
		final Semaphore semaphore = new Semaphore(maxThreads+1) ;
		ExecutorService threadPool = AsyncTask.getDefaultThreadPool() ;
		
		int totalIterations = (endIndex-initIndex)+1 ;
		
		if (multiValueBlock < 0) {
			multiValueBlock = Math.min( -multiValueBlock , 2 ) ;
			int idealSz = (totalIterations / maxThreads) / 2 ;
			
			if ( idealSz < multiValueBlock && idealSz >= 2 && idealSz >= multiValueBlock/2 ) {
				multiValueBlock = idealSz ;
			}
		}
		
		int multiValueBlockMin1 = multiValueBlock-1 ;
		
		@SuppressWarnings("unchecked")
		AsyncTask<Object>[] tasks = new AsyncTask[ (totalIterations/multiValueBlock)+1 ] ;
		int tasksSz = 0 ;
		
		for (int i = initIndex; i <= endIndex; i+= multiValueBlock) {
			final int init = i ;
			int end = init+multiValueBlockMin1 ;
			if (end > endIndex) end = endIndex ;
			
			final int endF = end ;
			final int lng = (end-init) +1 ;
			
			AsyncTask<Object> task = new AsyncTask<Object>() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					E[] array = (E[]) Array.newInstance(elemClass, lng);
					int arraySz = 0 ;
					
					for (int j = init; j <= endF; j++) {
						array[arraySz++] = get(j) ;
					}
					
					iteration.iterateMulti(array, init) ;
					
					semaphore.release() ;
					
					notifyFinished() ;
				}
			};
			
			try {
				semaphore.acquire(2) ;
			} catch (InterruptedException e) {}
			
			tasks[tasksSz++] = task ;
			threadPool.execute(task) ;
			
			semaphore.release() ;
		}
		
		AsyncTask.waitTasks(tasks) ;
		
	}
	
}

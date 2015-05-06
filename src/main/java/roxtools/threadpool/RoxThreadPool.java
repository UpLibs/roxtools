package roxtools.threadpool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RoxThreadPool {
	
	static private class PoolQueue implements BlockingQueue<Runnable> {

		
		private int poolCapacity ;

		private ThreadPoolExecutor threadPoolExecutor ;
		
		private final ArrayDeque<Runnable> queue = new ArrayDeque<Runnable>() ;
		
		public PoolQueue(int poolCapacity) {
			this.poolCapacity = poolCapacity ;
		}
		
		public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
			synchronized (queue) {
				this.threadPoolExecutor = threadPoolExecutor;	
			}
		}
		
		@Override
		public Runnable remove() {
			synchronized (queue) {
				return queue.remove() ;
			}
		}

		@Override
		public Runnable poll() {
			synchronized (queue) {
				return queue.poll() ;
			}
		}

		@Override
		public Runnable element() {
			synchronized (queue) {
				return queue.element() ;
			}
		}

		@Override
		public Runnable peek() {
			synchronized (queue) {
				return queue.peek() ;
			}
		}

		@Override
		public int size() {
			synchronized (queue) {
				return queue.size() ;
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (queue) {
				return queue.isEmpty() ;
			}
		}

		@Override
		public Iterator<Runnable> iterator() {
			synchronized (queue) {
				return queue.iterator();
			}
		}

		@Override
		public Object[] toArray() {
			synchronized (queue) {
				return queue.toArray() ;
			}
		}

		@Override
		public <T> T[] toArray(T[] a) {
			synchronized (queue) {
				return queue.toArray(a) ;
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			synchronized (queue) {
				return queue.containsAll(c) ;
			}
		}

		@Override
		public boolean addAll(Collection<? extends Runnable> c) {
			synchronized (queue) {
				boolean ret = queue.addAll(c) ;
				if (waitingQueueCount > 0) queue.notifyAll() ;
				return ret ;
			}
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			synchronized (queue) {
				return queue.removeAll(c) ;
			}
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			synchronized (queue) {
				return queue.retainAll(c) ;
			}
		}

		@Override
		public void clear() {
			synchronized (queue) {
				queue.clear();
			}			
		}

		@Override
		public boolean add(Runnable e) {
			synchronized (queue) {
				boolean ret = queue.add(e) ;
				if (waitingQueueCount > 0) queue.notifyAll() ;
				return ret ;
			}
		}
		
		@Override
		public boolean offer(Runnable e) {
			synchronized (queue) {
				if ( threadPoolExecutor != null && threadPoolExecutor.getPoolSize() < threadPoolExecutor.getMaximumPoolSize() ) {
					return false ;
				}
				
				boolean ok = queue.offer(e);
				if (waitingQueueCount > 0) queue.notifyAll() ;
				return ok ;
			}
		}

		@Override
		public void put(Runnable e) throws InterruptedException {
			synchronized (queue) {
				queue.add(e) ;
				if (waitingQueueCount > 0) queue.notifyAll() ;
			}
		}

		private int waitingQueueCount = 0 ;
		
		private void waitQueueSize(int desiredMinimalSize, int desiredMaximalSize, long timeout, TimeUnit unit) throws InterruptedException {
			synchronized (queue) {
				long limitTime = System.currentTimeMillis() + unit.toMillis(timeout) ;
				
				while ( queue.size() < desiredMinimalSize || queue.size() > desiredMaximalSize ) {
					long time = limitTime - System.currentTimeMillis() ;
					if (time < 1) break ;
					
					try {
						waitingQueueCount++ ;
						
						queue.wait();	
					}
					finally {
						waitingQueueCount-- ;
					}
					
				}	
			}
		}
		
		@Override
		public boolean offer(Runnable e, long timeout, TimeUnit unit) throws InterruptedException {
			synchronized (queue) {
				if ( threadPoolExecutor != null && threadPoolExecutor.getPoolSize() < threadPoolExecutor.getMaximumPoolSize() ) {
					return false ;
				}
				
				boolean ok = queue.offer(e) ;
				if (waitingQueueCount > 0) queue.notifyAll() ;
				return ok ;
			}
		}

		@Override
		public Runnable take() throws InterruptedException {
			synchronized (queue) {
				while (queue.isEmpty()) {
					
					try {
						waitingQueueCount++ ;
						
						queue.wait();	
					}
					finally {
						waitingQueueCount-- ;
					}
					
				}
				return queue.remove();
			}
		}

		@Override
		public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
			synchronized (queue) {
				if (queue.isEmpty()) {
					waitQueueSize(1, Integer.MAX_VALUE, timeout, unit);
				}
				return queue.poll();
			}
		}

		@Override
		public int remainingCapacity() {
			synchronized (queue) {
				int rest = poolCapacity - queue.size() ;
				return rest > 0 ? rest : 1 ;
			}
		}

		@Override
		public boolean remove(Object o) {
			synchronized (queue) {
				return queue.remove(o) ;
			}
		}

		@Override
		public boolean contains(Object o) {
			synchronized (queue) {
				return queue.contains(o) ;
			}
		}

		@Override
		public int drainTo(Collection<? super Runnable> c) {
			synchronized (queue) {
				int sz = queue.size() ;
				c.addAll(queue) ;
				queue.clear();
				return sz ;
			}
		}

		@Override
		public int drainTo(Collection<? super Runnable> c, int maxElements) {
			synchronized (queue) {
				int limit = queue.size() ;
				if (limit > maxElements) limit = maxElements ;
				
				int added = 0 ;
				while ( !queue.isEmpty() && added < limit ) {
					Runnable elem = queue.remove() ;
					c.add(elem) ;
					added++ ;
				}
				
				return added ;
			}
		}
		
	}
	
	/////////////////////////////////////
	
	static public final int AVAILABLE_CPU_CORES = Runtime.getRuntime().availableProcessors() ;
	static public final int DEFAULT_THREAD_POOL_MAX_SIZE = Math.max( AVAILABLE_CPU_CORES*4 , 4 ) ;
	
	static public final int DEFAULT_THREAD_KEEP_ALIVE_SECONDS = 60 ;
	
	static public final ThreadPoolExecutor DEFAULT_THREAD_POOL = newThreadPool() ;
	static public final ThreadPoolExecutor THREAD_POOL_FOR_CPU_CORES = newThreadPool( Math.max( AVAILABLE_CPU_CORES+1 , 4 ) ) ;
	
	static public ThreadPoolExecutor newThreadPoolUnlimited() {
		return newThreadPool(0, Integer.MAX_VALUE, DEFAULT_THREAD_KEEP_ALIVE_SECONDS) ;
	}
	
	static public ThreadPoolExecutor newThreadPool() {
		return newThreadPool(0, DEFAULT_THREAD_POOL_MAX_SIZE, DEFAULT_THREAD_KEEP_ALIVE_SECONDS) ;
	}
	
	static public ThreadPoolExecutor newThreadPool(int maxThreads) {
		return newThreadPool(0, maxThreads, DEFAULT_THREAD_KEEP_ALIVE_SECONDS) ;
	}
	
	static public ThreadPoolExecutor newThreadPoolByThreadsPerCore(double threadsPerCore) {
		return newThreadPool(0, Math.max(1, (int) (RoxThreadPool.AVAILABLE_CPU_CORES * threadsPerCore)) , DEFAULT_THREAD_KEEP_ALIVE_SECONDS) ;
	}
	
	static public ThreadPoolExecutor newThreadPool(int minThreads, int maxThreads, int threadKeepAliveSeconds) {
		if (minThreads < 0) minThreads = 0 ;
		if (maxThreads < 1) throw new IllegalArgumentException("maxThreads < 1: "+ maxThreads) ;
		
		PoolQueue poolQueue = new PoolQueue(maxThreads) ;
		
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
				minThreads, maxThreads,
				threadKeepAliveSeconds, TimeUnit.SECONDS,
				poolQueue
                );
		
		poolQueue.setThreadPoolExecutor(threadPoolExecutor);
		
		threadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if ( executor.isShutdown() || executor.isTerminated() || executor.isTerminating() ) {
					throw new RejectedExecutionException("Task "+ r.toString() +" rejected from "+ executor.toString());
				}
				
				executor.execute(r);
			}
		});
		
		return threadPoolExecutor ;
	}
	
	static public List<RoxTask> newTasksList() {
		return new ArrayList<RoxTask>() ;
	}
	
	static public void executeTasks(List<? extends Runnable> tasks) {
		executeTasks(DEFAULT_THREAD_POOL, tasks);
	}
	
	static public void executeTasks(ExecutorService threadPool, List<? extends Runnable> tasks) {
		
		for (Runnable runnable : tasks) {
			threadPool.execute(runnable);
		}
		
	}
	
	static public <T extends RoxTask> void executeAndWaitTasks(List<T> tasks) {
		executeAndWaitTasks(DEFAULT_THREAD_POOL, tasks);
	}
	
	static public <T extends RoxTask> void executeAndWaitTasks(ExecutorService threadPool, List<T> tasks) {
		
		for (RoxTask task : tasks) {
			threadPool.execute(task);
		}
		
		for (RoxTask task : tasks) {
			task.waitFinished();
		}
		
	}
	
	static public <T extends RoxTask> void waitTasks(List<T> tasks) {
		
		for (RoxTask task : tasks) {
			task.waitFinished();
		}
		
	}
	
	static public <T extends Runnable> void executeTaskAndAddToList(List<T> tasks, T task) {
		executeTaskAndAddToList(DEFAULT_THREAD_POOL, tasks, task);
	}
	
	static public <T extends Runnable> void executeTaskAndAddToList(ExecutorService threadPool, List<T> tasks, T task) {
		threadPool.execute(task);
		synchronized (tasks) {
			tasks.add(task) ;
		}
	}
	
}

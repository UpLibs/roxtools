package roxtools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

abstract public class AsyncTask<R> implements Runnable {
	
	static final public int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors() ;
	
	static private ExecutorService defaultThreadPool ;
	
	static public ExecutorService getDefaultThreadPool() {
		
		if (defaultThreadPool == null) {
			synchronized (AsyncTask.class) {
				if (defaultThreadPool != null) return defaultThreadPool ;
				defaultThreadPool = createThreadPool() ;
				return defaultThreadPool ;
			}
		}
		
		return defaultThreadPool ;
	}
	
	static public ExecutorService createThreadPool() {
		//return new ThreadPoolExecutor(AVAILABLE_CORES, AVAILABLE_CORES * 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		return Executors.newCachedThreadPool() ;
	}
	
	static public void executeTasks( ExecutorService threadPool, AsyncTask<?>... tasks ) {
		
		for (int i = 0; i < tasks.length; i++) {
			threadPool.execute(tasks[i]) ;
		}
		
	}
	
	static public void executeTasksAndWaitFinished( ExecutorService threadPool, AsyncTask<?>... tasks ) {
		int tasksLength = tasks.length;
		
		//if ( tasks.length > 10 ) throw new IllegalStateException("tasks: "+ tasks.length) ; 
			
		if (tasksLength == 0) {
			return ;
		}
		else if (tasksLength == 1) {
			try {
				tasks[0].run() ;
			} catch (Throwable e) {
				e.printStackTrace() ;
			}
			return ;
		}
		
		for (int i = 1; i < tasksLength; i++) {
			threadPool.execute(tasks[i]) ;
		}
		
		try {
			tasks[0].run() ;
		} catch (Throwable e) {
			e.printStackTrace() ;
		}
		
		waitTasks( tasks ) ;
	}
	
	static public int getAvailableThreads( ExecutorService threadPool ) {
		if ( threadPool instanceof ThreadPoolExecutor ) {
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) threadPool ;
			
			int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize() ;
			
			int activeCount = threadPoolExecutor.getActiveCount() ;
			
			return maximumPoolSize - activeCount ;
		}
		
		return -1 ;
	}
	
	static public void waitTasks( AsyncTask<?>... tasks ) {
		
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].waitFinished() ;
		}
		
	}
	
	///////////////////////////////////////////////////
	
	volatile private R result ;
	
	public R getResult() {
		return result;
	}
	
	public void setResult(R result) {
		this.result = result;
	}
	
	volatile private boolean finished = false ;
	
	public boolean isFinished() {
		synchronized (this) {
			return finished;
		}
	}
	
	volatile private int waiting = 0 ;
	
	public void waitFinished() {
		synchronized (this) {
			while (!finished) {
				try {
					waiting++ ;
					this.wait() ;
				}
				catch (InterruptedException e) {}
				finally {
					waiting-- ;
				}
			}
		}
	}
	
	public void notifyFinished() {
		synchronized (this) {
			finished = true ;
			if (waiting > 0) this.notifyAll() ;
		}
	}
	
}
package roxtools.threadpool;

abstract public class RoxTask implements Runnable {
	
	static public class RunnableWrapper extends RoxTask {
		private Runnable runnable ;

		public RunnableWrapper(Runnable runnable) {
			this.runnable = runnable;
		}
		
		public Runnable getRunnable() {
			return runnable;
		}
		
		@Override
		public void task() {
			runnable.run();
		}
	}

	private Throwable error ;
	
	public Throwable getError() {
		synchronized (this) {
			return error;
		}
	}
	
	public boolean hasError() {
		synchronized (this) {
			return error != null ;	
		}
	}
	
	public void setError(Throwable e) {
		synchronized (this) {
			error = e ;
		}
	}
	
	private Object result ;
	
	public void setResult(Object result) {
		synchronized (this) {
			this.result = result;
		}
	}
	
	public Object getResult() {
		synchronized (this) {
			return this.result ;
		}
	}
	
	public Object waitResult() {
		synchronized (this) {
			waitFinished();
			return this.result ;
		}
	}
	
	@Override
	final public void run() {
		try {
			task();
		}
		catch (Throwable e) {
			setError(e);
		}
		finally {
			notifyFinished();
		}
	}
	
	abstract public void task() ;
	
	private volatile boolean finished = false ;
	
	public boolean isFinished() {
		synchronized (this) {
			return finished ;	
		}
	}
	
	private void notifyFinished() {
		synchronized (this) {
			finished = true ;
			this.notifyAll();
		}
	}
	
	public void waitFinished() {
		synchronized (this) {
			while (!finished) {
				try {
					this.wait();
				} catch (InterruptedException e) {}
			}
		}
	}
	
}

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
	
	private boolean printError = true ;
	
	public void setPrintError(boolean printError) {
		this.printError = printError;
	}
	
	public boolean getPrintError() {
		return printError;
	}
	
	public void setError(Throwable e) {
		synchronized (this) {
			error = e ;
		}
		
		if (printError && e != null) {
			e.printStackTrace(); 
		}
	}
	
	volatile private Object result ;
	
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
	
	public Object waitResult(long timeout) {
		synchronized (this) {
			waitFinished(timeout);
			return this.result ;
		}
	}
	
	private volatile long initTime ;
	private volatile long endTime ;
	
	public long getInitTime() {
		return initTime;
	}
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getExecutionTime() {
		return endTime - initTime ;
	}
	
	@Override
	final public void run() {
		initTime = System.currentTimeMillis() ;
		
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
			endTime = System.currentTimeMillis() ;
			
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
	
	public boolean waitFinished(long timeout) {
		synchronized (this) {
			long initTime = System.currentTimeMillis() ;
			
			while (!finished) {
				long time = System.currentTimeMillis() - initTime ;
				long timeLeft = timeout - time ;
				
				if (timeLeft < 1) return finished ;
				
				try {
					this.wait(timeLeft);
				} catch (InterruptedException e) {}
			}
			
			return finished ;
		}
	}
	
}

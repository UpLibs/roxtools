package roxtools.jobpool;

import java.util.List;



final public class JobResult<T> {
	
	@SuppressWarnings("rawtypes")
	static public void waitAllFinished(JobResult... results) {
		for (JobResult jobResult : results) {
			jobResult.waitFinished();
		}
	}
	
	@SuppressWarnings("rawtypes")
	static public void waitAllFinished(List<JobResult> results) {
		for (JobResult jobResult : results) {
			jobResult.waitFinished();
		}
	}
	
	@SuppressWarnings("rawtypes")
	static public void waitAllFinished(long timeout, JobResult... results) {
		for (JobResult jobResult : results) {
			jobResult.waitFinished(timeout);
		}
	}
	
	@SuppressWarnings("rawtypes")
	static public void waitAllFinished(long timeout, List<JobResult> results) {
		for (JobResult jobResult : results) {
			jobResult.waitFinished(timeout);
		}
	}
	
	/////////////////////////////////////////
	
	
	private T result ;
	private Throwable error ;
	private boolean resultSet = false ;
	private boolean dispatched = false ;
	
	protected JobResult() {
	}
	
	public JobResult(T result) {
		this.result = result ;
		this.resultSet = true ;
		this.dispatched = true ;
	}
	
	public JobResult(Throwable error) {
		this.error = error ;
		this.resultSet = true ;
		this.dispatched = true ;
	}
	
	protected void setDispatched() {
		synchronized (this) {
			this.dispatched = true ;
		}
	}
	
	public boolean isDispatched() {
		synchronized (this) {
			return dispatched ;
		}
	}
	
	public T getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}

	public void setResult(T result) {
		synchronized (this) {
			this.result = result ;
			this.resultSet = true ;
			this.notifyAll() ;
		}
	}
	
	public void setError(Throwable error) {
		synchronized (this) {
			this.error = error;
			this.resultSet = true ;
			this.notifyAll() ;
		}
	}
	
	public boolean isFinished() {
		synchronized (this) {
			return resultSet ;
		}
	}
	
	private void checkDispatched() {
		synchronized (this) {
			if (!dispatched) throw new IllegalStateException("Can't wait results of non dispatched job!") ;
		}
	}
	
	public T waitResult() {
		
		synchronized (this) {
			checkDispatched(); 
			
			while (!resultSet) {
				try {
					this.wait() ;
				} catch (InterruptedException e) {}
			}

			return result ;
		}
		
	}
	
	public T waitResult(long timeout) {
		
		long init = System.currentTimeMillis() ;
		
		synchronized (this) {
			checkDispatched();
			
			while (true) {
				if (result != null) return result ;
				
				long time = System.currentTimeMillis() - init ;
				long timeLeft = timeout - time ;
				if (timeLeft <= 0) return null ;
				
				try {
					this.wait(timeLeft) ;
				} catch (InterruptedException e) {}
			}
			
		}
		
	}
	

	public void waitFinished() {
		waitResult() ;
	}
	
	public void waitFinished(long timeout) {
		waitResult(timeout) ;
	}
	
	
	
}

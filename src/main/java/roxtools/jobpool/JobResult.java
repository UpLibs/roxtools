package roxtools.jobpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



final public class JobResult<T> implements Serializable {
	private static final long serialVersionUID = 5022181341681235801L;

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
	
	@SuppressWarnings("rawtypes")
	static public <T> List<T> grabResults(JobResult... results) {
		return grabResults(false, results) ;
	}
	
	@SuppressWarnings("rawtypes")
	static public <T> List<T> grabResults(boolean verbose, JobResult... results) {
		ArrayList<T> list = new ArrayList<T>() ;
		
		for (JobResult jobResult : results) {
			@SuppressWarnings("unchecked")
			T res = (T) jobResult.waitResult() ;
			list.add(res) ;
			
			if (verbose) System.out.println("-- JobResult.grabResults: "+ list.size() +" / "+ results.length);
		}
		
		return list ;
	}
	
	@SuppressWarnings("rawtypes")
	static public List<Throwable> grabErrors(JobResult... results) {
		return grabErrors(false, results) ;
	}
	
	@SuppressWarnings("rawtypes")
	static public List<Throwable> grabErrors(boolean verbose, JobResult... results) {
		ArrayList<Throwable> list = new ArrayList<Throwable>() ;
		
		for (JobResult jobResult : results) {
			jobResult.waitFinished();
			
			Throwable error = jobResult.getError() ;
			list.add(error) ;
			
			if (verbose) System.out.println("-- JobResult.grabErrors: "+ list.size() +" / "+ results.length);
		}
		
		return list ;
	}
	
	@SuppressWarnings("rawtypes")
	static public <T> List<T> grabResults(List<JobResult> results) {
		return grabResults(false, results) ;
	}
	
	@SuppressWarnings("rawtypes")
	static public <T> List<T> grabResults(boolean verbose, List<JobResult> results) {
		
		ArrayList<T> list = new ArrayList<T>() ;
		
		for (JobResult jobResult : results) {
			@SuppressWarnings("unchecked")
			T res = (T) jobResult.waitResult() ;
			list.add(res) ;
			
			if (verbose) System.out.println("-- JobResult.grabResults: "+ list.size() +" / "+ results.size());
		}
		
		return list ;
	}
	
	@SuppressWarnings("rawtypes")
	static public List<Throwable> grabErrors(List<JobResult> results) {
		return grabErrors(false, results) ;
	}
	
	@SuppressWarnings("rawtypes")
	static public List<Throwable> grabErrors(boolean verbose, List<JobResult> results) {
		ArrayList<Throwable> list = new ArrayList<Throwable>() ;
		
		for (JobResult jobResult : results) {
			jobResult.waitFinished();
			
			Throwable error = jobResult.getError() ;
			list.add(error) ;
			
			if (verbose) System.out.println("-- JobResult.grabErrors: "+ list.size() +" / "+ results.size());
		}
		
		return list ;
	}
	
	/////////////////////////////////////////
	
	
	volatile private T result ;
	volatile private Throwable error ;
	volatile private boolean resultSet = false ;
	volatile private boolean dispatched = false ;
	
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

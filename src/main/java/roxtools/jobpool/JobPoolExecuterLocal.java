package roxtools.jobpool;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;

import roxtools.threadpool.RoxThreadPool;

public class JobPoolExecuterLocal implements JobPoolExecutor {

	private final ExecutorService threadPool ;
	
	public JobPoolExecuterLocal() {
		this( RoxThreadPool.newThreadPool() ) ;
	}
	
	public JobPoolExecuterLocal(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}
	
	final static private class Task implements Runnable {
		final private JobCall call ;
		
		public Task(JobCall call) {
			this.call = call;
		}
		
		@Override
		public void run() {
			try {
				Object res = call.invokeLocal() ;
				call.setResult(res);
			}
			catch (IllegalAccessException | IllegalArgumentException e) {
				e.printStackTrace() ;
				call.setResult(e);
			}
			catch (InvocationTargetException e) {
				e.printStackTrace() ;
				call.setResult(e.getCause());
			}
		}
		
	}
	
	@Override
	public void executeJobs(JobCall[] calls) {
		
		for (int i = 0; i < calls.length; i++) {
			JobCall call = calls[i];
			
			Task task = new Task(call) ;
			threadPool.execute(task);
		}
	
	}
	
}

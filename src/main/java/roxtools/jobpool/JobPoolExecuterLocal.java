package roxtools.jobpool;

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
	
	@Override
	public void executeJobs(JobCall[] calls) {
		
		for (int i = 0; i < calls.length; i++) {
			JobCall call = calls[i];
			threadPool.execute(call);
		}
	
	}
	
}

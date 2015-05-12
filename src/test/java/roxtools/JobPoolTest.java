package roxtools;

import static org.junit.Assert.* ;

import org.junit.Test;

import roxtools.jobpool.JobPool;
import roxtools.jobpool.JobPoolExecuterLocal;
import roxtools.jobpool.JobResult;

public class JobPoolTest {
	
	static public class TestJob {
		
		static public String calc(int n1, int n2) {
			int m = n1 * n2 ;
			return n1+"+"+n2+"="+m ;
		}
		
		public String testCall(int n1, int n2) {
			return calc(n1, n2) ;
		}
		
		public JobResult<String> testCallJobResult(int n1, int n2) {
			return new JobResult<String>( calc(n1, n2) ) ;
		}
		
	}
	
	@Test
	public void testBasic() {
		
		JobPool jobPool = new JobPool( new JobPoolExecuterLocal() ) ;
		
		TestJob job1 = jobPool.newJob(TestJob.class) ;
		
		assertNotNull(job1);
		
		String res0 = job1.testCall(11, 22) ;
		
		assertNull(res0);
		
		JobResult<String> jobResult1 = job1.testCallJobResult(33,44) ;
		
		assertNotNull(jobResult1);
		
		assertFalse( jobResult1.isFinished() );
		assertFalse( jobResult1.isDispatched() );
		
		JobResult<?>[] jobs = jobPool.executeJobs() ;
		
		assertTrue( jobs.length == 2 );
		
		assertTrue( jobs[1] == jobResult1 );
		
		JobResult.waitAllFinished(jobs);
		
		assertEquals( jobs[0].getResult() , TestJob.calc(11, 22) );
		assertEquals( jobs[1].getResult() , TestJob.calc(33, 44) );
		
	}

}

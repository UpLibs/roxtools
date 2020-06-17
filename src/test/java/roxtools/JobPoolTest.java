package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.jobpool.JobPool;
import roxtools.jobpool.JobPoolExecuterLocal;
import roxtools.jobpool.JobResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JobPoolTest {

    static public class TestJob {

        static public String calc(int n1, int n2) {
            var m = n1 * n2;
            return n1 + "+" + n2 + "=" + m;
        }

        public String testCall(int n1, int n2) {
            return calc(n1, n2);
        }

        public JobResult<String> testCallJobResult(int n1, int n2) {
            return new JobResult<>(calc(n1, n2));
        }

    }

    @Test
    public void testBasic() {
        var jobPool = new JobPool(new JobPoolExecuterLocal());
        var job = jobPool.newJob(TestJob.class);

        assertNotNull(job, "Job 1 should've been created");
        assertNull(job.testCall(11, 22), "Call should return null as job hasn't been executed yet");

        var result = job.testCallJobResult(33, 44);

        assertNotNull(result);
        assertFalse(result.isFinished());
        assertFalse(result.isDispatched());

        var jobs = jobPool.executeJobs();

        assertEquals(2, jobs.length);
        assertSame(jobs[1], result);

        JobResult.waitAllFinished(jobs);

        assertEquals(jobs[0].getResult(), TestJob.calc(11, 22));
        assertEquals(jobs[1].getResult(), TestJob.calc(33, 44));
    }

}

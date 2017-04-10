package roxtools.threadpool;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RoxChainTaskTest {

	@Test
	public void testBasic() {
		
		RoxChainTaskPool<Integer,Double> chainTaskPool = new RoxChainTaskPool<>() ;
		
		chainTaskPool.add( new RoxChainTask<Integer, Integer>(10) {
			@Override
			public Integer task(Integer input) {
				
				int res = input * 10 ;
				
				return res ;
			}
		});
		
		chainTaskPool.add( new RoxChainTask<Integer, Float>(10) {
			@Override
			public Float task(Integer input) {
				
				int res = input * 10 ;
				
				return res + 0.5f ;
			}
		});
		
		chainTaskPool.add( new RoxChainTask<Float, Double>(10) {
			@Override
			public Double task(Float input) {
				
				float res = input * 10 ;
				
				return res + 0.0001d ;
			}
		});
		
		chainTaskPool.addInitialInputs(1,2,3,4,5);
		
		chainTaskPool.start();
		
		Assert.assertTrue( chainTaskPool.isStarted() );
		
		List<Double> finalOutput = chainTaskPool.getChainFinalOutput() ;
		
		Assert.assertTrue( chainTaskPool.isChainFinished() );
		
		Assert.assertNotNull( finalOutput );
		
		Assert.assertEquals( 5 , finalOutput.size() );
		
		Assert.assertArrayEquals( new Double[] { 1005.0001 , 2005.0001 , 3005.0001 , 4005.0001 , 5005.0001 } , finalOutput.toArray(new Double[finalOutput.size()]) );
		
	}
	
}

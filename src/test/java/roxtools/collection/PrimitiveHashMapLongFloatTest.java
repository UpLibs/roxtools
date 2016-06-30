package roxtools.collection;

import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class PrimitiveHashMapLongFloatTest {

	@Test
	public void testBasic1() {
		testBasic(10);
	}
	
	@Test
	public void testBasic2() {
		testBasic(100);
	}
	
	@Test
	public void testBasic3() {
		testBasic(1000);
	}

	
	public void testBasic(int totalValues) {
		
		final int kMult = 10 ;
		final int vMult = 100 ;
		
		PrimitiveHashMapLongFloat map = new PrimitiveHashMapLongFloat() ;
		
		for (int i = 0; i < totalValues; i++) {
			Long k = (long) (i*kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float prev = map.put(k, v) ;
			
			Assert.assertNull(prev);
			
			boolean contains = map.containsKey(k) ;
			
			Assert.assertTrue(contains);
			
			Float v2 = map.get(k) ;
			
			Assert.assertEquals( v , v2 );
		}
		
		Assert.assertEquals( totalValues , map.size() );
		
		int iterateCount = 0 ;
		
		for (Long k : map.keySet()) {
			int i = (int) (k/kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float v2 = map.get(k) ;
			
			Assert.assertEquals( v , v2 );
			iterateCount++ ;
		}
		
		Assert.assertEquals( totalValues , iterateCount );
		
		iterateCount = 0 ;
		
		for (Entry<Long, Float> entry : map.entrySet()) {
			Long k = entry.getKey() ;
			
			int i = (int) (k/kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float v1 = entry.getValue() ;
			
			Assert.assertEquals( v , v1 );
			
			Float v2 = map.get(k) ;
			
			Assert.assertEquals( v , v2 );
			iterateCount++ ;
		}
		
		Assert.assertEquals( totalValues , iterateCount );
		
		for (int i = 0; i < totalValues/2; i++) {
			Long k = (long) (i*kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float prev = map.remove(k) ;
			
			Assert.assertEquals( v , prev );
			
			boolean contains = map.containsKey(k) ;
			
			Assert.assertFalse(contains);
			
			Float v3 = map.get(k) ;
			
			Assert.assertNull(v3);
		}
		
		Assert.assertEquals( totalValues/2 , map.size() );
		
		for (int i = 0; i < totalValues/2; i++) {
			Long k = (long) (i*kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float prev = map.put(k, v) ;
			
			Assert.assertNull(prev);
			
			boolean contains = map.containsKey(k) ;
			
			Assert.assertTrue(contains);
			
			Float v2 = map.get(k) ;
			
			Assert.assertEquals( v , v2 );
		}
		
		Assert.assertEquals( totalValues , map.size() );

		for (int i = 0; i < totalValues; i++) {
			Long k = (long) (i*kMult) ;
			Float v = (float) (i*vMult) ;
			
			Float prev = map.remove(k) ;
			
			Assert.assertEquals( v , prev );
			
			boolean contains = map.containsKey(k) ;
			
			Assert.assertFalse(contains);
			
			Float v3 = map.get(k) ;
			
			Assert.assertNull(v3);
		}
		
		Assert.assertTrue( map.isEmpty() );
		
	}
	
}

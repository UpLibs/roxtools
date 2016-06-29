package roxtools.collection;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import roxtools.collection.SimpleHashMap;

public class SimpleHashMapTest {

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
		
		SimpleHashMap<Integer,Float> map = new SimpleHashMap<>() ;
		
		for (int i = 0; i < totalValues; i++) {
			Integer k = i*kMult ;
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
		
		for (Integer k : map.keySet()) {
			int i = k/kMult ;
			Float v = (float) (i*vMult) ;
			
			Float v2 = map.get(k) ;
			
			Assert.assertEquals( v , v2 );
			iterateCount++ ;
		}
		
		Assert.assertEquals( totalValues , iterateCount );
		
		for (int i = 0; i < totalValues/2; i++) {
			Integer k = i*kMult ;
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
			Integer k = i*kMult ;
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
			Integer k = i*kMult ;
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

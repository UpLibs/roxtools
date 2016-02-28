package roxtools;

import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class BigHashMapTest {

	@Test
	public void testBasic() {
		testBasic(100000);
	}
	
	@Test
	public void testBig() {
		testBasic(2000000);
	}
	
	private void testBasic(int totalInserts) {
		
		BigHashMap<Integer,Integer> bigHashMap = new BigHashMap<Integer,Integer>() ;
		
		for (int i = 0; i < totalInserts; i++) {
			bigHashMap.put( new Integer(i) , new Integer(i*10) ) ;
		}
		
		Assert.assertTrue( bigHashMap.size() == totalInserts );
		
		int checkI = 0 ;
		for (Entry<Integer, Integer> entry : bigHashMap.entrySet()) {
			
			Assert.assertTrue( entry.getKey() == checkI );	
			Assert.assertTrue( entry.getValue() == checkI*10 );
			
			checkI++ ;
		}
		
		Assert.assertTrue( checkI == totalInserts );
		
		for (int i = 0; i < totalInserts; i++) {
			boolean containsKey = bigHashMap.containsKey( new Integer(i) ) ;
			Assert.assertTrue( containsKey );
		}
		
		for (int i = totalInserts; i < totalInserts+1000; i++) {
			boolean containsKey = bigHashMap.containsKey( new Integer(i) ) ;
			Assert.assertTrue( !containsKey );
		}
		
		for (int i = 0; i < totalInserts/2; i++) {
			Integer prev = bigHashMap.remove( new Integer(i) ) ;
			Assert.assertNotNull( prev );
			Assert.assertTrue( prev.equals(i*10) );
		}
		
		Assert.assertTrue( bigHashMap.size() == totalInserts - (totalInserts/2) );
		
		for (int i = 0; i < totalInserts; i++) {
			boolean containsKey = bigHashMap.containsKey( new Integer(i) ) ;
			
			if (i < totalInserts/2) {
				Assert.assertFalse( containsKey );
			}
			else {
				Assert.assertTrue( containsKey );
			}
		}		
		
	}
	
}

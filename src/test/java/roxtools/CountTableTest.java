package roxtools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

public class CountTableTest {

	@Test
	public void testBasic() {
		
		CountTable<String> countTable = new CountTable<String>() ;
		
		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			
			int v = countTable.increment(k) ;
			
			assertTrue( v == 1 );
		}
		

		for (int i = 0; i < 10; i+=2) {
			String k = "k:"+i ;
			int v = countTable.increment(k) ;
			
			assertTrue( v == 2 );
		}
		
		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			int v = countTable.get(k) ;
			
			if ( i % 2 == 0 ) {
				assertTrue( v == 2 );
			}
			else {
				assertTrue( v == 1 );
			}
		}
		
		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			
			boolean ok = countTable.contains(k) ;
			
			assertTrue(ok);
		}
		
		for (int i = 10; i < 20; i++) {
			String k = "k:"+i ;
			
			boolean ok = countTable.contains(k) ;
			
			assertFalse(ok);
		}
		
		List<String> keysList = countTable.getKeys() ;
		Object[] keysObjs = countTable.getKeysArray() ;
		String[] keysArray = countTable.getKeysArray( new String[countTable.size()] ) ;
		
		assertTrue( keysList.size() == countTable.size() );
		assertTrue( keysObjs.length == countTable.size() );
		assertTrue( keysArray.length == countTable.size() );
		
		for (int i = 0; i < keysArray.length; i++) {
			String kList = keysList.get(i);
			String kObj = (String) keysObjs[i];
			String kArray = keysArray[i];
			
			assertTrue( kList.equals(kObj) );
			assertTrue( kList.equals(kArray) );
		}
		
		countTable.clear();
		
		assertTrue( countTable.size() == 0 );
		
		for (int i = 0; i < 20; i++) {
			String k = "k:"+i ;
			
			boolean ok = countTable.contains(k) ;
			
			assertFalse(ok);
		}
		
	}
	
	@Test
	public void testMinimum() {
		

		CountTable<String> countTable = new CountTable<String>() ;
		
		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			
			int v = countTable.sum(k, 10) ;
			
			assertTrue( v == 10 );
		}
		
		for (int i = 0; i < 10; i+=2) {
			String k = "k:"+i ;
			
			boolean set = countTable.setMinimum(k, 1) ;
			
			assertTrue(set);
		}
		
		for (int i = 1; i < 10; i+=2) {
			String k = "k:"+i ;
			
			boolean set = countTable.setMinimum(k, 20) ;
			
			assertFalse(set);
		}

		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			int v = countTable.get(k) ;
			
			if ( i % 2 == 0 ) {
				assertTrue( v == 1 );
			}
			else {
				assertTrue( v == 10 );
			}
		}
				
	}
	

	@Test
	public void testMaximum() {
		

		CountTable<String> countTable = new CountTable<String>() ;
		
		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			
			int v = countTable.sum(k, 10) ;
			
			assertTrue( v == 10 );
		}
		
		for (int i = 0; i < 10; i+=2) {
			String k = "k:"+i ;
			
			boolean set = countTable.setMaximum(k, 20) ;
			
			assertTrue(set);
		}
		
		for (int i = 1; i < 10; i+=2) {
			String k = "k:"+i ;
			
			boolean set = countTable.setMaximum(k, 1) ;
			
			assertFalse(set);
		}

		for (int i = 0; i < 10; i++) {
			String k = "k:"+i ;
			int v = countTable.get(k) ;
			
			if ( i % 2 == 0 ) {
				assertTrue( v == 20 );
			}
			else {
				assertTrue( v == 10 );
			}
		}
				
	}
	
	@Test
	public void testBigHashTable() {
		
		CountTable<Integer> countTable = new CountTable<Integer>() ;
		HashMap<Integer, Integer> countTable2 = new HashMap<Integer, Integer>() ;
		
		Random rand = new Random(123) ;
		
		for (int i = 0; i < 1000000; i++) {
			int key = rand.nextInt(10000) ;
			int amount = rand.nextInt(100) ;
			
			countTable.sum(key, amount) ;
			
			Integer count = countTable2.get(key) ;
			count = count != null ? count+amount : amount ;
			countTable2.put(key, count) ;
		}
		
		assertTrue( countTable.size() == countTable2.size() ) ;
		
		Integer[] keys = countTable.getKeysArray( new Integer[countTable.size()] ) ;
		Integer[] keys2 = ArrayUtils.toArray( countTable2.keySet() , new Integer[countTable2.size()] ) ;
		
		Arrays.sort(keys);
		Arrays.sort(keys2);
		
		assertTrue( Arrays.equals(keys, keys2) );
		
		for (Integer key : keys) {
			
			int val = countTable.get(key) ;
			int val2 = countTable2.get(key) ;
			
			assertTrue( val == val2 );
		}
		
	}
	
}

package roxtools;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.junit.Test;

import roxtools.WeakMapRecyclable.Entry;
import roxtools.WeakMapRecyclable.RecycleHandler;
import static org.junit.Assert.*;

public class WeakMapRecyclableTest {

	@Test
	public void testBasic() {
		
		WeakMapRecyclable<String,Integer> map = new WeakMapRecyclable<String,Integer>() ;
		
		String[] keys = {"a","b","c"} ;
		
		for (String k : keys) {
			map.put(k, map.size()+1) ;	
		}
		
		assertTrue( map.size() == 3 );
		
		assertTrue( map.contains("a") );
		assertTrue( map.contains("b") );
		assertTrue( map.contains("c") );
		
		assertFalse( map.contains("A") );
		
		assertTrue( map.get("a") == 1 );
		assertTrue( map.get("b") == 2 );
		assertTrue( map.get("c") == 3 );
		
		assertNull( map.get("A") );
		
		String[] keys2 = map.getKeysArray(new String[map.size()]) ;
		
		Arrays.sort(keys2);
		
		assertArrayEquals(keys, keys2);
		
		Entry<String, Integer> entry = map.remove("b") ;
		
		assertNotNull( entry );
		
		assertTrue( entry.getValue() == 2 );
		
		assertNull( map.get("b") );
		assertFalse( map.contains("b") );
		
		assertTrue( map.contains("a") );
		assertTrue( map.contains("c") );
		
		// to hold keys references:
		assertTrue( keys.length == 3 );
	}
	
	@Test
	public void testLoseRef() {
		
		WeakMapRecyclable<String,Integer> map = new WeakMapRecyclable<String,Integer>() ;
		
		final Vector<Integer> recycledValues = new Vector<Integer>() ;
		
		map.setRecycleHandler( new RecycleHandler<Integer>() {
			@Override
			public void recycleValue(Integer value) {
				recycledValues.add(value) ;
			}
		});
		
		
		map.put( new String("a".getBytes()) , 1) ;
		map.put( new String("b".getBytes()) , 2) ;
		map.put( new String("c".getBytes()) , 3) ;
		
		assertTrue( map.size() == 3 );
		
		System.gc();
		
		try { Thread.sleep(3000) ;} catch (InterruptedException e) {}
		
		System.gc();
		
		map.expungeStaleEntries();
		
		assertTrue( map.isEmpty() );
		
		assertFalse( map.contains("a") );
		assertFalse( map.contains("b") );
		assertFalse( map.contains("c") );
		
		Collections.sort(recycledValues);
		
		assertTrue( recycledValues.size() == 3 );
		
		assertTrue( recycledValues.get(0) == 1 );
		assertTrue( recycledValues.get(1) == 2 );
		assertTrue( recycledValues.get(2) == 3 );
		
	}
	
}

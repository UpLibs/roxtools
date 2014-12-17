package roxtools;

import org.junit.Test;

import static org.junit.Assert.*;
import roxtools.Memoize.MemKey;

public class MemoizeTest {

	@Test
	public void testBasic() {
		
		Memoize<Long> memoize = new Memoize<Long>() ;
		
		memoize.put(new MemKey("1"), 123L);
		
		Long val1 = memoize.get(new MemKey("1"));
		
		assertTrue(val1 == 123L);
		
		Long val2 = memoize.get(new MemKey("2"));
		
		assertNull(val2);
		
		memoize.put(new MemKey("2"), 456L);
		
		val2 = memoize.get(new MemKey("2"));
		
		assertTrue(val2 == 456L);
		
		Long val2b = memoize.remove(new MemKey("2"));
		
		assertTrue(val2 == val2b);
		assertTrue(val2 == 456L);
		
		assertFalse( memoize.contains(new MemKey("2")) );
		
	}
	
	@Test
	public void testTimeout() {
		
		Memoize<Long> memoize = new Memoize<Long>() ;
		
		memoize.setMemoryTimeout(1000L);
		
		memoize.put(new MemKey("1"), 123L);
		
		Long val = memoize.get(new MemKey("1"));
		
		assertTrue( val == 123L );
		
		System.out.println("Sleeping, 100ms...");
		try { Thread.sleep(100L) ;} catch (InterruptedException e) {}
		
		Long valA = memoize.get(new MemKey("1"));
		
		assertTrue( valA == 123L );
		
		System.out.println("Sleeping to force timeout: 2s...");
		try { Thread.sleep(2000L) ;} catch (InterruptedException e) {}
		
		Long valB = memoize.get(new MemKey("1"));
		
		assertNull( valB );
		
	}
	
}

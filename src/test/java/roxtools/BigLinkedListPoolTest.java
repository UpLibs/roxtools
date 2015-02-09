package roxtools;

import org.junit.Test;

import roxtools.BigLinkedListPool.BigLinkedList;
import static org.junit.Assert.*;

public class BigLinkedListPoolTest {
	
	static public final boolean ALLOW_BIG_TESTS ;
	
	static {
		
		Runtime runtime = Runtime.getRuntime() ;
		
		long maxMemory = runtime.maxMemory() ;
		
		final long bigMemory = (long) (1024L*1024*1024*1.5) ;
		
		ALLOW_BIG_TESTS = maxMemory >= bigMemory ;
		
		System.out.println("** ALLOW_BIG_TESTS: "+ ALLOW_BIG_TESTS +" > "+ (maxMemory/1024) +"KB / "+ (bigMemory/1024)+"KB" );
		
	}
	
	static private boolean checkAllowBigTests() {
		if (!ALLOW_BIG_TESTS) {
			Throwable throwable = new Throwable() ;
			
			StackTraceElement[] stackTrace = throwable.getStackTrace() ;
			String methodName = stackTrace[1].getMethodName() ;
			
			System.out.println("** Skipping["+methodName+"]> ALLOW_BIG_TESTS: "+ ALLOW_BIG_TESTS);
		}
		return ALLOW_BIG_TESTS ;
	}

	@Test
	public void testBasic1() {
		testBasicImplem(100, 1000);
	}
	
	@Test
	public void testBasic2() {
		if ( checkAllowBigTests() ) {
			testBasicImplem(100, 10000);
		}
	}
	
	@Test
	public void testBasic3() {
		if (checkAllowBigTests()) {
			testBasicImplem(1000, 100);
		}
	}
	
	@Test
	public void testBasic4() {
		testBasicImplem(10000, 100);	
	}
	
	private void testBasicImplem( int totalLists, int totalValues ) {
		BigLinkedListPool<Integer> pool = new BigLinkedListPool<Integer>(Integer.class) ;
		
		assertTrue( pool.size() == 0 );
		
		@SuppressWarnings("unchecked")
		BigLinkedList<Integer>[] linkedLists = new BigLinkedList[totalLists] ; 
		
		int valIdRange = totalValues*10 ;
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] = pool.createLinkedList() ;
			
			for (int j = 0; j < totalValues; j++) {
				Integer val = (i*valIdRange) + j ;
				bigLinkedList.add(val);
			}
			
			assertTrue( bigLinkedList.size() == totalValues );
		}
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] ;
			
			for (int j = 0; j < totalValues; j++) {
				Integer val = (i*valIdRange) + j ;
				Integer val2 = bigLinkedList.get(j) ;
			
				assertTrue( val.equals(val2) );
			}
			
			int j = 0 ;
			for (Integer val : bigLinkedList) {
				Integer v = (i*valIdRange) + j ;
				
				assertTrue( val.equals(v) );
				
				j++ ;
			}
			
			assertTrue( bigLinkedList.size() == totalValues );
		}
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] ;
			bigLinkedList.removeLast() ;
			
			bigLinkedList.add(-1);
		}
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] ;
			
			assertTrue( bigLinkedList.size() == totalValues );
			
			Integer valLast = bigLinkedList.get( bigLinkedList.size()-1 ) ;
			Integer valLast2 = bigLinkedList.getLast() ;
			assertTrue( valLast == -1 );
			assertTrue( valLast.equals(valLast2) );
		}
		
		System.out.println(pool);
		
	}

}

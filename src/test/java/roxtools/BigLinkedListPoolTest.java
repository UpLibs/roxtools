package roxtools;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import roxtools.BigLinkedListPool.BigLinkedList;

public class BigLinkedListPoolTest {
	
	static public final boolean ALLOW_BIG_TESTS ;
	
	static {
		
		Runtime runtime = Runtime.getRuntime() ;
		
		long maxMemory = runtime.maxMemory() ;
		
		final long bigMemory = (long) (1024L*1024*1024*1.5) ;
		
		boolean allow = maxMemory >= bigMemory ;
		
		ALLOW_BIG_TESTS = allow ;
		
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
	
	@Test
	public void testRemove() {
		BigLinkedListPool<Integer> pool = new BigLinkedListPool<Integer>(Integer.class) ;
		
		ArrayList<BigLinkedList<Integer>> lists = new ArrayList< BigLinkedList<Integer> >() ;
		
		int totalSize = 0 ;
		
		for (int i = 0; i < 100; i++) {
			
			BigLinkedList<Integer> list = pool.createLinkedList() ;
			
			lists.add(list) ;
			
			for (int j = 0; j < 1000; j++) {
				list.add(j);
			}
			
			totalSize += list.size() ;
		}
		
		assertTrue( pool.size() == totalSize );

		int totalSize2 = 0 ;
		
		for (BigLinkedList<Integer> list : lists) {
			
			Integer[] array = list.toArray() ;
			ArrayList<Integer> checkList = new ArrayList<Integer>( array.length ) ;
			Collections.addAll(checkList, array) ;
			
			assertTrue( list.size() == checkList.size() );
			
			int preRemoveSize = list.size() ;
			
			int removeCount = 0 ;
			for (int j = 0; j < 1000; j+=2) {
				int rmIdx = j-removeCount ;
				Integer val = list.remove(rmIdx) ;
				Integer val2 = checkList.remove(rmIdx) ;
				
				assertNotNull(val);
				assertNotNull(val2);
				
				assertTrue( val.equals(val2) );
				
				assertTrue( val.equals(j) );
				
				removeCount++ ;
			}
			
			assertTrue( list.size() == checkList.size() );
			
			int sz = list.size() ;
			
			assertTrue( sz == preRemoveSize/2 );
			
			
			for (int i = 0; i < sz; i++) {
				Integer v = list.get(i) ;
				Integer v2 = checkList.get(i) ;
				
				assertNotNull(v);
				assertNotNull(v2);
				
				assertTrue( v.equals(v2) );
			}
			
			totalSize2 += list.size() ;
			
		}
		
		assertTrue( pool.size() == totalSize2 );
		
		for (BigLinkedList<Integer> list : lists) {
			
			Integer[] array = list.toArray() ;
			
			assertTrue( array.length == list.size() );
			
			Integer prevV = null ;
			for (Integer v : list) {
				
				if (prevV != null) {
					assertTrue( v == prevV+2 );
				}
				
				prevV = v ;
			}
			
		}
		
	}

}

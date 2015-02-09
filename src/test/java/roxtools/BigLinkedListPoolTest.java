package roxtools;

import org.junit.Test;

import roxtools.BigLinkedListPool.BigLinkedList;
import static org.junit.Assert.*;

public class BigLinkedListPoolTest {

	@Test
	public void testBasic() {
		BigLinkedListPool<Integer> pool = new BigLinkedListPool<Integer>(Integer.class) ;
		
		assertTrue( pool.size() == 0 );
		
		@SuppressWarnings("unchecked")
		BigLinkedList<Integer>[] linkedLists = new BigLinkedList[10] ; 
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] = pool.createLinkedList() ;
			
			for (int j = 0; j < 1000; j++) {
				Integer val = (i*100000) + j ;
				bigLinkedList.add(val);
			}
			
			assertTrue( bigLinkedList.size() == 1000 );
		}
		
		for (int i = 0; i < linkedLists.length; i++) {
			BigLinkedList<Integer> bigLinkedList = linkedLists[i] ;
			
			for (int j = 0; j < 1000; j++) {
				Integer val = (i*100000) + j ;
				Integer val2 = bigLinkedList.get(j) ;
			
				assertTrue( val.equals(val2) );
			}
			
			assertTrue( bigLinkedList.size() == 1000 );
		}
		
	}

}

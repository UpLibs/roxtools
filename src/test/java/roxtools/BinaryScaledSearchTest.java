package roxtools;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

public class BinaryScaledSearchTest {

	static private int[] createSet(int size, int min, int max, long seed) {
		Random rand = new Random(seed) ;
		
		int[] a = new int[size] ;
		
		int scale = max-min ;
		
		for (int i = 0; i < a.length; i++) {
			a[i] = min + rand.nextInt(scale) ;
		}
		
		return a ;
	}
	
	@Test
	public void testBasic() {
		testImplem( 123 , 456 );
	}
	
	@Test
	public void testMultipleSeeds() {
		
		Random random = new Random(781198943);
		
		for (int i = 0; i < 1000; i++) {
			testImplem( random.nextLong() , random.nextLong() );
		}
	}
	
	public void testImplem(long seed1, long seed2) {
		
		int[] set = createSet(10000, 0, 100000, seed1) ;
		
		Arrays.sort(set);
		
		int[] keysToSearch = createSet(10000, 0, 100000, seed2) ;
		
		Arrays.sort(keysToSearch);
		
		for (int i = 0; i < keysToSearch.length; i++) {
			int k = keysToSearch[i];
		
			int idx = BinaryScaledSearch.search(set, k) ;
			int idxCheck = BinaryScaledSearch.binarySearchOriginal(set, k) ;
			
			// both should find the key (idx >= 0), or both shouldn't find the key (idx < 0):
			assertTrue( (idx >= 0) == (idxCheck >= 0) );
			
			// if can't find the key, the insert position/index should be the same:
			if (idx < 0) {
				assertTrue(idx+" == "+ idxCheck , idx == idxCheck );	
			}
			// if found the key, the index can be different, but the value should be the same:
			else {
				int v1 = set[idx] ;
				int v2 = set[idxCheck] ;
				
				assertTrue( v1 == v2 );
			}
			
		}
		
	}
	
	
	// uncomment to execute it as Unit Test:
	//@Test
	public void benchmark() {
		
		System.out.println("prepare");
		benchmarkImplem();
		
		System.out.println("sleep...");
		try { Thread.sleep(3000) ;} catch (InterruptedException e) {}
		
		System.gc();
		
		System.out.println("sleep...");
		try { Thread.sleep(3000) ;} catch (InterruptedException e) {}
		
		System.out.println("benchmark");
		benchmarkImplem();
		
	}
	
	private void benchmarkImplem() {
		
		int[] set = createSet(10000, 0, 100000, 123) ;
		
		Arrays.sort(set);
		
		int[] keysToSearch = createSet(1000, 0, 100000, 456) ;
		
		Arrays.sort(keysToSearch);
		
		long timeBinaryScaledSearch = System.currentTimeMillis() ;
		
		for (int loop = 9999; loop >= 0; loop--) {
			for (int i = keysToSearch.length-1; i >= 0; i--) {
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
				BinaryScaledSearch.search(set, keysToSearch[i]) ;
			}
		}
		
		timeBinaryScaledSearch = System.currentTimeMillis() - timeBinaryScaledSearch ;

		System.out.println("timeBinaryScaledSearch: "+ timeBinaryScaledSearch);
		
		long timeBinarySearchOriginal = System.currentTimeMillis() ;
		
		for (int loop = 9999; loop >= 0; loop--) {
			for (int i = keysToSearch.length-1; i >= 0; i--) {
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
				BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]) ;
			}
		}
		
		timeBinarySearchOriginal = System.currentTimeMillis() - timeBinarySearchOriginal ;
		
		System.out.println("timeBinarySearchOriginal: "+ timeBinarySearchOriginal);
		
	}
	
}
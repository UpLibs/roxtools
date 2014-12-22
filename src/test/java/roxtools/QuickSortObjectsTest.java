package roxtools;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import roxtools.QuickSortObjects.ObjectCompareValueInt;

public class QuickSortObjectsTest {

	static private Object[] createIntArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		int[] array = new int[size];
		Object[] objs = new Object[size] ;
		
		for(int i=0;i<size;i++){
			int v = rand.nextInt(size*10) ;
			array[i] = v ;
			objs[i] = "s:"+ v ;
		}
		
		return new Object[] { array , objs } ;
	}
	
	static private String[] createStringArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		String[] objs = new String[size] ;
		
		for(int i=0;i<size;i++){
			int v = rand.nextInt(size*10) ;
			objs[i] = ""+v ;
		}
		
		return objs ;
	}

	static private void checkSortInt(int[] a, Object[] o) {
		for (int i = 1; i < a.length; i++) {
			int v = a[i];
			int vPrev = a[i-1];
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
			
			Object vO = o[i] ;
			
			if ( !("s:"+v).equals(vO) ) throw new IllegalStateException("Object not matching index") ;
		}
		
	}
	
	static private void checkSortString(String[] o) {
		for (int i = 1; i < o.length; i++) {
			String strVal = o[i];
			String strValPrev = o[i-1];
			
			int v = Integer.parseInt(strVal) ;
			int vPrev = Integer.parseInt(strValPrev) ;
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
		}
		
	}

	@Test
	public void testSort() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createIntArray(10000, seed) ;
			
			int[] a = (int[]) ret[0] ;
			Object[] o = (Object[]) ret[1] ;
			
			QuickSortObjects.sort(a, o);
			
			checkSortInt(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortOffset() {
		
		int offset = 1000 ;
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createIntArray(10000, seed) ;
			
			int[] a = (int[]) ret[0] ;
			Object[] o = (Object[]) ret[1] ;
			
			for (int i = 0; i < offset; i++) {
				int v = (-offset)+i ;
				a[i] = v ;
				o[i] = "s:"+v ;
			}
			
			QuickSortObjects.sort(a, o, offset, a.length-1);
			
			checkSortInt(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortObjects() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L ;
		
		for (int loop = 0; loop < 10; loop++) {
			String[] o = createStringArray(10000, seed) ;
			
			QuickSortObjects.sort(o, new ObjectCompareValueInt<String>() {
				@Override
				public int getObjectCompareValue(String obj) {
					return Integer.parseInt(obj) ;
				}
			});
			
			checkSortString(o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(o)) ;
		}
		
	}
	
	@Test
	public void testSortObjectsOffset() {
		
		int offset = 1000 ;
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L ;
		
		for (int loop = 0; loop < 10; loop++) {
			String[] o = createStringArray(10000, seed) ;
			
			for (int i = 0; i < offset; i++) {
				int v = (-offset)+i ;
				o[i] = ""+v ;
			}
			
			QuickSortObjects.sort(o, new ObjectCompareValueInt<String>() {
				@Override
				public int getObjectCompareValue(String obj) {
					return Integer.parseInt(obj) ;
				}
			} , offset , o.length-1);
			
			checkSortString(o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(o)) ;
		}
		
	}
	
}

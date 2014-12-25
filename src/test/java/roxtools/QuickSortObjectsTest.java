package roxtools;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;
import org.junit.Test;

import roxtools.QuickSortObjects.ObjectCompareValueInt;

public class QuickSortObjectsTest {

	static private Object[] createIntArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		int[] array = new int[size];
		Object[] objs = new Object[size] ;
		
		for(int i=0;i<size;i++){
			int v = rand.nextInt(size*10) ;
			array[i] = v ;
			objs[i] = "s:"+ v ;
		}
		
		return new Object[] { array , objs } ;
	}
	
	static private Object[] createFloatArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		float[] array = new float[size];
		Object[] objs = new Object[size] ;
		
		for(int i=0;i<size;i++){
			float v = rand.nextInt(size*10) / 2f ;
			array[i] = v ;
			objs[i] = "s:"+ v ;
		}
		
		return new Object[] { array , objs } ;
	}
	
	static private Object[] createDoubleArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		double[] array = new double[size];
		Object[] objs = new Object[size] ;
		
		for(int i=0;i<size;i++){
			double v = rand.nextInt(size*10) / 2d ;
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
	
	static private void checkSortFloat(float[] a, Object[] o) {
		for (int i = 1; i < a.length; i++) {
			float v = a[i];
			float vPrev = a[i-1];
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
			
			Object vO = o[i] ;
			
			if ( !("s:"+v).equals(vO) ) throw new IllegalStateException("Object not matching index") ;
		}
		
	}
	
	static private void checkSortDouble(double[] a, Object[] o) {
		for (int i = 1; i < a.length; i++) {
			double v = a[i];
			double vPrev = a[i-1];
			
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
	
	/////////////////////////////////////////////////////////////////////

	@Test
	public void testSortInt() {
		
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
	public void testSortFloat() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 2 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createFloatArray(10000, seed) ;
			
			float[] a = (float[]) ret[0] ;
			Object[] o = (Object[]) ret[1] ;
			
			QuickSortObjects.sort(a, o);
			
			checkSortFloat(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortDouble() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 3 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createDoubleArray(10000, seed) ;
			
			double[] a = (double[]) ret[0] ;
			Object[] o = (Object[]) ret[1] ;
			
			QuickSortObjects.sort(a, o);
			
			checkSortDouble(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortOffset() {
		
		int offset = 1000 ;
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 5 ;
		
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
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 7 ;
		
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
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 11 ;
		
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

	@Test
	public void testBigSort() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 13 ;
		
		System.out.println("testBigSort<<");
		
		Random random = new Random(seed) ;
		
		int[] a = new int[100000000] ;
		
		System.out.println("array length: "+ a.length);
		
		for (int i = 0; i < a.length; i++) {
			a[i] = random.nextInt(a.length) ;
		}
		
		System.out.println("creating objs...");
		
		String[] objsStrs = new String[1000] ;
		
		for (int i = 0; i < objsStrs.length; i++) {
			objsStrs[i] = ""+i ;
		}
		
		Object[] o = new Object[a.length] ;
		
		for (int i = 0; i < o.length; i++) {
			o[i] = objsStrs[ i % objsStrs.length ] ;
		}
		
		System.out.println("checkking sort...");
		
		assertFalse( ArrayUtils.isSorted(a) );
		
		System.out.println("sorting...");
		
		QuickSortObjects.sort(a, o);

		System.out.println("checkking sort...");
		
		assertTrue( ArrayUtils.isSorted(a) );
		
		System.out.println(">>testBigSort");
	}
	
}

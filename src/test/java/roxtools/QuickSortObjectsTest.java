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
	
	static private Object[] createFloatIntArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		float[] array = new float[size];
		int[] objs = new int[size] ;
		
		for(int i=0;i<size;i++){
			float v = rand.nextInt(size*10) / 2f ;
			array[i] = v ;
			objs[i] = (int) v ;
		}
		
		return new Object[] { array , objs } ;
	}
	
	static private Object[] createFloatFloatArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		float[] array = new float[size];
		float[] objs = new float[size] ;
		
		for(int i=0;i<size;i++){
			float v = rand.nextInt(size*10) / 2f ;
			array[i] = v ;
			objs[i] = v*2 ;
		}
		
		return new Object[] { array , objs } ;
	}
	
	static private Object[] createFloatIntFloat2DArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		float[] array = new float[size];
		int[] objs = new int[size] ;
		float[][] objs2 = new float[size][] ;
		
		for(int i=0;i<size;i++){
			float v = rand.nextInt(size*10) / 2f ;
			array[i] = v ;
			objs[i] = (int) v ;
			objs2[i] = new float[] { v , v*2 } ;
		}
		
		return new Object[] { array , objs , objs2 } ;
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
	
	static private Number[] createNumberArray(int size, long seed){
		Random rand = new Random(seed) ;
		
		size += ( rand.nextInt(size/10) ) ;
		
		Number[] array = new Number[size];
		
		for(int i=0;i<size;i++){
			double v = rand.nextInt(size*10) / 2d ;
			
			int type = rand.nextInt(3) ;
			
			if (type == 0) {
				array[i] = (int)v ;	
			}
			else if (type == 1) {
				array[i] = (float)v ;
			}
			else if (type == 2) {
				array[i] = (double)v ;
			}
			
		}
		
		return array ;
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
	
	static private void checkSortNumberAsInteger(Number[] a) {
		for (int i = 1; i < a.length; i++) {
			Number v = a[i];
			Number vPrev = a[i-1];
			
			if (vPrev.intValue() > v.intValue()) throw new IllegalStateException("Array not sorted") ;
		}
	}
	
	static private void checkSortNumberAsFloat(Number[] a) {
		for (int i = 1; i < a.length; i++) {
			Number v = a[i];
			Number vPrev = a[i-1];
			
			if (vPrev.floatValue() > v.floatValue()) throw new IllegalStateException("Array not sorted") ;
		}
	}
	
	static private void checkSortNumberAsDouble(Number[] a) {
		for (int i = 1; i < a.length; i++) {
			Number v = a[i];
			Number vPrev = a[i-1];
			
			if (vPrev.doubleValue() > v.doubleValue()) throw new IllegalStateException("Array not sorted") ;
		}
	}
	
	static private void checkSortFloatInt(float[] a, int[] o) {
		for (int i = 1; i < a.length; i++) {
			float v = a[i];
			float vPrev = a[i-1];
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
			
			int vO = o[i] ;
			
			if ( ((int)v) != vO ) throw new IllegalStateException("Value not matching index") ;
		}
		
	}
	
	static private void checkSortFloatFloat(float[] a, float[] o) {
		for (int i = 1; i < a.length; i++) {
			float v = a[i];
			float vPrev = a[i-1];
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
			
			float vO = o[i] ;
			
			if ( (v*2) != vO ) throw new IllegalStateException("Value not matching index") ;
		}
		
	}
	
	static private void checkSortFloatIntFloat2D(float[] a, int[] o, float[][] o2) {
		for (int i = 1; i < a.length; i++) {
			float v = a[i];
			float vPrev = a[i-1];
			
			if (vPrev > v) throw new IllegalStateException("Array not sorted") ;
			
			int vO = o[i] ;
			
			if ( ((int)v) != vO ) throw new IllegalStateException("Value not matching index") ;
			
			float[] vO2 = o2[i] ;
			
			if ( v != vO2[0] ) throw new IllegalStateException("Value not matching index") ;
			if ( v*2 != vO2[1] ) throw new IllegalStateException("Value not matching index") ;
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
	public void testSortFloatAndInt() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 3 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createFloatIntArray(10000, seed) ;
			
			float[] a = (float[]) ret[0] ;
			int[] o = (int[]) ret[1] ;
			
			QuickSortObjects.sort(a, o);
			
			checkSortFloatInt(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}


	@Test
	public void testSortFloatAndFloat() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 5 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createFloatFloatArray(10000, seed) ;
			
			float[] a = (float[]) ret[0] ;
			float[] o = (float[]) ret[1] ;
			
			QuickSortObjects.sort(a, o);
			
			checkSortFloatFloat(a, o);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortFloatAndIntAndFloat2D() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 7 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Object[] ret = createFloatIntFloat2DArray(10000, seed) ;
			
			float[] a = (float[]) ret[0] ;
			int[] o = (int[]) ret[1] ;
			float[][] o2 = (float[][]) ret[2] ;
			
			QuickSortObjects.sort(a, o, o2);
			
			checkSortFloatIntFloat2D(a, o, o2);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}


	
	@Test
	public void testSortDouble() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 11 ;
		
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
	public void testSortNumberAsInteger() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 2557 ;
		
		for (int loop = 0; loop < 10; loop++) {
			Number[] a = createNumberArray(10000, seed) ;
			
			QuickSortObjects.sortNumbersAsInt(a);
			
			checkSortNumberAsInteger(a);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	@Test
	public void testSortNumberAsFloat() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * (2557+1) ;
		
		for (int loop = 0; loop < 10; loop++) {
			Number[] a = createNumberArray(10000, seed) ;
			
			QuickSortObjects.sortNumbersAsFloat(a);
			
			checkSortNumberAsFloat(a);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	

	@Test
	public void testSortNumberAsDouble() {
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * (2557+3) ;
		
		for (int loop = 0; loop < 10; loop++) {
			Number[] a = createNumberArray(10000, seed) ;
			
			QuickSortObjects.sortNumbersAsDouble(a);
			
			checkSortNumberAsDouble(a);
			
			seed = seed ^ (seed * 31 + Arrays.hashCode(a)) ;
		}
		
	}
	
	
	@Test
	public void testSortOffset() {
		
		int offset = 1000 ;
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 13 ;
		
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
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 17 ;
		
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
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 19 ;
		
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
		
		long seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 1759 ;
		
		System.out.println("testBigSort<<");
		
		Runtime runtime = Runtime.getRuntime();
		long availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory()) ;
		
		System.out.println("availableMemory: "+ availableMemory);
		
		int availableArraySize = (int)( ( availableMemory / (4+8) ) * 0.90);
		
		System.out.println("availableArraySize: "+ availableArraySize);
		
		if (availableArraySize < 10000) availableArraySize = 10000 ;
		else if (availableArraySize > 10000000) availableArraySize = 10000000 ;
		
		int[] a = new int[availableArraySize] ;
		System.out.println("array length: "+ a.length);
		
		Random random = new Random(seed) ;
		
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

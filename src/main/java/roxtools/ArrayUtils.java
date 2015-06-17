package roxtools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ArrayUtils {

	final static private Random random = new Random() ;

	static public void halfSet(byte[] src, byte[] dst) {
		int j = 0 ;
		for (int i = 0; i < src.length; i+=2) {
			dst[j++] = src[i] ;
		}
	}
	
	static public void doubleSet(byte[] src, byte[] dst) {
		int j = 0 ;
		for (int i = 0; i < src.length; i++) {
			dst[j++] = src[i] ;
			dst[j++] = src[i] ;
		}
	}
	
	static public int[] toInts(byte[] bs) {
		int[] ns = new int[bs.length] ;
		
		for (int i = ns.length-1; i >= 0; i--) {
			ns[i] = bs[i] & 0xff ;
		}
		
		return ns ;
	}
	
	static public int[] toInts(Number[] objs) {
		int[] ns = new int[objs.length] ;
		
		for (int i = ns.length-1; i >= 0; i--) {
			ns[i] = objs[i].intValue() ;
		}
		
		return ns ;
	}

	static public int[] toInts(float[] fs, int scale) {
		int[] ns = new int[fs.length] ;
		
		for (int i = ns.length-1; i >= 0; i--) {
			ns[i] = (int) (fs[i] * scale) ;
		}
		
		return ns ;
	}
	
	static public byte[] toBytes(int[] ns) {
		byte[] bs = new byte[ns.length] ;
		
		for (int i = bs.length-1; i >= 0; i--) {
			bs[i] = (byte) ns[i] ;
		}
		
		return bs ;
	}
	
	static public byte[] toBytes(float[] ns) {
		byte[] bs = new byte[ns.length] ;
		
		for (int i = bs.length-1; i >= 0; i--) {
			bs[i] = (byte) ((int)ns[i]) ;
		}
		
		return bs ;
	}
	
	static public float[] toFloats(int[] vals) {
		float[] res = new float[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = (float) vals[i] ;
		}
		
		return res ;
	}
	
	static public float[] toFloats(Number[] vals) {
		float[] res = new float[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i].floatValue() ;
		}
		
		return res ;
	}
	
	static public float[] toFloats(double[] vals) {
		float[] res = new float[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = (float) vals[i] ;
		}
		
		return res ;
	}
	
	static public float[] toFloats(byte[] vals) {
		float[] res = new float[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = (float) (vals[i] & 0xFF) ;
		}
		
		return res ;
	}

	static public double[] toDoubles(int[] vals) {
		double[] res = new double[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i] ;
		}
		
		return res ;
	}
	
	static public double[] toDoubles(Number[] vals) {
		double[] res = new double[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i].doubleValue() ;
		}
		
		return res ;
	}
	
	static public double[] toDoubles(byte[] vals) {
		double[] res = new double[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i] & 0xFF ;
		}
		
		return res ;
	}
	
	static public boolean[] toBooleans(int[] vals, int threshold) {
		boolean[] res = new boolean[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i] >= threshold ;
		}
		
		return res ;
	}
	
	static public boolean[] toBooleans(float[] vals, float threshold) {
		boolean[] res = new boolean[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i] >= threshold ;
		}
		
		return res ;
	}
	
	static public boolean[] toBooleans(double[] vals, double threshold) {
		boolean[] res = new boolean[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = vals[i] >= threshold ;
		}
		
		return res ;
	}
	
	static public void scale(int[] vals, int min, int max, int min2, int max2) {
		int scale = max - min ;
		int scale2 = max2 - min2 ;
		
		for (int i = vals.length-1; i >= 0; i--) {
			int v = vals[i] ;
			vals[i] = min2 + (((v - min) / scale) * scale2) ;
		}
	}
	
	static public void scale(float[] vals, float min, float max, float min2, float max2) {
		float scale = max - min ;
		float scale2 = max2 - min2 ;
		
		for (int i = vals.length-1; i >= 0; i--) {
			float v = vals[i] ;
			vals[i] = min2 + (((v - min) / scale) * scale2) ;
		}
	}
	
	static public void scale(double[] vals, double min, double max, double min2, double max2) {
		double scale = max - min ;
		double scale2 = max2 - min2 ;
		
		for (int i = vals.length-1; i >= 0; i--) {
			double v = vals[i] ;
			vals[i] = min2 + (((v - min) / scale) * scale2) ;
		}
	}
	
	static public void clip(float[] vals, float min, float max) {
		for (int i = vals.length-1; i >= 0; i--) {
			float v = vals[i] ;
			if (v < min) vals[i] = min ;
			else if (v > max) vals[i] = max ;
		}
	}
	
	static public void clip(int[] vals, int min, int max) {
		for (int i = vals.length-1; i >= 0; i--) {
			float v = vals[i] ;
			if (v < min) vals[i] = min ;
			else if (v > max) vals[i] = max ;
		}
	}
	
	static public int[] copy(int[] vals) {
		return copy(vals, 0, vals.length) ;
	}
	
	static public int[] copy(int[] vals, int off, int lng) {
		int[] vals2 = new int[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	
	static public float[] copy(float[] vals) {
		return copy(vals, 0, vals.length) ;
	}
	
	static public float[] copy(float[] vals, int off, int lng) {
		float[] vals2 = new float[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	
	static public double[] copy(double[] vals) {
		return copy(vals, 0, vals.length) ;
	}
	
	static public double[] copy(double[] vals, int off, int lng) {
		double[] vals2 = new double[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	

	static public int[][] copy(int[][] a) {
		int[][] a2 = new int[a.length][] ;
		
		for (int i = 0; i < a2.length; i++) {
			int[] fs = a[i];
			a2[i] = fs != null ? fs.clone() : null ;
		}
		
		return a2 ;
	}

	static public float[][] copy(float[][] a) {
		float[][] a2 = new float[a.length][] ;
		
		for (int i = 0; i < a2.length; i++) {
			float[] fs = a[i];
			a2[i] = fs != null ? fs.clone() : null ;
		}
		
		return a2 ;
	}

	static public double[][] copy(double[][] a) {
		double[][] a2 = new double[a.length][] ;
		
		for (int i = 0; i < a2.length; i++) {
			double[] fs = a[i];
			a2[i] = fs != null ? fs.clone() : null ;
		}
		
		return a2 ;
	}
	
	
	static public int indexOf(float[] a, float v) {
		for (int i = 0; i < a.length; i++) {
			if ( a[i] == v ) return i ;
		}
		return -1 ;
	}
	
	static public int indexOf(double[] a, double v) {
		for (int i = 0; i < a.length; i++) {
			if ( a[i] == v ) return i ;
		}
		return -1 ;
	}
	
	static public int indexOf(int[] a, int v) {
		for (int i = 0; i < a.length; i++) {
			if ( a[i] == v ) return i ;
		}
		return -1 ;
	}
	
	static public int indexOf(Object[] a, Object v) {
		for (int i = 0; i < a.length; i++) {
			Object obj = a[i] ;
			if ( obj != null && obj.equals(v) ) return i ;
		}
		return -1 ;
	}
	
	static public int[] join(int[]... fs) {
		int total = 0 ;
		
		for (int i = fs.length-1; i >= 0; i--) {
			total += fs[i].length ;
		}
		
		int[] all = new int[total] ;
		int allSz = 0 ;

		for (int i = 0; i < fs.length; i++) {
			int[] a = fs[i];
			System.arraycopy(a, 0, all, allSz, a.length);
			allSz += a.length ;
		}

		return all ;
	}
		
	static public float[] join(float[]... fs) {
		int total = 0 ;
		
		for (int i = fs.length-1; i >= 0; i--) {
			total += fs[i].length ;
		}
		
		float[] all = new float[total] ;
		int allSz = 0 ;

		for (int i = 0; i < fs.length; i++) {
			float[] a = fs[i];
			System.arraycopy(a, 0, all, allSz, a.length);
			allSz += a.length ;
		}

		return all ;
	}
	
	static public double[] join(double[]... fs) {
		int total = 0 ;
		
		for (int i = fs.length-1; i >= 0; i--) {
			total += fs[i].length ;
		}
		
		double[] all = new double[total] ;
		int allSz = 0 ;

		for (int i = 0; i < fs.length; i++) {
			double[] a = fs[i];
			System.arraycopy(a, 0, all, allSz, a.length);
			allSz += a.length ;
		}

		return all ;
	}
	
	static public void swap(int[] a, int idx1,int idx2){
		int tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
	}
	
	static public void swap(float[] a, int idx1,int idx2){
		float tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
	}
	
	static public void swap(double[] a, int idx1,int idx2){
		double tmp = a[idx1];
		a[idx1] = a[idx2];
		a[idx2] = tmp;
	}
	
	public static void shuffle(int[] a) {
		shuffle(a, random);
	}
	
	public static void shuffle(float[] a) {
		shuffle(a, random);
	}
	
	public static void shuffle(double[] a) {
		shuffle(a, random);
	}
	
	public static void shuffle(int[] a, Random rnd) {
        for (int i= a.length; i>1; i--)
        	swap(a, i-1, rnd.nextInt(i)) ;
    }
	
	public static void shuffle(float[] a, Random rnd) {
        for (int i= a.length; i>1; i--)
        	swap(a, i-1, rnd.nextInt(i)) ;
    }
	
	public static void shuffle(double[] a, Random rnd) {
        for (int i= a.length; i>1; i--)
        	swap(a, i-1, rnd.nextInt(i)) ;
    }
	
	public static boolean isSorted(int[] a) {
		for (int i = a.length-2 ; i >= 0; i--) {
			if ( a[i] > a[i+1] ) return false ;
		}
		return true ;
	}
	
	public static boolean isSorted(float[] a) {
		for (int i = a.length-2 ; i >= 0; i--) {
			if ( a[i] > a[i+1] ) return false ;
		}
		return true ;
	}
	
	public static boolean isSorted(double[] a) {
		for (int i = a.length-2 ; i >= 0; i--) {
			if ( a[i] > a[i+1] ) return false ;
		}
		return true ;
	}

	@SuppressWarnings("unchecked")
	static public <T> T[] asArrayOfSize(T[] a, int size) {
		if (a.length < size) a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size) ;
		return a;
	}
	
	@SuppressWarnings("unchecked")
	static public <T> T[] asArrayOfSize(T obj, int size) {
		return (T[]) java.lang.reflect.Array.newInstance(obj.getClass(), size) ;
	}
	
	@SuppressWarnings("unchecked")
	static public <T> T[] asArrayOfSize(Class<T> clazz, int size) {
		return (T[]) java.lang.reflect.Array.newInstance(clazz, size) ;
	}
	
	static public <T> T[] toArray(Set<T> set, T[] a) {
		return set.toArray(a) ;
	}
	
	static public <T> T[] toArray(Collection<T> collection, T[] a) {
		return collection.toArray(a) ;
	}
	
	static public <T> T[] toArray(Iterator<T> it, T[] a) {
		ArrayList<T> list = new ArrayList<T>() ;
		while (it.hasNext()) {
			T t = (T) it.next();
			list.add(t) ;
		}
		return list.toArray(a) ;
	}
	
	static public <K,V> K[] toArrayKeys(Map<K,V> map, K[] a) {
		return toArray(map.keySet(), a) ;
	}
	
	static public <K,V> V[] toArrayValues(Map<K,V> map, V[] a) {
		return toArray(map.values(), a) ;
	}
	
}

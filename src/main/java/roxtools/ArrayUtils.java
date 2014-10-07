package roxtools;

public class ArrayUtils {

	static public void toHalf(byte[] src, byte[] dst) {
		int j = 0 ;
		for (int i = 0; i < src.length; i+=2) {
			dst[j++] = src[i] ;
		}
	}
	
	static public void toDouble(byte[] src, byte[] dst) {
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
	
	static public float[] toFloats(byte[] vals) {
		float[] res = new float[vals.length] ;
		
		for (int i = res.length-1; i >= 0; i--) {
			res[i] = (float) (vals[i] & 0xFF) ;
		}
		
		return res ;
	}

	static public void scale(float[] vals, float min, float max, float min2, float max2) {
		float scale = max - min ;
		float scale2 = max2 - min2 ;
		
		for (int i = vals.length-1; i >= 0; i--) {
			float v = vals[i] ;
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
	
	static public int[] copy(int[] vals, int off, int lng) {
		int[] vals2 = new int[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	
	static public float[] copy(float[] vals, int off, int lng) {
		float[] vals2 = new float[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	
	static public double[] copy(double[] vals, int off, int lng) {
		double[] vals2 = new double[lng] ;
		System.arraycopy(vals, off, vals2, 0, lng) ;		
		return vals2 ;
	}
	
}

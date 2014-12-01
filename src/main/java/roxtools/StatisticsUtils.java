package roxtools;

final public class StatisticsUtils {
	
	static public int indexOf(int[] a, int v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return i ;
		}
		return -1 ;
	}
	
	static public int indexOf(float[] a, float v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return i ;
		}
		return -1 ;
	}
	
	static public int indexOf(double[] a, double v) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == v) return i ;
		}
		return -1 ;
	}
	
	static public float[] groupListValuesSum(float[] list, int groupSize) {
		float[] res = new float[ list.length / groupSize ] ;
		int resSz = 0 ;
		
		float[] groupBuffer = new float[groupSize] ;
		
		for (int i = 0; i < list.length; i+= groupSize) {
			System.arraycopy(list, i, groupBuffer, 0, groupSize) ;
			
			float sum = calcSum(groupBuffer) ;
		
			res[resSz++] = sum ;
		}
		
		return res ;
	}
	
	static public float[] groupListValuesSum(float[] list, float[] groupAmplifier, int groupSize) {
		float[] res = new float[ list.length / groupSize ] ;
		int resSz = 0 ;
		
		float[] groupBuffer = new float[groupSize] ;
		
		for (int i = 0; i < list.length; i+= groupSize) {
			System.arraycopy(list, i, groupBuffer, 0, groupSize) ;
			
			for (int j = 0; j < groupBuffer.length; j++) {
				groupBuffer[j] *= groupAmplifier[j] ;
			}
			
			float sum = calcSum(groupBuffer) ;
		
			res[resSz++] = sum ;
		}
		
		return res ;
	}
	

	static public float[] groupListValuesMean(float[] list, int groupSize) {
		float[] res = new float[ list.length / groupSize ] ;
		int resSz = 0 ;
		
		float[] groupBuffer = new float[groupSize] ;
		
		for (int i = 0; i < list.length; i+= groupSize) {
			System.arraycopy(list, i, groupBuffer, 0, groupSize) ;
			
			float mean = calcMean(groupBuffer) ;
		
			res[resSz++] = mean ;
		}
		
		return res ;
	}

	static public float[] floatMatrix2FloatList(float[][] matrix) {
		int w = matrix.length ;
		int h = matrix[0].length ;
		
		float[] list = new float[ w*h ] ;
		int listSz = 0 ;
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				float f = matrix[i][j] ;
				list[ listSz++ ] = f ;
			}
		}
		
		return list ;
	}
	
	static public float[][] floatList2FloatMatrix(float[] list, int matrixW, int matrixH) {
		
		float[][] matrix = new float[matrixW][matrixH] ;


		for (int i = 0; i < list.length; i++) {
			int x = i % matrixW ;
			int y = i / matrixH ;
			
			float f = list[i];
			
			matrix[x][y] = f ;
			
		}
		
		return matrix ;
	}
	
	static public float[] subtract(float[] vals1, float[] vals2) {
		float[] res = new float[ vals1.length ] ;
		
		for (int i = 0; i < vals1.length; i++) {
			float v1 = vals1[i];
			float v2 = vals2[i];
			float diff = v1 - v2 ;
			res[i] = diff ;
		}
		
		return res ;
	}
	
	static public float[] subtractAbsolute(float[] vals1, float[] vals2) {
		float[] res = new float[ vals1.length ] ;
		
		for (int i = 0; i < vals1.length; i++) {
			float v1 = vals1[i];
			float v2 = vals2[i];
			float diff = v1 - v2 ;
			if (diff < 0) diff = -diff ;
			res[i] = diff ;
		}
		
		return res ;
	}
	
	static public float[] amplifyValues(float[] values) {
		float[] vs = new float[ values.length ] ;
		
		float[] minMax = calcMinMax(values) ;
		
		float min = minMax[0] ;
		float max = minMax[1] ;
		float scale = max - min ;
		
		for (int i = 0; i < vs.length; i++) {
			vs[i] = (values[i] - min) / scale ;
		}
		
		return vs ;
	}
	
	static public double[] amplifyValues(double[] values) {
		double[] vs = new double[ values.length ] ;
		
		double[] minMax = calcMinMax(values) ;
		
		double min = minMax[0] ;
		double max = minMax[1] ;
		double scale = max - min ;
		
		for (int i = 0; i < vs.length; i++) {
			vs[i] = (values[i] - min) / scale ;
		}
		
		return vs ;
	}
	
	static public boolean equalsArray(float[] a1 , float[] a2, float tolerance) {
		int sz = a1.length ;
		
		for (int i = 0; i < sz; i++) {
			float f1 = a1[i] ;
			float f2 = a2[i] ;
			float diff = f1 - f2 ;
			if (diff < 0) diff = -diff ;
			
			if (diff > tolerance) return false ;
		}
		
		return true ;
	}
	
	static public boolean equalsArray(double[] a1 , double[] a2, double tolerance) {
		int sz = a1.length ;
		
		for (int i = 0; i < sz; i++) {
			double d1 = a1[i] ;
			double d2 = a2[i] ;
			double diff = d1 - d2 ;
			if (diff < 0) diff = -diff ;
			
			if (diff > tolerance) return false ;
		}
		
		return true ;
	}
	
	static public boolean[] booleanArrayAND(boolean[] a1 , boolean[] a2) {
		boolean[] and = new boolean[a1.length] ;
		
		for (int i = 0; i < and.length; i++) {
			and[i] = a1[i] && a2[i] ;
		}
		
		return and ;
	}

	static public int countActivatedIndexes(boolean[] highActivatedIndexes) {
		int count = 0 ;
		
		for (int i = 0; i < highActivatedIndexes.length; i++) {
			if (  highActivatedIndexes[i] ) count++ ;
		}
		
		return count ;
	}
	
	static public float[] calcHistogramRatio(int[] values) {
		int[] minMax = calcMinMax(values) ;
		return calcHistogramRatio(values, minMax[0], minMax[1]) ;
	}
	
	static public float[] calcHistogramRatio(int[] values, int minVal, int maxVal) {
		int[] hist = calcHistogram(values, minVal, maxVal) ;
		float[] histRatio = new float[ hist.length ] ;
		
		float total = values.length ;
		
		for (int i = hist.length-1 ; i >= 0; i--) {
			histRatio[i] = hist[i] / total ;
		}
		
		return histRatio ;
	}
	
	static public int[] calcHistogram(int[] values) {
		int[] minMax = calcMinMax(values) ;
		return calcHistogram(values, minMax[0], minMax[1]) ;
	}
	
	static public int[] calcHistogram(int[] values, int minVal, int maxVal) {
		int scale = maxVal-minVal ;
		int[] hist = new int[scale+1] ;
		
		for (int i = values.length-1; i >= 0; i--) {
			int v = values[i];
			int vIdx = v-minVal ;
			hist[vIdx]++ ;
		}
		
		return hist ;
	}
	
	static public boolean[] calcHighActivatedIndexes(float[] out, double minActivation) {
		
		float minActivationF = (float) minActivation ;
		
		boolean[] highActivated = new boolean[out.length] ;
		
		for (int i = 0; i < highActivated.length; i++) {
			highActivated[i] = out[i] >= minActivationF ;
		}
		
		return highActivated ;
	}

	static public int getMostActivatedIndex(int[] output) {
		
		int v = -1 ;
		int vIdx = -1 ;
		
		for (int i = 0; i < output.length; i++) {
			int o = output[i];
			
			if (o > v) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public int getMostActivatedIndex(int[] output, int[] ignoreIndexes, int ignoreIndexesLength) {
		
		int v = -1 ;
		int vIdx = -1 ;
		
		FIND:
		for (int i = 0; i < output.length; i++) {
			
			for (int j = 0; j < ignoreIndexesLength; j++) {
				if ( i == ignoreIndexes[j] ) continue FIND ;
			}
			
			int o = output[i];
			
			if (o > v) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public int getMostActivatedIndex(float[] output) {
		
		float v = Float.NEGATIVE_INFINITY ;
		int vIdx = -1 ;
		
		for (int i = 0; i < output.length; i++) {
			float o = output[i];
			
			if (o > v) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public int getMostActivatedIndex(float[] output, int ignoreIndex) {
		
		float v = Float.NEGATIVE_INFINITY ;
		int vIdx = -1 ;
		
		for (int i = 0; i < output.length; i++) {
			float o = output[i];
			
			if (o > v && i != ignoreIndex) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public int getMostActivatedIndex(float[] output, int ignoreIndex, float minActivation) {
		
		float v = Float.NEGATIVE_INFINITY ;
		int vIdx = -1 ;
		
		for (int i = 0; i < output.length; i++) {
			float o = output[i];
			
			if (o > minActivation && o > v && i != ignoreIndex) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public int getMostActivatedIndex(double[] output) {
		
		double v = Double.NEGATIVE_INFINITY ;
		int vIdx = -1 ;
		
		for (int i = 0; i < output.length; i++) {
			double o = output[i];
			
			if (o > v) {
				v = o ;
				vIdx = i ;
			}
		}
		
		return vIdx ;
	}
	
	static public double[] float2double( float[] fs ) {
		double[] ds = new double[ fs.length ] ;
		
		for (int i = 0; i < ds.length; i++) {
			ds[i] = fs[i] ;
		}
		
		return ds ;
	}
	
	static public float[] double2float( double[] ds ) {
		float[] fs = new float[ ds.length ] ;
		
		for (int i = 0; i < fs.length; i++) {
			fs[i] = (float) ds[i] ;
		}
		
		return fs ;
	}
	
	static public double[] calcDataInfo(double[] vals) {
		
		if (vals.length == 1) return new double[] { vals[0] , vals[0] , 0} ;
		
		double mean = calcMean(vals) ;
		
		double sum = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (vals.length-1) ; 
		
		double deviation = Math.sqrt( variation ) ; 
		

		double maxDist = deviation * 2 ;
		
		double meanByNormal = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] ;
			double dist = v - deviation ;
			if (dist < 0) dist *= -1 ;
			
			double weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanByNormal += v * weight ;
		}
		
		meanByNormal /= vals.length ;

		return new double[] { mean, meanByNormal, deviation } ;
	}


	static public float[] calcDataInfo(float[] vals) {
		
		if (vals.length == 1) return new float[] { vals[0] , vals[0] , vals[0] , vals[0] , vals[0] , vals[0] } ;
		
		float mean = calcMean(vals) ;
		
		double sum = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (vals.length-1) ; 
		
		if (mean == 0 && sum == 0) {
			return new float[] { 0 , 0 , 0 , 0 , 0 , 0 } ;
		}
		
		float deviation = (float) Math.sqrt( variation ) ; 
		

		float maxDist = deviation * 2 ;
		
		double meanByNormal = 0 ;
		double meanByNormalInit = 0 ;
		double meanByNormalCenter = 0 ;
		double meanByNormalEnd = 0 ;
		
		int centerSize = vals.length/3 ;
		int centerInit = centerSize ;
		int centerEnd =  centerInit + centerSize ;
		
		int initSize = centerInit ;
		int endSize = vals.length - centerEnd ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i] ;
			float dist = v - deviation ;
			if (dist < 0) dist *= -1 ;
			
			float weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanByNormal += v * weight ;
			
			if ( i < centerInit ) {
				meanByNormalInit += v * weight ; 
			}
			
			if ( i >= centerInit && i < centerEnd ) {
				meanByNormalCenter += v * weight ; 
			}
			
			if ( i >= centerEnd ) {
				meanByNormalEnd += v * weight ; 
			}
		}
		
		meanByNormal /= vals.length ;
		meanByNormalInit /= initSize ;
		meanByNormalCenter /= centerSize ;
		meanByNormalEnd /= endSize ;
		
		return new float[] { mean, (float)meanByNormal, deviation , (float)meanByNormalCenter , (float)meanByNormalInit , (float)meanByNormalEnd } ;
	}

	static public float calcSum(float[] vals) {
		double sum = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			sum += vals[i];
		}

		return (float) sum ;
	}
	
	static public double calcSum(double[] vals) {
		double sum = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			sum += vals[i];
		}

		return sum ;
	}
	
	static public int calcMean(int[] vals) {
		int mean = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			mean += vals[i];
		}
		
		mean /= vals.length ;

		return mean ;
	}
	
	static public double calcMean(double[] vals) {
		double mean = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			mean += vals[i];
		}
		
		mean /= vals.length ;

		return mean ;
	}


	static public float calcMean(float[] vals) {
		double mean = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			mean += vals[i];
		}

		return (float)( mean / sz ) ;
	}
	
	static public float calcMean(float[] vals, int ignoreIndex) {
		double mean = 0 ;
		int meanTotal = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			if (i == ignoreIndex) continue ;
			mean += vals[i];
			meanTotal++ ;
		}

		return (float)( mean / meanTotal ) ;
	}
	
	static public float calcMeanLimited(float[] vals, float maxValue) {
		double mean = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			float v = vals[i];
			mean += v < maxValue ? v : maxValue ;
		}

		return (float)( mean / sz ) ;		
	}
	
	static public double calcMeanAsDouble(float[] vals) {
		double mean = 0 ;
		
		int sz = vals.length ;
		
		for (int i = 0; i < sz; i++) {
			mean += vals[i];
		}

		return mean / sz ;
	}
	
	static public int[] calcMinMax(int[] vals) {
		int min = Integer.MAX_VALUE ;
		int max = Integer.MIN_VALUE ;
		
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		return new int[] { min , max } ;
	}
	
	static public void calcMinMax(int[] vals, float[] ret) {
		int min = Integer.MAX_VALUE ;
		int max = Integer.MIN_VALUE ;
		
		for (int i = 0; i < vals.length; i++) {
			int v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		ret[0] = min ;
		ret[1] = max ;
	}
	
	static public double[] calcMinMax(double[] vals) {
		double min = Double.POSITIVE_INFINITY ;
		double max = Double.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		return new double[] { min , max } ;
	}
	
	static public void calcMinMax(double[] vals, double[] ret) {
		double min = Double.POSITIVE_INFINITY ;
		double max = Double.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		ret[0] = min ;
		ret[1] = max ;
	}
	
	static public float[] calcMinMax(float[] vals) {
		float min = Float.POSITIVE_INFINITY ;
		float max = Float.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		return new float[] { min , max } ;
	}
	
	static public float[] calcMinMax(float[] vals, float ignoreValue) {
		float min = Float.POSITIVE_INFINITY ;
		float max = Float.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i];
			
			if (v == ignoreValue) continue ;
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		return new float[] { min , max } ;
	}
	
	static public double[] calcMinMax(double[] vals, double ignoreValue) {
		double min = Double.POSITIVE_INFINITY ;
		double max = Double.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i];
			
			if (v == ignoreValue) continue ;
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}
		
		return new double[] { min , max } ;
	}
	
	static public void calcMinMax(float[] vals, float[] ret) {
		float min = Float.POSITIVE_INFINITY ;
		float max = Float.NEGATIVE_INFINITY ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i];
			
			if ( v < min ) min = v ;
			if ( v > max ) max = v ;
		}

		ret[0] = min ;
		ret[1] = max ;
	}

	static public float calcMinAndIndex(float[] vals, int[] returnIndex) {
		float min = Float.POSITIVE_INFINITY ;
		int minIdx = -1 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i];
			
			if ( v < min ) {
				min = v ;
				minIdx = i ;
			}
		}
		
		returnIndex[0] = minIdx ;
		
		return min ;
	}
	
	static public float calcMaxAndIndex(float[] vals, int[] returnIndex) {
		float max = Float.NEGATIVE_INFINITY ;
		int maxIdx = -1 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i];
			
			if ( v > max ) {
				max = v ;
				maxIdx = i ;
			}
		}
		
		returnIndex[0] = maxIdx ;
		
		return max ;
	}
	
	static public float[] scale(float[] vals, float scale) {
		float[] norm = new float[ vals.length ] ;
		
		for (int i = 0; i < norm.length; i++) {
			norm[i] = vals[i] * scale ;
		}
		
		return norm ;
	}
	
	static public double[] scale(double[] vals, double scale) {
		double[] norm = new double[ vals.length ] ;
		
		for (int i = 0; i < norm.length; i++) {
			norm[i] = vals[i] * scale ;
		}
		
		return norm ;
	}
	
	static public float[] normalize(float[] vals) {
		float[] minMax = calcMinMax(vals) ;
		
		return normalize(vals, minMax[0], minMax[1]) ;
	}
	
	static public float[] normalize(float[] vals, float min, float max) {
		float scale = max - min ;
		
		float[] norm = new float[ vals.length ] ;
		
		for (int i = 0; i < norm.length; i++) {
			norm[i] = (vals[i] - min) / scale ;
		}
		
		return norm ;
	}
	
	static public double[] normalize(double[] vals) {
		double[] minMax = calcMinMax(vals) ;
		
		return normalize(vals, minMax[0], minMax[1]) ;
	}

	static public double[] normalize(double[] vals, double min, double max) {
		double scale = max - min ;
		
		double[] norm = new double[ vals.length ] ;
		
		for (int i = 0; i < norm.length; i++) {
			norm[i] = (vals[i] - min) / scale ;
		}
		
		return norm ;
	}

	
	static public float calcMean(float[] vals, int off, int length) {
		double mean = 0 ;
		
		int limit = off + length ;
		
		for (int i = off; i < limit; i++) {
			mean += vals[i];
		}
		
		mean /= length ;

		return (float) mean ;
	}

	static public float calcMeanCenter(float[] vals) {
		int centerSize = vals.length/3 ;
		int centerInit = centerSize ;
		
		return calcMean(vals, centerInit, centerSize) ;
	}
	
	static public double calcMeanNoOutlier(double[] vals, double maxDeviation) {
		
		if (vals.length == 1) return vals[0] ;
		
		double mean = calcMean(vals) ;
		
		double deviation = calcStandardDeviation(vals, mean) ;
		
		if (deviation == 0) return 0 ;
		
		double maxDist = deviation * maxDeviation ;
		
		double meanNoOutlier = 0 ;
		int meanNoOutlierSz = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] ;
			double dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			if (dist > maxDist) continue ;
			
			meanNoOutlier += v ;
			meanNoOutlierSz++ ; 
		}
		
		meanNoOutlier /= meanNoOutlierSz ;

		return meanNoOutlier ;
	}

	static public float calcMeanNoOutlier(float[] vals, float maxDeviation) {
		
		if (vals.length == 1) return vals[0] ;
		
		float mean = calcMean(vals) ;
		
		float deviation = calcStandardDeviation(vals, mean) ;
		
		if (deviation == 0) return 0 ;
		
		float maxDist = deviation * maxDeviation ;
		
		double meanNoOutlier = 0 ;
		int meanNoOutlierSz = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i] ;
			float dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			if (dist > maxDist) continue ;
			
			meanNoOutlier += v ;
			meanNoOutlierSz++ ; 
		}
		
		meanNoOutlier /= meanNoOutlierSz ;

		return (float) meanNoOutlier ;
	}
	
	static public double calcMeanByNormal(double[] vals) {
		
		if (vals.length == 1) return vals[0] ;
		
		double mean = calcMean(vals) ;
		
		double deviation = calcStandardDeviation(vals, mean) ;
		
		if (deviation == 0) return 0 ;
		
		double maxDist = deviation * 2 ;
		
		double meanWeighted = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] ;
			double dist = (v - mean) ;
			if (dist < 0) dist = -dist ;
			
			double weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanWeighted += v * weight ;
		}
		
		meanWeighted /= vals.length ;

		return meanWeighted ;
	}
	
	static public float calcMeanByNormal(float[] vals) {
		return calcMeanByNormal(vals, 2) ;
	}
	
	static public float calcMeanByNormal(float[] vals, float maxDeviation) {
		
		if (vals.length == 1) return vals[0] ;
		
		float mean = calcMean(vals) ;
		
		float deviation = calcStandardDeviation(vals, mean) ;
		
		if (deviation == 0) return 0 ;
		
		float maxDist = deviation * maxDeviation ;
		
		double meanWeighted = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i] ;
			float dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			float weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanWeighted += v * weight ;
		}
		
		meanWeighted /= vals.length ;

		return (float) meanWeighted ;
	}
	
	static public float calcMeanByNormalCenter(float[] vals) {
		if (vals.length == 1) return vals[0] ;
		
		int centerSize = vals.length/3 ;
		int centerInit = centerSize ;
		
		float mean = calcMean(vals, centerInit, centerSize) ;
		
		float deviation = calcStandardDeviation(vals, centerInit, centerSize) ;
		
		if (deviation == 0) return 0 ;
		
		float maxDist = deviation * 2 ;
		
		double meanByNormalCenter = 0 ;
		
		int limit = centerInit + centerSize ;
		
		for (int i = centerInit; i < limit; i++) {
			float v = vals[i] ;
			float dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			float weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanByNormalCenter += v * weight ; 
		}
		
		meanByNormalCenter /= centerSize ;

		return (float) meanByNormalCenter ;
	}
	
	static public float calcMeanByNormalInit(float[] vals) {
		if (vals.length == 1) return vals[0] ;
		
		int centerSize = vals.length/3 ;
		
		int initSize = centerSize ;
		
		float mean = calcMean(vals, 0, initSize) ;
		
		float deviation = calcStandardDeviation(vals, 0, initSize) ;
		
		if (deviation == 0) return 0 ;
		
		float maxDist = deviation * 2 ;
		
		double meanByNormalInit = 0 ;
		
		for (int i = 0; i < initSize; i++) {
			float v = vals[i] ;
			float dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			float weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanByNormalInit += v * weight ;
		}
		
		meanByNormalInit /= initSize ;

		return (float) meanByNormalInit ;
	}


	static public float calcMeanByNormalEnd(float[] vals) {
		if (vals.length == 1) return vals[0] ;
		
		int centerSize = vals.length/3 ;
		int centerInit = centerSize ;
		int centerEnd =  centerInit + centerSize ;
		
		int endSize = vals.length - centerEnd ;
		
		float mean = calcMean(vals, centerEnd, endSize) ;
		
		float deviation = calcStandardDeviation(vals, centerEnd, endSize) ;
		
		if (deviation == 0) return 0 ;
		
		float maxDist = deviation * 2 ;
		
		double meanByNormalEnd = 0 ;
		
		int limit = vals.length ;
		
		for (int i = centerEnd; i < limit; i++) {
			float v = vals[i] ;
			float dist = v - mean ;
			if (dist < 0) dist = -dist ;
			
			float weight = (maxDist - dist) / maxDist ;
			if (weight < 0) weight = 0 ;
			
			meanByNormalEnd += v * weight ;
		}
		
		meanByNormalEnd /= endSize ;

		return (float) meanByNormalEnd ;
	}


	static public double calcMeanUnderStandardDeviation(double[] set, double maxStandardDeviationRatio) {
		double mean = calcMean(set) ;
		
		double deviation = StatisticsUtils.calcStandardDeviation(set, mean) ;
		
		double total = 0 ;
		int samples = 0 ;
		
		for (int i = set.length-1 ; i >= 0; i--) {
			double v = set[i];
			double diff = mean - v ;
			if (diff < 0) diff = -diff ;
			
			if (diff < deviation * maxStandardDeviationRatio) {
				total += v ;
				samples++ ;
			}
		}
		
		if (samples == 0) return mean ;
		
		return total / samples ;
	}
	
	static public float calcMeanUnderStandardDeviation(float[] set, double maxStandardDeviationRatio) {
		float mean = calcMean(set) ;
		
		float deviation = StatisticsUtils.calcStandardDeviation(set, mean) ;
		
		float total = 0 ;
		int samples = 0 ;
		
		for (int i = set.length-1 ; i >= 0; i--) {
			float v = set[i];
			float diff = mean - v ;
			if (diff < 0) diff = -diff ;
			
			if (diff < deviation * maxStandardDeviationRatio) {
				total += v ;
				samples++ ;
			}
		}
		
		if (samples == 0) return mean ;
		
		return total / samples ;
	}
	
	static public int calcMeanUnderStandardDeviation(int[] set, double maxStandardDeviationRatio) {
		int mean = calcMean(set) ;
		
		double deviation = StatisticsUtils.calcStandardDeviation(set, mean) ;
		
		int total = 0 ;
		int samples = 0 ;
		
		for (int i = set.length-1 ; i >= 0; i--) {
			float v = set[i];
			float diff = mean - v ;
			if (diff < 0) diff = -diff ;
			
			if (diff < deviation * maxStandardDeviationRatio) {
				total += v ;
				samples++ ;
			}
		}
		
		if (samples == 0) return mean ;
		
		return total / samples ;
	}
	
	static public double calcStandardDeviation(int[] vals) {
		return calcStandardDeviation(vals , calcMean(vals)) ;
	}
	
	static public double calcStandardDeviation(int[] vals, int mean) {
		if (vals.length == 1) return 0 ;
		
		double sum = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (vals.length-1) ; 
		
		double deviation = Math.sqrt( variation ) ; 
		
		return deviation ;
	}
	
	static public double calcStandardDeviation(double[] vals) {
		return calcStandardDeviation(vals, calcMean(vals)) ;
	}
	
	static public double calcStandardDeviation(double[] vals, double mean) {
		if (vals.length == 1) return 0 ;
		
		double sum = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (vals.length-1) ; 
		
		double deviation = Math.sqrt( variation ) ; 
		
		return deviation ;
	}
	
	static public float calcStandardDeviation(float[] vals) {
		return calcStandardDeviation(vals , calcMean(vals) ) ;
	}
	
	static public float calcStandardDeviation(float[] vals, float mean) {
		if (vals.length == 1) return 0 ;
		
		double sum = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			float v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (vals.length-1) ; 
		
		float deviation = (float) Math.sqrt( variation ) ; 
		
		return deviation ;
	}
	
	static public float calcStandardDeviation(float[] vals, int off, int length) {
		if (vals.length == 1) return 0 ;
		
		float mean = calcMean(vals, off, length) ;
		
		double sum = 0 ;
		
		int limit = off + length ;
		
		for (int i = off; i < limit; i++) {
			float v = vals[i] - mean ;
			v = v * v ;
			sum += v ;
		}
		
		double variation = sum / (length-1) ; 
		
		float deviation = (float) Math.sqrt( variation ) ; 
		
		return deviation ;
	}
	
	static public double[] calcNormalDistribution(float[] data , int divisions) {
		
		int divisionsM1 = divisions-1 ;
		
		float[] minMax = calcMinMax(data) ;
		
		float dataMin = minMax[0] ;
		float dataMax = minMax[1] ;
		float dataScale = dataMax - dataMin ;
		
		float dataMean = calcMean(data) ;
		
		int[] divs = new int[divisions] ;
		
		for (int i = 0; i < data.length; i++) {
			float v = data[i] ;
			v = (v-dataMin) / dataScale ;
			
			int divIdx = (int) (v * divisionsM1 + 0.01) ;
			
			divs[divIdx]++ ;
		}
		
		{
			float v = (dataMean - dataMin) / dataScale ;
			
			int divIdx = (int) (v * divisionsM1 + 0.01) ;
			
			int div1 = divs[divIdx] ;
			int div2 = divs[divIdx-1] ;
			
			double centerDataRatio = ((div1+div2)*1d) / data.length ;
			
			return new double[] { dataMean , centerDataRatio } ;
		}
		
	}
	
	static public float calcSpatialDistribution(float[] vals) {
		return calcSpatialDistribution(vals, 0, vals.length) ;
	}
	
	static public float calcSpatialDistributionCenter(float[] vals) {
		int size = vals.length/3 ;
		int init = size ;
		return calcSpatialDistribution(vals, init, size) ;
	}
	
	static public float calcSpatialDistributionInit(float[] vals) {
		return calcSpatialDistribution(vals, 0, vals.length/3) ;
	}
	
	static public float calcSpatialDistributionEnd(float[] vals) {
		int size = vals.length/3 ;
		int init = size*2 ;
		return calcSpatialDistribution(vals, init, size) ;
	}
	
	static public float calcSpatialDistribution(float[] vals, int off, int length) {
		float idx = calcDotDensityIndex(vals, off, length) ;
		return idx / length ;
	}
	
	static public int calcDotDensityIndex(float[] vals) {
		return calcDotDensityIndex(vals, 0, vals.length) ;
	}

	static public int calcDotDensityIndex(float[] vals, int off, int lng) {
		double min = Double.POSITIVE_INFINITY ;
		double max = Double.NEGATIVE_INFINITY ;
		
		int size = vals.length ;
		
		int limit = off + lng ;
		
		if (limit > size) throw new IllegalArgumentException("off: "+ off +" ; length: "+ lng +" ; size: "+ vals.length) ;
		
		for (int i=off; i< limit ; i++) {
			double v = vals[i] ;

			if (min > v) min = v ;
			if (max < v) max = v ;
		}
		
		double dist = max - min ;

		int dotIdx = 0 ;
		double dotDensity = Double.NEGATIVE_INFINITY ;

		for (int i=off; i< limit; i++) {
			double v = vals[i] ;
			
			double density = 0 ;
			
			for (int j = 0; j < size ; j++) {
				double v2 = vals[j] ;
				
				double diff = v - v2 ;
				if (diff < 0) diff = -diff ;
				
				double force = dist - diff ;
				
				density += force * force ;
			}

			if (dotDensity < density) {
				dotDensity = density ;
				dotIdx = i ;
			}
		}
		
		
		return dotIdx ;
	}
	
	static public int calcDotDensityIndex(int[] vals) {
		return calcDotDensityIndex(vals, 0, vals.length) ;
	}
	
	static public int calcDotDensityIndex(int[] vals, int off, int lng) {
		int min = vals[0] ;
		int max = vals[0] ;
		
		int size = vals.length ;
		
		int limit = off + lng ;
		
		if (limit > size) throw new IllegalArgumentException("off: "+ off +" ; length: "+ lng +" ; size: "+ vals.length) ;
		
		for (int i=off; i< limit ; i++) {
			int v = vals[i] ;

			if (min > v) min = v ;
			if (max < v) max = v ;
		}
		
		int dist = max - min ;

		int dotIdx = 0 ;
		int dotDensity = Integer.MIN_VALUE ;

		for (int i=off; i< limit; i++) {
			int v = vals[i] ;
			
			int density = 0 ;
			
			for (int j = 0; j < size ; j++) {
				int v2 = vals[j] ;
				
				int diff = v - v2 ;
				if (diff < 0) diff = -diff ;
				
				int force = dist - diff ;
				
				density += force * force ;
			}

			if (dotDensity < density) {
				dotDensity = density ;
				dotIdx = i ;
			}
		}
		
		
		return dotIdx ;
	}
	
	static public int[] calcDifferences(int[] ns1, int[] ns2) {
		int[] diffs = new int[ ns1.length ] ;
		
		for (int i = diffs.length-1; i >= 0; i--) {
			int v1 = ns1[i];
			int v2 = ns2[i];
			
			int diff ;
			if (v1 > v2) diff = v1 - v2 ;
			else diff = v2 - v1 ;
			
			diffs[i] = diff ;
		}
		
		return diffs ;
	}
	
	static public float[] calcDifferences(float[] fs1, float[] fs2) {
		float[] diffs = new float[ fs1.length ] ;
		
		for (int i = diffs.length-1; i >= 0; i--) {
			float f1 = fs1[i];
			float f2 = fs2[i];
			
			float diff ;
			if (f1 > f2) diff = f1 - f2 ;
			else diff = f2 - f1 ;
			
			diffs[i] = diff ;
		}
		
		return diffs ;
	}
	
	static public float[] calcDifferencesSquare(float[] fs1, float[] fs2) {
		float[] diffs = new float[ fs1.length ] ;
		
		for (int i = diffs.length-1; i >= 0; i--) {
			float f1 = fs1[i];
			float f2 = fs2[i];
			
			float diff ;
			if (f1 > f2) diff = f1 - f2 ;
			else diff = f2 - f1 ;
			
			diffs[i] = diff * diff ;
		}
		
		return diffs ;
	}
	
	static public void cutMinValues(float[] values, float minValue, float resetValue) {
		for (int i = 0; i < values.length; i++) {
			if ( values[i] < minValue ) values[i] = resetValue ;
		}
	}

	static public void cutMaxValues(float[] values, float maxValue, float resetValue) {
		for (int i = 0; i < values.length; i++) {
			if ( values[i] >= maxValue ) values[i] = resetValue ;
		}
	}
	
	static public float calcMeanValuesIgnoreBorder(float[] values, int width, int height, int borderSize) {
		double total = 0 ;
		int totalSize = 0 ;
		
		int yLimit = height-borderSize ;
		int xLimit = width-borderSize ;
		
		for (int j = borderSize; j < yLimit; j++) {
			int jIdx = j * width ;
			for (int i = borderSize; i < xLimit; i++) {
				int iIdx = jIdx + i ;
				
				float v = values[iIdx] ;
				
				total += v ;
				totalSize++ ;
			}
		}
	
		return (float) (total / totalSize) ;
	}
	
	static public float[] spreadValues(float[] values, int width, int height, int valueSize, float[] spreadRange) {
		
		int lineValsSize = width * valueSize ;
		
		int spreadBorderSz = spreadRange.length -1 ;
		
		int spreadSize = spreadBorderSz + 1 + spreadBorderSz ;
		int spreadCenter = spreadBorderSz ;
		
		float[] values2 = new float[values.length] ;
				
		for (int j = 0; j < height; j++) {
			int jIdx = j * lineValsSize ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + (i * valueSize) ;
				
				for (int shiftY = 0; shiftY < spreadSize; shiftY++) {
					int yDist = shiftY-spreadCenter ;
					int y = j+yDist ;
					if (y < 0 || y >= height) continue ;
					
					int yIdx = y * lineValsSize ;
					int yDistPositive = yDist < 0 ? -yDist : yDist ;
					
					for (int shiftX = 0; shiftX < spreadSize; shiftX++) {
						int xDist = shiftX-spreadCenter;
						int x = i+xDist ;
						if (x < 0 || x >= width) continue ;
						
						int xIdx = yIdx + (x*valueSize) ;
						int xDistPositive = xDist < 0 ? -xDist : xDist ;
						
						
						int spreadRangeIdx = Math.max( yDistPositive , xDistPositive ) ;
						float spread = spreadRange[spreadRangeIdx] ;
						
						for (int k = 0; k < valueSize; k++) {
							values2[xIdx+k] += values[iIdx+k] * spread ; 
						}
						
					}	
				}
			}
		}
		
		return values2 ;
	}
	
	static public void printMatrix(float[] vals, int width, int height, int valueSize) {
		
		int lineValsSize = width * valueSize ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j * lineValsSize ;
			
			for (int i = 0; i < width; i++) {
				int iIdx = jIdx + (i * valueSize) ;
				
				System.out.print("(");
				for (int k = 0; k < valueSize; k++) {
					float v = vals[iIdx+k] ;
					if (k > 0) System.out.print(", ");
					System.out.print(v);
				}
				System.out.print(") ");
			}
			
			System.out.println();
		}
		
		
	}
	
	public static void main(String[] args) {
		
		double[] vals = new double[] { 1 ,1 , 2 , 3 , 3 , 3 , 4 , 5 , 10 } ;
		//double[] vals = new double[] { 0 , 1 , 0 , 1 , 0 , 1 , 0 , 1} ;
		
		double mean = calcMean(vals) ;
		double weightedMean = calcMeanByNormal(vals) ;
		double d = calcStandardDeviation(vals) ;
		
		System.out.println("mean: "+mean);
		System.out.println("weightedMean: "+weightedMean);
		System.out.println("d: "+d);
		System.out.println("d*2: "+d*2);
		System.out.println("-----------");
		
		double in1 = 0 ;
		double in2 = 0 ;
		
		for (int i = 0; i < vals.length; i++) {
			double v = vals[i];
			double dist = mean - v ;
			if (dist < 0) dist *= -1 ;
			
			if ( dist < d ) in1++ ;
			if ( dist < d*2 ) in2++ ;
			
			System.out.println(i+"> "+ v +" > "+ dist);
		}
		
		System.out.println("-----------");
		System.out.println("in1> "+ in1 +" > "+ ( in1/vals.length ));
		System.out.println("in2> "+ in2 +" > "+ ( in2/vals.length ));
		
	}
	
}

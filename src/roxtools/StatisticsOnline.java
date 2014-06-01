package roxtools;

abstract public class StatisticsOnline {

	protected int n = 0 ;
	
	public void clear() {
		n = 0 ;
	}
	
	final public int getSamplesSize() {
		return n ;
	}
	
	abstract protected void calcSample(double value) ;
	
	protected void calcSample(float value) {
		calcSample( (double)value ) ;
	}

	final public void add( double val ) {
		calcSample(val) ;
		n++ ;
	}
	
	final public void add( float val ) {
		calcSample(val) ;
		n++ ;
	}
	
	final public void add( double[] vals ) {
		for (int i = vals.length-1; i >= 0 ; i--) {
			calcSample( vals[i] ) ;
		}
		
		n += vals.length ;
	}
	
	final public void add( float[] vals ) {
		for (int i = vals.length-1; i >= 0 ; i--) {
			calcSample( vals[i] ) ;
		}
		
		n += vals.length ;
	}
	
	/////////////////////////////////////////////////////////////////
	
	static protected class MeanOnlineImplem extends StatisticsOnline {
		protected double sum = 0 ;
		
		public void clear() {
			super.clear() ;
			sum = 0 ;
		}
		
		@Override
		protected void calcSample(double value) {
			sum += value ;
		}
		
		@Override
		protected void calcSample(float value) {
			sum += value ;
		}
		
		public double mean() {
			return sum/n ;
		}
	}
	
	static protected class ScaleOnlineImplem extends MeanOnlineImplem {
		protected double min = Double.POSITIVE_INFINITY ;
		protected double max = Double.NEGATIVE_INFINITY ;
		
		public void clear() {
			super.clear() ;
			min = Double.POSITIVE_INFINITY ;
			max = Double.NEGATIVE_INFINITY ;
		}
		
		@Override
		protected void calcSample(double value) {
			super.calcSample(value) ;
			if (value < min) min = value ;
			if (value > max) max = value ;
		}
		
		@Override
		protected void calcSample(float value) {
			super.calcSample(value) ;
			if (value < min) min = value ;
			if (value > max) max = value ;
		}
		
		public double getMinimal() {
			return min;
		}
		
		public double getMaximal() {
			return max;
		}
		
		public double scale() {
			return max-min ;
		}
		
	}
	
	
	static protected class StandardDeviationOnlineImplem extends MeanOnlineImplem {
		private double sumSqr = 0 ;
		
		@Override
		protected void calcSample(double val) {
			super.calcSample(val) ;
			sumSqr += val*val ;
		}
		
		@Override
		protected void calcSample(float val) {
			super.calcSample(val) ;
			sumSqr += val*val ;
		}
		
		public double variance() {
			return (sumSqr - ( (sum*sum)/n )) / (n-1) ;
		}
		
		public double deviation() {
			return Math.sqrt( variance() ) ;
		}
		
	}
	

	static protected class StandardDeviationAndScaleOnlineImplem extends ScaleOnlineImplem {
		private double sumSqr = 0 ;
		
		@Override
		protected void calcSample(double val) {
			super.calcSample(val) ;
			sumSqr += val*val ;
		}
		
		@Override
		protected void calcSample(float val) {
			super.calcSample(val) ;
			sumSqr += val*val ;
		}
		
		public double variance() {
			return (sumSqr - ( (sum*sum)/n )) / (n-1) ;
		}
		
		public double deviation() {
			return Math.sqrt( variance() ) ;
		}
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	static public double[] mergeScale(double min1, double scale1, double min2, double scale2) {
		double max1 = min1 + scale1 ; 
		double max2 = min2 + scale2 ;
		
		double minMerge = min1 < min2 ? min1 : min2 ;
		
		double maxMerge = max1 > max2 ? max1 : max2 ;
		
		double scaleMerge = maxMerge - minMerge ;
		
		return new double[] { minMerge , scaleMerge } ;
	}
	
	static public double[] mergeStandardDeviation(double deviation1, double mean1, int n1, double deviation2, double mean2, int n2) {
		double sum1 = mean1 * n1 ;
		double sum2 = mean2 * n2 ;
		
		double variance1 = deviation1 * deviation1 ;
		double variance2 = deviation2 * deviation2 ;
	
		double sumSqr1 = (variance1 * (n1-1)) + ( (sum1*sum1)/n1 ) ;
		double sumSqr2 = (variance2 * (n2-1)) + ( (sum2*sum2)/n2 ) ;
		
		////
		
		int nMerge = n1 + n2 ;
		
		double sumMerge = sum1 + sum2 ;
		double sumSqrMerge = sumSqr1 + sumSqr2 ;
		
		double meanMerge = sumMerge / nMerge ;
		
		double varianceMerge = (sumSqrMerge - ( (sumMerge*sumMerge)/nMerge )) / (nMerge-1) ;
		
		double deviationMerger = Math.sqrt( varianceMerge ) ;
		
		return new double[] { deviationMerger , meanMerge , nMerge } ;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	final static public class StandardDeviationAndScaleOnline extends StandardDeviationAndScaleOnlineImplem {}
	final static public class StandardDeviationOnline extends StandardDeviationOnlineImplem {}
	final static public class ScaleOnline extends ScaleOnlineImplem {}
	final static public class MeanOnline extends MeanOnlineImplem {}
	
}

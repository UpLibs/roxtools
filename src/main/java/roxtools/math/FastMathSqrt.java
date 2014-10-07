package roxtools.math;

final public class FastMathSqrt {

	final private int precision ;
	final private double limit ;
	
	public FastMathSqrt(double limit) {
		this( (int)(Math.sqrt(limit) * 3) , limit) ;
	}
	
	public FastMathSqrt(int precision, double limit) {
		this.precision = precision;
		this.limit = limit;
		
		computeTable() ;
	}

	public double getLimit() {
		return limit;
	}
	
	public double getStep() {
		return step;
	}
	
	private double[] table ;
	private double step ;
	
	private void computeTable() {
		
		double[] table = new double[precision+1] ;
		int tableSz = 0 ;
		
		double step = limit / precision ;
		
		for (double v = 0; v < limit; v+= step) {
			
			double r = Math.sqrt(v) ;
			
			table[tableSz++] = r ;
		}
		
		System.out.println(">> "+this.getClass().getName()+"> "+ limit +" / "+ precision +" = "+ step +" > table size: "+ (table.length*8) );
		
		this.table = table ;
		this.step = step ;
	}
	

	public double calcSimple(double v) {
		int idx = (int) (v /step) ;
		return table[idx] ;
	}
	
	
	public double calc(double v) {
		double idxRatio = (v /step) ;
		int idx = (int) idxRatio ;
		
		double vRatio = idxRatio - idx ;
		
		double res1 = table[idx] ;
		double res2 = table[idx+1] ;
		
		double rDist = res2-res1 ;
		
		double r = res1 + (rDist * vRatio) ; 
		
		//System.out.println("   "+ v +"> "+ idx +" - "+ vRatio +" - "+ idxRatio+ " > "+ res1 +" .. "+ r +" .. "+ res2);
		
		return r ;
	}
	
	public static void main(String[] args) {
		
		FastMathSqrt fastMathSqrt = new FastMathSqrt(256*256*2) ;
		
		double diffMean = 0 ;
		int diffMeanSz = 0 ;
		double diffMax = 0 ;
		
		for (double i = 0; i < fastMathSqrt.limit; i+= 0.5) {
			
			double r = fastMathSqrt.calcSimple(i) ;
			
			double r2 = Math.sqrt(i) ;
			
			double diff = r2 - r ;
			
			System.out.println(i+"> "+ r +" =~ "+ r2 +" >>> \t "+ diff);
		
			diffMean += diff ;
			diffMeanSz++ ;
			
			if (diff > diffMax) diffMax = diff ;
		}
		
		diffMean /= diffMeanSz ;
		
		System.out.println("diffMean> "+diffMean);
		System.out.println("diffMax> "+ diffMax);
	}
	
}

package roxtools.math;

final public class FastMathSqrtInt {

	final private int precision ;
	final private int limit ;
	
	public FastMathSqrtInt(int limit) {
		this( (int)(Math.sqrt(limit) * 3) , limit) ;
	}
	
	public FastMathSqrtInt(int precision, int limit) {
		if (precision > limit) precision = limit ;
		
		this.precision = precision;
		this.limit = limit;
		
		computeTable() ;
	}

	public int getLimit() {
		return limit;
	}
	
	public int getStep() {
		return step;
	}
	
	private int[] table ;
	private int step ;
	private double stepD ;
	
	private void computeTable() {

		int step = limit / precision ;
		
		int[] table = new int[limit/step+1] ;
		int tableSz = 0 ;
		
		for (int v = 0; v < limit; v+= step) {
			double r = Math.sqrt(v) ;
			
			table[tableSz++] = (int) r ;
		}
		

		System.out.println(">> "+this.getClass().getName()+"> "+ limit +" / "+ precision +" = "+ step +" > table size: "+ (table.length*4) );
		
		this.table = table ;
		this.step = step ;
		this.stepD = step ;
	}
	

	public int calcSimple(int v) {
		return table[v / step] ;
	}
	
	
	public int calc(int v) {
		double idxRatio = (v /stepD) ;
		int idx = (int) idxRatio ;
		
		double vRatio = idxRatio - idx ;
		
		int res1 = table[idx] ;
		int res2 = table[idx+1] ;
		
		int rDist = res2-res1 ;
		
		int r = (int) (res1 + (rDist * vRatio)) ; 
		
		//System.out.println("   "+ v +"> "+ idx +" - "+ vRatio +" - "+ idxRatio+ " > "+ res1 +" .. "+ r +" .. "+ res2);
		
		return r ;
	}
	
	public static void main(String[] args) {
		
		FastMathSqrtInt fastMathSqrt = new FastMathSqrtInt(256*256*2) ;
		
		double diffMean = 0 ;
		int diffMeanSz = 0 ;
		double diffMax = 0 ;
		
		for (double i = 0; i < fastMathSqrt.limit; i+= 0.5) {
			int n = (int) i ;
			double r = fastMathSqrt.calcSimple(n) ;
			
			double r2 = Math.sqrt(n) ;
			
			double diff = r2 - r ;
			
			System.out.println(i+"> "+ r +" =~ "+ r2 +" >>> \t "+ diff);
		
			diffMean += diff ;
			diffMeanSz++ ;
			
			if (diff > diffMax) diffMax = diff ;
		}
		
		diffMean /= diffMeanSz ;
		
		System.out.println("diffMean> "+ diffMean);
		System.out.println("diffMax> "+ diffMax);
		
	}
	
}

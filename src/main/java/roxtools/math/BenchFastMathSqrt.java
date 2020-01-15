package roxtools.math;

/**
 * 
 * Math square root, using precomputed table of results. Useful specially on ARM architecture.
 * 
 * @author gracilianomp
 *
 */
final public class BenchFastMathSqrt {

	final private FastMathSqrt fastMathSqrt ;
	final private int loops ;
	
	
	
	public BenchFastMathSqrt(int precision, double limit, int loops) {
		super();
		this.fastMathSqrt = new FastMathSqrt(precision, limit) ;
		this.loops = loops;
	}

	private void benchNormal() {
		System.out.println("-----------------------------------------------------------------");
		System.out.println("Bench normal...");
		
		double limit = fastMathSqrt.getLimit() ;
		double step = fastMathSqrt.getStep() ;
		
		double total = 0 ;
		
		double testStep = step/10 ;
		
		if (testStep == 0) throw new IllegalStateException() ;
		
		long time = System.currentTimeMillis() ;
		double r ;
		
		for (int i = 0; i < loops; i++) {
			
			for (double v = 0; v < limit; v += testStep) {
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
				r = Math.sqrt(v) ;
				total += r ;
			}
			
		}
		
		time = System.currentTimeMillis() - time ;
		
		System.out.println(total);
		System.out.println("TIME: "+ time);
	}
	

	private void benchFast() {
		System.out.println("-----------------------------------------------------------------");
		System.out.println("Bench fast...");
		
		double limit = fastMathSqrt.getLimit() ;
		double step = fastMathSqrt.getStep() ;
		
		double total = 0 ;
		
		double testStep = step/10 ;
		
		if (testStep == 0) throw new IllegalStateException() ;
		
		long time = System.currentTimeMillis() ;
		double r ;
		
		for (int i = 0; i < loops; i++) {
			
			for (double v = 0; v < limit; v += testStep) {
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
				r = fastMathSqrt.calcSimple(v) ;
				total += r ;
			}
			
		}
		
		time = System.currentTimeMillis() - time ;
		
		System.out.println(total);
		System.out.println("TIME: "+ time);
	}
	
}

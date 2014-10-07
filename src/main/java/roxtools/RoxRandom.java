package roxtools;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


final public class RoxRandom implements Serializable {
	private static final long serialVersionUID = 6211417059255249939L;

	final static public class RoxRandomState implements Serializable {
		private static final long serialVersionUID = -4498219552210662976L;

		final private int[] vector ;
		final private int state;
		
		protected RoxRandomState(int[] vector, int state) {
			this.vector = vector;
			this.state = state;
		}
	}
	
	static private final int[] RANDOM_MASK = new int[100] ;
	
	static {
		Random rand = new Random() ;
		
		for (int i = 0; i < RANDOM_MASK.length; i++) {
			RANDOM_MASK[i] = rand.nextInt() ;
		}
		
		rand = new Random(rand.nextLong() + System.nanoTime()) ;
		
		for (int i = 0; i < rand.nextInt(100); i++) {
			rand.nextInt() ;
		}
		
		for (int i = 0; i < RANDOM_MASK.length; i++) {
			RANDOM_MASK[i] ^= rand.nextInt() ;
		}
		
		for (int i = RANDOM_MASK.length-1 ; i > 0 ; i--) {
			int i1 = rand.nextInt(i) ;
			
			int v0 = RANDOM_MASK[i] ;
			int v1 = RANDOM_MASK[i1] ;
			
			RANDOM_MASK[i] = v1 ;
			RANDOM_MASK[i1] = v0 ;
		}
	}
	
	static private AtomicLong createSeedCount = new AtomicLong(0) ;
	
	static protected long createSeed() {
		long now = System.nanoTime() + createSeedCount.incrementAndGet() ;
		long seed = now ^ RANDOM_MASK[ (int) (now % RANDOM_MASK.length) ] ;
		return seed ;
	}
	
	/////////////////////////////////////////////////////////

	final private int[] vector = new int[624];
	private int state;
	
	public RoxRandom() {
		seed(initseed());
	}

	public RoxRandom(int seedval) {
		seed(seedval);
	}
	
	public RoxRandom(RoxRandomState state) {
		this.state = state.state ;
		System.arraycopy(state.vector, 0, this.vector, 0, state.vector.length) ;
	}

	public RoxRandomState getState() {
		return new RoxRandomState(vector.clone(), state) ;
	}
	
	static private int initseed() {
		int c = (int)createSeed() ;
		
		boolean d = (c%2) != 0;
		c=c>>2;
		c=c<<1;
		c+=d ? 1 : 0;
		
		return (c<<16)+c;
	}

	public void seed(int _seed) {
		vector[0] = _seed ;

		for (short a = 1 ; a < 624 ; a++) {
			vector[a] = 0x10DCD * vector[a-1] ;
		}
		
		this.gen() ;
		this.state = 0 ;
	}
	
	private void gen(){
		int y=0;
		for (short a=0;a<=622;a++){
			y = this.vector[a] & 0x7FFFFFFF;
			y += (int)((double)(this.vector[a+1]&0xFFFFFFFF)/0xFFFFFFFF);
			if ((y%2) == 0)
				this.vector[a]=this.vector[(a+367)%624]^(y>>1);
			else
				this.vector[a]=this.vector[(a+397)%624]^(y>>1)^0x9908B0DF;
		}
		y=this.vector[623]&0x7FFFFFFF;
		y+=(int)((double)(this.vector[0]&0xFFFFFFFF)/0xFFFFFFFF);
		if ((y%2) == 0)
			this.vector[623]=this.vector[396]^(y>>1);
		else
			this.vector[623]=this.vector[396]^(y>>1)^0x9908B0DF;
	}



	public int nextInt(){
		int state = this.state ;
		int y = this.vector[state];
		
		if (state == 622) {
			this.gen() ;
			this.state = 0 ;
		}
		else {
			this.state = state+1 ;	
		}
		
		y=y^(y>>11);
		y=y^(y<<7)&0x9D2C5680;
		y=y^(y<<15)&0xEFC60000;
		y=y^(y>>18);
		
		return y;
	}
	
	public double nextDouble() {
		int y = this.nextInt() & 0x7FFFFFFF ;
		if (y == 0x7FFFFFFF) --y ;
		
		return ( (double)(y) / 0x7FFFFFFF ) % 1.0D ;
	}

    public float nextFloat() {
    	int y = this.nextInt() & 0x7FFFFFFF ;
		if (y == 0x7FFFFFFF) --y ;
		
		return ( (float)(y) / 0x7FFFFFFF ) % 1.0F ;
    }
	
	public boolean nextBoolean() {
		return (nextInt() & 1) == 0;
	}
	
	////////////////////////////////////////////////////////////////
	
	public void fillArray(double[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = nextDouble() ;
		}
	}

	public void fillArray(float[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = nextFloat() ;
		}
	}

	public void fillArray(int[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = nextInt() ;
		}
	}

	public void fillArray(boolean[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = nextBoolean() ;
		}
	}
	
	/////////////////////////////////////////////////////////////////

}

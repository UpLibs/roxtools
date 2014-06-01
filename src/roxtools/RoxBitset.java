package roxtools;

import java.util.Arrays;
import java.util.BitSet;

final public class RoxBitset extends BitSet {
	private static final long serialVersionUID = 3239515996120705124L;
	
	public RoxBitset() {
		super();
	}

	public RoxBitset(int nbits) {
		super(nbits);
	}

	public void set(int initIndex, boolean[] values) {
		if (values.length == 0) return ;
		
		boolean lastVal = values[0] ;
		int lastValInitIdx = 0 ;
		
		for (int i = 1; i < values.length; i++) {
			boolean b = values[i];
			
			if (b != lastVal) {
				set(lastValInitIdx, i, lastVal) ;
				
				lastVal = b ;
				lastValInitIdx = i ;
			}
		}
		
		set(lastValInitIdx, values.length, lastVal) ;
	}
	
	public boolean[] toBooleanArray() {
		int sz = size() ;
		
		boolean[] ar = new boolean[sz] ;
		
		for (int i = 0; i < ar.length;) {
			int setIdx = nextSetBit(i) ;
			if (setIdx < 0) break ;
			
			int clearIdx = nextClearBit(setIdx) ;
			
			for (int j = setIdx; j < clearIdx; j++) {
				ar[j] = true ;	
			}
			
			i = clearIdx ;
		}
		
		return ar ;
	}
	
	public static void main(String[] args) {
		
		RoxBitset roxBitset = new RoxBitset();
		
		roxBitset.set(0, new boolean[] {true,true, false, false, true,false,true,true,true,false,false,false,true}) ;
		
		System.out.println(roxBitset);
		
		System.out.println( Arrays.toString( roxBitset.toBooleanArray() ) );
		
		
	}

}

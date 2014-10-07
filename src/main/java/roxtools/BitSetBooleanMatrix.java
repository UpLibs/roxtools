package roxtools;

import java.util.BitSet;

final public class BitSetBooleanMatrix {

	final private int width ;
	final private int height ;
	final private BitSet bitSet ;
	
	public BitSetBooleanMatrix(boolean[][] matrix) {
		this.width = matrix.length;
		this.height = matrix[0].length;
		this.bitSet = new BitSet(width*height) ;
		setBooleanMatrix(matrix) ;
	}
	
	public BitSetBooleanMatrix(int width, int height) {
		this.width = width;
		this.height = height;
		this.bitSet = new BitSet(width*height) ;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean get(int x, int y) {
		int idx = y*width + x ;
		return bitSet.get(idx) ;
	}
	
	public void set(int x, int y, boolean val) {
		int idx = y*width + x ;
		bitSet.set(idx, val) ;
	}
	
	public void set(int bitIdx, boolean val) {
		bitSet.set(bitIdx, val) ;
	}
	
	public void setBooleanMatrix(boolean[][] matrix) {
		if ( matrix.length != width ) throw new IllegalArgumentException("Invalid matrix width: "+ matrix.length +" != "+ width) ;
		if ( matrix[0].length != height ) throw new IllegalArgumentException("Invalid matrix height: "+ matrix[0].length +" != "+ height) ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int idx = jIdx + i ;
				bitSet.set(idx , matrix[i][j]) ;
			}
		}	
	}
	
	public boolean[][] toBooleanMatrix() {
		boolean[][] matrix = new boolean[width][height] ;
		
		for (int j = 0; j < height; j++) {
			int jIdx = j*width ;
			
			for (int i = 0; i < width; i++) {
				int idx = jIdx + i ;
				matrix[i][j] = bitSet.get(idx) ;
			}
		}
		
		return matrix ;
	}
	
	/////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {

		BitSetBooleanMatrix matrix = new BitSetBooleanMatrix(100, 100) ;
		
		int width = matrix.getWidth() ;
		int height = matrix.getHeight() ;
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				matrix.set(i, j,  (i+j) % 2 == 0 ) ;
			}
		}
		
		System.out.println("--------------------------");
		
		for (int j = 0; j < height; j++) {
			System.out.println();
			for (int i = 0; i < width; i++) {
				System.out.print( matrix.get(i, j)  ? "1":"0" );
			}
		}
		
	}
	
}

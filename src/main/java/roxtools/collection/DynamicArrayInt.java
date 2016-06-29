package roxtools.collection;

final public class DynamicArrayInt extends DynamicArray<Integer,int[]> {
	
	public DynamicArrayInt() {
		super();
	}

	public DynamicArrayInt(int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize);
	}

	@Override
	public int getInt(int idx) {
		int[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return block[i] ;
	}

	@Override
	public long getLong(int idx) {
		return getInt(idx) ;
	}
	
	@Override
	public float getFloat(int idx) {
		return getInt(idx) ;
	}

	@Override
	public double getDouble(int idx) {
		return getInt(idx) ;
	}
	
	@Override
	public Integer get(int idx) {
		return getInt(idx) ;
	}

	@Override
	public void addInt(int val) {
		int idx = this.size ;
		ensureCapacityForIndex(idx);
		
		int[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		block[i] = val ;
		
		this.size++ ;
	}
	
	@Override
	public void addLong(long val) {
		addInt((int)val);
	}
	
	@Override
	public void addFloat(float val) {
		addInt( (int)val );
	}

	@Override
	public void addDouble(double val) {
		addInt( (int)val );
	}
	
	@Override
	public void add(Integer val) {
		addInt(val);
	}

	@Override
	protected int[] createBlock(int size) {
		return new int[size] ;
	}

	@Override
	protected int[][] createBlockTable(int size) {
		return new int[size][] ;
	}

	@Override
	protected int setInt(int[] block, int idx, int val) {
		int prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected long setLong(int[] block, int idx, long val) {
		return setInt(block, idx, (int)val) ;
	}
	
	@Override
	protected float setFloat(int[] block, int idx, float val) {
		return setInt(block, idx, (int)val) ;
	}
	
	@Override
	protected double setDouble(int[] block, int idx, double val) {
		return setInt(block, idx, (int)val) ;
	}
	
	@Override
	protected Integer set(int[] block, int idx, Integer val) {
		Integer prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected void set(int[] blockSrc, int idxSrc, int[] blockDest, int idxDest) {
		blockDest[idxDest] = blockSrc[idxSrc] ;
	}

	@Override
	protected void reset(int[] block, int idx) {
		block[idx] = 0 ;
	}

}

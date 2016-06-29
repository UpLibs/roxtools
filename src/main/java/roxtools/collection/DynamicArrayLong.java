package roxtools.collection;

final public class DynamicArrayLong extends DynamicArray<Long,long[]> {
	
	public DynamicArrayLong() {
		super();
	}

	public DynamicArrayLong(int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize);
	}

	@Override
	public int getInt(int idx) {
		return (int) getLong(idx) ;
	}
	
	@Override
	public long getLong(int idx) {
		long[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return block[i] ;
	}

	@Override
	public float getFloat(int idx) {
		return getLong(idx) ;
	}
	
	@Override
	public double getDouble(int idx) {
		return getLong(idx) ;
	}

	@Override
	public Long get(int idx) {
		return getLong(idx) ;
	}

	@Override
	public void addInt(int val) {
		addLong(val);
	}
	
	@Override
	public void addLong(long val) {
		int idx = this.size ;
		ensureCapacityForIndex(idx);
		
		long[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		block[i] = val ;
		
		this.size++ ;
	}
	
	@Override
	public void addFloat(float val) {
		addLong((long)val);
	}
	
	@Override
	public void addDouble(double val) {
		addLong((long)val);
	}

	@Override
	public void add(Long val) {
		addLong(val);
	}

	@Override
	protected long[] createBlock(int size) {
		return new long[size] ;
	}

	@Override
	protected long[][] createBlockTable(int size) {
		return new long[size][] ;
	}
	
	@Override
	protected int setInt(long[] block, int idx, int val) {
		return (int) setLong(block, idx, val);
	}
	
	@Override
	protected long setLong(long[] block, int idx, long val) {
		long prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected float setFloat(long[] block, int idx, float val) {
		return (float)setLong(block, idx, (long)val) ;
	}
	
	@Override
	protected double setDouble(long[] block, int idx, double val) {
		return (double)setLong(block, idx, (long)val) ;
	}
	
	@Override
	protected Long set(long[] block, int idx, Long val) {
		Long prev = block[idx] ;
		block[idx] = val ;
		return prev ;		
	}

	@Override
	protected void set(long[] blockSrc, int idxSrc, long[] blockDest, int idxDest) {
		blockDest[idxDest] = blockSrc[idxSrc] ;
	}

	@Override
	protected void reset(long[] block, int idx) {
		block[idx] = 0 ;
	}

}

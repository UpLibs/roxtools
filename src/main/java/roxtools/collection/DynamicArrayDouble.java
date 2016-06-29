package roxtools.collection;

final public class DynamicArrayDouble extends DynamicArray<Double,double[]> {
	
	public DynamicArrayDouble() {
		super();
	}

	public DynamicArrayDouble(int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize);
	}

	@Override
	public int getInt(int idx) {
		return (int) getDouble(idx) ;
	}

	@Override
	public long getLong(int idx) {
		return (long) getDouble(idx) ;
	}
	
	@Override
	public float getFloat(int idx) {
		return (float) getDouble(idx) ;
	}
	
	@Override
	public double getDouble(int idx) {
		double[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return block[i] ;
	}

	@Override
	public Double get(int idx) {
		return getDouble(idx) ;
	}

	@Override
	public void addInt(int val) {
		addDouble(val);
	}
	
	@Override
	public void addLong(long val) {
		addDouble(val);
	}
	
	@Override
	public void addFloat(float val) {
		addDouble(val);
	}
	
	@Override
	public void addDouble(double val) {
		int idx = this.size ;
		ensureCapacityForIndex(idx);
		
		double[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		block[i] = val ;
		
		this.size++ ;
	}

	@Override
	public void add(Double val) {
		addDouble(val);
	}

	@Override
	protected double[] createBlock(int size) {
		return new double[size] ;
	}

	@Override
	protected double[][] createBlockTable(int size) {
		return new double[size][] ;
	}
	
	@Override
	protected int setInt(double[] block, int idx, int val) {
		return (int) setDouble(block, idx, val) ;
	}
	
	@Override
	protected long setLong(double[] block, int idx, long val) {
		return (long) setDouble(block, idx, val) ;
	}

	@Override
	protected float setFloat(double[] block, int idx, float val) {
		return (float) setDouble(block, idx, val) ;
	}
	
	@Override
	protected double setDouble(double[] block, int idx, double val) {
		double prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected Double set(double[] block, int idx, Double val) {
		Double prev = block[idx] ;
		block[idx] = val ;
		return prev ;		
	}
	
	@Override
	protected void set(double[] blockSrc, int idxSrc, double[] blockDest, int idxDest) {
		blockDest[idxDest] = blockSrc[idxSrc] ;
	}

	@Override
	protected void reset(double[] block, int idx) {
		block[idx] = 0 ;
	}

}

package roxtools.collection;

final public class DynamicArrayFloat extends DynamicArray<Float,float[]> {
	
	public DynamicArrayFloat() {
		super();
	}

	public DynamicArrayFloat(int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize);
	}

	@Override
	public int getInt(int idx) {
		return (int) getFloat(idx) ;
	}

	@Override
	public long getLong(int idx) {
		return (long) getFloat(idx) ;
	}
	
	@Override
	public float getFloat(int idx) {
		float[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return block[i] ;
	}
	
	@Override
	public double getDouble(int idx) {
		return getFloat(idx) ;
	}

	@Override
	public Float get(int idx) {
		return getFloat(idx) ;
	}

	@Override
	public void addInt(int val) {
		addFloat(val);
	}
	
	@Override
	public void addLong(long val) {
		addFloat((float)val);
	}
	
	@Override
	public void addFloat(float val) {
		int idx = this.size ;
		ensureCapacityForIndex(idx);
		
		float[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		block[i] = val ;
		
		this.size++ ;
	}
	
	@Override
	public void addDouble(double val) {
		addFloat((float)val);
	}

	@Override
	public void add(Float val) {
		addFloat(val);
	}

	@Override
	protected float[] createBlock(int size) {
		return new float[size] ;
	}

	@Override
	protected float[][] createBlockTable(int size) {
		return new float[size][] ;
	}

	@Override
	protected int setInt(float[] block, int idx, int val) {
		return (int) setFloat(block, idx, val) ;
	}
	
	@Override
	protected long setLong(float[] block, int idx, long val) {
		return (long) setFloat(block, idx, val) ;
	}
	
	@Override
	protected float setFloat(float[] block, int idx, float val) {
		float prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected double setDouble(float[] block, int idx, double val) {
		return setFloat(block, idx, (float)val) ;
	}
	
	@Override
	protected Float set(float[] block, int idx, Float val) {
		Float prev = block[idx] ;
		block[idx] = val ;
		return prev ;		
	}
	
	@Override
	protected void set(float[] blockSrc, int idxSrc, float[] blockDest, int idxDest) {
		blockDest[idxDest] = blockSrc[idxSrc] ;
	}
	
	@Override
	protected void set(float[] blockSrc, int idxSrc, float[] blockDest, int idxDest, int lng) {
		for (int i = 0; i < lng; i++) {
			blockDest[idxDest+i] = blockSrc[idxSrc+i] ;
		}	
	}

	@Override
	protected void reset(float[] block, int idx) {
		block[idx] = 0 ;
	}

}

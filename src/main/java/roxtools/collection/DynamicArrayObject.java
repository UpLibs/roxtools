package roxtools.collection;

import java.lang.reflect.Array;

public class DynamicArrayObject<O> extends DynamicArray<O,O[]> {

	private Class<?> objectType ;
	
	public DynamicArrayObject(Class<O> objectType) {
		this(objectType, 128, 1024*128) ;
	}
	
	public DynamicArrayObject(Class<O> objectType, int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize, false) ;
		this.objectType = objectType ;
		
		initializeBlocks();
	}
	
	public DynamicArrayObject() {
		this(128, 1024*128) ;
	}
	
	public DynamicArrayObject(int minBlockSize, int maxBlockSize) {
		super(minBlockSize, maxBlockSize, false) ;
		this.objectType = Object.class ;
		
		initializeBlocks();
	}
	
    protected Class<?> objectType() {
    	return objectType ;
    }

	@Override
	public int getInt(int idx) {
		O o = get(idx) ;
		
		if (o instanceof Number) {
			Number n = (Number) o ; 
			return n.intValue() ;
		}
		
		throw new UnsupportedOperationException("Can't convert type "+ o.getClass() +" to int.") ;
	}
	
	@Override
	public long getLong(int idx) {
		O o = get(idx) ;
		
		if (o instanceof Number) {
			Number n = (Number) o ; 
			return n.longValue();
		}
		
		throw new UnsupportedOperationException("Can't convert type "+ o.getClass() +" to long.") ;
	}

	@Override
	public float getFloat(int idx) {
		O o = get(idx) ;
		
		if (o instanceof Number) {
			Number n = (Number) o ; 
			return n.floatValue() ;
		}
		
		throw new UnsupportedOperationException("Can't convert type "+ o.getClass() +" to float.") ;
	}

	@Override
	public double getDouble(int idx) {
		O o = get(idx) ;
		
		if (o instanceof Number) {
			Number n = (Number) o ; 
			return n.doubleValue() ;
		}
		
		throw new UnsupportedOperationException("Can't convert type "+ o.getClass() +" to double.") ;
	}
	
	@Override
	public O get(int idx) {
		O[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return block[i] ;
	}

	@Override
	public void addInt(int val) {
		Integer i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		add(o);
	}
	
	@Override
	public void addLong(long val) {
		Long i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		add(o);
	}
	
	@Override
	public void addFloat(float val) {
		Float f = val ;
		@SuppressWarnings("unchecked")
		O o = (O) f ;
		add(o);
	}
	
	@Override
	public void addDouble(double val) {
		Double f = val ;
		@SuppressWarnings("unchecked")
		O o = (O) f ;
		add(o);
	}

	@Override
	public void add(O val) {
		int idx = this.size ;
		ensureCapacityForIndex(idx);
		
		O[] block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		block[i] = val ;
		
		this.size++ ;
	}
	
	    
    private transient Class<O[]> objectArrayType ;
    
    @SuppressWarnings("unchecked")
	private Class<O[]> objectArrayType() {
    	if (objectArrayType == null) {
    		Class<?> objectType = objectType() ;
    		O[] a = (O[]) Array.newInstance( objectType , 1 ) ;
    		objectArrayType = (Class<O[]>) a.getClass() ;
    	}
    	return objectArrayType ;
    }

	
	@SuppressWarnings("unchecked")
	@Override
	protected O[] createBlock(int size) {
		Class<?> objectType = objectType() ;
		return (O[]) Array.newInstance( objectType , size ) ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected O[][] createBlockTable(int size) {
		Class<O[]> objectArrayType = objectArrayType() ;
		return (O[][]) Array.newInstance( objectArrayType , size ) ;
	}

	@Override
	protected int setInt(O[] block, int idx, int val) {
		Integer i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		return (int) set(block, idx, o) ;
	}
	
	@Override
	protected long setLong(O[] block, int idx, long val) {
		Long i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		return (long) set(block, idx, o) ;
	}
	
	@Override
	protected float setFloat(O[] block, int idx, float val) {
		Float i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		return (float) set(block, idx, o) ;
	}
	
	@Override
	protected double setDouble(O[] block, int idx, double val) {
		Double i = val ;
		@SuppressWarnings("unchecked")
		O o = (O) i ;
		return (double) set(block, idx, o) ;
	}
	
	@Override
	protected O set(O[] block, int idx, O val) {
		O prev = block[idx] ;
		block[idx] = val ;
		return prev ;
	}
	
	@Override
	protected void set(O[] blockSrc, int idxSrc, O[] blockDest, int idxDest) {
		blockDest[idxDest] = blockSrc[idxSrc] ;
	}

	@Override
	protected void reset(O[] block, int idx) {
		block[idx] = null ;
	}

}

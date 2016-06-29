package roxtools.collection;

import java.util.Arrays;
import java.util.Iterator;

abstract public class DynamicArray<O,B> implements Iterable<O> {

	final int minBlockSize ;
	final int maxBlockSize ;
	
	int blockSize ;
	
	B[] blocks ;
	
	int size = 0 ;
	
	public DynamicArray() {
		this(128, 1024*128) ;
	}
	
	public DynamicArray(int minBlockSize, int maxBlockSize) {
		this(minBlockSize, maxBlockSize, true) ;
	}
	
	protected DynamicArray(int minBlockSize, int maxBlockSize, boolean initializeBlocks) {
		this.minBlockSize = minBlockSize;
		this.maxBlockSize = maxBlockSize;
		
		this.blockSize = minBlockSize ;
		
		if (initializeBlocks) {
			initializeBlocks();
		}
	}

	public int getBlockSize() {
		return blockSize;
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		initializeBlocks();
	}

	//////////////////////////////////////////////
	
	protected void initializeBlocks() {
		this.blocks = createBlockTable(1) ;
		this.blocks[0] = createBlock(blockSize) ;
	}

	protected void ensureCapacity(int totalBlocks) {
		if ( this.blocks.length >= totalBlocks ) return ;
		
		B[] blocks2 = createBlockTable(totalBlocks) ;
		System.arraycopy(this.blocks, 0, blocks2, 0, blocks.length);
		
		for (int i = this.blocks.length; i < blocks2.length; i++) {
			blocks2[i] = createBlock(this.blockSize) ;
		}
		
		this.blocks = blocks2 ;
	}
	
	protected void ensureCapacityForIndex(int idx) {
		int blkIdx = idx/this.blockSize ;
		ensureCapacity( blkIdx+1 );
	}
	
	protected void checkToReduceCapacity() {
		int neededBlocksForSize = (size/blockSize)+1 ;
		
		int maxCapacityBlocks = neededBlocksForSize+1 ;
		
		if ( maxCapacityBlocks < this.blocks.length ) {
			B[] blocks2 = createBlockTable(maxCapacityBlocks) ;
			System.arraycopy(this.blocks, 0, blocks2, 0, maxCapacityBlocks);	
			
			this.blocks = blocks2 ;
		}
	}
	
	protected int getBlockIndex(int idx) {
		int blkIdx = idx/this.blockSize ;
		return blkIdx ;
	}
	
	protected B getBlockForIndex(int idx) {
		int blkIdx = idx/this.blockSize ;
		return this.blocks[ blkIdx ] ;
	}
	
	protected B getBlock(int blockIdx) {
		return this.blocks[blockIdx] ;
	}
	
	protected int getIndexInBlock(int idx) {
		int blkIdx = idx/this.blockSize ;
		return idx - (blkIdx*this.blockSize) ;
	}
	
	protected int getBlockSizeForIndex(int idx) {
		int blkIdx = idx/this.blockSize ;
		return getBlockElementsSize(blkIdx) ;
	}
	
	protected int getBlockElementsSize(int blockIndex) {
		int blockInitIndex = blockIndex * blockSize ;
		int blockLastIndex = (blockInitIndex + blockSize) -1 ;
		
		if ( this.size < blockLastIndex ) {
			int sz = this.size - blockInitIndex ;
			return sz > 0 ? sz : 0 ;
		}
		else {
			return blockSize ;
		}
	}
	
	public int getTotalCapacity() {
		return this.blocks.length * blockSize ;
	}
	
	public int getAllocatedBlocks() {
		return this.blocks.length ;
	}
	
	public int getLastIndex() {
		return getTotalCapacity() - 1; 
	}

	//////////////////////////////////////////////
	
	abstract public int getInt(int idx) ;
	abstract public long getLong(int idx) ;
	abstract public float getFloat(int idx) ;
	abstract public double getDouble(int idx) ;
	abstract public O get(int idx) ;
	
	abstract public void addInt(int val) ;
	abstract public void addLong(long val) ;
	abstract public void addFloat(float val) ;
	abstract public void addDouble(double val) ;
	abstract public void add(O val) ;

	//////////////////////////////////////////////
	
	public int setInt(int idx, int val) {
		B block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return setInt(block, i, val) ;
	}
	
	public long setLong(int idx, long val) {
		B block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return setLong(block, i, val) ;
	}
	
	public float setFloat(int idx, float val) {
		B block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return setFloat(block, i, val) ;
	}
	
	public double setDouble(int idx, double val) {
		B block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return setDouble(block, i, val) ;
	}

	public O set(int idx, O val) {
		B block = getBlockForIndex(idx) ;
		int i = getIndexInBlock(idx) ;
		return set(block, i, val) ;
	}
	
	//////////////////////////////////////////////
	
	abstract protected int setInt(B block, int idx, int val) ;
	abstract protected long setLong(B block, int idx, long val) ;
	abstract protected float setFloat(B block, int idx, float val) ;
	abstract protected double setDouble(B block, int idx, double val) ;
	abstract protected O set(B block, int idx, O val) ;
	
	abstract protected B createBlock(int size) ;
	abstract protected B[] createBlockTable(int size) ;
	
	//////////////////////////////////////////////
	
	abstract protected void set(B blockSrc, int idxSrc, B blockDest, int idxDest) ;
	abstract protected void reset(B block, int idx) ;
	
	public void remove(int idx) {
		if (idx >= size) throw new ArrayIndexOutOfBoundsException("idx >= size: "+ idx +" >= "+ size) ;
		
		int blkIdx = getBlockIndex(idx);
		B block = getBlock(blkIdx) ;
		int i = getIndexInBlock(idx) ;
		
		int blockElemsSize = getBlockElementsSize(blkIdx) ;
		
		int blockMoved = blockElemsSize - i - 1;
        if (blockMoved > 0) {
        	System.arraycopy(block, i+1, block, i, blockMoved);
        }
        
        int blockLastIdx = this.blockSize-1 ;
        
        reset(block, blockLastIdx);
        
        B prevBlock = block ;
        
        int lastBlockIndexWithValues = this.size / this.blockSize ;
        
        for (int bI = blkIdx+1; bI <= lastBlockIndexWithValues; bI++) {
        	B b = this.blocks[bI] ;
        	
        	set(b, 0, prevBlock, blockLastIdx);
        	
        	int bSz = getBlockElementsSize(bI) ;
        	
        	if (bSz <= 0) break ;

        	int bMoved = bSz - 1;
        	
        	if (bMoved > 0) {
        		System.arraycopy(b, 1, b, 0, bMoved);	
        	}
        	
        	reset(b, bMoved);
        	
        	prevBlock = b ;
		}
        
        this.size-- ;
        
        checkToReduceCapacity();
	}

	////////////////////////////////////////////////////////////////////////
	
	private class MyIterator implements Iterator<O> {
		int cursor = 0 ;
		
		@Override
		public boolean hasNext() {
			return this.cursor < size ;
		}

		@Override
		public O next() {
			O o = get(cursor) ;
			cursor++ ;
			return o ;
		}
	}
	
	public Iterator<O> iterator() {
		return new MyIterator() ;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() +"#"+ size() +"/"+ getTotalCapacity() + Arrays.deepToString( this.blocks ) ;
	}
	
}

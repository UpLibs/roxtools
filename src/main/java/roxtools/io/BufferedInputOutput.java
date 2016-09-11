package roxtools.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.util.HashMap;

final public class BufferedInputOutput {
	
	final private Object mutex ;
	final private int blockSize ;
	final private SeekableInput in;
	final private SeekableOutput out;

	public BufferedInputOutput(int blockSize, RandomAccessFile randomAccessFile) {
		this(blockSize, randomAccessFile, null) ;
	}
	
	public BufferedInputOutput(int blockSize, RandomAccessFile randomAccessFile, Object mutex) {
		this(blockSize, new RandomAccessInput(randomAccessFile), new RandomAccessOutput(randomAccessFile), mutex) ;
	}
	
	public BufferedInputOutput(int blockSize, SeekableInput in, SeekableOutput out) {
		this(blockSize, in, out, null) ;
	}
	
	public BufferedInputOutput(int blockSize, SeekableInput in, SeekableOutput out, Object mutex) {
		super();
		
		this.mutex = mutex != null ? mutex : this ;
		
		this.blockSize = blockSize;
		
		this.in = in ;
		this.out = out ;
	}
	
	public int read(long pos, byte[] buffer, int offset, int length) throws IOException {
		
		synchronized (mutex) {
			int read = 0 ;
			
			while (length > 0) {
				int blockIdx = getBlockIndexForPos(pos) ;
				int blkPos = getPosInBlock(blockIdx, pos) ;
				
				Block block = getBlockForRead(blockIdx) ;
				
				int rest = blockSize-blkPos ;
				
				int lng = length > rest ? rest : length ;
				
				byte[] blockData = block.get() ;
				assert( blockData != null );
				
				System.arraycopy(blockData, blkPos, buffer, offset, lng);
				
				block.releaseRead();
				
				offset += lng ;
				length -= lng ;
				
				read += lng ;
			}
			
			assert(length == 0) ;
			
			return read ;
		}
		
	}
	
	public int write(int pos, byte[] buffer, int offset, int length) throws IOException {
		
		synchronized (mutex) {
			int write = 0 ;
			
			while (length > 0) {
				int blockIdx = getBlockIndexForPos(pos) ;
				int blkPos = getPosInBlock(blockIdx, pos) ;
				
				Block block = getBlockForWrite(blockIdx) ;
				
				int rest = blockSize-blkPos ;
				
				int lng = length > rest ? rest : length ;
				
				byte[] blockData = block.get() ;
				assert( blockData != null );
				
				System.arraycopy(buffer, offset, blockData, blkPos, lng) ;
				
				offset += lng ;
				length -= lng ;
				
				write += lng ;
			}
			
			assert(length == 0) ;
			
			return write ;
		}
		
	}


	private int getPosInBlock(int blockIdx, long pos) {
		long blkInitPos = blockIdx*blockSize ;
		int blkPos = (int) (pos-blkInitPos) ;
		assert(blkPos >= 0) ;
		return blkPos;
	}

	private int getBlockIndexForPos(long pos) {
		int idx = (int) (pos/blockSize) ;
		assert(idx >= 0) ;
		return idx ;
	}
	
	private class Block extends SoftReference<byte[]>{

		final private int index ;
		public Block(int index, byte[] referent) {
			super(referent);
			this.index = index ;
		}

		public int getIndex() {
			return index ;
		}

		private byte[] holderRead ;
		private byte[] holderWrite ;
		
		@SuppressWarnings("unused")
		public boolean isHolding() {
			return holderRead != null || holderWrite != null ;
		}
		
		public boolean isHoldingWrite() {
			return holderWrite != null ;
		}
		
		public void holdRead() {
			this.holderRead = get();
		}
		
		public void releaseRead() {
			this.holderRead = null ;
		}
		
		public void holdWrite(HashMap<Block, Object> blocksToWrite) {
			if ( this.holderWrite == null ) {
				byte[] data ;
				
				this.holderWrite = data = get();
				
				if ( data != null ) {
					blocksToWrite.put(this, Boolean.TRUE) ;
				}
			}
		}
		
		public void releaseWrite() {
			this.holderWrite = null ;
		}
		
		public byte[] getAndHoldRead() {
			byte[] data = get() ;
			holdRead();
			return data ;
		}
		
		public byte[] getAndHoldWrite(HashMap<Block, Object> blocksToWrite) {
			byte[] data = get() ;
			holdWrite(blocksToWrite);
			return data ;
		}
		
	}
	
	private Block[] blocks = new Block[1024] ; 
	private final HashMap<Block, Object> blocksToWrite = new HashMap<>() ;
	
	private Block getBlock(int blockIdx) throws IOException {
		if ( blockIdx >= this.blocks.length ) {
			int blkArraySz = ((blockIdx / 1024)+1) * 128 ;
			
			Block[] blocks2 = new Block[blkArraySz] ;
			System.arraycopy(blocks, 0, blocks2, 0, blocks.length);
			
			this.blocks = blocks2 ;
		}
		
		Block block = this.blocks[blockIdx] ;
		return block ;
	}
	
	private Block getBlockForRead(int blockIdx) throws IOException {
		Block block = getBlock(blockIdx) ;
		
		byte[] data = block != null ? block.getAndHoldRead() : null ;
		
		if (data == null) {
			data = instantiateBlockData(blockIdx) ;
			block = new Block(blockIdx, data) ;
			block.holdRead();
			
			blocks[blockIdx] = block ;
		}
		
		return block;
	}

	private Block getBlockForWrite(int blockIdx) throws IOException {
		Block block = blocks[blockIdx] ;
		
		byte[] data = block.getAndHoldWrite(blocksToWrite) ;
		
		if (data == null) {
			data = instantiateBlockData(blockIdx) ;
			
			block = new Block(blockIdx, data) ;
			block.holdWrite(blocksToWrite);
			
			blocks[blockIdx] = block ;
		}
		
		return block;
	}
	
	private byte[] instantiateBlockData(int blockIdx) throws IOException {
		int initPos = blockIdx*blockSize ;
		
		byte[] blk = new byte[blockSize] ;
		
		in.seek(initPos);
		in.read(blk) ;
		
		return blk ;
	}
	
	public void flush() throws IOException {
		
		synchronized (mutex) {
			for (Block block : blocksToWrite.keySet()) {
				assert( block.isHoldingWrite() ) ;
				flushBlock( block ) ;
			}
			
			blocksToWrite.clear();
		}
		
	}

	private void flushBlock(Block block) throws IOException {
		int blockIdx = block.getIndex() ;
		
		byte[] blockData = block.get() ;
		
		int initPos = blockIdx*blockSize ;
		
		out.seek(initPos);
		out.write(blockData);
		
		block.releaseWrite();
	}
	
}

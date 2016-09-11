package roxtools.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import roxtools.RoxDeque;

final public class BufferedInputOutput implements SeekableInputOutput {
	
	final private Object mutex ;
	final private int blockSize ;
	final private long blockSizeL ;
	
	final private SeekableInput in;
	final private SeekableOutput out;
	
	private long size = 0 ;

	public BufferedInputOutput(int blockSize, RandomAccessFile randomAccessFile) throws IOException {
		this(blockSize, randomAccessFile, null) ;
	}
	
	public BufferedInputOutput(int blockSize, RandomAccessFile randomAccessFile, Object mutex) throws IOException {
		this(blockSize, new RandomAccessInput(randomAccessFile), new RandomAccessOutput(randomAccessFile), mutex) ;
	}
	
	public BufferedInputOutput(int blockSize, SeekableInput in, SeekableOutput out) throws IOException {
		this(blockSize, in, out, null) ;
	}
	
	public BufferedInputOutput(int blockSize, SeekableInput in, SeekableOutput out, Object mutex) throws IOException {
		super();
		
		this.mutex = mutex != null ? mutex : this ;
		
		this.blockSize = blockSize;
		this.blockSizeL = blockSize ;
		
		this.in = in ;
		this.out = out ;
		
		this.size = in.length();
		
		if ( in.length() != out.length() ) throw new IOException("Input length() different of Output! "+ in.length() +" != "+ out.length()) ;
	}
	
	public Object getMutex() {
		return mutex;
	}
	
	public int read(long pos) throws IOException {
		
		synchronized (mutex) {
			if (pos > size) return -1 ;
			
			int blockIdx = getBlockIndexForPos(pos) ;
			int blkPos = getPosInBlock(blockIdx, pos) ;
			assert( blockSize-blkPos > 0 ) ;
			
			Block block = getBlockForRead(blockIdx) ;
			
			byte[] blockData = block.get() ;
			assert( blockData != null );
			
			int b = blockData[blkPos] & 0xFF ;
			
			block.releaseRead();
			
			return b ;
		}
		
	}

	public void write(long pos, int b) throws IOException {
		
		synchronized (mutex) {
			int blockIdx = getBlockIndexForPos(pos) ;
			
			int blkPos = getPosInBlock(blockIdx, pos) ;
			assert( blockSize-blkPos > 0 ) ;
			
			Block block = getBlockForWrite(blockIdx) ;
			
			byte[] blockData = block.get() ;
			assert( blockData != null );
			
			blockData[blkPos] = (byte) b ;
			
			block.markWriteRegion(blkPos, 1);
			
			long newPos = pos+1 ;
			if (newPos > size) size = newPos ;
		}
		
	}
	
	public int read(long pos, byte[] buffer, int offset, int length) throws IOException {
		
		synchronized (mutex) {
			if (pos > size) return -1 ;
			
			int read = 0 ;
			
			while (length > 0) {
				long remain = size-pos ;
				if (remain <= 0) break ;
				
				int blockIdx = getBlockIndexForPos(pos) ;
				int blkPos = getPosInBlock(blockIdx, pos) ;
				
				Block block = getBlockForRead(blockIdx) ;
				
				int rest = blockSize-blkPos ;
				if (rest > remain) rest = (int) remain ;
				
				int lng = length > rest ? rest : length ;
				
				byte[] blockData = block.get() ;
				assert( blockData != null );
				
				System.arraycopy(blockData, blkPos, buffer, offset, lng);
				
				block.releaseRead();
				
				offset += lng ;
				length -= lng ;
				
				read += lng ;
				
				pos += lng ;
			}
			
			return read ;
		}
		
	}
	
	public int write(long pos, byte[] buffer) throws IOException {
		return write(pos, buffer, 0, buffer.length) ;
	}
	
	public int write(long pos, byte[] buffer, int offset, int length) throws IOException {
		
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
				
				block.markWriteRegion(blkPos, lng);
				
				offset += lng ;
				length -= lng ;
				
				write += lng ;
				
				pos += lng ;
			}
			
			assert(length == 0) ;
			
			return write ;
		}
		
	}


	private int getPosInBlock(int blockIdx, long pos) {
		long blkInitPos = blockIdx*blockSizeL ;
		int blkPos = (int) (pos-blkInitPos) ;
		assert(blkPos >= 0) ;
		return blkPos;
	}

	private int getBlockIndexForPos(long pos) {
		int idx = (int) (pos/blockSize) ;
		assert(idx >= 0) ;
		return idx ;
	}
	
	static final private BlockComparator BLOCK_COMPARATOR = new BlockComparator() ;
	
	static private class BlockComparator implements Comparator<Block> {

		@Override
		public int compare(Block o1, Block o2) {
			return Integer.compare(o1.index, o2.index) ;
		}
		
	}
	
	private class Block extends SoftReference<byte[]> implements Comparable<Block> {

		final private int index ;
		public Block(int index, byte[] referent) {
			super(referent);
			this.index = index ;
		}
		
		@Override
		public String toString() {
			return "[#"+index + (isHoldingWrite() ? "!" : "") +"]" ;
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
		
		public void holdWrite(RoxDeque<Block> blocksToWrite) {
			if ( this.holderWrite == null ) {
				byte[] data ;
				
				this.holderWrite = data = get();
				
				if ( data != null ) {
					int idx = blocksToWrite.binarySearch(this, BLOCK_COMPARATOR) ;
					
					if (idx < 0) {
						idx = (-idx)-1 ;
						blocksToWrite.add(idx , this) ;
					}
					else {
						System.out.println("!!!");
					}
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
		
		public byte[] getAndHoldWrite(RoxDeque<Block> blocksToWrite) {
			byte[] data = get() ;
			holdWrite(blocksToWrite);
			return data ;
		}

		public void dispose() {
			this.holderRead = null ;
			this.holderWrite = null ;
			this.clear();
		}

		public int writeInit = -1 ;
		public int writeEnd = 0 ;
		
		public boolean containsWriteMark() {
			return writeInit >= 0 ;
		}
		
		public void clearWriteMark() {
			writeInit = -1 ;
			writeEnd = 0 ;
		}
		
		public void markWriteRegion(int init, int lng) {
			assert(init >= 0) ;
			assert(lng > 0) ;
			
			int end = init+lng ;
			
			if ( writeInit < 0 ) {
				this.writeInit = init ;
				this.writeEnd = end ;	
			}
			else {
				
				if (init < writeInit) {
					writeInit = init ;
				}
				
				if (end > writeEnd) {
					writeEnd = end ;
				}
			}
			
		}
		
		@Override
		public int compareTo(Block o) {
			return Integer.compare( this.index , o.index ) ;
		}
		
	}
	
	private Block[] blocks = new Block[1024] ; 
	private final RoxDeque<Block> blocksToWrite = new RoxDeque<>() ;
	
	private Block getBlock(int blockIdx) throws IOException {
		if ( blockIdx >= this.blocks.length ) {
			int blkArraySz = ((blockIdx / 1024)+1) * 1024 ;
			
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
		Block block = getBlock(blockIdx) ;
		
		byte[] data = block != null ? block.getAndHoldWrite(blocksToWrite) : null ;
		
		if (data == null) {
			data = instantiateBlockData(blockIdx) ;
			
			block = new Block(blockIdx, data) ;
			block.holdWrite(blocksToWrite);
			
			blocks[blockIdx] = block ;
		}
		
		return block;
	}
	
	private byte[] instantiateBlockData(int blockIdx) throws IOException {
		long initPos = blockIdx*blockSizeL ;
		
		long endPos = initPos+blockSize ;
		
		if (endPos > size) endPos = size ;
		
		byte[] blk = new byte[blockSize] ;
		
		int lng = (int) (endPos - initPos) ;
		
		if (lng > 0) {
			in.seek(initPos);
			in.read(blk) ;
		}
		
		return blk ;
	}
	
	public void setLength(long length) throws IOException {
		
		synchronized (mutex) {
			int maxBlockIndex = (int) ((length-1)/blockSize) ;
			
			int delBlockInitIndex = maxBlockIndex+1 ;
			
			for (int i = delBlockInitIndex; i < blocks.length; i++) {
				Block block = blocks[i];
				if (block == null) continue ;
				
				block.dispose() ;
				
				blocksToWrite.remove(block);
				
				blocks[i] = null ;
			}
			
			if ( delBlockInitIndex < blocks.length/2 ) {
				int newBlksSz = ((delBlockInitIndex/1024)+1) * 1024 ;
				
				if (newBlksSz < blocks.length) {
					Block[] blocks2 = new Block[newBlksSz] ;
					System.arraycopy(blocks, 0, blocks2, 0, newBlksSz);
					this.blocks = blocks2 ;
				}
			}
			
			size = length ;
			out.setLength(length);
			
			if (pos > size) pos = size ;
		}
		
	}
	
	synchronized public boolean hasUnflushedData() {
		
		synchronized (mutex) {
			return !blocksToWrite.isEmpty() ;
		}
		
	}
	
	public void flush() throws IOException {
		
		synchronized (mutex) {
			if ( !hasUnflushedData() ) return ;
			
			for (Block block : blocksToWrite) {
				assert( block.isHoldingWrite() ) ;
				flushBlock( block ) ;
			}
			
			blocksToWrite.clear();
			
			long outLng = out.length() ;
			
			if (this.size != outLng) {
				throw new IOException("size out of synch with output! "+ this.size +" != "+ outLng) ;
			}
		}
		
	}

	private void flushBlock(Block block) throws IOException {
		
		if ( !block.isHoldingWrite() ) {
			assert( !block.containsWriteMark() ) ;
			return ;
		}
		
		assert( block.containsWriteMark() ) ;
		
		int blockIdx = block.getIndex() ;
		
		byte[] blockData = block.get() ;
		
		assert(blockData != null) ;
		
		long initBlockPos = blockIdx*blockSizeL ;
		
		long writeInit = initBlockPos + block.writeInit ;
		long writeEnd = initBlockPos + block.writeEnd ;
		
		if (writeEnd > size) {
			writeEnd = size ;
		}
		
		if (writeEnd <= writeInit) {
			block.clearWriteMark();
			block.releaseWrite();
			
			return ;
		}
		
		assert( writeInit >= 0 ) ;
		assert( writeEnd > writeInit ) ;
		
		int writeLng = (int) (writeEnd - writeInit) ;
		
		out.seek(writeInit);
		out.write(blockData, block.writeInit, writeLng);
		
		block.clearWriteMark();
		block.releaseWrite();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////

	public void reset() throws IOException {
		synchronized (mutex) {
			seek(0);
			setLength(0);
			
			for (int i = 0; i < blocks.length; i++) {
				Block block = blocks[i];
				if (block == null) continue ;
				
				block.dispose();
				blocks[i] = null ;
			}
			
			blocksToWrite.clear();
		}
	}
	
	private long pos = 0 ;
	
	public void seekToEnd() throws IOException {
		seek(size);
	}
	
	public void seekToBegin() throws IOException {
		seek(0);
	}
	
	public void seekFromEnd(int posFromEnd) throws IOException {
		seek( size-pos );
	}
	
	@Override
	public void seek(long pos) throws IOException {
		synchronized (mutex) {
			this.pos = pos ;	
		}
	}

	@Override
	public long position() throws IOException {
		return pos ;
	}
	
	@Override
	public long length() throws IOException {
		return size;
	}

	@Override
	public void write(int b) throws IOException {
		synchronized (mutex) {
			write(pos, b) ;
			pos++ ;
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		synchronized (mutex) {
			int w = write(pos, b, off, len);
			pos += w ;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		synchronized (mutex) {
			int w = write(pos, b, 0, b.length);
			pos += w ;
		}
	}

	@Override
	public int read() throws IOException {
		synchronized (mutex) {
			int b = read(pos) ;
			pos++ ;
			return b ;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		synchronized (mutex) {
			int read = read(pos, b, off, len) ;
			if (read > 0) pos += read ;
			return read ;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		synchronized (mutex) {
			int read = read(pos, b, 0, b.length) ;
			if (read > 0) pos += read ;
			return read ;
		}
	}
	
	public long writeTo(OutputStream out) throws IOException {
		return writeTo(0, size, out) ;
	}
	
	public long writeTo(long position, long length, OutputStream out) throws IOException {
		byte[] buffer = new byte[blockSize] ;
		
		while (length > 0) {
			int r = read(position, buffer, 0, blockSize) ;
			if (r < 0) break ;
			
			out.write(buffer, 0, r);
			
			length -= r ;
			position += r ;
		}
		
		return position ;
	}
	
	public byte[] toByteArray() throws IOException {
		return toByteArray(0, (int)size) ;
	}
	
	public byte[] toByteArray(long position, int length) throws IOException {
		byte[] buffer = new byte[length] ;
		
		int r = read(position, buffer, 0, length) ;
		
		if (r < length) throw new EOFException() ;
		
		assert(r == length) ;
		
		return buffer ;
	}
	
	static final private Timer flushTimer = new Timer("BufferedInputOutput::flushTimer",true) ;
	
	public void scheduleFlush(int delay) {
		scheduleFlush(delay, false);
	}
	
	public void scheduleFlush(int delay, boolean repeatedly) {
		
		synchronized (mutex) {
			if ( !hasUnflushedData() ) return ;
			
			if (delay <= 0) {
				try {
					flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return ;
			}

			TimerTask task = new TimerTask() {
				
				@Override
				public void run() {
					try {
						flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			
			if (repeatedly) {
				flushTimer.schedule(task, delay,delay);
			}
			else {
				flushTimer.schedule(task, delay);	
			}	
		}
		
	}

	public void dispose() {

		synchronized (mutex) {
			blocksToWrite.clear();
			
			for (int i = 0; i < blocks.length; i++) {
				Block block = blocks[i];
				if (block == null) continue ;
				
				block.dispose();
				
				blocks[i] = null ;
			}
			
		}
		
	}
	
}

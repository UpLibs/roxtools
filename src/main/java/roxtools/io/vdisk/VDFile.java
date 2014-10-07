package roxtools.io.vdisk;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final public class VDFile {

	final private VDisk vDisk ;
	final private VDBlock initBlock ;
	
	private VDBlock endBlock ;
	private int totalBlocks ;

	private VDBlock blockCursor ;
	private int blockCursorIdx ;
	
	protected VDFile(VDisk vDisk, VDBlock initBlock) {
		if ( initBlock.hasPrevBlock() ) throw new IllegalArgumentException("Can't have init block of file with previous blocks: "+ initBlock) ;
		
		this.vDisk = vDisk;
		this.initBlock = initBlock;
		
		if ( initBlock.hasNextBlock() ) {
			
			VDBlock lastBlock = initBlock ;
			int totalBlock = 1 ;
			
			while (true) {
				VDBlock nextBlock = lastBlock.getNextBlock() ;
				if (nextBlock == null) break ;
				
				lastBlock = nextBlock ;
				totalBlock++ ;
			}
			
			this.endBlock = lastBlock ;
			this.totalBlocks = totalBlock ;
		}
		else {
			this.endBlock = initBlock ;
			this.totalBlocks = 1 ;
		}
		
		this.blockCursor = initBlock ;
		this.blockCursorIdx = 0 ;
	}
	
	public String getID() {
		return this.initBlock.toStringIdent() ;
	}
	
	protected VDBlock getInitBlock() {
		return initBlock;
	}
	
	public int getTotalBlocks() {
		return totalBlocks;
	}
	
	public int size() {
		if ( isDeleted() ) return 0 ;
		return (vDisk.blockSize * (totalBlocks-1)) + endBlock.size() ;
	}
	
	public int sizeInDisk() {
		return vDisk.blockSize * totalBlocks ;
	}

	/////////////////////////////////////////////////////////////////////////////////
	
	public void setSize(int size) throws IOException {
		
		int blockSize = vDisk.blockSize ;
		
		int sizeInDiskForSize = (size / blockSize) * blockSize ;
		int sizeInDiskForSize_fullBlocks = sizeInDiskForSize ; 
		if (sizeInDiskForSize < size) sizeInDiskForSize += blockSize ;
		
		///////////////////////////////////////
		
		while ( sizeInDisk() > sizeInDiskForSize ) {
			boolean removed = removeBlock() ;
			assert(removed) ;
		}
		
		int endBlockSize = size - sizeInDiskForSize_fullBlocks ;
		
		assert( endBlockSize > 0 ) ;
		
		this.endBlock.setSize(endBlockSize) ;
		
		assert( size() == size ) ;
		
	}
	
	private boolean removeBlock() throws IOException {
		if ( this.endBlock == this.initBlock ) return false ; 
		
		VDBlock prevBlock = this.endBlock.getPrevBlock() ;
		
		this.endBlock.delete() ;
		
		this.endBlock = prevBlock ;
		this.totalBlocks-- ;
		
		assert( this.endBlock != null ) ;
		assert( this.totalBlocks > 0 ) ;
		
		return true ;
	}
	
	public void clear() throws IOException {
		
		VDBlock cursor = this.endBlock ;
		
		do {
			VDBlock prevBlock = cursor.getPrevBlock() ;
			
			if ( cursor != this.initBlock ) {
				cursor.delete() ;
			}
			
			cursor = prevBlock ;
		}
		while (cursor != null) ;
		
		this.endBlock = this.initBlock ;
		this.totalBlocks = 1 ;
		
		this.initBlock.setSize(0) ;
		
	}
	
	public void delete() throws IOException {
		
		VDBlock cursor = this.endBlock ;
		
		do {
			VDBlock prevBlock = cursor.getPrevBlock() ;
			
			cursor.delete() ;
			
			cursor = prevBlock ;
		}
		while (cursor != null) ;
		
		this.endBlock = null ;
		this.totalBlocks = 0 ;
		
	}
	
	public boolean isDeleted() {
		return this.totalBlocks == 0 ;
	}

	/////////////////////////////////////////////////////////////////////////////////
	
	private int pos = 0 ;
	
	public int getPosition() {
		return pos ;
	}
	
	public void seek(int pos) {
		this.pos = pos ;
	}
	
	public void skip(int n) {
		this.pos += n ;
	}
	
	public int available() {
		return size() - this.pos ;
	}
	
	public byte[] readFullData() throws IOException {
		int size = size() ;
		byte[] buff = new byte[size] ;
		
		int prevPos = this.pos ;
		
		seek(0) ;
		read(buff) ;
		
		this.pos = prevPos ;
		
		return buff ;
	}
	
	public void setFullData(byte[] data) throws IOException {
		
		setSize(data.length) ;
		
		seek(0) ;
		write(data) ;
		
	}
	
	public void read(byte[] buff) throws IOException {
		read(buff, 0, buff.length) ;
	}
	
	public void read(int pos, byte[] buff, int off, int length) throws IOException {
		
		int prevPos = this.pos ;
		
		this.pos = pos ;
		
		read(buff, off, length) ;
		
		this.pos = prevPos ;
		
	}
	
	public void read(byte[] buff, int off, int length) throws IOException {
		
		int blockSize = vDisk.blockSize ;
		
		while ( length > 0 ) {
			int blockIdx = pos / blockSize ;
			int posInBlock = pos - (blockIdx*blockSize) ;
			
			VDBlock block = getMyBlock(blockIdx) ;
			
			int available = block.size() - posInBlock ;
			
			int lng = length < available ? length : available ; 
			
			if (lng == 0) throw new EOFException() ;
			
			block.read(posInBlock, buff, off, lng) ;
			
			off += lng ;
			length -= lng ;
			
			pos += lng ;
		}
		
	}
	
	public void write(byte[] buff) throws IOException {
		write(buff, 0, buff.length) ;
	}
	
	public void write(int pos, byte[] buff, int off, int length) throws IOException {
		
		int prevPos = this.pos ;
		
		this.pos = pos ;
		
		write(buff, off, length) ;
		
		this.pos = prevPos ;
		
	}
	
	public void write(byte[] buff, int off, int length) throws IOException {
		
		int blockSize = vDisk.blockSize ;
		
		while ( length > 0 ) {
			int blockIdx = pos / blockSize ;
			int posInBlock = pos - (blockIdx*blockSize) ;
			
			VDBlock block = getMyBlock(blockIdx) ;
			
			if (block == null) {
				appendNewBlock() ;
				continue ;
			}
			
			int available = blockSize - posInBlock ;
			
			int lng = length < available ? length : available ; 
			
			if (lng == 0) {
				appendNewBlock() ;
				continue ;
			}
			
			block.write(posInBlock, buff, off, lng) ;
			
			off += lng ;
			length -= lng ;
			
			pos += lng ;
		}
		
	}
	
	private boolean appendNewBlock() throws IOException {
		
		VDSector sector = endBlock.getSector() ;
		
		if ( appendNewBlock(sector, endBlock.getBlockIndex()) ) return true ;
		
		int totalSectors = vDisk.getTotalSectors() ;
		
		int nextSectorIdx = sector.getSectorIndex()+1 ;
		
		if (nextSectorIdx < totalSectors) {
			VDSector nextSector = vDisk.getSector(nextSectorIdx) ;
			
			if ( appendNewBlock(nextSector) ) return true ;
		}
		
		int sectorIdx = sector.getSectorIndex() ;
		
		for (int i = 0; i < totalSectors; i++) {
			if (i == sectorIdx || i == nextSectorIdx) continue ;
			
			VDSector sect = vDisk.getSector(i) ;
			
			if ( appendNewBlock(sect) ) return true ;
		}
		
		if ( !vDisk.isStaticSectorSize() ) {
			
			VDSector newSector = vDisk.appendSector() ;
			
			if ( appendNewBlock(newSector) ) return true ;
		}
		
		throw new IOException("Can't find blocks in sectors! VDisk is full! Can't allocate a new block!") ;
	}
	
	private boolean appendNewBlock(VDSector sector) throws IOException {
		
		VDBlock newBlock = sector.createBlock() ;
		
		if (newBlock != null) return appendNewBlock(newBlock) ;
		
		return false ;
	}
	
	private boolean appendNewBlock(VDSector sector, int refBlockIndex) throws IOException {
		
		VDBlock newBlock = sector.createBlockAfterIndex(refBlockIndex) ;
		
		if (newBlock != null) return appendNewBlock(newBlock) ;
		
		newBlock = sector.createBlockBeforeIndex(refBlockIndex) ;
		
		if (newBlock != null) return appendNewBlock(newBlock) ;
		
		return false ;
	}
	
	private boolean appendNewBlock(VDBlock block) throws IOException {
		
		endBlock.setNextBlock(block) ;
		
		endBlock = block ;
		totalBlocks++ ;
		
		return true ;
	}
	
	private VDBlock getMyBlock(int myBlockIdx) {
		
		if (myBlockIdx == blockCursorIdx) return blockCursor ;
		
		if (myBlockIdx == 0) return initBlock ;
		if (myBlockIdx == totalBlocks-1) return endBlock ;
		
		if (myBlockIdx >= totalBlocks) return null ;
		
		int initDist = myBlockIdx ;
		int endDist = totalBlocks-myBlockIdx ;
		int cursorDistNext = myBlockIdx-blockCursorIdx ;
		int cursorDistPrev = blockCursorIdx-myBlockIdx ;
		
		if (cursorDistNext < 0) cursorDistNext = Integer.MAX_VALUE ;
		if (cursorDistPrev < 0) cursorDistPrev = Integer.MAX_VALUE ;
		
		VDBlock block ;
		if ( cursorDistNext == 1 ) {
			block = walkCursorNext(initBlock, 0, myBlockIdx) ;
		}
		else if ( initDist <= endDist && initDist <= cursorDistNext && initDist <= cursorDistPrev ) {
			block = walkCursorNext(initBlock, 0, myBlockIdx) ;
		}
		else if ( endDist <= cursorDistNext && endDist <= cursorDistPrev ) {
			block = walkCursorPrev(endBlock, totalBlocks-1, myBlockIdx) ;
		}
		else if ( cursorDistNext < cursorDistPrev ) {
			block = walkCursorNext(blockCursor, blockCursorIdx, myBlockIdx) ;
			if (block == null) block = walkCursorNext(initBlock, 0, myBlockIdx) ;
		}
		else if ( cursorDistPrev < cursorDistNext ) {
			block = walkCursorPrev(blockCursor, blockCursorIdx, myBlockIdx) ;
			if (block == null) block = walkCursorNext(initBlock, 0, myBlockIdx) ;
		}
		else {
			block = walkCursorNext(initBlock, 0, myBlockIdx) ;
		}
		
		blockCursor = block ;
		blockCursorIdx = myBlockIdx ;
		
		return block ;
	}
	
	private VDBlock walkCursorNext(VDBlock cursor , int cursorIdx, int targetIdx) {
		do {
			if (cursorIdx == targetIdx) return cursor ;
			
			cursor = cursor.getNextBlock() ;
			cursorIdx++ ;
		}
		while (cursor != null) ;
		
		throw new IllegalStateException("Can't walk until block!") ;
	}
	
	private VDBlock walkCursorPrev(VDBlock cursor , int cursorIdx, int targetIdx) {
		do {
			if (cursorIdx == targetIdx) return cursor ;
			
			cursor = cursor.getPrevBlock() ;
			cursorIdx-- ;
		}
		while (cursor != null) ;
		
		throw new IllegalStateException("Can't walk until block!") ;
	}

	/////////////////////////////////////////////////////////////////////////////////
	

	public void setMetaData(VDMetaData metaData) throws IOException {
		initBlock.setMetaData(metaData);
	}

	public VDMetaData getMetaData() throws IOException {
		return initBlock.getMetaData();
	}
	
	public String getMetaDataKey() throws IOException {
		return initBlock.getMetaDataKey() ;
	}

	public void clearMetaData() throws IOException {
		initBlock.clearMetaData();
	}

	public boolean hasMetaData() {
		return initBlock.hasMetaData();
	}

	
	/////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder() ;
		
		str.append(this.getClass().getName()) ;
		
		str.append("#") ;
		str.append(initBlock.toStringIdent()) ;
		
		str.append("[") ;
		
		if ( isDeleted() ) {
			str.append("deleted") ;	
		}
		else {
			
			str.append("size: ") ;
			str.append( this.size() ) ;
			str.append("/") ;
			str.append( this.sizeInDisk() ) ;
			
			
			if ( totalBlocks == 1 ) {
				str.append(" ; block: ") ;
				str.append( this.initBlock.toStringIdent() ) ;
			}
			else {
				str.append(" ; initBlock: ") ;
				str.append( this.initBlock.toStringIdent() ) ;
				
				str.append(" ; endBlock: ") ;
				str.append( this.endBlock.toStringIdent() ) ;
				
				str.append(" ; totalBlocks: ") ;
				str.append( this.totalBlocks ) ;
			}
			
			
			if ( hasMetaData() ) {
				int metaDataParameter0 = initBlock.getMetaDataParameter0() ;
				int metaDataParameter1 = initBlock.getMetaDataParameter1() ;
				
				str.append(" ; metaData: ") ;
				str.append(metaDataParameter0) ;
				str.append("@") ;
				str.append(metaDataParameter1) ;
			}
			
		}
		str.append("]") ;
		return str.toString();
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	
	final static private class MyInputStream extends InputStream {

		private VDFile vdFile ;
		
		protected MyInputStream(VDFile vdFile) {
			this.vdFile = vdFile;
		}

		private int streamPos = 0 ;
		
		@Override
		public long skip(long n) throws IOException {
			try {
				int available = availableNoIOException() ;
				
				int len = (int) n ;
				if (len > available) len = available ;
				
				streamPos += len ;
				
				return len ;	
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
		
		@Override
		public int available() throws IOException {
			try {
				return vdFile.size() - streamPos ;
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
		
		private int availableNoIOException() {
			return vdFile.size() - streamPos ;
		}
		
		final private byte[] bufferOneByte = new byte[1] ;
		
		@Override
		public int read() throws IOException {
			try {
				if ( availableNoIOException() <= 0 ) return -1 ;
				
				vdFile.read(streamPos, bufferOneByte,0,1) ;
				
				streamPos++ ;
				
				return bufferOneByte[0] & 0xff ;
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			try {
				int available = availableNoIOException() ;
				if ( available <= 0 ) return -1 ;
				
				if (len > available) len = available ;
				
				vdFile.read(streamPos, b,off,len) ;
				
				streamPos += len ;
				
				return len ;
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
	
		@Override
		public void close() throws IOException {
			VDFile vdFile = this.vdFile ;
			
			this.vdFile = null ;
			this.streamPos = 0 ;
			
			if (vdFile != null) vdFile.closeInputStream(this) ;
		}
		
	}

	private MyInputStream inputStream ;
	
	private void closeInputStream(MyInputStream stream) {
		synchronized (this) {
			if ( this.inputStream == stream ) {
				this.inputStream = null ;
			}
		}
	}
	
	public InputStream getInputStream() {
		if (inputStream == null) {
			synchronized (this) {
				if (inputStream != null) return inputStream ;
				inputStream = new MyInputStream(this) ;
			}
		}
		
		return inputStream ;
	}
	
	public InputStream createInputStream() {
		return new MyInputStream(this) ;
	}
	
	final static private class MyOutputStream extends OutputStream {
		
		private VDFile vdFile ;
		
		public MyOutputStream(VDFile vdFile) {
			this.vdFile = vdFile;
		}

		private int streamPos = 0 ;
		
		final private byte[] bufferOneByte = new byte[1] ;
		
		@Override
		public void write(int b) throws IOException {
			try {
				bufferOneByte[0] = (byte)b ;
				
				vdFile.write(streamPos, bufferOneByte, 0, 1) ;
				
				streamPos += 1 ;
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			try {
				vdFile.write(streamPos, b, off, len) ;
				
				streamPos += len ;
			}
			catch (NullPointerException e) {
				throw new IOException("Closed stream", e) ;
			}
		}
		
		@Override
		public void close() throws IOException {
			VDFile vdFile = this.vdFile ;
			
			this.vdFile = null ;
			this.streamPos = 0 ;
			
			if (vdFile != null) vdFile.closeOutputStream(this) ;
		}
		
	}

	private MyOutputStream outputStream ;
	
	private void closeOutputStream(MyOutputStream stream) {
		synchronized (this) {
			if ( this.outputStream == stream ) {
				this.inputStream = null ;
			}
		}
	}
	
	public OutputStream getOutputStream() {
		if (outputStream == null) {
			synchronized (this) {
				if (outputStream != null) return outputStream ;
				outputStream = new MyOutputStream(this) ;
			}
		}
		
		return outputStream ;
	}
	
	public OutputStream createOutputStream() {
		return new MyOutputStream(this) ;
	}
	
}

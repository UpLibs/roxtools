package roxtools.io.vdisk;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import roxtools.SerializationUtils;
import roxtools.io.vdisk.VDisk.FilesMetaDataKeyFilter;

final public class VDSector {
	
	static public final String SECTOR_FILE_SUFIX = ".sector" ;
	
	static public int getSectorFileIndex(File sectorFile) {
		
		String name = sectorFile.getName() ;
		
		if ( !name.endsWith(SECTOR_FILE_SUFIX) ) return -1 ;
		
		String nameInit = name.substring(0, name.length()-SECTOR_FILE_SUFIX.length()) ;
		
		return Integer.parseInt(nameInit) ;
		
	}
	
	static public File createSectorFile(VDisk vDisk, int sectorIndex) {
		return new File( vDisk.getVDiskDir() , sectorIndex+SECTOR_FILE_SUFIX );
	}

	static private final SoftReference<VDBlock> NULL_REF_BLOCK = new SoftReference<VDBlock>(null) ;
	

	final private VDisk vDisk ;
	
	final protected int blockSize ;
	final protected int blockUsageSize ;
	final protected int blockUsageSizeBytes ;
	
	
	final private int sectorIndex ;
	
	final private File sectorFile ;
	
	final private SoftReference<VDBlock>[] blocks ;
	
	private RandomAccessFile io ;

	final private byte[] header ;
	
	final private FileKeysTable keysTable ;
	
	@SuppressWarnings("unchecked")
	protected VDSector(VDisk vDisk, int sectorIndex) throws IOException {
		super();
		this.vDisk = vDisk;
		
		this.blockSize = vDisk.blockSize ;
		this.blockUsageSize = vDisk.blockUsageSize ;
		this.blockUsageSizeBytes = vDisk.blockUsageSizeBytes ;
		
		this.sectorIndex = sectorIndex ;
		this.sectorFile = createSectorFile(vDisk, sectorIndex) ; 
		
		this.blocks = new SoftReference[ vDisk.sectorSize ] ;
		
		this.io = new RandomAccessFile(sectorFile, "rw") ;
		
		if (this.io.length() != vDisk.totalSectorSize) {
			eraseSector() ;
		}
		
		this.header = new byte[ vDisk.sectorHeaderSize ] ;
		
		read(0, header, 0, header.length) ;
		
		for (int i = 0; i < this.blocks.length; i++) {
			boolean unused = isHeaderBlockUnused(i) ;
			this.blocks[i] = unused ? null : NULL_REF_BLOCK ;
		}
		
		keysTable = new FileKeysTable(this) ;
	}
	
	private void eraseSector() throws IOException {
		
		int sz = vDisk.totalSectorSize ;
		
		this.io.setLength(sz) ;
		
		byte[] buff = new byte[ Math.min(sz, 1024*1024)] ;
		
		int wrote = 0 ;
		
		while ( wrote < sz ) {
			int lng = Math.min( buff.length , sz-wrote ) ;
			this.io.write(buff, 0, lng) ;
			wrote += lng ;
		}
		
		if (wrote != sz) throw new IllegalStateException() ;
	}
	
	public VDisk getVDisk() {
		return vDisk;
	}
	
	protected int getSectorIndex() {
		return sectorIndex;
	}
	
	public int getTotalBlocks() {
		return vDisk.sectorSize ;
	}
	
	//////////////////////////////////////////////////////////////////////
	
	synchronized protected void read(int pos, byte[] buff, int off, int lng) throws IOException {
		
		io.seek(pos) ;
		
		int r ;
		while ( lng > 0 && (r = io.read(buff, off, lng)) >= 0 ) {
			off += r ;
			lng -= r ;
		}
		
		if (lng != 0) throw new EOFException("Trying to read from position "+ pos +". Still needing "+ lng +" bytes to read.") ;
	}
	
	synchronized protected void readFromBlock(int blockIndex, int posInsideBlock, byte[] buff, int off, int lng) throws IOException {
		if (posInsideBlock+lng > blockSize) throw new IOException("Reading outside of blockSize: pos:"+ posInsideBlock+" + lng:"+lng +" > "+ blockSize) ;
		
		int blockInitPos = header.length + ( blockIndex * blockSize ) ;
		
		read(blockInitPos+ posInsideBlock, buff, off, lng) ;
		
	}
	
	synchronized protected void writeToBlock(int blockIndex, int posInsideBlock, byte[] buff, int off, int lng) throws IOException {
		if (posInsideBlock+lng > blockSize) throw new IOException("Writing outside of blockSize: pos:"+ posInsideBlock+" + lng:"+lng +" > "+ blockSize) ;
			
		int blockInitPos = header.length + ( blockIndex * blockSize ) ;
		
		int pos = blockInitPos + posInsideBlock ;
		
		io.seek(pos) ;
		
		io.write(buff, off, lng) ;
		
	}
	
	synchronized protected int[] getHeaderBlockUsage(int blockIndex) {
		
		int pos = blockIndex * blockUsageSizeBytes ;
		
		int[] usage = SerializationUtils.readInts(header, pos, blockUsageSizeBytes) ;
		
		return usage ;
	}
	
	private int headerUnflushed_init = Integer.MAX_VALUE ;
	private int headerUnflushed_end = Integer.MIN_VALUE ;
	
	synchronized protected void writeHeaderBlockUsage(int blockIndex, int[] usage) {
		
		int pos = blockIndex * blockUsageSizeBytes ;
		
		if (usage.length != blockUsageSizeBytes/4) throw new IllegalStateException() ;
		
		SerializationUtils.writeInts(usage, header, pos) ;
		
		if ( pos < headerUnflushed_init ) headerUnflushed_init = pos ;
		
		int end = pos + blockUsageSizeBytes ;
		
		if ( end > headerUnflushed_end ) headerUnflushed_end = end ;
	}
	
	synchronized protected boolean isHeaderBlockUnused(int blockIndex) {
		
		int pos = blockIndex * blockUsageSizeBytes ;
		int end = pos + blockUsageSizeBytes ;
		
		for (int i = pos; i < end; i++) {
			if (header[i] != 0) return false ;
		}
		
		return true ;
	}
	
	synchronized private boolean hasUnflushedHeader() {
		return headerUnflushed_end > headerUnflushed_init ;
	}
	
	protected void flushHeader() throws IOException {
		
		synchronized (this) {
			if ( hasUnflushedHeader() ) {
				writeHeaderImplem() ;
			}
		}
		
	}
	
	synchronized protected void close() throws IOException {
		
		this.keysTable.close() ;
		
		this.io.close() ;
		
	}
	
	private void writeHeader() throws IOException {
	
		if ( vDisk.isAsyncWriteHeadersEnabled() ) {
			scheduleAsyncWriteHeaders() ;
		}
		else {
			writeHeaderImplem() ; 
		}
		
	}
	
	private class AsyncWriteHeader extends TimerTask {

		@Override
		public void run() {
	
			try {
				writeHeaderScheduled() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
	}
	
	final static private Timer ASYNC_HEADER_WRITER_TIMER = new Timer("VDisk:header_writer", true) ;
	
	static final public int ASYNC_WRITE_HEADERS_DELAY = 1000 ;
	
	private void scheduleAsyncWriteHeaders() {
		if ( !hasUnflushedHeader() ) return ;
		
		ASYNC_HEADER_WRITER_TIMER.schedule(new AsyncWriteHeader(), ASYNC_WRITE_HEADERS_DELAY) ;
	}
	
	private void writeHeaderScheduled() throws IOException {
	
		synchronized (this) {
			if ( hasUnflushedHeader() ) {
				writeHeaderImplem() ;
			}
		}
		
	}
	
	synchronized private void writeHeaderImplem() throws IOException {
		
		int unflushedLng = headerUnflushed_end - headerUnflushed_init ;
		
		if (unflushedLng <= 0) {
			//System.out.println(sectorIndex+"> HEADER WRITE> full");
			
			new Throwable().printStackTrace() ;
			
			io.seek(0) ;
			io.write(header) ;	
		}
		else {
			//System.out.println(sectorIndex+"> HEADER WRITE> "+ headerUnflushed_init +" .. "+ headerUnflushed_end);
			
			io.seek(headerUnflushed_init) ;
			io.write(header, headerUnflushed_init , unflushedLng) ;
		}
		
		headerUnflushed_init = Integer.MAX_VALUE ;
		headerUnflushed_end = Integer.MIN_VALUE ;
		
	}
	
	synchronized protected boolean isBlockUsed(int blockIndex) {
		return !isHeaderBlockUnused(blockIndex) ;
	}
	
	synchronized protected boolean isRootBlock(int blockIndex) {
		int[] usage = getHeaderBlockUsage(blockIndex) ;
		
		return usage[0] != 0 && usage[3] == 0 && usage[4] == 0 ;
	}
	
	public List<String> getRootBlocksIdents() {
		ArrayList<String> ids = new ArrayList<String>() ;
		
		getRootBlocksIdents(ids) ;
		
		return ids ;
	}
	
	synchronized public void getRootBlocksIdents(List<String> ids) {
		int sz = getTotalBlocks() ;
		
		for (int i = 0; i < sz; i++) {
			if ( isRootBlock(i) ) {
				String ident = VDBlock.toStringIdent(i, sectorIndex) ;
				
				ids.add(ident) ; 
			}
		}
	}
	
	public Iterator<String> iterateRootBlocksIdents() {
		return new Iterator<String>() {
			int cursor = 0 ;
			
			private int lastCheckedCursor = -1 ;
			
			private void checkCursor() {
				if ( lastCheckedCursor == cursor ) return ;
				
				while ( !isRootBlock(cursor) ) {
					cursor++ ;
					if (cursor >= vDisk.sectorSize) break ;
				}
				
				lastCheckedCursor = cursor ;
			}
			
			@Override
			public boolean hasNext() {
				checkCursor() ;
				return cursor < vDisk.sectorSize ;
			}

			@Override
			public String next() {
				checkCursor() ;
				
				int i = cursor;
				if (i >= vDisk.sectorSize) throw new NoSuchElementException();

	            cursor = i + 1;
	            
	            return i+"@"+sectorIndex ;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException() ;
			}
		};
		
	}
	
	public List<String> getMetaDataKeys() {
		ArrayList<String> keys = new ArrayList<String>() ;
		
		getMetaDataKeys(keys) ;
		
		return keys ;
	}
	
	synchronized public void getMetaDataKeys( List<String> keys ) {
		
		synchronized (keysTable) {
			Iterator<String> iteratorKeys = keysTable.iteratorKeys() ;
			
			while ( iteratorKeys.hasNext() ) {
				String key = iteratorKeys.next() ;
				keys.add(key) ;
			}
		}
		
	}
	
	synchronized public void getMetaDataKeysWithPrefix( String prefix, List<String> keys ) {

		synchronized (keysTable) {
			Iterator<String> iteratorKeys = keysTable.iteratorKeys() ;
			
			while ( iteratorKeys.hasNext() ) {
				String key = iteratorKeys.next() ;
				if ( key.startsWith(prefix) ) keys.add(key) ;
			}
		}
		
	}
	
	synchronized public void getMetaDataKeys( FilesMetaDataKeyFilter filter, List<String> keys ) {
		
		synchronized (keysTable) {
			Iterator<String> iteratorKeys = keysTable.iteratorKeys() ;
			
			while ( iteratorKeys.hasNext() ) {
				String key = iteratorKeys.next() ;
				if ( filter.accept(key) ) keys.add(key) ;
			}
		}
		
	}
	
	public Iterator<String> iterateMetaDataKeys() {
		return keysTable.iteratorKeys() ;
	}
	
	synchronized protected VDBlock getBlock(int blockIndex) {
		SoftReference<VDBlock> ref = this.blocks[blockIndex] ;
		
		if (ref == null) return null ;
		
		VDBlock block = ref.get() ;
		
		if (block != null) return block ;
		
		block = new VDBlock(this, blockIndex, false) ;
		
		this.blocks[blockIndex] = new SoftReference<VDBlock>(block) ;
		
		return block ;
	}
	
	synchronized protected VDBlock createBlock(int blockIndex) throws IOException {
		VDBlock block = getBlock(blockIndex) ;
		
		if (block != null) return block ;
		
		return createBlockAtImplem(blockIndex) ;
	}

	private VDBlock createBlockAtImplem(int blockIndex) throws IOException {
		VDBlock block ;
		
		synchronized (this) {
			block = new VDBlock(this, blockIndex, true) ;
			
			this.blocks[blockIndex] = new SoftReference<VDBlock>(block) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		writeHeader() ;
		
		return block ;
	}
	
	synchronized protected VDBlock createBlock() throws IOException {
		int sz = blocks.length ;
		
		for (int i = 0; i < sz; i++) {
			SoftReference<VDBlock> ref = blocks[i] ;
			if (ref == null) return createBlockAtImplem(i) ;
		}
		
		return null ;
	}
	
	synchronized protected VDBlock createBlockNearIndex(int idx) throws IOException {
		int sz = blocks.length ;
		
		int next = idx+1 ;
		int prev = idx-1 ;
		
		while (true) {
			
			if ( next < sz && prev >= 0 ) {
				SoftReference<VDBlock> refNext = blocks[next] ;
				if (refNext == null) return createBlockAtImplem(next) ;
				
				SoftReference<VDBlock> refPrev = blocks[prev] ;
				if (refPrev == null) return createBlockAtImplem(prev) ;
				
				next++ ;
				prev-- ;
			}
			else  if ( next < sz ) {
				return createBlockAfterIndex(next) ;
			}
			else if ( prev >= 0 ) {
				return createBlockBeforeIndex(prev) ;
			}
			
		}
		
	}
	
	synchronized protected VDBlock createBlockAfterIndex(int idx) throws IOException {
		int sz = blocks.length ;

		for (int i = idx+1; i < sz; i++) {
			SoftReference<VDBlock> ref = blocks[i] ;
			if (ref == null) return createBlockAtImplem(i) ;
		}
		
		return null ;
	}
	
	synchronized protected VDBlock createBlockBeforeIndex(int idx) throws IOException {

		for (int i = idx-1; i >= 0; i--) {
			SoftReference<VDBlock> ref = blocks[i] ;
			if (ref == null) return createBlockAtImplem(i) ;
		}
		
		return null ;
	}
	
	protected void deleteBlock(int blockIndex) throws IOException {
		
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
			
			if ( block.hasPrevBlock() ) {
				block.unlinkPrevBlock() ;
			}
			
			block.setInternalsDeleted() ;
			
			this.blocks[blockIndex] = null ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		writeHeader() ;
	}
	
	protected void clear() throws IOException {
		
		int sz = getTotalBlocks() ;
	
		for (int i = 0; i < sz; i++) {
			
			VDBlock block = getBlock(i) ;
			
			if (block == null) continue ;
			
			block.deleteBlockChain() ;
			
		}
		
		for (int i = 0; i < sz; i++) {
			
			VDBlock block = getBlock(i) ;
			
			if (block == null) continue ;
			
			block.deleteBlockChain() ;
			
		}
		
		for (int i = 0; i < sz; i++) {
			
			VDBlock block = getBlock(i) ;
			
			if (block == null) continue ;
			
			block.deleteForce() ;
			
		}
	
		keysTable.clearKeysTables();
		
	}
	
	protected void setBlockSize(int blockIndex, int size) throws IOException {
		if (size < 0 || size > blockSize) throw new IllegalArgumentException("Invalid block size: "+ size) ;
		
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
			
			if (block.size() == size) return ;
			
			
			if ( size < blockSize && block.hasNextBlock() ) throw new IOException("Can't set size of a block that have next block! Blocks with next block need to have full size.") ;
			
			block.setInternalsSize(size) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		writeHeader() ;
	}
	
	protected void setBlockMetaData(int blockIndex, int metaData0, int metaData1) throws IOException {
		
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
			
			block.setInternalsMetaData(metaData0, metaData1) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		writeHeader() ;
		
	}
	
	protected VDBlock getBlockSameSector(int blockIndex, int blockSector) {
		if (blockSector == this.sectorIndex) {
			return getBlock(blockIndex) ;
		}
		else {
			throw new IllegalStateException() ;
		}
	}
	
	protected VDBlock getBlockOtherSector(int blockIndex, int blockSector) {
		if (blockSector != this.sectorIndex) {
			VDSector sector = vDisk.getSectorNoSynch(blockSector) ;
			return sector.getBlock(blockIndex) ;
		}
		else {
			throw new IllegalStateException() ;
		}
	}
	
	protected void setNextBlock(int blockIndex, int nextBlockIndex, int nextBlockSector) throws IOException {
		
		VDBlock nextBlock ;
		
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
			
			int currentSize = block.size() ;
			
			if (currentSize != blockSize) throw new IOException("Can't set next block if current block is not full: "+ currentSize +" / "+ blockSize) ;
			
			if (nextBlockSector == sectorIndex) {
				nextBlock = getBlockSameSector(nextBlockIndex, nextBlockSector) ;
				if (nextBlock == null) throw new IOException("Can't find next block in same sector") ;
				if ( nextBlock.hasPrevBlock() ) throw new IOException("Next block already have a previous block: "+ nextBlock) ;
			}
			else {
				nextBlock = null ;
			}
			
			block.setInternalsNextBlock(nextBlockIndex, nextBlockSector) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		if (nextBlock == null) {
			nextBlock = getBlockOtherSector(nextBlockIndex, nextBlockSector) ;
			if (nextBlock == null) throw new IOException("Can't find next block in other sector") ;
			if ( nextBlock.hasPrevBlock() ) throw new IOException("Next block already have a previous block: "+ nextBlock) ;
		}
		
		if ( nextBlock.getSectorIndex() == sectorIndex ) {
			nextBlock.getSector().setPrevBlock(nextBlockIndex, blockIndex, sectorIndex) ;	
		}
		else {
			VDSector sector2 = nextBlock.getSector() ;
			
			Object mutexA ;
			Object mutexB ;
			
			if ( this.hashCode() < sector2.hashCode() ) {
				mutexA = this ;
				mutexB = sector2 ;
			}
			else {
				mutexA = sector2 ;
				mutexB = this ;
			}
			
			synchronized (mutexA) {
				synchronized (mutexB) {
					sector2.setPrevBlock(nextBlockIndex, blockIndex, sectorIndex) ;		
				}
			}
			
		}
		
		if ( hasUnflushedHeader() ) writeHeader() ;
	}
	
	protected void setPrevBlock(int blockIndex, int prevBlockIndex, int prevBlockSector) throws IOException {
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
			
			if ( block.hasPrevBlock() ) throw new IOException("Block already have a previous block: "+ block) ;
			
			block.setInternalsPrevBlock(prevBlockIndex, prevBlockSector) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		writeHeader() ;
	}

	public void unlinkNextBlock(int blockIndex, int nextBlockIndex, int nextBlockSector) throws IOException {
		
		VDBlock nextBlock ;
		
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;
	
			if ( !block.hasNextBlock() ) throw new IOException("Block doens't have next block: "+ block) ;
			
			nextBlock = block.getNextBlock() ;
			
			block.setInternalsNextBlock(-1, -1) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
		
		
		if ( nextBlock.getSectorIndex() == sectorIndex ) {
			nextBlock.getSector().unlinkPrevBlock(nextBlockIndex, blockIndex, sectorIndex) ;	
		}
		else {
			VDSector sector2 = nextBlock.getSector() ;
			
			Object mutexA ;
			Object mutexB ;
			
			if ( this.hashCode() < sector2.hashCode() ) {
				mutexA = this ;
				mutexB = sector2 ;
			}
			else {
				mutexA = sector2 ;
				mutexB = this ;
			}
			
			synchronized (mutexA) {
				synchronized (mutexB) {
					sector2.unlinkPrevBlock(nextBlockIndex, blockIndex, sectorIndex) ;		
				}
			}
			
		}
		
		if ( hasUnflushedHeader() ) writeHeader() ;
		
	}
	
	public void unlinkPrevBlock(int blockIndex, int prevBlockIndex, int prevBlockSector) throws IOException {
		synchronized (this) {
			VDBlock block = getBlock(blockIndex) ;
			
			if (block == null) return ;

			if ( !block.hasPrevBlock() ) throw new IOException("Block doens't have prev block: "+ block) ;
			
			block.setInternalsPrevBlock(-1, -1) ;
			
			writeHeaderBlockUsage(blockIndex, block.usage) ;
		}
				
		writeHeader() ;
	}
	
	synchronized protected int[] getMetaDataKey(String key) {
		return keysTable.getFileIdent(key) ;
	}
	
	synchronized protected boolean containsMetaDataKey(String key) {
		return keysTable.containsFileIdent(key) ;
	}
	
	public void notifyMetaDataKeyChange(String key, int blockIndex, int blockSector) {
		keysTable.notifyMetaDataKeyChange(key, blockIndex, blockSector);
	}

	public void notifyMetaDataKeyRemove(String key, int blockIndex, int blockSector) {
		keysTable.notifyMetaDataKeyRemove(key, blockIndex, blockSector);
	}
	
	////////////////////////////////////////////////////////////////////////
	
	static protected int[] joinIdents(int[] idents, int blockIndex, int blockSector) {
		
		for (int i = 0; i < idents.length; i+=2) {
			int blkIdx = idents[i] ;
			
			if (blkIdx != blockIndex) continue ;
			
			int blkSect = idents[i+1] ;
			
			if (blkSect != blockSector) continue ;
			
			return idents ;
		}
		
		int[] idents2 = new int[ idents.length +2 ] ;
		
		idents2[0] = blockIndex ;
		idents2[1] = blockSector ;
		
		System.arraycopy(idents, 0, idents2, 2, idents.length) ;
		
		return idents2 ;
	}
	
	static protected int[] removeIdent(int[] idents, int blockIndex, int blockSector) {
		
		int foundIndex = -1 ;
		
		for (int i = 0; i < idents.length; i+=2) {
			int blkIdx = idents[i] ;
			
			if (blkIdx != blockIndex) continue ;
			
			int blkSect = idents[i+1] ;
			
			if (blkSect != blockSector) continue ;
			
			foundIndex = i ;
			break ;
		}
		
		if (foundIndex < 0) return idents ;
		
		if (idents.length == 2) return null ;
		
		int[] idents2 = new int[ idents.length -2 ] ;
		
		System.arraycopy(idents, 0, idents2, 0, foundIndex) ;
		System.arraycopy(idents, foundIndex+2, idents2, foundIndex, idents.length-(foundIndex+2)) ;
		
		return idents2 ;
	}
	
	static protected int[] joinIdentsNoCheckDuplicity(int[] idents1, int[] idents2) {
		int[] idents3 = new int[ idents1.length + idents2.length ] ;
		
		System.arraycopy(idents1, 0, idents3, 0, idents1.length) ;
		System.arraycopy(idents2, 0, idents3, idents1.length, idents2.length) ;
		
		return idents3 ;
	}

}

package roxtools.io.vdisk;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;

final public class VDBlock implements Serializable {
	private static final long serialVersionUID = 6967344815644813651L;

	private final VDSector sector ;
	
	protected int blockIndex ;
	
	protected int[] usage ;
	
	protected VDBlock(VDSector sector, int blockIndex, boolean blank) {
		this.sector = sector;
		this.blockIndex = blockIndex;
		
		if (blank) {
			this.usage = new int[sector.blockUsageSize] ;
			
			this.usage[2] = -1 ;
		}
		else {
			this.usage = sector.getHeaderBlockUsage(blockIndex) ;	
		}
		
	}
	
	/////////////////////////////////////////////////////
	
	public boolean isRootBlock() {
		synchronized (sector) {
			return !hasPrevBlock() && !isDeleted() ;
		}
	}
	
	private VDFile vdFile ;
	
	protected boolean hasVDFile() {
		synchronized (sector) {
			return isRootBlock() ;
		}
	}
	
	protected VDFile getVDFile() {
		synchronized (sector) {
			if ( this.vdFile != null ) return this.vdFile ;
			
			if ( !isRootBlock() ) return null ;
			
			VDFile vdFile = new VDFile(sector.getVDisk() , this) ;
			
			this.vdFile = vdFile ;
			
			return vdFile ;
		
		}
	}
	
	/////////////////////////////////////////////////////
	
	protected VDSector getSector() {
		return sector;
	}
	
	protected int getSectorIndex() {
		return sector.getSectorIndex() ;
	}

	public boolean isUsed() {
		synchronized (sector) {
			return 
					this.usage[0] != 0 || 
					this.usage[1] != 0 ||
					this.usage[2] != 0 ||
					this.usage[3] != 0 ||
					this.usage[4] != 0 ;
		}
	}
	
	public VDBlock getNextBlock() {
		if (!hasNextBlock()) return null ;
		
		int nextBlockIndex = getNextBlockIndex() ;
		int nextBlockSector = getNextBlockSector() ;
		
		if (nextBlockSector == sector.getSectorIndex()) {
			return sector.getBlockSameSector(nextBlockIndex, nextBlockSector) ;
		}
		else {
			return sector.getBlockOtherSector(nextBlockIndex, nextBlockSector) ;	
		}
	}
	
	public VDBlock getPrevBlock() {
		if (!hasPrevBlock()) return null ;
		
		int prevBlockIndex = getPrevBlockIndex();
		int prevBlockSector = getPrevBlockSector();
		
		if (prevBlockSector == sector.getSectorIndex()) {
			return sector.getBlockSameSector(prevBlockIndex, prevBlockSector) ;
		}
		else {
			return sector.getBlockOtherSector(prevBlockIndex, prevBlockSector) ;	
		}
	}
	
	public int getNextBlockIndex() {
		synchronized (sector) {
			return this.usage[1] -1 ;
		}
	}
	
	public int getNextBlockSector() {
		synchronized (sector) {
			return this.usage[2] -1 ;
		}
	}
	
	public int getPrevBlockIndex() {
		synchronized (sector) {
			return this.usage[3] -1 ;
		}
	}
	
	public int getPrevBlockSector() {
		synchronized (sector) {
			return this.usage[4] -1 ;
		}
	}
	
	public boolean hasNextBlock() {
		synchronized (sector) {
			return this.usage[1] > 0 ;
		}
	}
	
	public boolean hasPrevBlock() {
		synchronized (sector) {
			return this.usage[3] > 0 ;
		}
	}
	
	public int size() {
		synchronized (sector) {
			return usage[0] ;
		}
	}
	
	public boolean isFull() {
		return size() == sector.blockSize ;
	}
	
	public int getBlockIndex() {
		int idx = this.blockIndex ;
		
		return idx >= 0 ? idx : (-idx)-1 ;
	}

	protected void setInternalsDeleted() {
		for (int i = 0; i < usage.length; i++) {
			usage[i] = 0 ;
		}
		
		this.blockIndex = this.blockIndex >= 0 ? -(this.blockIndex+1) : this.blockIndex ;
		
		this.vdFile = null ;
	}

	public boolean isDeleted() {
		synchronized (sector) {
			return this.blockIndex < 0 ;	
		}
	}
	
	public void delete() throws IOException {
		if ( isDeleted() ) return ;
		if  ( hasNextBlock() ) throw new IOException("Can't delete block with next block! Please use deleteBlockChain().") ;
		
		clearMetaData() ;
		
		this.sector.deleteBlock(this.blockIndex) ;
	}
	
	public void deleteForce() throws IOException {
		if ( isDeleted() ) return ;
		
		clearMetaData() ;
		
		this.sector.deleteBlock(this.blockIndex) ;
	}
	
	public void deleteBlockChain() throws IOException {
		if ( isDeleted() ) return ;
		
		if ( this.hasNextBlock() ) {
			
			VDBlock lastBlock = this.getNextBlock() ;
			
			while ( lastBlock.hasNextBlock() ) {
				lastBlock = lastBlock.getNextBlock() ;
			}
			
			VDBlock prevBlock = lastBlock.getPrevBlock() ;
			
			while (true) {
				lastBlock.delete() ;
				
				if (prevBlock == null) break ;
				
				lastBlock = prevBlock ;
				prevBlock = lastBlock.getPrevBlock() ;
			}
			
		}
		else {
			this.delete() ;
		}
		
	}
	
	public void setSize(int size) throws IOException {
		this.sector.setBlockSize(blockIndex, size) ;
	}

	protected void setInternalsSize(int size) {
		this.usage[0] = size ;
	}

	protected void setMetaDataParameter(int metaData0, int metaData1) throws IOException {
		this.sector.setBlockMetaData(blockIndex, metaData0, metaData1) ;
	}
	
	protected void removeMetaDataParameter() throws IOException {
		this.sector.setBlockMetaData(blockIndex, -1, -1) ;
	}
	
	protected boolean hasMetaDataParameterSet() {
		if ( !canHaveMetaData() ) return false ;
		
		return this.usage.length > 5 && (this.usage[5] != 0 || this.usage[6] != 0) ;
	}
	
	protected int getMetaDataParameter0() {
		return this.usage[5] -1 ; 
	}
	
	protected int getMetaDataParameter1() {
		return this.usage[6] -1 ; 
	}
	
	protected void setInternalsMetaData(int metaData0, int metaData1) {
		this.usage[5] = metaData0+1 ;
		this.usage[6] = metaData1+1 ;
	}
	

	private VDFile getMetaDataFile() throws IOException {
		if ( !canHaveMetaData() ) throw new UnsupportedOperationException() ;
		
		if ( hasPrevBlock() ) throw new IOException("Can't have meta data with previous block!") ;
		
		VDisk metadataDisk = sector.getVDisk().getMetadataDisk() ;
		
		if ( !hasMetaDataParameterSet() ) {
			VDFile metaDataFile = metadataDisk.createFile() ;
			
			VDBlock metaDataFileInitBlock = metaDataFile.getInitBlock() ;
			
			setMetaDataParameter( metaDataFileInitBlock.getBlockIndex() , metaDataFileInitBlock.getSectorIndex() ) ;
			
			return metaDataFile ;
		}
		else {
			int metaDataParameter0 = getMetaDataParameter0() ;
			int metaDataParameter1 = getMetaDataParameter1() ;
			
			VDFile metaDataFile = metadataDisk.getFileByID( metaDataParameter0 , metaDataParameter1 ) ;
		
			return metaDataFile ;
		}
	}
	
	transient private SoftReference<VDMetaData> metaDataRef ;
	transient private String metaDataKey ;
	
	public void setMetaData(VDMetaData metaData) throws IOException {
		
		VDFile metaDataFile = getMetaDataFile() ;
		
		metaDataFile.setFullData(metaData.getSerial()) ;
		
		metaData.setParentBlock(getBlockIndex(), getSectorIndex()) ;
		
		metaDataRef = new SoftReference<VDMetaData>(metaData) ;
		metaDataKey = metaData.getKey() ;
		
		sector.notifyMetaDataKeyChange(metaData.getKey() , blockIndex, getSectorIndex()) ;
	}
	
	public String getMetaDataKey() throws IOException {
		if ( !hasMetaDataParameterSet() ) return null ;
		
		if (metaDataKey != null) {
			return metaDataKey ;
		}
		
		if (metaDataRef != null) {
			VDMetaData metaData = metaDataRef.get() ;
			if (metaData != null) {
				return metaDataKey = metaData.getKey() ;
			}
		}
		
		VDFile metaDataFile = getMetaDataFile() ;
		
		if (metaDataFile == null) throw new IOException("Can't find metadata file to read block key: sectorIdx: "+ getSectorIndex() +" ; blockIdx: "+ this.getBlockIndex()) ;
		
		return metaDataKey = VDMetaData.readKeyFromSerial(metaDataFile) ;
	}
	
	public VDMetaData getMetaData() throws IOException {
		if ( !hasMetaDataParameterSet() ) return null ;
		
		if (metaDataRef != null) {
			VDMetaData metaData = metaDataRef.get() ;
			if (metaData != null) return metaData ;
		}
		
		VDFile metaDataFile = getMetaDataFile() ;
		
		byte[] serial = metaDataFile.readFullData() ;
		
		VDMetaData metaData = new VDMetaData(serial) ;

		metaData.setParentBlock(getBlockIndex(), getSectorIndex()) ;
		
		metaDataRef = new SoftReference<VDMetaData>(metaData) ;
		metaDataKey = metaData.getKey() ;
		
		return metaData ;
	}
	
	public void clearMetaData() throws IOException {
		if ( !hasMetaDataParameterSet() ) return ;
		
		String metaDataKey = null ;
		
		if (metaDataRef != null) {
			VDMetaData metaData = metaDataRef.get() ;
			if (metaData != null) {
				metaDataKey = metaData.getKey() ;
				metaData.clearParentBlock() ;
			}
		}
		
		VDFile metaDataFile = getMetaDataFile() ;
		
		if (metaDataKey == null) {
			metaDataKey = VDMetaData.readKeyFromSerial(metaDataFile) ;
		}
		
		metaDataFile.delete() ;
		
		removeMetaDataParameter() ;
		
		metaDataRef = null ;
		metaDataKey = null ;
		
		sector.notifyMetaDataKeyRemove(metaDataKey , blockIndex, getSectorIndex()) ;
		
	}
	
	public boolean canHaveMetaData() {
		return !sector.getVDisk().isMetaDataDisk() ;
	}
	
	public boolean hasMetaData() {
		if ( !canHaveMetaData() ) return false ;
		
		return hasMetaDataParameterSet() ;
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	private int pos = 0 ;
	
	public int getPosition() {
		return pos ;
	}
	
	public void seek(int pos) throws IOException {
		if (pos > this.sector.blockSize) throw new IOException("position out of blockSize: "+ sector.blockSize) ;
		this.pos = pos ;
	}
	
	public int remainingToWrite() {
		return sector.blockSize - size() ;
	}
	
	public int availableToRead() {
		return size() - pos ;
	}
	
	public void write(byte[] buff) throws IOException {
		write(buff, 0, buff.length) ;
	}
	
	public void write(int pos, byte[] buff, int off, int lng) throws IOException {
		sector.writeToBlock(blockIndex, pos, buff, off, lng) ;
		this.pos = pos + lng ;
		
		if ( this.pos > size() ) {
			setSize(this.pos) ;
		}
	}
	
	public void write(byte[] buff, int off, int lng) throws IOException {
		sector.writeToBlock(blockIndex, this.pos, buff, off, lng) ;
		this.pos += lng ;
		
		if ( this.pos > size() ) {
			setSize(this.pos) ;
		}
	}
	
	public void read(byte[] buff) throws IOException {
		read(buff, 0, buff.length) ;
	}
	
	public void read(int pos, byte[] buff, int off, int lng) throws IOException {
		sector.readFromBlock(blockIndex, pos, buff, off, lng) ;
		this.pos = pos + lng ;
	}
	
	public void read(byte[] buff, int off, int lng) throws IOException {
		sector.readFromBlock(blockIndex, pos, buff, off, lng) ;
		pos += lng ;
	}
	
	public byte[] readFullBlock() throws IOException {
		int size = size() ;
		byte[] buff = new byte[size] ;
		
		int prevPos = this.pos ;
		
		seek(0) ;
		read(buff) ;
		
		this.pos = prevPos ;
		
		return buff ;
	}
	
	public byte[] read(int pos, int size) throws IOException {
		byte[] buff = new byte[size] ;
		
		int prevPos = this.pos ;
		
		seek(pos) ;
		read(buff) ;
		
		this.pos = prevPos ;
		
		return buff ;
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	public void unlinkNextBlock() throws IOException {
		if ( !hasNextBlock() ) return ;
		
		sector.unlinkNextBlock( blockIndex , getNextBlockIndex() , getNextBlockSector() ) ;
	}
	
	public void unlinkPrevBlock() throws IOException {
		if ( !hasPrevBlock() ) return ;
		
		VDBlock prevBlock = getPrevBlock() ;
		
		prevBlock.unlinkNextBlock() ;
	}
	
	public void setNextBlock( VDBlock block ) throws IOException {
		if ( block == this ) throw new IllegalArgumentException("Invalid block: "+ block +" > this: "+ this) ;
		
		if ( this.getNextBlockIndex() == block.blockIndex && this.getSectorIndex() == block.getSectorIndex() ) return ;
		
		sector.setNextBlock(blockIndex, block.blockIndex, block.getSectorIndex()) ;
	}
	
	protected void setInternalsNextBlock(int nextBlockIndex, int nextBlockSector) {
		if (nextBlockIndex == blockIndex && nextBlockSector == sector.getSectorIndex()) {
			throw new IllegalStateException("Can't set next block to my self!") ;
		}
		
		this.usage[1] = nextBlockIndex+1 ;
		this.usage[2] = nextBlockSector+1 ;
	}
	
	protected void setInternalsPrevBlock(int prevBlockIndex, int prevBlockSector) {
		if (prevBlockIndex == blockIndex && prevBlockSector == sector.getSectorIndex()) {
			throw new IllegalStateException("Can't set prev block to my self!") ;
		}
		
		this.usage[3] = prevBlockIndex+1 ;
		this.usage[4] = prevBlockSector+1 ;
		
		if ( prevBlockIndex >= 0 ) {
			this.vdFile = null ;
			
			if ( hasMetaData() ) throw new IllegalStateException("Can't have meta data with previous block!") ;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	
	public int[] getIdent() {
		return new int[] { blockIndex , getSectorIndex() } ;
	}
	
	static public String toStringIdent(int blockIndex, int sectorIndex) {
		StringBuilder str = new StringBuilder() ;
		
		str.append(blockIndex) ;
		str.append("@") ;
		str.append(sectorIndex) ;
		
		return str.toString() ;
	}
	
	public String toStringIdent() {
		return toStringIdent( getBlockIndex() , getSectorIndex() ) ;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder() ;
		str.append( this.getClass().getName() ) ;
		
		str.append("#") ;
		str.append( getBlockIndex() ) ;
		str.append("@") ;
		str.append( getSectorIndex() ) ;
		
		str.append("[") ;
		
		if ( isDeleted() ) {
			str.append("deleted") ;
		}
		else {
			str.append("size: ") ;
			str.append(size()) ;
			
			if (hasPrevBlock()) {
				str.append(" ; prevBlock: ") ;
				str.append(getPrevBlockIndex()) ;
				str.append("@") ;
				str.append(getPrevBlockSector()) ;
			}
			
			if (hasNextBlock()) {
				str.append(" ; nextBlock: ") ;
				str.append(getNextBlockIndex()) ;
				str.append("@") ;
				str.append(getNextBlockSector()) ;
			}
			
			
		}
		
		str.append("]") ;
		
		return str.toString() ;
	}
	
}

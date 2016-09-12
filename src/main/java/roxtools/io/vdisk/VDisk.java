package roxtools.io.vdisk;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import roxtools.FileUtils;
import roxtools.Mutex;

final public class VDisk implements Serializable {
	private static final long serialVersionUID = 5767308745082861624L;

	static final public boolean DISABLE_VDISK_BUFFERED_IO = System.getProperty("DISABLE_VDISK_BUFFERED_IO") != null ;
	
	static final private ArrayList<WeakReference<VDisk>> vDiskInstances = new ArrayList<WeakReference<VDisk>>() ;
	
	private final File vdiskDir ;
	
	protected final int blockSize ;
	
	protected final int sectorSize ;
	
	static final public int MAX_BLOCK_SIZE = 1024*1024*1024 ;
	static final public int MAX_SECTOR_SIZE = 1024*1024*10 ;
	
	final protected int sectorHeaderSize ;
	final protected int totalSectorSize ;
	final protected int sectorIOBufferSize ;

	
	final private Mutex sectorMUTEX = new Mutex() ;
	private VDSector[] sectors ;
	transient volatile private VDSector[] sectorsVolatile ;
	private boolean staticSectorSize ;
	
	////////////////////////////////////////////////////////

	final private boolean isbufferedIO ;
	final private boolean isMetadataDisk ;
	final private VDisk metadataDisk ;
	
	///////////////////////////////////////////// size ; nextBlkIdx ; nextBlkSect ; prevBlkIdx ; prevBlkSect ; meta0 ; meta1
	static final private int BLOCK_USAGE_SIZE          = 1    + 1          + 1           + 1          + 1           + 1     + 1     ;
	static final private int BLOCK_USAGE_SIZE_METADATA = 1    + 1          + 1           + 1          + 1                            ;
	
	final protected int blockUsageSize ;
	final protected int blockUsageSizeBytes ;
	
	////////////////////////////////////////////////////////	
	
	public VDisk(File vdiskDir, int blockSize, int sectorSize, int totalSectors) throws IOException {
		this(vdiskDir, blockSize, sectorSize, totalSectors, true) ;
	}
	
	public VDisk(File vdiskDir, int blockSize, int sectorSize, int totalSectors, boolean bufferedIO) throws IOException {
		this(vdiskDir, blockSize, sectorSize, totalSectors, bufferedIO, false) ;
	}
	
	private VDisk(File vdiskDir, int blockSize, int sectorSize, int totalSectors, boolean bufferedIO, boolean isMetadataDisk) throws IOException {
		if ( !vdiskDir.isDirectory() ) throw new IllegalArgumentException("Invalid vdiskDir: "+ vdiskDir) ;
		if ( blockSize < 4 || blockSize > MAX_BLOCK_SIZE ) throw new IllegalArgumentException("Invalid blockSize: "+ blockSize) ;
		if ( sectorSize < 4 || sectorSize > MAX_SECTOR_SIZE ) throw new IllegalArgumentException("Invalid sectorSize: "+ sectorSize) ;
		
		this.vdiskDir = vdiskDir;
		
		if (DISABLE_VDISK_BUFFERED_IO && bufferedIO) {
			System.out.println("VDisk> ** DISABLE_VDISK_BUFFERED_IO: "+ vdiskDir);
			bufferedIO = false ;
		}
		
		this.isbufferedIO = bufferedIO ;
		this.isMetadataDisk = isMetadataDisk ;
		
		if (!isMetadataDisk) {
			lockDisk() ;
		}
		
		this.blockSize = blockSize;
		this.sectorSize = sectorSize;
		
		if (!isMetadataDisk) {
			File metaDataDir = new File(vdiskDir, "metadata.disk") ;
			metaDataDir.mkdirs() ;
			
			this.metadataDisk = new VDisk(metaDataDir, 32, sectorSize, 0, bufferedIO, true) ;
			
			this.blockUsageSize = BLOCK_USAGE_SIZE ;
			this.blockUsageSizeBytes = BLOCK_USAGE_SIZE*4 ;
		}
		else {
			this.metadataDisk = null ;
			
			this.blockUsageSize = BLOCK_USAGE_SIZE_METADATA ;
			this.blockUsageSizeBytes = BLOCK_USAGE_SIZE_METADATA*4 ;
		}
		
		this.sectorHeaderSize = this.blockUsageSizeBytes * sectorSize ;
		
		this.totalSectorSize = this.sectorHeaderSize + ( blockSize * sectorSize ) ; 
		
		if (totalSectorSize < 0) throw new IllegalArgumentException("Invalid blockSize * sectorSize: "+ blockSize +" * "+ blockSize +" = "+ totalSectorSize) ;
				
		this.sectorIOBufferSize = calculateSectorIOBufferSize() ;
		
		if (totalSectors >= 1) {
			int[] indexes = listSectorsFilesIndexesChecked() ;
			
			if (indexes.length > 0 && indexes.length != totalSectors) throw new IOException("Different previous number of sections: "+ indexes.length +" != "+ totalSectors) ;
			
			this.sectorsVolatile = this.sectors = new VDSector[totalSectors] ;
			
			for (int i = 0; i < this.sectors.length; i++) {
				this.sectors[i] = new VDSector(this, i) ;
			}	
			
			this.staticSectorSize = true ;
		}
		else {
			int[] indexes = listSectorsFilesIndexesChecked() ;
			
			this.sectorsVolatile = this.sectors = new VDSector[ Math.max( indexes.length , 1 )] ;
			
			for (int i = 0; i < this.sectors.length; i++) {
				this.sectors[i] = new VDSector(this, i) ;
			}
			
			
			this.staticSectorSize = false ;
		}
		
		addToVDiskInstances();
	}
	
	private int calculateSectorIOBufferSize() {
		int bufferSz = 1024*512 ;
		
		if ( bufferSz > this.totalSectorSize/5 ) {
			bufferSz = this.totalSectorSize/5 ;
		}
		
		if ( bufferSz < 1024*8 ) {
			bufferSz = this.totalSectorSize ;
		}
			
		return bufferSz ;
	}
	

	private void addToVDiskInstances() {
		synchronized (vDiskInstances) {
			vDiskInstances.add( new WeakReference<VDisk>(this) ) ;
		}
	}
	
	transient private File vdiskLockFile ;
	transient private RandomAccessFile lockIO ;
	transient private FileLock lock ;
	
	synchronized private void lockDisk() throws IOException {
		if (this.lockIO != null) return ;
		
		this.vdiskLockFile = new File( vdiskDir , "vdisk.lock" ) ;
		
		this.lockIO = new RandomAccessFile(vdiskLockFile, "rw") ;
		
		FileChannel lockChannel = lockIO.getChannel();

		boolean lockError = true ;
		try {
			//System.out.println("*** TRY LOCK> "+ vdiskLockFile);
			
			this.lock = lockChannel.tryLock();
			lockError = false ;
		}
		catch (OverlappingFileLockException e) {
			throw new IOException("Can't lock vDisk! "+ this.vdiskLockFile, e) ;
		}
		
		if ( lockError || this.lock == null ) {
			this.lock = null ;
			
			try {
				this.lockIO.close() ;
			}
			catch (Exception e) {}
			
			this.lockIO = null ;
			
			throw new IOException("Can't lock vDisk! "+ this.vdiskLockFile) ;
		}

	}

	synchronized private void unlockDisk() throws IOException {
		if (this.lockIO == null || this.lock == null) return ;
		
		this.lock.release() ;
		
		this.lock = null ;
		
		try {
			this.lockIO.close() ;
		}
		catch (Exception e) {}
		
		this.lockIO = null ;
		
	}
	
	public boolean isIsbufferedIO() {
		return isbufferedIO;
	}
	
	protected boolean isMetadataDisk() {
		return this.isMetadataDisk ;
	}
	
	protected VDisk getMetadataDisk() {
		return metadataDisk;
	}
	
	@SuppressWarnings("unused")
	private File[] listSectorsFiles() {
		
		File[] sectorFiles = vdiskDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) return false ;
				
				String name = pathname.getName() ;
				
				return name.endsWith( VDSector.SECTOR_FILE_SUFIX );
			}
		}) ;
	
		Arrays.sort(sectorFiles , new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int i1 = VDSector.getSectorFileIndex(o1) ;
				int i2 = VDSector.getSectorFileIndex(o2) ;
				return i1 < i2 ? -1 : (i1 == i2 ? 0 : 1);
			}
		}) ;
		
		return sectorFiles ;
	}
	
	private int[] listSectorsFilesIndexesChecked() throws IOException {
		int[] indexes = listSectorsFilesIndexes() ;
		
		for (int i = 0; i < indexes.length; i++) {
			int idx = indexes[i];
			if (idx != i) throw new IOException("Sector files not in index sequence: "+ Arrays.toString(indexes));
		}
		
		return indexes ;
	}
	
	private int[] listSectorsFilesIndexes() {
		
		File[] sectorFiles = vdiskDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) return false ;
				
				String name = pathname.getName() ;
				
				return name.endsWith( VDSector.SECTOR_FILE_SUFIX );
			}
		}) ;
	
		int[] indexes = new int[sectorFiles.length] ;
		
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = VDSector.getSectorFileIndex( sectorFiles[i] ) ;
		}
		
		Arrays.sort(indexes) ;
		
		return indexes ;
	}
	
	public boolean isStaticSectorSize() {
		return staticSectorSize;
	}

	public File getVDiskDir() {
		return vdiskDir;
	}
	
	public int getBlockSize() {
		return blockSize;
	}
	
	public int getSectorSize() {
		return sectorSize;
	}
	
	public int getTotalSectors() {
		synchronized (sectorMUTEX) {
			return sectors.length ;
		}
	}
	
	protected VDSector getSector(int sectorIndex) {
		synchronized (sectorMUTEX) {
			return sectors[sectorIndex] ;
		}
	}
	
	protected VDSector getSectorNoSynch(int sectorIndex) {
		return sectorsVolatile[sectorIndex] ;
	}
	
	public String[] getFilesIDsArray() {
		ArrayList<String> filesIDs = getFilesIDs() ;
		return filesIDs.toArray( new String[filesIDs.size()] ) ;
	}
	
	public ArrayList<String> getFilesIDs() {
		
		synchronized (sectorMUTEX) {
			int totalSectors = getTotalSectors() ;
			
			int initialCapacity = (int) ((totalSectors * sectorSize) * 0.10) ; 
			if (initialCapacity < 100) initialCapacity = 100 ;
			
			ArrayList<String> filesIds = new ArrayList<String>(initialCapacity) ;
			
			for (int i = 0; i < totalSectors; i++) {
				VDSector sector = sectors[i] ;
				sector.getRootBlocksIdents(filesIds) ;
			}
		
			return filesIds ;
		}
		
	}
	
	public Iterator<String> iterateFileIDs() {
		
		return new Iterator<String>() {
			int sectorCursor = 0 ;
			Iterator<String> sectorIterator = getSector(0).iterateRootBlocksIdents() ;
			
			@Override
			public boolean hasNext() {
				boolean hasNext = sectorIterator.hasNext() ;
				if (hasNext) return true ;

				while (true) {
					sectorCursor++ ;
					
					if (sectorCursor >= sectors.length) return false ;
					
					sectorIterator = getSector(sectorCursor).iterateRootBlocksIdents() ;
					hasNext = sectorIterator.hasNext() ;
					
					if (hasNext) return true ;
				}
			}

			@Override
			public String next() {
				return sectorIterator.next() ;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException() ;
			}
		};
		
	}
	
	public String[] getFilesMetaDataKeysArray() {
		ArrayList<String> keys = getFilesMetaDataKeys() ;
		return keys.toArray( new String[keys.size()] ) ;
	}
	
	public ArrayList<String> getFilesMetaDataKeys() {
	
		synchronized (sectorMUTEX) {
			int totalSectors = getTotalSectors() ;
			
			int initialCapacity = (int) ((totalSectors * sectorSize) * 0.10) ; 
			if (initialCapacity < 100) initialCapacity = 100 ;
			
			ArrayList<String> keys = new ArrayList<String>(initialCapacity) ;
			
			for (int i = 0; i < totalSectors; i++) {
				VDSector sector = getSector(i) ;
				sector.getMetaDataKeys(keys) ;
			}
			
			return keys ;
		}
	}
	
	public ArrayList<String> getFilesMetaDataKeysWithPrefix(String prefix) {
		
		synchronized (sectorMUTEX) {
			int totalSectors = getTotalSectors() ;
			
			int initialCapacity = (int) ((totalSectors * sectorSize) * 0.10) ; 
			if (initialCapacity < 100) initialCapacity = 100 ;
			
			ArrayList<String> keys = new ArrayList<String>(initialCapacity) ;
			
			for (int i = 0; i < totalSectors; i++) {
				VDSector sector = getSector(i) ;
				sector.getMetaDataKeysWithPrefix(prefix, keys) ;
			}
			
			return keys ;
		}
	}
	
	static public interface FilesMetaDataKeyFilter {
		public boolean accept( String metadataKey ) ;
	}
	
	public ArrayList<String> getFilesMetaDataKeys( FilesMetaDataKeyFilter filter ) {
		
		synchronized (sectorMUTEX) {
			int totalSectors = getTotalSectors() ;
			
			int initialCapacity = (int) ((totalSectors * sectorSize) * 0.10) ; 
			if (initialCapacity < 100) initialCapacity = 100 ;
			
			ArrayList<String> keys = new ArrayList<String>(initialCapacity) ;
			
			for (int i = 0; i < totalSectors; i++) {
				VDSector sector = getSector(i) ;
				sector.getMetaDataKeys(filter, keys) ;
			}
			
			return keys ;
		}
	}
	
	public Iterator<String> iterateFileMetaDataKeys() {
		
		return new Iterator<String>() {
			int sectorCursor = 0 ;
			Iterator<String> sectorIterator = getSector(0).iterateMetaDataKeys() ;
			
			@Override
			public boolean hasNext() {
				boolean hasNext = sectorIterator.hasNext() ;
				if (hasNext) return true ;

				while (true) {
					sectorCursor++ ;
					
					if (sectorCursor >= sectors.length) return false ;
					
					sectorIterator = getSector(sectorCursor).iterateMetaDataKeys() ;
					hasNext = sectorIterator.hasNext() ;
					
					if (hasNext) return true ;
				}
			}

			@Override
			public String next() {
				return sectorIterator.next() ;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException() ;
			}
		};
		
	}
	
	public VDFile getFileByID(String id) {
		int indexOf = id.indexOf('@') ;
		
		int init = id.charAt(0) == '#' ? 1 : 0 ;
		
		int blockIndex = Integer.parseInt(  id.substring( init , indexOf ) ) ;
		int blockSector = Integer.parseInt(  id.substring( indexOf+1 ) ) ;
		
		return getFileByID(blockIndex, blockSector) ;
	}
	
	public boolean containsFileByID(String id) {
		int indexOf = id.indexOf('@') ;
		
		int init = id.charAt(0) == '#' ? 1 : 0 ;
		
		int blockIndex = Integer.parseInt(  id.substring( init , indexOf ) ) ;
		int blockSector = Integer.parseInt(  id.substring( indexOf+1 ) ) ;
		
		return containsFileByID(blockIndex, blockSector) ;
	}
	
	public boolean containsFileByID(int blockIndex, int sectorIndex) {
		VDBlock block ;
		synchronized (sectorMUTEX) {
			VDSector sector = getSector(sectorIndex) ;
			
			block = sector.getBlock(blockIndex) ;
		}
		
		if (block == null) return false ;
		
		return block.hasVDFile() ;
	}
	
	public VDFile getFileByID(int blockIndex, int sectorIndex) {
		synchronized (sectorMUTEX) {
			VDSector sector = getSector(sectorIndex) ;
			
			VDBlock block = sector.getBlock(blockIndex) ;
			
			if (block == null) return null ;
			
			return block.getVDFile() ;
		}
	}
	
	public VDFile getOrCreateFileWithMetaDataKey(String metaDataKey) throws IOException {
		return getOrCreateFileWithMetaData(metaDataKey, VDMetaData.dummyData) ;
	}
	
	public VDFile getOrCreateFileWithMetaData(String metaDataKey, byte[] data) throws IOException {
		synchronized (this.sectorMUTEX) {
			VDFile file = this.getFirstFileByMetaDataKey(metaDataKey) ;
			
			if (file == null) {
				file = this.createFile() ;
				file.setMetaData( new VDMetaData(metaDataKey, data) ) ;
			}
			
			return file ;
		}
	}

	public VDFile getOrCreateFileWithMetaData(VDMetaData metaData) throws IOException {
		synchronized (this.sectorMUTEX) {
			VDFile file = this.getFirstFileByMetaDataKey( metaData.getKey() ) ;
			
			if (file == null) {
				file = this.createFile() ;
				file.setMetaData(metaData) ;
			}
			
			return file ;
		}
	}
	
	private int lastCreatedFileSectorIndex = 0 ;
	
	public VDFile createFile() throws IOException {
		
		synchronized (sectorMUTEX) {
			synchronized (this) {
				int sz = sectors.length ;
				
				int maxSectorToTry = sz ;
				
				if ( lastCreatedFileSectorIndex < sectors.length ) {
					maxSectorToTry = lastCreatedFileSectorIndex ;
					
					while (lastCreatedFileSectorIndex < sectors.length) {
						VDSector sector = sectors[lastCreatedFileSectorIndex] ;
						VDBlock block = sector.createBlock() ;
						
						if (block != null) {
							return block.getVDFile() ;
						}
						else {
							lastCreatedFileSectorIndex++ ;
						}
					}	
				}
		 		
				for (int i = 0; i < maxSectorToTry; i++) {
					VDSector sector = sectors[i] ;
					
					VDBlock block = sector.createBlock() ;
					
					if (block != null) {
						lastCreatedFileSectorIndex = i ;
						return block.getVDFile() ;
					}
				}
				
				if (!staticSectorSize) {
					VDSector sector = appendSector() ;
					
					VDBlock block = sector.createBlock() ;
					
					if (block != null) {
						lastCreatedFileSectorIndex = sector.getSectorIndex() ;
						return block.getVDFile() ;
					}
				}
				
				throw new IOException("Can't find blocks in sectors! VDisk is full! Can't allocate a new block!") ;
			}
		}
	}
	
	protected VDSector appendSector() throws IOException {
		if (staticSectorSize) {
			throw new IOException("Can't append sector! VDisk initialized with static number of sectors") ;
		}
		
		synchronized (sectorMUTEX) {
			int newIndex = this.sectors.length ;
			
			VDSector newSector = new VDSector(this, newIndex) ;
			
			VDSector[] sectors2 = Arrays.copyOf(this.sectors, this.sectors.length+1) ;
			sectors2[ sectors2.length-1 ] = newSector ;
			
			this.sectorsVolatile = this.sectors = sectors2 ;
			
			return newSector ;	
		}
	}
	
	private boolean asyncWriteHeaders = true ;
	
	public void setAsyncWriteHeaders(boolean asyncWriteHeaders) {
		this.asyncWriteHeaders = asyncWriteHeaders;
	}
	
	public boolean isAsyncWriteHeadersEnabled() {
		return asyncWriteHeaders;
	}
	
	////////////////////////////////////////////////////////////
	
	protected int[] getMetaDataKey(String key) {
		
		synchronized (sectorMUTEX) {
			int sz = sectors.length ;
			
			int[] allIDents = null ;
			
			for (int i = 0; i < sz; i++) {
				VDSector sector = sectors[i] ;
				
				int[] idents = sector.getMetaDataKey(key) ;
				
				if (idents == null) continue ;
				
				if (allIDents == null) allIDents = idents ;
				else allIDents = VDSector.joinIdentsNoCheckDuplicity(allIDents, idents) ;
			}
			
			return allIDents ;
		}
		
	}
	
	protected int[] getMetaDataKeyFirstIdent(String key) {
		
		synchronized (sectorMUTEX) {
			int sz = sectors.length ;
			
			for (int i = 0; i < sz; i++) {
				VDSector sector = sectors[i] ;
				
				int[] idents = sector.getMetaDataKey(key) ;
				
				if (idents != null) return idents ;
			}
			
			return null ;
		}
		
	}
	
	private int containsMetaDataKey_lastSectorIndex = -1 ;
	
	protected boolean containsMetaDataKey(String key) {
		synchronized (sectorMUTEX) {
			int sz = sectors.length ;
			
			if (containsMetaDataKey_lastSectorIndex >= 0 && containsMetaDataKey_lastSectorIndex < sz) {
				VDSector sector = sectors[containsMetaDataKey_lastSectorIndex] ;
				if ( sector.containsMetaDataKey(key) ) return true ;
			}
			
			for (int i = 0; i < sz; i++) {
				if (i == containsMetaDataKey_lastSectorIndex) continue ;
				
				VDSector sector = sectors[i] ;
				if ( sector.containsMetaDataKey(key) ) {
					containsMetaDataKey_lastSectorIndex = i ;
					return true ;
				}
				
			}
			
			return false ;
		}
	}

	public boolean containsFileByMetaDataKey(String fileMetaDataKey) {
		return containsMetaDataKey(fileMetaDataKey) ;
	}
	
	public VDFile getFirstFileByMetaDataKey(String fileMetaDataKey) {
		int[] idents = getMetaDataKeyFirstIdent(fileMetaDataKey) ;
		
		if (idents == null) return null ;
		
		VDFile file = getFileByID( idents[0] , idents[1] ) ;
		
		return file ;
	}
	
	public VDFile[] getFileByMetaDataKey(String fileMetaDataKey) {
		int[] idents = getMetaDataKey(fileMetaDataKey) ;
		
		if (idents == null) return null ;
		
		VDFile[] files = new VDFile[ idents.length ] ;
		int filesSz = 0 ;
		
		for (int i = 0; i < idents.length; i+=2) {
			int blkIdx = idents[i] ;
			int blkSect = idents[i+1] ;
			
			VDFile file = getFileByID(blkIdx, blkSect) ;
			
			if (file != null) {
				files[filesSz++] = file ;
			}
		}
		
		if (files.length != filesSz) {
			files = Arrays.copyOf(files, filesSz) ;
		}
		
		return files ;
	}
	
	public void deleteFiles(String name) throws IOException {
		VDFile file = this.getFirstFileByMetaDataKey(name) ;
		if (file == null) return ;
		
		file.delete() ;
		
		file = this.getFirstFileByMetaDataKey(name) ;
		
		if (file != null) {
			file.delete() ;
			
			VDFile[] files = this.getFileByMetaDataKey(name) ;
			
			if (files != null) {
				for (VDFile vdFile : files) {
					vdFile.delete() ;
				}
			}
		}
	}
	
	////////////////////////////////////////////////////////////
	
	public void clear() {
		
		synchronized (sectorMUTEX) {
			int sz = this.sectors.length ;
			
			for (int i = 0; i < sz; i++) {
				try {
					this.sectors[i].clear() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			containsMetaDataKey_lastSectorIndex = -1 ;
		}
		
		flush() ;
		
	}

	public void flush() {
		flush(false);
	}
	
	public void flush(boolean force) {
	
		synchronized (sectorMUTEX) {
			if (isClosed()) return ;
			
			int sz = this.sectors.length ;
			
			for (int i = 0; i < sz; i++) {
				try {
					this.sectors[i].flushHeader(force) ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (this.metadataDisk != null) {
			this.metadataDisk.flush(force);
		}
		
	}
	
	private boolean closed = false ;
	
	synchronized public boolean isClosed() {
		return closed;
	}
	
	public void close() {
		closeImplem() ;
		
		removeFromVDiskInstances() ;
	}
	
	private void closeImplem() {
		if ( isClosed() ) return ;
		
		flush(true) ;
	
		synchronized (sectorMUTEX) {
			int sz = this.sectors.length ;
			
			for (int i = 0; i < sz; i++) {
				try {
					this.sectors[i].close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (this.metadataDisk != null) {
			this.metadataDisk.close(); 
		}
		
		synchronized (this) {
			try {
				unlockDisk() ;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				closed = true ;	
			}
		}
		
	}
	
	public boolean delete() {
		
		close();
		
		return FileUtils.deleteTree(vdiskDir.getParentFile(), vdiskDir) ;
		
	}

	///////////////////////////////////////////////////////////////////
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		this.flush();
		out.defaultWriteObject();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		this.sectorsVolatile = this.sectors ;
		
		if ( !isMetadataDisk() ) {
			lockDisk() ;
		}
	}

	///////////////////////////////////////////////////////////////////
	
	private void removeFromVDiskInstances() {
	
		synchronized (vDiskInstances) {
			
			int sz = vDiskInstances.size() ;
			
			for (int i = 0; i < sz; i++) {
				WeakReference<VDisk> ref = vDiskInstances.get(i) ;
				VDisk vDisk = ref.get() ;
				
				if (vDisk == this) {
					vDiskInstances.remove(i) ;
					break ;
				}
			}
			
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeImplem() ;
	}
	
	static {
		
		Runtime.getRuntime().addShutdownHook(new Shutdown()) ;
		
	}
	
	static private class Shutdown extends Thread {
		@Override
		public void run() {
			
			System.out.println("** SHUTDOWN VDISKs");
			
			synchronized (vDiskInstances) {
				
				@SuppressWarnings("unchecked")
				ArrayList<WeakReference<VDisk>> instances = (ArrayList<WeakReference<VDisk>>) vDiskInstances.clone() ;
				
				int sz = instances.size() ;
				
				for (int i = 0; i < sz; i++) {
					WeakReference<VDisk> ref = instances.get(i) ;
					VDisk vDisk = ref.get() ;
					
					if (vDisk != null) {
						vDisk.closeImplem() ;
					}
				}
				
				vDiskInstances.clear() ;
				
			}
			
		}
	}
	
}


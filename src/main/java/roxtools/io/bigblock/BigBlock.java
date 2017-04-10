package roxtools.io.bigblock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roxtools.SerializationUtils;

final public class BigBlock {
	
	final static private Logger LOG = LoggerFactory.getLogger(BigBlock.class) ;
	
	static private String normalizeName(String name) {
		return name.replaceAll("\\s+", "-").replaceAll("[^\\w-]+", "") ;
	}
	
	private final String name ;
	private final int compressionLevel ;
	private final BigBlockStorage storage ;
	
	public BigBlock(String name, BigBlockStorage storage) {
		this(name, 4, storage) ;
	}
	
	public BigBlock(String name, int compressionLevel, BigBlockStorage storage) {
		this.name = normalizeName(name) ;
		if (this.name.isEmpty()) throw new IllegalArgumentException("Invalid name: "+ name) ;
		
		this.compressionLevel = compressionLevel ;
		this.storage = storage;
	}
	
	private int maxStorageEntries = 1000 ;
	
	public int getMaxStorageEntries() {
		return maxStorageEntries;
	}
	
	public void setMaxStorageEntries(int maxStorageEntries) {
		this.maxStorageEntries = maxStorageEntries;
	}
	
	public String getName() {
		return name;
	}
	
	public int getCompressionLevel() {
		return compressionLevel;
	}
	
	public BigBlockStorage getStorage() {
		return storage;
	}
	
	public BigBlockIndex buildFromDirectory(File directory) throws IOException {
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName() ;
				return !name.startsWith(".") && pathname.isFile() ;
			}
		});
		
		return build(files);
	}
	
	public BigBlockIndex build(List<File> files) throws IOException {
		File[] filesAr = files.toArray(new File[files.size()]) ;
		return build(filesAr);
	}
	
	private BigBlockIndex index ;
	
	public BigBlockIndex getIndex() {
		return index;
	}
	
	public boolean isBuilt() {
		return index != null ;
	}
	
	public BigBlockIndex build(File[] files) throws IOException {
		
		BigBlockIndex blockIndex = new BigBlockIndex(name,compressionLevel) ;
		
		for (File file : files) {
			BigBlockEntry entry = blockIndex.addEntry(file) ;
			
			if (entry == null) {
				LOG.debug("Invalid entry file: {}", file);
			}
		}
		
		blockIndex.build(storage, maxStorageEntries) ;
		
		this.index = blockIndex ;
		
		return blockIndex ;
	}
	
	public BigBlockIndex load() throws IOException {
		byte[] indexSerial = storage.readStoredBlockIndex(name) ;
		
		BigBlockIndex blockIndex = new BigBlockIndex(new ByteArrayInputStream(indexSerial)) ;
		
		this.index = blockIndex ;
		
		return blockIndex ;
	}

	private void checkBuilt() {
		if (!isBuilt()) throw new IllegalStateException("Not load or not build!") ;
	}

	public List<BigBlockEntry> getFileEntries() {
		checkBuilt();
		return index.getSortedEntries() ;
	}
	
	public BigBlockEntry getFileEntry(String fileName) {
		checkBuilt();
		return index.getEntry(fileName) ;
	}
	
	public byte[] getFileData(String fileName) throws IOException {
		return getFileData( getFileEntry(fileName) ) ;
	}
	
	public byte[] getFileDataCompressed(BigBlockEntry blockEntry) throws IOException {
		byte[] dataCompressed = storage.getEntryDataStored(name, blockEntry) ;
		return dataCompressed ;
	}
	
	public byte[] getFileData(BigBlockEntry blockEntry) throws IOException {
		byte[] dataCompressed = getFileDataCompressed(blockEntry);
		
		if ( dataCompressed == null ) throw new IOException("Can't get compressed data: "+ blockEntry) ;
		
		if ( dataCompressed.length != blockEntry.getStorageLength() ) throw new IOException("Compressed data of differente size of entry: "+ blockEntry) ;
		
		GZIPInputStream gzIn = new GZIPInputStream(new ByteArrayInputStream(dataCompressed)) ;
		
		try {
			byte[] data = SerializationUtils.readFull(gzIn, blockEntry.getLength()) ;
			return data ;
		}
		finally {
			gzIn.close();
		}
	}
	
	

}

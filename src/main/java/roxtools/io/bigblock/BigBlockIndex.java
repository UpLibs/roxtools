package roxtools.io.bigblock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import roxtools.SerializationUtils;
import roxtools.SerializationUtils.GZIPOutputStreamWithLevel;
import roxtools.VectorLong;

final public class BigBlockIndex {

	private String name ;
	private int compressionLevel ;
	
	private long[] bigBlockPartsLengths ; 
	
	public BigBlockIndex(String name, int compressionLevel) {
		this.name = name ;
		this.compressionLevel = compressionLevel < 0 ? 4 : compressionLevel ;
	}
	
	public String getName() {
		return name;
	}

	public long[] getBigBlockPartsLengths() {
		return bigBlockPartsLengths;
	}
	
	final private HashMap<String, BigBlockEntry> entries = new HashMap<>() ;

	public BigBlockEntry addEntry(File file) {

		BigBlockEntry entry = new BigBlockEntry(file) ;
		
		if (entry.getLength() <= 0) return null ;
		
		synchronized (entries) {
			BigBlockEntry prev = entries.get(entry.getName()) ;
			if (prev != null) return null ;
			
			entries.put( entry.getName() , entry) ;
			
			return entry ;
		}
		
	}
	
	public int getTotalEntries() {
		synchronized (entries) {
			return entries.size() ;
		}
	}
	
	public List<BigBlockEntry> getSortedEntries() {
		ArrayList<BigBlockEntry> allEntries = new ArrayList<>() ;
		
		synchronized (entries) {
			for (BigBlockEntry entry : entries.values()) {
				allEntries.add(entry) ;
			}	
		}
		
		Collections.sort(allEntries);
		
		return allEntries ;
	}
	
	protected void build(BigBlockStorage storage, int maxStorageEntries) throws IOException {
	
		List<BigBlockEntry> sortedEntries = getSortedEntries() ;
		
		int blockCount = 0 ;
		int storageEntryHeaderSize = 0 ;
		
		VectorLong partsLengths = new VectorLong() ;
		
		OutputStream blockOutput = storage.openBigBlockOutput(name, blockCount) ;
		int entriesCount = 0 ;
		long storageCursor = 0 ;
		
		for (BigBlockEntry entry : sortedEntries) {
			
			int compressedLng = entry.writeLocalFileCompressedTo(blockOutput, compressionLevel) ;
			
			entry.setStorageLength(compressedLng);
			entry.setStorageBlockPart(blockCount);
			entry.setStoragePosition(storageCursor);
			
			storageCursor += storageEntryHeaderSize + compressedLng ;
			
			entriesCount++ ;
			
			if ( entriesCount >= maxStorageEntries ) {
				partsLengths.add(storageCursor);
				
				storage.closeBigBlockOutput(name, blockCount, blockOutput) ;
				
				blockCount++ ;
				blockOutput = storage.openBigBlockOutput(name, blockCount) ;
				entriesCount = 0 ;
				storageCursor = 0 ;
			}
		}
		
		this.bigBlockPartsLengths = partsLengths.toArray() ;
		
		byte[] serial = getSerial() ;
		
		storage.storeIndex(name, serial) ;
		
	}
	
	public byte[] getSerial() throws IOException {
		return getSerial(null) ;
	}
	
	private byte[] getSerial(List<BigBlockEntry> sortedEntries) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		GZIPOutputStreamWithLevel gzOut = new SerializationUtils.GZIPOutputStreamWithLevel(bout, compressionLevel) ;
		writeTo(gzOut, sortedEntries);
		gzOut.close();
		return bout.toByteArray() ;
	}

	public void writeTo(OutputStream out, List<BigBlockEntry> sortedEntries) throws IOException {
		if (sortedEntries == null) sortedEntries = getSortedEntries() ;
		
		SerializationUtils.writeStringUTF8(name, out);
		SerializationUtils.writeInt(this.compressionLevel, out);
		
		SerializationUtils.writeLongsBlock(this.bigBlockPartsLengths, out);
		
		SerializationUtils.writeInt(sortedEntries.size(), out);
		
		for (BigBlockEntry entry : sortedEntries) {
			entry.writeTo(out) ;
		}
		
	}
	
	public BigBlockIndex(InputStream in) throws IOException {
		readFrom(new GZIPInputStream(in));
	}
	
	public void readFrom(InputStream in) throws IOException {

		this.name = SerializationUtils.readStringUTF8(in) ;
		this.compressionLevel = SerializationUtils.readInt(in) ;
		
		this.bigBlockPartsLengths = SerializationUtils.readLongsBlock(in);
		
		int sz = SerializationUtils.readInt(in) ;
		
		entries.clear();
		
		for (int i = 0; i < sz; i++) {
			BigBlockEntry entry = new BigBlockEntry(in) ;
			
			entries.put( entry.getName() , entry ) ;
		}
		
	}

	public boolean containsEntry(String fileName) {
		synchronized (entries) {
			return entries.containsKey(fileName) ;
		}
	}
	
	public BigBlockEntry getEntry(String fileName) {
		synchronized (entries) {
			return entries.get(fileName) ;
		}
	}
	
}

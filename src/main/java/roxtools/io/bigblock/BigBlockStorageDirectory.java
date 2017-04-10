package roxtools.io.bigblock;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import roxtools.SerializationUtils;

public class BigBlockStorageDirectory implements BigBlockStorage {

	private File directory ;

	public BigBlockStorageDirectory(File directory) {
		if (!directory.isDirectory()) throw new IllegalArgumentException("Invalid store directory: "+ directory) ;
		this.directory = directory;
	}
	
	private File getBlockIndexFile(String name) {
		return new File(directory, "bigblock--"+name+".index") ;
	}
	
	public File[] listBlockIndexFiles() {
		return listBlockFiles(".index") ;
	}
	
	private File getBlockFile(String name, int blockPart) {
		return new File(directory, "bigblock--"+name+"--"+blockPart+".block") ;
	}
	
	public File[] listBlockFiles() {
		return listBlockFiles(".block") ;
	}
	
	private File[] listBlockFiles(final String extension) {
		return directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName() ;
				return name.startsWith("bigblock--") && name.endsWith(extension) && pathname.isFile();
			}
		});
	}
	
	@Override
	public OutputStream openBigBlockOutput(String name, int blockPart) throws FileNotFoundException {
		File blockFile = getBlockFile(name, blockPart) ;
		
		return new FileOutputStream(blockFile) ;
	}
	
	@Override
	public void closeBigBlockOutput(String name, int blockPart, OutputStream blockOutput) throws IOException {
		try {
			blockOutput.flush();
			blockOutput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void storeIndex(String name, byte[] serial) throws IOException {
		File indexFile = getBlockIndexFile(name) ;
		
		SerializationUtils.writeFile(indexFile, serial);
	}
	
	@Override
	public byte[] readStoredBlockIndex(String name) throws IOException {
		File indexFile = getBlockIndexFile(name) ;
		
		return SerializationUtils.readFile(indexFile);
	}
	
	@Override
	public byte[] getEntryDataStored(String name, BigBlockEntry blockEntry) throws IOException {
		int storageLength = blockEntry.getStorageLength();
		int storageBlockPart = blockEntry.getStorageBlockPart() ;
		long storagePosition = blockEntry.getStoragePosition() ;
		
		
		if ( storageLength < 0 || storageBlockPart < 0 || storagePosition < 0) throw new IllegalStateException("Getting data from not stored entry: "+ blockEntry) ;
		
		File blockFile = getBlockFile(name, storageBlockPart) ;
		
		RandomAccessFile blockIO = new RandomAccessFile(blockFile, "r") ;
		
		blockIO.seek(storagePosition);
		
		byte[] data = new byte[storageLength] ;
		blockIO.readFully(data);
		
		blockIO.close();
		
		return data;
	}
	
	
}

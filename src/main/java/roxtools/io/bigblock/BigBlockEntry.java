package roxtools.io.bigblock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import roxtools.SerializationUtils;
import roxtools.SerializationUtils.OutputStreamWriteCounter;

public class BigBlockEntry implements Comparable<BigBlockEntry>{

	final private String name ;
	final private int length ;
	
	private int storageLength ;
	private int storageBlockPart ;
	private long storagePosition ;
	private File localFile ;
	
	public BigBlockEntry(File localFile) {
		this.name = localFile.getName() ;
		this.length = (int) localFile.length() ;
		this.localFile = localFile ;
		
		this.storageLength = -1 ;
		this.storagePosition = -1 ;
	}
	
	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}
	
	public int getStorageLength() {
		return storageLength;
	}
	
	protected void setStorageLength(int storageLength) {
		this.storageLength = storageLength;
	}
	
	public int getStorageBlockPart() {
		return storageBlockPart;
	}
	
	protected void setStorageBlockPart(int storageBlockPart) {
		this.storageBlockPart = storageBlockPart;
	}
	
	public long getStoragePosition() {
		return storagePosition;
	}
	
	protected void setStoragePosition(long storagePosition) {
		this.storagePosition = storagePosition;
	}
	
	public File getLocalFile() {
		return localFile;
	}
	
	protected void setLocalFile(File localFile) {
		this.localFile = localFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + length;
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		BigBlockEntry other = (BigBlockEntry) obj;
		if (length != other.length) return false;
		if (!name.equals(other.name)) return false;

		return true;
	}

	@Override
	public int compareTo(BigBlockEntry o) {
		int cmp = this.name.compareTo(o.name) ;
		
		if (cmp == 0) {
			cmp = Integer.compare( this.length , o.length ) ;
		}
		
		return cmp;
	}
	
	public int writeLocalFileTo(OutputStream out) throws IOException {
		FileInputStream fin = new FileInputStream(localFile) ;
		
		try {
			return SerializationUtils.writeTo(fin, out);
		}
		finally { 
			fin.close();
		}
	}
		
	public int writeLocalFileCompressedTo(OutputStream out, int compressionLevel) throws IOException {
		FileInputStream fin = new FileInputStream(localFile) ;
		
		try {
			OutputStreamWriteCounter writeCounter = new SerializationUtils.OutputStreamWriteCounter(out) ;
			
			SerializationUtils.writeToCompressed(fin, writeCounter, compressionLevel);
			
			return writeCounter.getWriteCount() ;
		}
		finally { 
			fin.close();
		}
	}
	
	public void writeTo(OutputStream out) throws IOException {
		SerializationUtils.writeStringUTF8(name, out);
		SerializationUtils.writeInt(length, out);
		SerializationUtils.writeInt(storageLength, out);
		SerializationUtils.writeInt(storageBlockPart, out);
		SerializationUtils.writeLong(storagePosition, out);
	}
	
	public BigBlockEntry(InputStream in) throws IOException {
		this.name = SerializationUtils.readStringUTF8(in) ;
		this.length = SerializationUtils.readInt(in);
		this.storageLength = SerializationUtils.readInt(in);
		this.storageBlockPart = SerializationUtils.readInt(in);
		this.storagePosition = SerializationUtils.readLong(in);
	}

	@Override
	public String toString() {
		return "[name: "+ name +" ; lenght: "+ length +" ; storagePosition: "+ storagePosition +"]" ;
	}

	
}

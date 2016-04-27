package roxtools.snapshot.directory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import roxtools.FileUtils;
import roxtools.SerializationUtils;
import roxtools.snapshot.SnapshotID;

public class SnapshotIDDirectory extends SnapshotID {
	
	private String directoryPath ;
	
	public SnapshotIDDirectory(String gruopId, long snapshotTime, File directoryRoot, File directory) {
		this(gruopId, snapshotTime , FileUtils.getFilePathFromRoot(directoryRoot, directory)) ;
	}
	
	public SnapshotIDDirectory(String gruopId, long snapshotTime, String directoryPath) {
		super(gruopId, snapshotTime);
		this.directoryPath = directoryPath ;
	}
	
	public String getDirectoryPath() {
		return directoryPath;
	}
	
	@Override
	public String getUID() {
		String groupIdNormalized = gruopId.replaceAll("\\s", "-_-") ;
		String directoryPathNormalized = directoryPath.replaceAll("\\s", "-_-") ;
		return getSnapshotTime() +"--"+ groupIdNormalized +"--"+ directoryPathNormalized ;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directoryPath == null) ? 0 : directoryPath.hashCode());
		result = prime * result + (int) (snapshotTime ^ (snapshotTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		SnapshotIDDirectory other = (SnapshotIDDirectory) obj;
		
		if (!directoryPath.equals(other.directoryPath)) return false;
		if (snapshotTime != other.snapshotTime) return false;
		
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getName() +"[gruopId: "+ gruopId +"snapshotTime: "+ snapshotTime +" ; directoryPath: "+ directoryPath +"]" ;
	}
	
	//////////////////////////////////////////////////////////////////

	public SnapshotIDDirectory(InputStream in) throws IOException {
		super(in) ;
	}

	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		
		SerializationUtils.writeStringLATIN1(directoryPath, out);
	}
	
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in); 
		
		this.directoryPath = SerializationUtils.readStringLATIN1(in);
	}


}

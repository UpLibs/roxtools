package roxtools.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import roxtools.SerializationUtils;

abstract public class SnapshotID {

	protected long snapshotTime ;
	
	public SnapshotID(long snapshotTime) {
		this.snapshotTime = snapshotTime ;
	}
	
	public long getSnapshotTime() {
		return snapshotTime;
	}
	
	abstract public String getUID() ;
	
	@Override
	abstract public int hashCode() ;
	
	@Override
	abstract  public boolean equals(Object obj) ;
	
	@Override
	abstract public String toString() ;
	
	///////////////////////////////////////////////////////////

	public SnapshotID(InputStream in) throws IOException {
		readFrom(in);
	}

	public void writeTo(OutputStream out) throws IOException {
		SerializationUtils.writeLong(snapshotTime, out);
	}
	
	public void readFrom(InputStream in) throws IOException {
		this.snapshotTime = SerializationUtils.readLong(in) ;
	}

	
}

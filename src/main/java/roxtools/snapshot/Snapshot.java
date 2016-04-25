package roxtools.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

abstract public class Snapshot<I extends SnapshotID> {
	
	protected I snapshotID ;
	
	public Snapshot(I snapshotID) {
		this.snapshotID = snapshotID ;
	}
	
	public I getSnapshotID() {
		return snapshotID;
	}
	
	public Snapshot(byte[] serial) throws IOException {
		readFrom( new ByteArrayInputStream(serial) );
		checkIntegrity();
	}

	public Snapshot(InputStream in) throws IOException {
		readFrom(in);
		checkIntegrity();
	}

	private void checkIntegrity() {
		if (this.snapshotID == null) throw new NullPointerException("SnapshotID can't be null after read") ;
	}
	
	public byte[] getSerial() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		writeTo(bout);
		return bout.toByteArray() ;
	}
	
	abstract public void writeTo(OutputStream out) throws IOException ;
	
	abstract public void readFrom(InputStream in) throws IOException ;
	
}

package roxtools.snapshot;

import java.io.IOException;

public interface ScanpshotCapturer<I extends SnapshotID, S extends Snapshot<I>> {
	
	public S takeSnapshot() throws IOException ;
	
	public void restoreSnapshot(byte[] serial) throws IOException ;
	public void restoreSnapshot(S snapshot) throws IOException ;
	
	public boolean canTakeSnapshot() ;
	
}

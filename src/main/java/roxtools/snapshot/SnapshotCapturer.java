package roxtools.snapshot;

import java.io.IOException;

public interface SnapshotCapturer<I extends SnapshotID, S extends Snapshot<I>> {
	
	public S takeSnapshot() throws IOException ;
	
	public boolean restoreSnapshot(byte[] serial) throws IOException ;
	public boolean restoreSnapshot(S snapshot) throws IOException ;
	
	public boolean canTakeSnapshot() ;
	
}

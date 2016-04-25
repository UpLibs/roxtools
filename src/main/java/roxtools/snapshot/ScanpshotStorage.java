package roxtools.snapshot;

abstract public class ScanpshotStorage<I extends SnapshotID, S extends Snapshot<I>> {

	abstract public I storeSnapshot( S snapshot ) ;
	
	abstract public S loadSnapshot( I snapshotID ) ;
	
	abstract public byte[] readSnapshotData( I snapshotID ) ;
	
	abstract public boolean containsSnapshot( I snapshotID ) ;
	
}


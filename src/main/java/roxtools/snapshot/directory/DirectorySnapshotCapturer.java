package roxtools.snapshot.directory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import roxtools.snapshot.SnapshotCapturer;

public class DirectorySnapshotCapturer implements SnapshotCapturer<SnapshotIDDirectory , DirectorySnapshot> {

	final private String groupId ;
	final private File directoryRoot ;
	final private File directory ;
	
	public DirectorySnapshotCapturer(String groupId, File directoryRoot, String path) {
		this(groupId, directoryRoot, new File(directoryRoot, path)) ;
	}
	
	public DirectorySnapshotCapturer(String groupId, File directoryRoot, File directory) {
		this.groupId = groupId;
		this.directoryRoot = directoryRoot;
		this.directory = directory;
	}
	
	public String getGroupId() {
		return groupId;
	}

	@Override
	public DirectorySnapshot takeSnapshot() throws IOException {
		DirectorySnapshot directorySnapshot = new DirectorySnapshot(groupId, directoryRoot, directory) ;
		return directorySnapshot ;
	}

	@Override
	public boolean restoreSnapshot(byte[] serial) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(serial) ;
		DirectorySnapshot directorySnapshot = new DirectorySnapshot(bin) ;
		
		return restoreSnapshot(directorySnapshot);
	}
	
	@Override
	public boolean restoreSnapshot(DirectorySnapshot snapshot) throws IOException {
		
		File restoredDirectory = snapshot.restoreDirectory(directoryRoot);
		
		if ( !directory.equals(restoredDirectory) ) {
			throw new IllegalStateException("Restored directory not expected: "+ restoredDirectory +" != "+ directory ) ;
		}
		
		return true ;
	}

	@Override
	public boolean canTakeSnapshot() {
		return directory.isDirectory() ;
	}

}

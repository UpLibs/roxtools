package roxtools.snapshot.directory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import roxtools.snapshot.ScanpshotCapturer;

public class DirectoryScanpshotCapturer implements ScanpshotCapturer<SnapshotIDDirectory , DirectorySnapshot> {

	final private File directoryRoot ;
	final private File directory ;
	
	public DirectoryScanpshotCapturer(File directoryRoot, String path) {
		this(directoryRoot, new File(directoryRoot, path)) ;
	}
	
	public DirectoryScanpshotCapturer(File directoryRoot, File directory) {
		this.directoryRoot = directoryRoot;
		this.directory = directory;
	}

	@Override
	public DirectorySnapshot takeSnapshot() throws IOException {
		DirectorySnapshot directorySnapshot = new DirectorySnapshot(directoryRoot, directory) ;
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

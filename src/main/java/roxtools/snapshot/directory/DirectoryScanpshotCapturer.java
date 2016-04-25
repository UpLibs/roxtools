package roxtools.snapshot.directory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import roxtools.FileUtils;
import roxtools.snapshot.ScanpshotCapturer;
import roxtools.snapshot.directory.DirectorySnapshot.DirectoryFileData;

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
	public void restoreSnapshot(byte[] serial) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(serial) ;
		DirectorySnapshot directorySnapshot = new DirectorySnapshot(bin) ;
		
		restoreSnapshot(directorySnapshot);
	}
	
	@Override
	public void restoreSnapshot(DirectorySnapshot snapshot) throws IOException {
		
		FileUtils.deleteTree(directoryRoot, directory) ;
		
		directory.mkdirs() ;
		
		List<DirectoryFileData> files = snapshot.getFiles() ;
		
		for (DirectoryFileData fileData : files) {
			String path = fileData.getPath() ;
			byte[] data = fileData.getData() ;
			
			File file = new File( directory , path ) ;
			
			FileUtils.saveFile(file, data);
		}
		
	}

	@Override
	public boolean canTakeSnapshot() {
		return directory.isDirectory() ;
	}

}

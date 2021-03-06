package roxtools.snapshot.directory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import roxtools.FileUtils;
import roxtools.FileUtils.FileInTree;
import roxtools.SerializationUtils;
import roxtools.snapshot.Snapshot;

public class DirectorySnapshot extends Snapshot<SnapshotIDDirectory> {

	static final Charset CHARSET_LATIN1 = Charset.forName("iso-8859-1") ;
	
	static public class DirectoryFileData {
		private String path ;
		private byte[] data ;
		
		public DirectoryFileData(String path, byte[] data) {
			this.path = path;
			this.data = data;
		}
		
		public String getPath() {
			return path;
		}
		
		public byte[] getData() {
			return data;
		}
		
		public void writeTo(OutputStream out) throws IOException {
			SerializationUtils.writeStringLATIN1(path, out);
			SerializationUtils.writeBlock(data, out);
		}
		
		public DirectoryFileData(InputStream in) throws IOException {
			readFrom(in);
		}
		
		public void readFrom(InputStream in) throws IOException {
			this.path = SerializationUtils.readStringLATIN1(in) ;
			this.data = SerializationUtils.readBlock(in) ;
		}
		
		public void saveToDirectory(File targetDirectory) throws IOException {
			File file = new File( targetDirectory , path ) ;
			
			FileOutputStream fout = new FileOutputStream(file) ;
			
			try {
				fout.write(data);
			}
			finally {
				fout.close();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	
	private ArrayList<DirectoryFileData> files ;
	
	public DirectorySnapshot(String groupId, File directoryRoot, String path) throws IOException {
		this( groupId, directoryRoot , new File(directoryRoot , path) ) ;
	}
	
	public DirectorySnapshot(String groupId, File directoryRoot, File directory) throws IOException {
		this(groupId, directoryRoot, directory, true) ;
	}
	
	public DirectorySnapshot(String groupId, File directoryRoot, File directory, boolean recursive) throws IOException {
		this(groupId, directoryRoot, directory, recursive, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName() ;
				return !name.startsWith(".") ;
			}
		}) ;
	}
	
	public DirectorySnapshot(String groupId, File directoryRoot, File directory, boolean recursive, FileFilter filter) throws IOException {
		super(new SnapshotIDDirectory(groupId, System.currentTimeMillis(), directoryRoot, directory)) ;
		
		loadFromDirectory(directory, recursive, filter);
	}
	
	public ArrayList<DirectoryFileData> getFiles() {
		return files;
	}

	private void loadFromDirectory(File directory, boolean recursive, FileFilter filter) throws IOException {
		FileInTree[] files ;
		if (recursive) {
			files = FileUtils.listTree(directory, filter) ;
		}
		else {
			files = FileUtils.listDirectory(directory, filter) ;
		}
		
		this.files = new ArrayList<>( files.length ) ;
		
		for (FileInTree fileInTree : files) {
			String pathFromRoot = fileInTree.getPathFromRoot() ;
			byte[] data = FileUtils.readFile(fileInTree.getFile()) ;
			
			this.files.add( new DirectoryFileData(pathFromRoot, data) ) ;
		}
	}
	
	public void saveToDirectory(File targetDirectoryRoot, boolean clearTargetDirectory) throws IOException {
		if (!targetDirectoryRoot.isDirectory()) throw new IllegalArgumentException("Directory doesn't exists: "+ targetDirectoryRoot) ;
		
		File targetDirectory = new File(targetDirectoryRoot , getSnapshotID().getDirectoryPath()) ;
		
		if (clearTargetDirectory) {
			FileUtils.deleteTree(targetDirectoryRoot, targetDirectory) ;
		}
		
		targetDirectory.mkdirs() ;
		
		if ( !targetDirectory.isDirectory() ) throw new IOException("Can't create directory: "+ targetDirectory) ;
		
		for (DirectoryFileData fileData : files) {
			fileData.saveToDirectory(targetDirectory);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		
		snapshotID.writeTo(out);
		
		int sz = files.size() ;
		
		SerializationUtils.writeInt( sz , out );
		
		for (int i = 0; i < sz; i++) {
			DirectoryFileData fileData = files.get(i) ;
			
			fileData.writeTo(out);
		}
		
	}
	
	public DirectorySnapshot(InputStream in) throws IOException {
		super(in) ;
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		
		this.snapshotID = new SnapshotIDDirectory(in) ;
		
		int sz = SerializationUtils.readInt(in) ;
		
		this.files = new ArrayList<>(sz) ;
		
		for (int i = 0; i < sz; i++) {
			DirectoryFileData fileData = new DirectoryFileData(in) ;
			files.add(fileData);
		}
		
	}
	
	///////////////////////////////////////////////////////////////////////
	
	public File restoreDirectory(File directoryRoot) throws IOException {
		File targetDirectory = new File( directoryRoot , this.snapshotID.getDirectoryPath() ) ;
		
		FileUtils.deleteTree(directoryRoot, targetDirectory) ;
		
		targetDirectory.mkdirs() ;
		
		List<DirectoryFileData> files = this.getFiles() ;
		
		for (DirectoryFileData fileData : files) {
			String path = fileData.getPath() ;
			byte[] data = fileData.getData() ;
			
			File file = new File( targetDirectory , path ) ;
			
			File parentFile = file.getParentFile() ;
			if ( !parentFile.equals(targetDirectory) ) {
				parentFile.mkdirs() ;
			}
			
			FileUtils.saveFile(file, data);
		}
		
		return targetDirectory ;
	}

	
}

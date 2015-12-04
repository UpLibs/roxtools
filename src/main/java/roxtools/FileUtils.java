package roxtools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Random;

final public class FileUtils {
	
	static public String getFilePathFromRoot(File root, File file) {
		ArrayList<String> keyParts = new ArrayList<String>() ;
		
		while (true) {
			keyParts.add( file.getName() ) ;
			
			File parentFile = file.getParentFile() ;
			
			if ( parentFile.equals(root) ) break ;
		}
		
		StringBuilder key = new StringBuilder() ;
		
		for (int i = keyParts.size()-1; i >= 0; i--) {
			if (key.length() > 0) key.append("/") ;
			key.append( keyParts.get(i) ) ;
		}
		
		return key.toString() ;
	}
	
	static public class FileInTree {
		private final File root ;
		private final File file ;
		
		public FileInTree(File root, File file) {
			this.root = root;
			this.file = file;
		}
		
		public File getFile() {
			return file;
		}
		
		public File getRoot() {
			return root;
		}
		
		public long lastModified() {
			return file.lastModified();
		}

		public String getPathFromRoot() {
			return getFilePathFromRoot(root, file) ;
		}
		
		@Override
		public String toString() {
			return file.toString() ;
		}
	}

	
	static public FileInTree[] listTree(File dir, FileFilter filter) {
		ArrayList<FileInTree> tree = new ArrayList<FileInTree>() ;
		
		listTree(dir, dir, filter, tree);
		
		return tree.toArray( new FileInTree[tree.size()] ) ;
	}
	
	static private void listTree(File root, File dir, FileFilter filter, ArrayList<FileInTree> tree) {
		File[] files = dir.listFiles(filter) ;
		
		if (files == null || files.length == 0) return ;
		
		for (File file : files) {
			if ( file.isDirectory() ) {
				listTree(root, file, filter, tree); 
			}
			else {
				FileInTree fileWrapper = new FileInTree(root, file) ;
				tree.add(fileWrapper);
			}
		}
	}
	
	static public File getTemporaryDirectory() {
		String path = System.getProperty("java.io.tmpdir") ;
		File dir = new File(path).getAbsoluteFile() ;
		if (!dir.isDirectory()) throw new IllegalStateException("Invalid temporary directory: "+ dir) ;
		return dir ;
	}
	
	static public boolean isTemporaryFileClassicPath() {
		File tempDir0 = getTemporaryDirectory() ;
		File tempClassic = new File("/tmp/").getAbsoluteFile() ;
		
		String tempClassicStr = tempClassic.toString() ;
		if (!tempClassicStr.endsWith("/")) tempClassicStr += "/" ; 
		
		return tempDir0.toString().startsWith(tempClassicStr) ;
	}
	
	static public boolean setTemporaryFileToClassicPath() {
		if ( isTemporaryFileClassicPath() ) return true ;
		
		File tempDir = new File("/tmp/") ;
		
		if (!tempDir.isDirectory()) return false ;
		
		File jvmTempDir = new File("/tmp/jvm-tmpdir/") ;
		jvmTempDir.mkdirs() ;
		
		if (jvmTempDir.isDirectory()) {
			System.setProperty("java.io.tmpdir" , "/tmp/jvm-tmpdir/") ;
			return true ;
		}
		else {
			return false ;
		}
		
	}
	
	static public File createTempFile(String prefix) throws IOException {
		return createTempFile(prefix, ".tmp") ;
	}
	
	static public File createTempFile(String prefix, String suffix) throws IOException {
		return File.createTempFile(prefix, suffix) ;
	}

	static final private Random RANDOM = new Random() ;
	
	static public File createTempFile(File parentDirectory, String prefix) throws IOException {
		return createTempFile(parentDirectory, prefix, ".tmp") ;
	}
	
	static public File createTempFile(File parentDirectory, String prefix, String suffix) throws IOException {
		
		for (int i = 0;; i++) {
			long randN ;
			
			synchronized (RANDOM) {
				randN = RANDOM.nextLong() ;
			}
			
			randN = randN ^ ( (System.currentTimeMillis() * 100L) + i ) ;
			
			File tmpFile = new File( parentDirectory , prefix + randN + suffix ) ;
			
			if (!tmpFile.exists()) {
				boolean ok = tmpFile.createNewFile() ;
				if (ok) {
					tmpFile.deleteOnExit();
					return tmpFile ;
				}
			}
			
			if (i % 10 == 0) {
				synchronized (RANDOM) {
					try { RANDOM.wait(1) ;} catch (InterruptedException e) {}
				}
			}
		}
		
	}
	
	static public File createTempDirectory(String prefix) throws IOException {
		return createTempDirectory(prefix, "-temp") ;
	}
	
	static public File createTempDirectory(String prefix, String suffix) throws IOException {
		for (int i = 0; i < 1000; i++) {
			File file = createTempFile(prefix, suffix) ;
			file.delete() ;
			file.mkdirs() ;
			
			if (file.isDirectory()) return file ;
			
			try { Thread.sleep(1) ;} catch (InterruptedException e) {}
		}
		
		throw new IOException("Can't create temporary directory at: "+ getTemporaryDirectory()) ;
	}
	
	static public File createTempDirectory(File parentDirectory, String prefix) throws IOException {
		return createTempDirectory(parentDirectory, prefix, "-temp") ;
	}
	
	static public File createTempDirectory(File parentDirectory, String prefix, String suffix) throws IOException {
		for (int i = 0; i < 1000; i++) {
			File file = createTempFile(parentDirectory, prefix, suffix) ;
			file.delete() ;
			file.mkdirs() ;
			
			if (file.isDirectory()) return file ;
			
			try { Thread.sleep(1) ;} catch (InterruptedException e) {}
		}
		
		throw new IOException("Can't create temporary directory at: "+ parentDirectory) ;
	}
	
	static public void copyFile(File src, File dest) throws IOException {
		FileInputStream fin = new FileInputStream(src) ;
		
		try {
			FileOutputStream fout = new FileOutputStream(dest) ;
			
			try {
				byte[] buffer = new byte[1024*8] ;
				int r ;
				
				while ( (r = fin.read(buffer)) >= 0 ) {
					fout.write(buffer, 0, r);
				}
				
			}
			finally {
				fout.close();
			}
		}
		finally {
			fin.close();
		}
		
	}
	
	static public long getFileCreationTime(File file, long defaultValue) {
		long creationTime = defaultValue ;
		
		try {
			BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class) ;
			
			FileTime creationFileTime = basicFileAttributes.creationTime() ;
			
			if (creationFileTime != null) creationTime = creationFileTime.toMillis() ;
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
		
		return creationTime ;
	}

	static public boolean setFileCreationTime(File file, long creationTime) {
		try {
			FileTime fileTime = FileTime.fromMillis(creationTime);
			Files.setAttribute(file.toPath(), "basic:creationTime", fileTime, LinkOption.NOFOLLOW_LINKS);
			return true ;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false ;
		}
	}

	//////////////////////////////////
	
	static public void saveFile(File file, String content) throws IOException {
		saveFile(file, content.getBytes());
	}
	
	static public void saveFile(File file, byte[] content) throws IOException {
		SerializationUtils.writeFile(file, content);
	}
	
	static public String readFileAsString(File file) throws IOException {
		return new String( readFile(file) ) ;
	}
	
	static public byte[] readFile(File file) throws IOException {
		return SerializationUtils.readFile(file) ;
	}
	
	//////////////////////////////////
	
	static public File resolveFilePath(File file) {
		file = file.getAbsoluteFile() ;
		
		try {
			file = file.getCanonicalFile() ;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return file ;
	}
	
	static public boolean isSamePath(File path, File subPath) {
		if (path == null || subPath == null) return false ;
		
		if (path.equals(subPath)) return true ;
		
		path = resolveFilePath(path) ;
		subPath = resolveFilePath(subPath) ;
		
		return path.equals(subPath) ;
	}
	
	static public boolean isSubPath(File path, File subPath) {
		return isSubPath(path, subPath, false) ;
	}
	
	static public boolean isSubPath(File path, File subPath, boolean acceptSamePath) {
		if (path == null || subPath == null) return false ;
		
		if (path.equals(subPath)) return acceptSamePath ;
		
		path = resolveFilePath(path) ;
		subPath = resolveFilePath(subPath) ;
		
		if ( path.equals(subPath) ) return acceptSamePath ;
		
		while (true) {
			File parent = subPath.getParentFile() ;
			if (parent == null) return false ;
			
			if ( path.equals(parent) ) return true ;
			
			subPath = parent ;
		}
	}

	static public void checkAuthorityPoint(File authorityPoint) {
		if (authorityPoint == null) throw new NullPointerException("Can't have a null authorityPoint") ;
		
		File resolved = resolveFilePath(authorityPoint) ;
		
		if ( resolved.equals( resolveFilePath(new File("/")) )) throw new IllegalArgumentException("Can't have a root authorityPoint: "+ authorityPoint +" -> "+ resolved) ;
		
		if ( !resolved.isAbsolute() ) throw new IllegalArgumentException("Can't have a non absolute authorityPoint: "+ authorityPoint +" -> "+ resolved) ;
		
	}
	
	static public boolean hasAuthorityOverFile(File authorityPoint, File targetFile) {
		checkAuthorityPoint(authorityPoint);
		
		if ( !isSubPath(authorityPoint, targetFile) ) return false ;
		
		return true ;
	}
	
	static public boolean deleteTree(File authorityPoint, File targetDir) {
		checkAuthorityPoint(authorityPoint);
		
		if ( !isSubPath(authorityPoint, targetDir) ) throw new IllegalArgumentException("Target directory not in authority point: "+ authorityPoint +" -> "+ targetDir) ;
		
		return deleteTreeImplem(authorityPoint, targetDir);
	}

	private static boolean deleteTreeImplem(File authorityPoint, File targetDir) {
		if ( !isSubPath(authorityPoint, targetDir) ) return false ;
		
		if ( targetDir.isDirectory() ) {
			
			File[] files = targetDir.listFiles() ;
			
			if ( files != null ) {
				for (File file : files) {
					
					if (file.isDirectory()) {
						deleteTree(authorityPoint , file);
					}
					else {
						file.delete() ;
					}
				}
			}
			
		}
		
		boolean ok = targetDir.delete() ;
		
		return ok ;
	}
	
}

package roxtools;

import java.io.File;
import java.io.IOException;

final public class FileUtils {

	public static void main(String[] args) {
		
		setTemporaryFileToClassicPath() ;
		
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
	
	static public File createTempDirectory(String prefix) throws IOException {
		return createTempDirectory(prefix, "-temp") ;
	}
	
	static public File createTempDirectory(String prefix, String suffix) throws IOException {
		for (int i = 0; i < 100; i++) {
			File file = File.createTempFile(prefix, suffix) ;
			file.delete() ;
			file.mkdirs() ;
			
			if (file.isDirectory()) return file ;
			
			try { Thread.sleep(1) ;} catch (InterruptedException e) {}
		}
		
		throw new IOException("Can't create temporary directory at: "+ getTemporaryDirectory()) ;
	}
	
}

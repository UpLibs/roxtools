package roxtools;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest {

	@Test
	public void testTempFile() throws IOException {
		
		for (int i = 0; i < 10; i++) {
			String prefix = "pref_"+i+"-" ;
			String suffix = "-suf_"+i ;
			
			File tempFile = FileUtils.createTempFile(prefix, suffix) ;
			
			System.out.println(tempFile);
			
			Assert.assertTrue( tempFile.exists() );
			
			Assert.assertTrue( tempFile.getName().startsWith(prefix) );
			Assert.assertTrue( tempFile.getName().endsWith(suffix) );
			
			tempFile.delete();
			
			Assert.assertTrue( !tempFile.exists() );
		}
		
	}
	
	@Test
	public void testTempFileParentDirectory() throws IOException {
		
		File parentDir = FileUtils.createTempDirectory("test-temp-dir","-junit-test") ;
		
		Assert.assertTrue( parentDir.exists() );
		Assert.assertTrue( parentDir.isDirectory() );
		
		for (int i = 0; i < 10; i++) {
			String prefix = "pref_"+i+"-" ;
			String suffix = "-suf_"+i ;
			
			File tempFile = FileUtils.createTempFile(parentDir, prefix, suffix) ;
			
			System.out.println(parentDir +" >> file: "+ tempFile);
			
			Assert.assertTrue( tempFile.exists() );
			Assert.assertTrue( tempFile.isFile() );
			
			Assert.assertTrue( tempFile.getName().startsWith(prefix) );
			Assert.assertTrue( tempFile.getName().endsWith(suffix) );
			
			Assert.assertTrue( tempFile.getParentFile().equals(parentDir) );
			
			
			tempFile.delete() ;
			
			Assert.assertTrue( !tempFile.exists() );
		}
		
		parentDir.delete() ;
		
		Assert.assertTrue( !parentDir.exists() );
		
	}
	
	@Test
	public void testTempDirectory() throws IOException {
		
		for (int i = 0; i < 10; i++) {
			String prefix = "pref_"+i+"-" ;
			String suffix = "-suf_"+i ;
			
			File tempFile = FileUtils.createTempDirectory(prefix, suffix) ;
			
			System.out.println(tempFile);
			
			Assert.assertTrue( tempFile.exists() );
			Assert.assertTrue( tempFile.isDirectory() );
			
			Assert.assertTrue( tempFile.getName().startsWith(prefix) );
			Assert.assertTrue( tempFile.getName().endsWith(suffix) );
			
			tempFile.delete() ;
			
			Assert.assertTrue( !tempFile.exists() );
		}
		
	}
	
	@Test
	public void testTempDirectoryParentDirectory() throws IOException {

		File parentDir = FileUtils.createTempDirectory("test-temp-dir","-junit-test") ;
		
		Assert.assertTrue( parentDir.exists() );
		Assert.assertTrue( parentDir.isDirectory() );
				
		for (int i = 0; i < 10; i++) {
			String prefix = "pref_"+i+"-" ;
			String suffix = "-suf_"+i ;
			
			File tempFile = FileUtils.createTempDirectory(parentDir, prefix, suffix) ;
			
			System.out.println(parentDir +" >> dir: "+ tempFile);
			
			Assert.assertTrue( tempFile.exists() );
			Assert.assertTrue( tempFile.isDirectory() );
			
			Assert.assertTrue( tempFile.getName().startsWith(prefix) );
			Assert.assertTrue( tempFile.getName().endsWith(suffix) );
			
			tempFile.delete() ;
			
			Assert.assertTrue( !tempFile.exists() );
		}

		parentDir.delete() ;
		Assert.assertTrue( !parentDir.exists() );
		
	}
	
}

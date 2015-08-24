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
	
	@Test
	public void testSaveReadFile() throws IOException {
		
		File file = FileUtils.createTempFile("test-saveread-file", "junit-temp") ;
		
		String content = "abc123" ;
		
		FileUtils.saveFile(file, content);
		
		Assert.assertTrue( file.exists() ) ;
		Assert.assertTrue( file.length() >= 6 ) ;
		
		Assert.assertTrue( FileUtils.readFileAsString(file).equals(content) ) ;
		
		file.delete() ;
		
	}
	
	@Test
	public void testResolveFilePath() {
		
		Assert.assertTrue( FileUtils.resolveFilePath(new File("/foo/bar")).equals(  new File("/foo/bar")  ) ) ;
		
		Assert.assertTrue( FileUtils.resolveFilePath(new File("/foo/bar/..")).equals(  new File("/foo")  ) ) ;
		
		Assert.assertTrue( FileUtils.resolveFilePath(new File("/foo/bar/xxx/..")).equals(  new File("/foo/bar")  ) ) ;
		
		Assert.assertTrue( FileUtils.resolveFilePath(new File("/foo/bar/xxx/../yyy")).equals(  new File("/foo/bar/yyy")  ) ) ;
		
		Assert.assertTrue(
				FileUtils.resolveFilePath(new File("./foo/bar/xxx/.."))
				.equals( 
						FileUtils.resolveFilePath(new File("./foo/bar/yyy/.."))  
				)
		) ;
		
	}
	
	@Test
	public void testSamePath() {
		
		Assert.assertTrue( FileUtils.isSamePath(new File("/foo/bar") , new File("/foo/bar") ) ) ;
		
		Assert.assertFalse( FileUtils.isSamePath(new File("/foo/bar") , new File("/foo/bar/aaaa") ) ) ;
		
		Assert.assertTrue( FileUtils.isSamePath(new File("/foo/bar") , new File("/foo/bar/zzz/../") ) ) ;
		
		Assert.assertTrue( FileUtils.isSamePath(new File("/foo/bar") , new File("/foo/xxx/zzz/../../bar") ) ) ;
		
	}
	
	@Test
	public void testSubPath() {
		
		Assert.assertTrue( FileUtils.isSubPath(new File("/foo/bar") , new File("/foo/bar/aaaa") ) ) ;
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAuthorityPoint_IllegalArgumentException1() {
		
		FileUtils.checkAuthorityPoint( new File("/") );

	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAuthorityPoint_IllegalArgumentException2() {
		
		FileUtils.checkAuthorityPoint( new File("../../../../../") );

	}
	
	@Test(expected=NullPointerException.class)
	public void testAuthorityPoint_NullPointerException() {
		
		FileUtils.checkAuthorityPoint(null);

	}
	
	@Test
	public void testAuthorityPoint() {
		
		Assert.assertTrue( FileUtils.hasAuthorityOverFile(new File("/foo"), new File("/foo/bar"))) ;
		
		Assert.assertTrue( FileUtils.hasAuthorityOverFile(new File("/foo"), new File("/foo/bar/xxx"))) ;
	
		Assert.assertFalse( FileUtils.hasAuthorityOverFile(new File("/xxx"), new File("/foo/bar"))) ;
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAuthorityPoint_IllegalArgumentException3() {
		
		Assert.assertTrue( FileUtils.hasAuthorityOverFile(new File("/foo"), new File("/foo/bar"))) ;
		
		Assert.assertTrue( FileUtils.hasAuthorityOverFile(new File("/foo/.."), new File("/foo/bar/xxx"))) ;
		
	}
	
	@Test
	public void testDeleteTree() throws IOException {
		
		// Ensure that this tests are OK for file system security reason:
		{
			
			testResolveFilePath();
			testAuthorityPoint();
			testSamePath();
			testSubPath();
			
		}
		
		File dirAuthority = FileUtils.createTempDirectory("test-fileutils-deletetree", "-junit-temp") ;
		
		Assert.assertTrue( dirAuthority.isDirectory() ) ;
		
		File dir = new File(dirAuthority, "thedir") ;
		
		dir.mkdirs() ;
		
		Assert.assertTrue( dir.isDirectory() ) ;
		
		File sub1 = new File(dir, "sub1") ;
		
		sub1.mkdirs() ;
		
		Assert.assertTrue( sub1.isDirectory() ) ;
		
		File sub2 = new File(dir, "sub2") ;
		
		sub2.mkdirs() ;
		
		///////////////////////
		
		FileUtils.saveFile(new File(sub1, "sub1-file1") ,"111");
		FileUtils.saveFile(new File(sub1, "sub1-file2") ,"222");
		
		FileUtils.saveFile(new File(sub2, "sub2-file1") ,"aaa");
		FileUtils.saveFile(new File(sub2, "sub2-file2") ,"bbb");
		
		FileUtils.saveFile(new File(dir, "file1") ,"xxx");
		FileUtils.saveFile(new File(dir, "file2") ,"yyy");
		
		///////////////////////
		
		Assert.assertTrue( sub2.isDirectory() ) ;
		
		File dirAuthorityWrong = new File(dirAuthority,"wrong-authority-dir");
		
		Assert.assertFalse( FileUtils.hasAuthorityOverFile(dirAuthorityWrong, dir) ) ;
		
		Exception errorWrongAuthority = null ;
		try {
			boolean ok = FileUtils.deleteTree(dirAuthorityWrong, dir) ;
			
			Assert.assertFalse(ok) ;	
		}
		catch (Exception e) {
			System.out.println("Expected authority error: "+ e.getMessage());
			errorWrongAuthority = e ;
		}
		
		Assert.assertNotNull(errorWrongAuthority);
		
		Assert.assertTrue( FileUtils.deleteTree(dirAuthority, dir) ) ;
		
		Assert.assertFalse( dir.exists() ) ;
		
	}
	
}

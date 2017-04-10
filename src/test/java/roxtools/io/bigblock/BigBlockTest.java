package roxtools.io.bigblock;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import roxtools.FileUtils;

public class BigBlockTest {

	@Test
	public void testBasic() throws IOException {
		
		File sourceDir = FileUtils.createTempDirectory("test-bigblock-source") ;
		File storageDir = FileUtils.createTempDirectory("test-bigblock-") ;
		
		BigBlockStorageDirectory storage = new BigBlockStorageDirectory(storageDir) ;
		
		try {
			
			for (int i = 0; i < 100; i++) {
				String name = "test"+i+".txt" ;
				String content = "content"+i;
				FileUtils.saveFile(new File(sourceDir,name), content);	
			}
			
			BigBlock bigBlock = new BigBlock("test1", storage) ;
			
			BigBlockIndex blockIndex = bigBlock.buildFromDirectory(sourceDir);
			
			testBigBlockEntries(bigBlock, blockIndex);
			
			BigBlockStorageDirectory storage2 = new BigBlockStorageDirectory(storageDir) ;
			
			BigBlock bigBlock2 = new BigBlock("test1", storage2) ;
			BigBlockIndex blockIndex2 = bigBlock2.load();
			
			testBigBlockEntries(bigBlock2, blockIndex2);
		}
		finally {
			FileUtils.deleteTree(sourceDir.getParentFile(), sourceDir) ;
			sourceDir.delete();
			
			cleanStorageDirectory(storage);	
			storageDir.delete();
		}
	}

	private void testBigBlockEntries(BigBlock bigBlock, BigBlockIndex blockIndex) throws IOException {
		Assert.assertEquals( 100 , blockIndex.getTotalEntries() );
		
		for (int i = 0; i < 100; i++) {
			String name = "test"+i+".txt" ;
			String content = "content"+i;
			
			Assert.assertTrue( blockIndex.containsEntry(name) );
			
			BigBlockEntry entry = blockIndex.getEntry(name);
			
			Assert.assertNotNull(entry);
			
			Assert.assertEquals(name, entry.getName());
			
			Assert.assertEquals(content.getBytes().length, entry.getLength());
			
			byte[] fileData = bigBlock.getFileData(name) ;
			Assert.assertArrayEquals(content.getBytes(), fileData);
		}
		
		for (int i = 0; i < 100; i++) {
			String name = "testNull"+i+".txt" ;
			
			Assert.assertFalse( blockIndex.containsEntry(name) );
			BigBlockEntry entry = blockIndex.getEntry(name);
			Assert.assertNull(entry);
		}
	}
	
	private void cleanStorageDirectory(BigBlockStorageDirectory storageDirectory) {
		
		deleteFiles( storageDirectory.listBlockIndexFiles() ) ;
		deleteFiles( storageDirectory.listBlockFiles() ) ;
		
	}

	private void deleteFiles(File[] files) {
		if (files == null) return ;
		for (File file : files) {
			file.delete();
		}
	}
	
}

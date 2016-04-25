package roxtools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import roxtools.snapshot.SnapshotCapturer;
import roxtools.snapshot.Snapshot;
import roxtools.snapshot.directory.DirectorySnapshotCapturer;

public class SnapshotTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testBasic() throws IOException {
		
		File directoryRoot = FileUtils.createTempDirectory("directory-snapshot-", "-root") ;
		
		File directory = new File( directoryRoot , "test-basic" ) ;

		directory.mkdirs() ;
		
		try {
			
			SnapshotCapturer snapshotCapturer = new DirectorySnapshotCapturer(directoryRoot, directory) ;
			
			double ver = 1.0 ;
			
			for (int i = 0; i <= 9; i++) {
				File file = new File( directory , "t"+i+".txt" ) ;
				FileUtils.saveFile(file, "test"+i+"--"+ ver);
			}
			
			for (int i = 0; i <= 9; i++) {
				File file = new File( directory , "t"+i+".txt" ) ;
				String content = FileUtils.readFileAsString(file) ;
				Assert.assertEquals("test"+i+"--"+ ver, content);
			}
			
			Snapshot snapshot1 = snapshotCapturer.takeSnapshot() ;
			
			ver = 2.0 ;
			
			for (int i = 0; i <= 9; i++) {
				File file = new File( directory , "t"+i+".txt" ) ;
				FileUtils.saveFile(file, "test"+i+"--"+ ver);
			}
			
			for (int i = 0; i <= 9; i++) {
				File file = new File( directory , "t"+i+".txt" ) ;
				String content = FileUtils.readFileAsString(file) ;
				Assert.assertEquals("test"+i+"--"+ ver, content);
			}

			snapshotCapturer.restoreSnapshot(snapshot1); 
			
			ver = 1.0 ;
			
			for (int i = 0; i <= 9; i++) {
				File file = new File( directory , "t"+i+".txt" ) ;
				String content = FileUtils.readFileAsString(file) ;
				Assert.assertEquals("test"+i+"--"+ ver, content);
			}
			
			File[] files = directory.listFiles() ;
			
			Arrays.sort(files);
			
			Assert.assertTrue( files.length == 10 );
			
			for (int i = 0; i <= 9; i++) {
				File file = files[i] ;
				File file2 = new File( directory , "t"+i+".txt" ) ;
				Assert.assertEquals( file2 , file );
			}
			
			
		}
		finally {
			FileUtils.deleteTree(directoryRoot, directory) ;
		}
		
	}
	
}

package roxtools;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import roxtools.ipc.ProcessRunner;

public class ProcessRunnerTest {

	public boolean isOSLinuxCompatible() {
		String os = System.getProperty("os.name") ;
		
		if (os == null || os.trim().isEmpty() ) return false ;
		
		os = os.toLowerCase() ;
		
		return os.contains("linux") || ( os.contains("mac") && os.contains("os x") ) ;
	}
	
	@Test
	public void testBasic() throws IOException, InterruptedException {
		if ( !isOSLinuxCompatible() ) {
			throw new IllegalStateException("Can't test if OS is not Linux compatible!") ;
		}
		
		ProcessRunner processRunner = new ProcessRunner("/bin/ls" , "/") ;
		
		processRunner.execute(true) ;
		
		int exitCode = processRunner.waitForProcess(true) ;
		
		Assert.assertTrue(exitCode == 0);
		
		String output = processRunner.getOutputConsumer().getOutputAsString() ;
		
		Assert.assertTrue( output.trim().length() > 1 );
		
		Assert.assertTrue( output.trim().split("\\r?\\n").length >= 3 );
		
	}
	
}

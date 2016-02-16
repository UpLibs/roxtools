package roxtools;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import roxtools.ipc.JVMRunner;
import roxtools.ipc.ProcessRunner;

public class JVMRunnerTest {

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
		
		JVMRunner jvmRunner = new JVMRunner( this.getClass().getName() ) ;
		
		Assert.assertTrue( jvmRunner.addVmArgument("-Xmx100m") );
		
		Assert.assertTrue( jvmRunner.containsVmArgument("-Xmx100m") );
		
		jvmRunner.setVMProperty("test.jvmrunner","123") ;
		
		Assert.assertTrue( jvmRunner.containsVMProperty("test.jvmrunner") )  ;
		
		jvmRunner.execute();
		
		Assert.assertTrue( jvmRunner.isRunning() );
		
		jvmRunner.waitForProcess() ;
		
		String output = jvmRunner.getOutputConsumer().waitFinished().getOutputAsString() ;
		
		Assert.assertEquals( "Hello World!\n" , output );
		
	}
	
	public static void main(String[] args) {
		
		System.out.println("Hello World!");
		
	}
	
}

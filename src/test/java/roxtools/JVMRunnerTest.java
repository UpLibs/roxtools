package roxtools;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import roxtools.ipc.JVMRunner;
import roxtools.ipc.ProcessRunner.OutputConsumer;
import roxtools.ipc.ProcessRunner.OutputConsumerListener;

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
	
	@Test
	public void testOutputListener() throws IOException, InterruptedException {
		if ( !isOSLinuxCompatible() ) {
			throw new IllegalStateException("Can't test if OS is not Linux compatible!") ;
		}
		
		JVMRunner jvmRunner = new JVMRunner( this.getClass().getName() ) ;
		
		Assert.assertTrue( jvmRunner.addVmArgument("-Xmx100m") );
		
		Assert.assertTrue( jvmRunner.containsVmArgument("-Xmx100m") );
		
		jvmRunner.setVMProperty("test.jvmrunner","123") ;
		
		Assert.assertTrue( jvmRunner.containsVMProperty("test.jvmrunner") )  ;
		
		final ArrayList<String> lines = new ArrayList<String>() ;
		
		jvmRunner.execute(true , new OutputConsumerListener() {
			@Override
			public void onReadLine(OutputConsumer outputConsumer, String line) {
				lines.add(line) ;
			}
			
			@Override
			public void onReadBytes(OutputConsumer outputConsumer, byte[] bytes, int length) {}
		});
		
		Assert.assertTrue( jvmRunner.isRunning() );
		
		jvmRunner.waitForProcess() ;
		
		String output = jvmRunner.getOutputConsumer().waitFinished().getOutputAsString() ;
		
		Assert.assertEquals( "Hello World!\n" , output );
		
		Assert.assertEquals( "Hello World!\n" , lines.get(0) );
		Assert.assertTrue( lines.size() == 1 ) ;
		
	}
	
	public static void main(String[] args) {
		
		System.out.println("Hello World!");
		
	}
	
}

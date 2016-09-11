package roxtools;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import roxtools.io.ByteArrayInputOutput;

public class ByteArrayInputOutputTest {

	@Test
	
	public void testBasic() throws IOException {
		
		testBasic(10, 100);
		testBasic(100, 1000);
		
		testBasic(1, 100);
		testBasic(1, 1000);
		
		testBasic(1024, 1024*1024);
		testBasic(1024, 1024*1024*3);
		
	}
	
	private void testBasic(int initialCapacity, int writeSize) throws IOException {
		
		ByteArrayInputOutput inOut = new ByteArrayInputOutput(initialCapacity) ;
		
		Assert.assertEquals( 0 , inOut.length() );
		Assert.assertEquals( 0 , inOut.position() );
		
		for (int i = 0; i < writeSize; i++) {
			inOut.write(i);
			Assert.assertEquals( i+1 , inOut.length() );
			Assert.assertEquals( i+1 , inOut.position() );
		}
		
		Assert.assertEquals( writeSize , inOut.length() );
		Assert.assertTrue( inOut.capacity() >= writeSize );
		
		inOut.seek(0);
		
		Assert.assertEquals( 0 , inOut.position() );
		
		byte[] bs = inOut.toByteArray() ;
		Assert.assertEquals( writeSize , bs.length );
		
		for (int i = 0; i < writeSize; i++) {
			byte b = bs[i] ;
			byte bCheck = (byte) i ;
			Assert.assertEquals( bCheck , b );	
		}
		
		
		int writeSizeHalf = writeSize/2;
		inOut.setLength(writeSizeHalf);
		
		Assert.assertEquals( writeSizeHalf , inOut.length() );
		Assert.assertTrue( inOut.capacity() >=  writeSizeHalf );
		
		byte[] bs2 = inOut.toByteArray() ;
		Assert.assertEquals( writeSizeHalf , bs2.length );
		
		for (int i = 0; i < writeSizeHalf; i++) {
			byte b = bs[i] ;
			byte bCheck = (byte) i ;
			Assert.assertEquals( bCheck , b );	
		}
		
		inOut.reset();

		Assert.assertEquals( 0 , inOut.length() );
		Assert.assertEquals( 0 , inOut.position() );

		Assert.assertTrue( inOut.capacity() >= writeSizeHalf );

		
	}
	
}

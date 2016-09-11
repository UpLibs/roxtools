package roxtools;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import roxtools.io.BufferedInputOutput;
import roxtools.io.ByteArrayInputOutput;

public class BufferedInputOutputTest {
	
	@Test
	public void testBasic() throws IOException {
		
		testBasic(3, 1000, true) ;
		
		////////
		
		testBasic(1, 1000) ;
		testBasic(2, 1000) ;
		testBasic(3, 1000) ;
		testBasic(4, 1000) ;
		testBasic(4, 1000) ;
		
		testBasic(9, 1000) ;
		testBasic(10, 1000) ;
		testBasic(11, 1000) ;
		
	}

	private void testBasic(int blockSize, int writeSize) throws IOException {
		testBasic(blockSize, writeSize, false);
		testBasic(blockSize, writeSize, true);
	}
	
	private void testBasic(int blockSize, int writeSize, boolean autoFlush) throws IOException {
		
		ByteArrayInputOutput out = new ByteArrayInputOutput(writeSize) ;
		
		BufferedInputOutput inOut = new BufferedInputOutput(blockSize, out,out) ;
		
		Assert.assertEquals( 0 , inOut.length() );
		Assert.assertEquals( 0 , inOut.position() );
		
		inOut.seek(0);
		Assert.assertEquals( 0 , inOut.position() );
		
		for (int i = 0; i < writeSize; i++) {
			inOut.write(i);
			Assert.assertEquals( i+1 , inOut.length() );
			Assert.assertEquals( i+1 , inOut.position() );
		}
		
		if (autoFlush) inOut.flush();
		
		Assert.assertEquals( writeSize , inOut.length() );
		Assert.assertEquals( writeSize , inOut.position() );
		
		inOut.seek(0);
		
		if (autoFlush) inOut.flush();
		
		for (int i = 0; i < writeSize; i++) {
			inOut.write(i,i);
			Assert.assertEquals( writeSize , inOut.length() );
			Assert.assertEquals( 0 , inOut.position() );
		}

		if (autoFlush) inOut.flush();
		
		Assert.assertEquals( writeSize , inOut.length() );
		Assert.assertEquals( 0 , inOut.position() );
		
		byte[] bs = inOut.toByteArray() ;
		
		Assert.assertEquals( 0 , inOut.position() );
		
		Assert.assertEquals( writeSize , bs.length );
		
		for (int i = 0; i < writeSize; i++) {
			byte b = bs[i] ;
			byte bCheck = (byte) i ;
			Assert.assertEquals( "byte error at index "+i+"! " , bCheck , b );
		}
		
		inOut.seek(writeSize);

		for (int i = writeSize; i < writeSize*2; i++) {
			inOut.write(i);
			Assert.assertEquals( i+1 , inOut.length() );
			Assert.assertEquals( i+1 , inOut.position() );
		}

		if (autoFlush) inOut.flush();

		Assert.assertEquals( writeSize*2 , inOut.length() );
		Assert.assertEquals( writeSize*2 , inOut.position() );
		
		for (int i = writeSize*2; i < writeSize*3; i++) {
			inOut.write(i);
			Assert.assertEquals( i+1 , inOut.length() );
			Assert.assertEquals( i+1 , inOut.position() );
		}
		
		int resize = writeSize*2 + writeSize/2 ;
		
		inOut.setLength(resize);

		if (autoFlush) inOut.flush();

		Assert.assertEquals( resize , inOut.length() );
		Assert.assertEquals( resize , inOut.position() );
		
		
	}
	
}

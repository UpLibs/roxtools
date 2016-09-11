package roxtools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilsTest {

	@Test
	public void testWriteReadInt() throws IOException {
		int totalWrite = 1000 ;
		
		int[] ns0 = new int[totalWrite] ;
		for (int i = 0; i < totalWrite; i++) {
			ns0[i] = i ;
		}
		
		ByteArrayOutputStream out0 = new ByteArrayOutputStream() ;
		SerializationUtils.writeInts(ns0, out0);
		byte[] serial0 = out0.toByteArray() ;
		
		Assert.assertEquals( totalWrite*4 , serial0.length );
		
		ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		
		for (int i = 0; i < totalWrite; i++) {
			SerializationUtils.writeInt(i, out);
		}
		
		byte[] serial = out.toByteArray() ;
		
		Assert.assertEquals( totalWrite*4 , serial.length );
		
		Assert.assertTrue( Arrays.equals(serial0, serial) );
		
		ByteArrayInputStream in = new ByteArrayInputStream(serial) ;
		
		for (int i = 0; i < totalWrite; i++) {
			int n = SerializationUtils.readInt(in) ;
			Assert.assertEquals(i, n);
		}
		
		ByteArrayInputStream in2 = new ByteArrayInputStream(serial) ;
		
		int[] ns = SerializationUtils.readInts(totalWrite, in2) ;
		
		for (int i = 0; i < ns.length; i++) {
			int n = ns[i];
			Assert.assertEquals(i, n);
		}
		
		int[] ns2 = SerializationUtils.readInts(serial, 0, serial.length) ;
				
		Assert.assertTrue( Arrays.equals(ns, ns2) );
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream() ;
		SerializationUtils.writeIntsBlock(ns, out2);
		byte[] seria2 = out2.toByteArray() ;
		
		Assert.assertEquals(4 + ns.length*4, seria2.length) ;
	
		ByteArrayInputStream in3 = new ByteArrayInputStream(seria2) ;
		int[] ns3 = SerializationUtils.readIntsBlock(in3) ;
		
		Assert.assertTrue( Arrays.equals(ns, ns3) );
	}
	
	@Test
	public void testWriteReadFloat() throws IOException {
		int totalWrite = 1000 ;
		
		float[] ns0 = new float[totalWrite] ;
		for (int i = 0; i < totalWrite; i++) {
			ns0[i] = i ;
		}
		
		ByteArrayOutputStream out0 = new ByteArrayOutputStream() ;
		SerializationUtils.writeFloats(ns0, out0);
		byte[] serial0 = out0.toByteArray() ;
		
		Assert.assertEquals( totalWrite*4 , serial0.length );
		
		ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		
		for (int i = 0; i < totalWrite; i++) {
			SerializationUtils.writeFloat(i, out);
		}
		
		byte[] serial = out.toByteArray() ;
		
		Assert.assertEquals( totalWrite*4 , serial.length );
		
		Assert.assertTrue( Arrays.equals(serial0, serial) );
		
		ByteArrayInputStream in = new ByteArrayInputStream(serial) ;
		
		for (int i = 0; i < totalWrite; i++) {
			int n = (int) SerializationUtils.readFloat(in) ;
			Assert.assertEquals(i, n);
		}
		
		ByteArrayInputStream in2 = new ByteArrayInputStream(serial) ;
		
		float[] ns = SerializationUtils.readFloats(totalWrite, in2) ;
		
		for (int i = 0; i < ns.length; i++) {
			int n = (int) ns[i];
			Assert.assertEquals(i, n);
		}
		
		float[] ns2 = SerializationUtils.readFloats(serial, 0, serial.length) ;
				
		Assert.assertTrue( Arrays.equals(ns, ns2) );
		
		ByteArrayOutputStream out2 = new ByteArrayOutputStream() ;
		SerializationUtils.writeFloatsBlock(ns, out2);
		byte[] seria2 = out2.toByteArray() ;
		
		Assert.assertEquals(4 + ns.length*4, seria2.length) ;
	
		ByteArrayInputStream in3 = new ByteArrayInputStream(seria2) ;
		float[] ns3 = SerializationUtils.readFloatsBlock(in3) ;
		
		Assert.assertTrue( Arrays.equals(ns, ns3) );
		
		////////
		
		float[][] nns1 = new float[10][totalWrite/10] ;
		
		SerializationUtils.readFloats(serial, 0, serial.length, nns1) ;
		
		int nsCursor = 0 ;
		
		for (int i = 0; i < nns1.length; i++) {
			float[] fs = nns1[i] ;
			
			for (int j = 0; j < fs.length; j++) {
				float f = fs[j];
				float f0 = ns[nsCursor++] ;
				Assert.assertEquals(f0, f, 0);
			}
		}
		
		/////////
		
		float[][][] nnns1 = new float[10][10][totalWrite/100] ;
		
		SerializationUtils.readFloats(serial, 0, serial.length, nnns1) ;

		nsCursor = 0 ;
		
		for (int i = 0; i < nns1.length; i++) {
			float[][] ffs = nnns1[i] ;
			
			for (int j = 0; j < ffs.length; j++) {
				float[] fs = ffs[j];
				
				for (int k = 0; k < fs.length; k++) {
					float f = fs[k];
					float f0 = ns[nsCursor++] ;
					Assert.assertEquals(f0, f, 0);	
				}
			}
		}
		
	}
	
}

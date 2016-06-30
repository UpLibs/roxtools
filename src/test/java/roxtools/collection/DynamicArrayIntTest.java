package roxtools.collection;

import org.junit.Assert;
import org.junit.Test;

import roxtools.collection.DynamicArrayInt;

public class DynamicArrayIntTest {

	@Test
	public void testBasic1() {
		testBasic(3, 100);
	}
	
	@Test
	public void testBasic2() {
		testBasic(3, 1234);
	}
	
	@Test
	public void testBasic3() {
		testBasic(111, 1234);
	}
	
	public void testBasic(int blockSize, int totalValues) {
		
		final int valMult = 10 ;
		
		DynamicArrayInt a = new DynamicArrayInt(blockSize,blockSize) ;
		
		for (int i = 0; i < totalValues; i++) {
			a.addInt(i*valMult);
		}
		
		Assert.assertEquals( totalValues , a.size() );
		
		Assert.assertEquals( blockSize , a.getBlockSize() );
		
		int allocatedBlocks = (totalValues / blockSize)+1 ;
		
		Assert.assertEquals( allocatedBlocks , a.getAllocatedBlocks() );
		
		for (int i = 0; i < 100; i++) {
			int v = a.getInt(i) ;
			
			Assert.assertTrue( v == i*valMult );	
		}
		
		a.resizeBlockSize( blockSize*2 );
		
		for (int i = 0; i < 100; i++) {
			int v = a.getInt(i) ;
			
			Assert.assertTrue( v == i*valMult );	
		}
		
		a.resizeBlockSize( blockSize );
		
		for (int i = 0; i < 100; i++) {
			int v = a.getInt(i) ;
			
			Assert.assertTrue( v == i*valMult );	
		}

		Assert.assertEquals( totalValues , a.size() );
		
		int rmIdx = blockSize/2 ;
		
		for (int i = 0; i < 10; i++) {
			a.remove(rmIdx);
			
			Assert.assertEquals( totalValues-(i+1) , a.size() );
			
			int v = a.getInt(rmIdx) ;
			
			int vExpected = ((rmIdx+i)+1)*valMult ;
			
			Assert.assertEquals( vExpected , v );	
		}

		Assert.assertEquals( totalValues-10 , a.size() );
		
	}
	
}

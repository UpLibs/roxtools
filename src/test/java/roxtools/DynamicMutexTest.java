package roxtools;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import roxtools.DynamicMutexHandler.DynamicMutex;

public class DynamicMutexTest {

	@Test
	public void testBasic() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		DynamicMutex m2 = dynamicMutexHandler.getMutex("2") ;
		
		Assert.assertTrue( m1.lock() );
		Assert.assertTrue( m1.unlock() ) ;
		
		Assert.assertTrue( m2.lock() );
		Assert.assertTrue( m2.unlock() );
		
		Assert.assertTrue( m1.lock() );
		  Assert.assertTrue( m2.lock() );
		  Assert.assertTrue( m2.unlock() );
		Assert.assertTrue( m1.unlock() );
		
	}
	
	@Test
	public void testMultiBasic() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		DynamicMutex m12 = dynamicMutexHandler.getMultiMutex("1","2") ;
		
		Assert.assertTrue( m12.lock() );
		
		Assert.assertTrue( m12.unlock() );
		
		
	}
	
	
	@Test
	public void testLockReentrant() {

		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		
		Assert.assertTrue( m1.lock() ) ;
		Assert.assertFalse( m1.lock() );
		
		Assert.assertTrue( m1.unlock() ) ;
		
		Assert.assertTrue( m1.isSomeThreadLocking() ) ;
		Assert.assertTrue( m1.isCurrentThreadLocking() ) ;
		
		Assert.assertTrue( m1.unlock() ) ;
		
		Assert.assertFalse( m1.isSomeThreadLocking() ) ;
		Assert.assertFalse( m1.isCurrentThreadLocking() ) ;
		
	}
	
	@Test
	public void testNoLock() {

		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		
		Assert.assertFalse( m1.unlock() ) ;
		
	}
	
	static private class Counter {
		volatile private int count ;
		
		public void increment() {
			count = count + 1 ;
		}
		
		public int get() {
			return count ;
		}
		
	}
	
	@Test
	public void testMultiThread() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		final DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		
		final Counter counter = new Counter() ;
		
		final int totalThreads = 10 ;
		final int incrementsPerThread = 200000 ;
		
		ArrayList<Thread> threads = new ArrayList<>() ;
		
		for (int i = 0; i < totalThreads; i++) {
			
			Thread thread = new Thread() {
				public void run() {
					for (int j = incrementsPerThread-1; j >= 0; j--) {
						m1.lock() ;
						counter.increment();
						m1.unlock();
					}
				};
			};
			
			threads.add(thread);
			
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		int correctCount = totalThreads * incrementsPerThread ;
		
		Assert.assertEquals( correctCount , counter.get() );
		
	}

	@Test
	public void testMultiMutex() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		DynamicMutex m2 = dynamicMutexHandler.getMutex("2") ;
		
		DynamicMutex mm12 = dynamicMutexHandler.getMultiMutex("1","2") ;
		
		m1.lock() ;
		
		mm12.lock() ;
		
		Assert.assertTrue( m1.isCurrentThreadLocking() );
		Assert.assertTrue( mm12.isCurrentThreadLocking() );
		Assert.assertTrue( m2.isCurrentThreadLocking() );
		
		mm12.unlock() ;
		
		m1.unlock() ;
		
		Assert.assertFalse( m1.isCurrentThreadLocking() );
		Assert.assertFalse( mm12.isCurrentThreadLocking() );
		Assert.assertFalse( m2.isCurrentThreadLocking() );
		
	}
	
	@Test
	public void testMultiMutexThreads() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler() ;
		
		final DynamicMutex m1 = dynamicMutexHandler.getMutex("1") ;
		final DynamicMutex m2 = dynamicMutexHandler.getMutex("2") ;
		
		final DynamicMutex mm12 = dynamicMutexHandler.getMultiMutex("1","2") ;

		StringBuilder strCheck1 = new StringBuilder() ;
		StringBuilder strCheck2 = new StringBuilder() ;
		
		for (int i = 0; i < 1000; i++) {
			strCheck1.append("12:"+i+"\n") ;
			strCheck2.append("12:"+i+"\n") ;
		}
		for (int i = 0; i < 1000; i++) {
			strCheck1.append("1:"+i+"\n") ;
		}
		for (int i = 0; i < 1000; i++) {
			strCheck2.append("2:"+i+"\n") ;
		}
		
		final StringBuilder str1 = new StringBuilder() ;
		final StringBuilder str2 = new StringBuilder() ;
		
		Thread thread1 = new Thread() {
			public void run() {
				mm12.lock();
				
				mm12.setPhase(1) ;
				
				for (int i = 0; i < 1000; i++) {
					str1.append("12:"+i+"\n") ;
					str2.append("12:"+i+"\n") ;
				}
				mm12.unlock();
			};
		};
		
		Thread thread2 = new Thread() {
			public void run() {
				m1.waitPhase(1);
				
				m1.lock();
				for (int i = 0; i < 1000; i++) {
					str1.append("1:"+i+"\n") ;
				}
				m1.unlock();
			};
		};
		
		Thread thread3 = new Thread() {
			public void run() {
				m2.waitPhase(1);
				
				m2.lock();
				for (int i = 0; i < 1000; i++) {
					str2.append("2:"+i+"\n") ;
				}
				m2.unlock();
			};
		};
		
		mm12.lock();
		
		thread1.start();
		thread2.start();
		thread3.start();
		
		mm12.unlock();
		
		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		Assert.assertEquals( strCheck1.toString() , str1.toString() );;
		Assert.assertEquals( strCheck2.toString() , str2.toString() );;
	}
	
}

package roxtools;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import roxtools.DynamicMutexHandler.DynamicMutex;
import roxtools.DynamicMutexHandler.DynamicMutexCachedResult;

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
		final int incrementsPerThread = 300000 ;
		
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
	
	@Test
	public void testUniqueIDs() {
		
		Assert.assertArrayEquals( new String[] {"1","2","3","4"} , DynamicMutexHandler.uniqueIDs( new String[] {"1","2","3","4"} ) );
		
		Assert.assertArrayEquals( new String[] {"1","2","3","4","5"} , DynamicMutexHandler.uniqueIDs( new String[] {"1","2","3","4","2","2","2","5"} ) );
		
		Assert.assertArrayEquals( new String[] {"1","2","3","4","5"} , DynamicMutexHandler.uniqueIDs( new String[] {"1","2","3","4","1","2","2","5"} ) );
		
		Assert.assertArrayEquals( new String[] {"1","2","3","4","5"} , DynamicMutexHandler.uniqueIDs( new String[] {"1","2","3","4","1","2","2","5","5"} ) );
		
		Assert.assertArrayEquals( new String[] {"1","2","3","4"} , DynamicMutexHandler.uniqueIDs( new String[] {"1","2","3","4","4"} ) );
		
	}
	
	@Test
	public void testMutexUniqueIDs() {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler();
		
		DynamicMutex mm1 = dynamicMutexHandler.getMultiMutex("1","2","3") ;
		DynamicMutex mm1_2 = dynamicMutexHandler.getMultiMutex("1","2","3","1") ;
		Assert.assertTrue( mm1 == mm1_2 );
		
		DynamicMutex m1 = dynamicMutexHandler.getMultiMutex("1") ;
		DynamicMutex m1_2 = dynamicMutexHandler.getMutex("1") ;
		Assert.assertTrue( m1 == m1_2 );
		
		DynamicMutex m2 = dynamicMutexHandler.getMultiMutex( (String[])null ) ;
		DynamicMutex m2_1 = dynamicMutexHandler.getMultiMutex( (String)null ) ;
		DynamicMutex m2_2 = dynamicMutexHandler.getMutex(null) ;
		DynamicMutex m2_3 = dynamicMutexHandler.getMultiMutex("") ;
		DynamicMutex m2_4 = dynamicMutexHandler.getMutex("") ;
		Assert.assertTrue( m2 == m2_1 );
		Assert.assertTrue( m2 == m2_2 );
		Assert.assertTrue( m2 == m2_3 );
		Assert.assertTrue( m2 == m2_4 );
		
	}

	static private class ThreadRun_testCacheResultMutex implements Runnable {

		final private DynamicMutexCachedResult mutex ;
		final private long timeout ;
		
		public ThreadRun_testCacheResultMutex(DynamicMutexCachedResult mutex, long timeout) {
			this.mutex = mutex;
			this.timeout = timeout;
		}

		volatile private Object result = null ; 

		public Object getResult() {
			return result;
		}
		
		public void setResult(Object result) {
			this.result = result;
		}
		
		@Override
		public void run() {
			Object res = timeout > 0 ? mutex.lockWithResult(timeout) : mutex.lockWithResult() ;
			
			if (res == null) {
				res = Thread.currentThread().getId() ;
				mutex.setResult(res);
				

				if (timeout == 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}	
				}
			}
			
			setResult(res);
			
			mutex.unlock();
		}
		
	}
	
	@Test
	public void testCacheResultMutex() {
		testCacheResultMutexImplem(0);
	}
	
	@Test
	public void testCacheResultMutexResultTimeout() {
		testCacheResultMutexImplem(1000);
	}
	
	private void testCacheResultMutexImplem(long resultTimeout) {
		
		DynamicMutexHandler dynamicMutexHandler = new DynamicMutexHandler();
		
		final DynamicMutexCachedResult m1 = dynamicMutexHandler.getCachedResultMutex("1") ;
		
		ThreadRun_testCacheResultMutex threadrun1 = new ThreadRun_testCacheResultMutex(m1,resultTimeout) ;
		ThreadRun_testCacheResultMutex threadrun2 = new ThreadRun_testCacheResultMutex(m1,resultTimeout) ;
		
		Thread th1 = new Thread(threadrun1) ;
		Thread th2 = new Thread(threadrun2) ;
		
		th1.start();
		th2.start();
		
		try {
			th1.join();
			th2.join();
		}
		catch (InterruptedException e) {
		
		}
	
		Assert.assertNotNull( threadrun1.getResult() );
		Assert.assertNotNull( threadrun2.getResult() );
		
		Assert.assertEquals( threadrun1.getResult() , threadrun2.getResult() );
	}
	
}

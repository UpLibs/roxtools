package roxtools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import roxtools.DynamicMutexHandler.DynamicMutexCachedResult;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class DynamicMutexTest {

    @Test
    void lockAndUnlockTwoMutexFromSameHandler() {
        var handler = new DynamicMutexHandler();

        var mutex1 = handler.getMutex("1");
        var mutex2 = handler.getMutex("2");

        assertTrue(mutex1.lock(), "Mutex 1 should have locked");
        assertTrue(mutex1.unlock(), "Mutex 1 should have unlocked");

        assertTrue(mutex2.lock(), "Mutex 2 should have locked");
        assertTrue(mutex2.unlock(), "Mutex 2 should have unlocked");

        assertTrue(mutex1.lock(), "Mutex 1 should have locked");
        assertTrue(mutex2.lock(), "Mutex 2 should have locked");

        assertTrue(mutex2.unlock(), "Mutex 2 should have unlocked");
        assertTrue(mutex1.unlock(), "Mutex 1 should have unlocked");

    }

    @Test
    void lockAndUnlockMultiMutex() {
        var handler = new DynamicMutexHandler();
        var multiMutex = handler.getMultiMutex("1", "2");

        assertTrue(multiMutex.lock(), "MultiMutex should have locked");
        assertTrue(multiMutex.unlock(), "MultiMutex should have locked");
    }


    @Test
    void testLockReentrant() {
        var handler = new DynamicMutexHandler();

        var mutex = handler.getMutex("1");

        assertTrue(mutex.lock(), "Mutex should have locked, lock count should be 1");
        assertFalse(mutex.lock(), "Current thread was already locked, lock count should be 2");

        assertTrue(mutex.unlock(), "Lock count should be 1, current thread remains locked");

        assertTrue(mutex.isSomeThreadLocking(), "Since lock count is 1, some thread should be locking");
        assertTrue(mutex.isCurrentThreadLocking(), "Since lock occurred on this thread and count is 1, this thread should be locking");

        assertTrue(mutex.unlock(), "Lock count should be 0, current thread is completely unlocked");

        assertFalse(mutex.isSomeThreadLocking(), "No thread should be still locked");
        assertFalse(mutex.isCurrentThreadLocking(), "Current thread shouldn't be locked");
    }

    @Test
    void testNoLock() {
        var handler = new DynamicMutexHandler();
        var mutex = handler.getMutex("1");
        assertFalse(mutex.unlock(), "Mutex was never locked and therefore shouldn't be able to unlock");
    }

    static private class Counter {
        volatile private int count;

        void increment() {
            count = count + 1;
        }

        public int get() {
            return count;
        }

    }

    @Test
    void testMultiThread() {
        var totalThreads = 10;
        var incrementsPerThread = 300000;
        var counter = new Counter();
        var threads = new ArrayList<Thread>();
        var handler = new DynamicMutexHandler();

        var mutex1 = handler.getMutex("1");

        for (var i = 0 ; i < totalThreads ; i++) {
            var thread = new Thread(() -> {
                for (int j = incrementsPerThread - 1 ; j >= 0 ; j--) {
                    mutex1.lock();
                    counter.increment();
                    mutex1.unlock();
                }
            });

            threads.add(thread);
            thread.start();
        }

        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        var expected = totalThreads * incrementsPerThread;
        assertEquals(expected, counter.get(), "Number of increments from multiple threads doesn't match expected value");
    }

    @Test
    void testMultiMutex() {
        var handler = new DynamicMutexHandler();

        var mutex1 = handler.getMutex("1");
        var mutex2 = handler.getMutex("2");
        var multiMutex12 = handler.getMultiMutex("1", "2");

        assertAll(
                () -> assertTrue(mutex1.lock(), "Mutex 1 should've locked current thread"),
                () -> assertFalse(multiMutex12.lock(), "Current thread was already locked by mutex 1, but lock count should be increased")
        );

        assertAll(
                () -> assertTrue(mutex1.isCurrentThreadLocking(), "Current thread should be locked for Mutex 1"),
                () -> assertTrue(multiMutex12.isCurrentThreadLocking(), "Current thread should be locked for MultiMutex 12"),
                () -> assertTrue(mutex2.isCurrentThreadLocking(), "Current thread should be locked for Mutex 2 due to lock from MultiMutex 12")
        );

        assertAll(
                () -> assertTrue(mutex1.unlock(), "Mutex 1 should've decremented current thread lock count"),
                () -> assertTrue(multiMutex12.unlock(), "MultiMutex 12 should've unlocked current thread")
        );

        assertAll(
                () -> assertFalse(mutex1.isCurrentThreadLocking(), "Current thread should be unlocked for Mutex 1"),
                () -> assertFalse(multiMutex12.isCurrentThreadLocking(), "Current thread should be unlocked for MultiMutex 12"),
                () -> assertFalse(mutex2.isCurrentThreadLocking(), "Current thread should be unlocked for Mutex 2 due to unlock from MultiMutex 12")
        );
    }

    @Test
    void testMultiMutexThreads() {
        var handler = new DynamicMutexHandler();

        final var mutex1 = handler.getMutex("1");
        final var mutex2 = handler.getMutex("2");

        final var multiMutex12 = handler.getMultiMutex("1", "2");

        var expected1 = new StringBuilder();
        var expected2 = new StringBuilder();

        var iterations = 1000;
        for (var i = 0 ; i < iterations ; i++) {
            expected1.append("12:").append(i).append("\n");
            expected2.append("12:").append(i).append("\n");
        }
        for (var i = 0 ; i < iterations ; i++) {
            expected1.append("1:").append(i).append("\n");
        }
        for (var i = 0 ; i < iterations ; i++) {
            expected2.append("2:").append(i).append("\n");
        }

        final var output1 = new StringBuilder();
        final var output2 = new StringBuilder();

        var thread1 = new Thread(() -> {
            multiMutex12.lock();

            multiMutex12.setPhase(1);

            for (var i = 0 ; i < iterations ; i++) {
                output1.append("12:").append(i).append("\n");
                output2.append("12:").append(i).append("\n");
            }
            multiMutex12.unlock();
        });

        var thread2 = new Thread(() -> {
            mutex1.waitPhase(1);

            mutex1.lock();
            for (var i = 0 ; i < iterations ; i++) {
                output1.append("1:").append(i).append("\n");
            }
            mutex1.unlock();
        });

        var thread3 = new Thread(() -> {
            mutex2.waitPhase(1);

            mutex2.lock();
            for (var i = 0 ; i < iterations ; i++) {
                output2.append("2:").append(i).append("\n");
            }
            mutex2.unlock();
        });

        multiMutex12.lock();

        thread1.start();
        thread2.start();
        thread3.start();

        multiMutex12.unlock();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertAll(
                () -> assertEquals(expected1.toString(), output1.toString(), "Output for mutexes with Id 1 doesn't match expected value"),
                () -> assertEquals(expected2.toString(), output2.toString(), "Output for mutexes with Id 2 doesn't match expected value")
        );

    }

    static Stream<Arguments> uniqueIDs() {
        return Stream.of(
                arguments(new String[] {"1", "2", "3", "4"}, new String[] {"1", "2", "3", "4"}),
                arguments(new String[] {"1", "2", "3", "4", "5"}, new String[] {"1", "2", "3", "4", "2", "2", "2", "5"}),
                arguments(new String[] {"1", "2", "3", "4", "5"}, new String[] {"1", "2", "3", "4", "1", "2", "2", "5"}),
                arguments(new String[] {"1", "2", "3", "4", "5"}, new String[] {"1", "2", "3", "4", "1", "2", "2", "5", "5"}),
                arguments(new String[] {"1", "2", "3", "4"}, new String[] {"1", "2", "3", "4", "4"})
        );
    }

    @MethodSource
    @ParameterizedTest
    void uniqueIDs(String[] expected, String[] inputs) {
        assertArrayEquals(expected, DynamicMutexHandler.uniqueIDs(inputs));
    }

    @Test
    void testMutexUniqueIDs() {
        var handler = new DynamicMutexHandler();

        var multiMutexFromNullStringArray = handler.getMultiMutex((String[]) null);

        assertAll(
                () -> assertSame(handler.getMultiMutex("1", "2", "3"), handler.getMultiMutex("1", "2", "3", "1"),
                        "Getting MultiMutexes with the same uniqueIDs should retrieve the same instance"),
                () -> assertSame(handler.getMultiMutex("1"), handler.getMutex("1"),
                        "Getting MultiMutex and single Mutex with the same id should retrieve the same instance"),
                () -> assertSame(multiMutexFromNullStringArray, handler.getMultiMutex((String) null),
                        "Getting MultiMutexes from null id and null array of ids should retrieve the same instance"),
                () -> assertSame(multiMutexFromNullStringArray, handler.getMutex(null),
                        "Getting MultiMutex and Mutex from null id and null array of ids should retrieve the same instance"),
                () -> assertSame(multiMutexFromNullStringArray, handler.getMultiMutex(""),
                        "Getting MultiMutexes from empty id and null array of ids should retrieve the same instance"),
                () -> assertSame(multiMutexFromNullStringArray, handler.getMutex(""),
                        "Getting MultiMutex and Mutex from empty id and null array of ids should retrieve the same instance")
        );
    }

    static private class ThreadRun_testCacheResultMutex implements Runnable {

        final private DynamicMutexCachedResult mutex;
        final private long timeout;

        public ThreadRun_testCacheResultMutex(DynamicMutexCachedResult mutex, long timeout) {
            this.mutex = mutex;
            this.timeout = timeout;
        }

        volatile private Object result = null;

        public Object getResult() {
            return result;
        }

        void setResult(Object result) {
            this.result = result;
        }

        @Override
        public void run() {
            var res = timeout > 0 ? mutex.lockWithResult(timeout) : mutex.lockWithResult();

            if (res == null) {
                res = Thread.currentThread().getId();
                mutex.setResult(res);

                if (timeout == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            setResult(res);
            mutex.unlock();
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1000})
    void testCacheResultMutex(long resultTimeout) {
        var handler = new DynamicMutexHandler();

        final DynamicMutexCachedResult m1 = handler.getCachedResultMutex("1");

        var run1 = new ThreadRun_testCacheResultMutex(m1, resultTimeout);
        var run2 = new ThreadRun_testCacheResultMutex(m1, resultTimeout);

        var thread1 = new Thread(run1);
        var thread2 = new Thread(run2);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {

        }

        assertAll(
                () -> assertNotNull(run1.getResult(), "Result from first thread shouldn't be null"),
                () -> assertNotNull(run2.getResult(), "Result from second thread shouldn't be null"),
                () -> assertEquals(run1.getResult(), run2.getResult(), "Results from both threads should be equal")
        );

    }

}

package roxtools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CallSemaphore {

    private static final Logger log = LoggerFactory.getLogger(CallSemaphore.class);

    private static final AtomicInteger INSTANCE_ID_COUNTER = new AtomicInteger(0);

    private final int instanceID = INSTANCE_ID_COUNTER.incrementAndGet();

    private final String id;

    private final Semaphore semaphore;

    private final long acquireTimeout;

    public CallSemaphore(String id, int maxSimultaneousCalls) {
        this(id, maxSimultaneousCalls, TimeUnit.SECONDS.toMillis(30));
    }

    public CallSemaphore(String id, int maxSimultaneousCalls, long acquireTimeout) {
        if (id == null || id.isEmpty()) id = "#" + this.instanceID;

        if (maxSimultaneousCalls < 1) maxSimultaneousCalls = 1;
        if (acquireTimeout < 1) acquireTimeout = 1;

        this.id = id;
        this.semaphore = new Semaphore(maxSimultaneousCalls, true);
        this.acquireTimeout = acquireTimeout;
    }

    private Consumer<CallResult<?>> callback;

    public int getInstanceID() {
        return instanceID;
    }

    public String getId() {
        return id;
    }

    public long getAcquireTimeout() {
        return acquireTimeout;
    }

    public Consumer<CallResult<?>> getCallback() {
        return callback;
    }

    public CallSemaphore setCallback(Consumer<CallResult<?>> callback) {
        this.callback = callback;
        return this;
    }

    public <R> R call(Callable<R> callable) {
        try {
            return callImpl(callable);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("ControllerSemaphore[" + id + "]: Can't make call: " + callable, e);
        }
    }

    public <R> R callWithExceptions(Callable<R> callable) throws Exception {
        return callImpl(callable);
    }

    public <R> R callImpl(Callable<R> callable) throws Exception {
        var callResult = doCall(callable);
        if (callResult.timeout) {
            throw new TimeoutException("ControllerSemaphore[" + id + "]: Timeout trying to acquire semaphore.");
        }
        else if (callResult.error != null) {
            throw callResult.error;
        }
        return callResult.result;
    }

    public static class CallResult<R> {
        public final CallSemaphore callSemaphore;
        public final Callable<R> call;
        public final R result;
        public final boolean timeout;
        public final long acquireSemaphoreTime;
        public final long callTime;
        public final long totalTime;
        public final Exception error;

        public CallResult(CallSemaphore callSemaphore, Callable<R> call, R result, boolean timeout, long acquireSemaphoreTime, long callTime, long totalTime, Exception error) {
            this.callSemaphore = callSemaphore;
            this.call = call;
            this.result = result;
            this.timeout = timeout;
            this.acquireSemaphoreTime = acquireSemaphoreTime;
            this.callTime = callTime;
            this.totalTime = totalTime;
            this.error = error;
        }
    }

    public <R> CallResult<R> doCall(Callable<R> callable) throws Exception {
        var init = System.currentTimeMillis();

        var acquired = semaphore.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS);

        var callInit = System.currentTimeMillis();
        var acquiredTime = callInit - init;

        if (acquired) {
            try {
                var result = callable.call();
                var now = System.currentTimeMillis();
                var callTime = now - callInit;
                var totalTime = now - init;

                var callResult = new CallResult<>(this, callable, result, false, acquiredTime, callTime, totalTime, null);
                notifyCallback(callResult);

                return callResult;
            } catch (Exception e) {
                var now = System.currentTimeMillis();
                var callTime = now - callInit;
                var totalTime = now - init;

                var callResult = new CallResult<>(this, callable, null, false, acquiredTime, callTime, totalTime, e);
                notifyCallback(callResult);

                return callResult;
            } finally {
                semaphore.release();
            }
        }
        else {
            return new CallResult<>(this, callable, null, true, acquiredTime, 0, acquiredTime, null);

        }
    }

    private <R> void notifyCallback(CallResult<R> callResult) {
        if (callback != null) {
            try {
                callback.accept(callResult);
            } catch (Throwable e) {
                log.error("An exception was thrown while notifying callback of semaphore [" + callResult.callSemaphore.id + "] with result: " + callResult.result, e);
            }
        }
        else {
            log.debug("Can't notify since callback hasn't been set");
        }
    }

}

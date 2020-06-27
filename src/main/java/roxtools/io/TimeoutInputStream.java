package roxtools.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements an {@link InputStream} with timeout for reading operations.
 */
public class TimeoutInputStream extends InputStream {

    final private InputStream input;
    final private long readTimeout;

    public TimeoutInputStream(InputStream input, long readTimeout) {
        this.input = input;
        this.readTimeout = readTimeout > 0 ? readTimeout : 1 ;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    /**
     * @return true if the consumption thread is started.
     */
    public boolean isStarted() {
        return started != null ;
    }

    /**
     * @return true if the consumption thread is stopped.
     */
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public int available() throws IOException {
        synchronized (consumeMUTEX) {
            return bufferSize-bufferOffset;
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (consumeMUTEX) {
            int available = ensureAvailableBytes(1) ;
            if (available <= 0) return -1 ;

            int b = buffer[bufferOffset++] & 0xFF;

            if (bufferSize-bufferOffset == 0) {
                bufferOffset = 0 ;
                bufferSize = 0 ;
            }

            return b ;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len <= 0) return 0 ;

        synchronized (consumeMUTEX) {
            int available = ensureAvailableBytes(len) ;
            if (available < 0) return -1 ;

            System.arraycopy(buffer, bufferOffset, b, off, available);
            bufferOffset += available ;

            if (bufferSize-bufferOffset == 0) {
                bufferOffset = 0 ;
                bufferSize = 0 ;
            }

            return available ;
        }
    }

    private int ensureAvailableBytes(int len) throws IOException {
        synchronized (consumeMUTEX) {
            ensureStarted();

            int available = bufferSize - bufferOffset;
            assert (available >= 0);

            if (available == 0) {
                if (consumeError != null) throw consumeError;

                bufferOffset = 0;
                bufferSize = 0;

                requestedBytes += len;
                consumeMUTEX.notifyAll();

                long init = System.currentTimeMillis();
                while (!stopped && bufferSize - bufferOffset == 0) {
                    long elapsedTime = System.currentTimeMillis() - init;
                    long remainingTime = readTimeout - elapsedTime;
                    if (remainingTime <= 0) break;
                    try {
                        consumeMUTEX.wait(remainingTime);
                    } catch (InterruptedException ignore) {
                    }
                }

                available = bufferSize - bufferOffset;

                if (available == 0) {
                    stop();
                    return -1;
                }
            } else if (available < len) {
                requestedBytes += len;
                consumeMUTEX.notifyAll();
            }

            return available;
        }
    }

    private volatile Thread started ;

    private void ensureStarted() {
        if (started != null) return ;
        started = new Thread(this::consumer, "TimeoutInputStream["+ input +"]");
        started.start();
    }

    private volatile boolean stopped = false ;

    private volatile int requestedBytes = 0 ;

    private final Object consumeMUTEX = new Object();

    private byte[] buffer = new byte[1024*4] ;
    private int bufferOffset = 0 ;
    private int bufferSize = 0 ;

    private volatile IOException consumeError ;

    private void ensureBufferCapacity(int extraSize) {
        int freeCapacity = buffer.length - bufferSize ;
        if (freeCapacity >= extraSize) return ;

        int bufferRealSize = bufferSize - bufferOffset;
        int neededSize = bufferRealSize + extraSize;

        if (neededSize <= buffer.length) {
            System.arraycopy( buffer , bufferOffset, buffer, 0, bufferRealSize );
        }
        else {
            byte[] buffer2 = new byte[neededSize] ;
            System.arraycopy( buffer , bufferOffset, buffer2, 0, bufferRealSize );
            buffer = buffer2 ;
        }
    }

    private void consumer() {

        while (true) {
            int request ;
            synchronized (consumeMUTEX) {
                if (stopped) break ;
                if (requestedBytes == 0) {
                    try {
                        consumeMUTEX.wait(1000);
                    } catch (InterruptedException ignore) { }
                }
                request = requestedBytes ;
            }

            if (request == 1) {
                try {
                    ensureBufferCapacity(1);

                    int b = input.read() ;

                    if (b < 0) {
                        consumeError = new EOFException() ;
                        break;
                    }
                    else {
                        buffer[bufferSize++] = (byte) b;
                        synchronized (consumeMUTEX) {
                            --requestedBytes;
                            consumeMUTEX.notifyAll();
                        }
                    }
                }
                catch (IOException e) {
                    consumeError = e ;
                    break;
                }
            }
            else if (request > 1) {
                try {
                    ensureBufferCapacity(request);

                    while (request > 0) {
                        int r = input.read(buffer, bufferSize, request) ;

                        if (r < 0) {
                            consumeError = new EOFException() ;
                            break;
                        }
                        else if (r > 0) {
                            bufferSize += r ;
                            request -= r ;
                            synchronized (consumeMUTEX) {
                                requestedBytes -= r;
                                consumeMUTEX.notifyAll();
                            }
                        }
                    }
                }
                catch (IOException e) {
                    consumeError = e ;
                    break;
                }
            }
        }
    }

    /**
     * Stops the consumption thread of input.
     */
    public void stop() {
        synchronized (consumeMUTEX) {
            stopped = true ;
            consumeMUTEX.notifyAll();
        }
    }

    /**
     * Stops consumption thread and closes input.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        stop();
        input.close();
    }

}

package roxtools.io;

import java.io.IOException;

public interface SeekableInput extends SeekableIO {
	public int read() throws IOException ;
	public int read(byte[] b, int off, int len) throws IOException ;
	public int read(byte[] b) throws IOException ;
}
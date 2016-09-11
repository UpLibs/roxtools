package roxtools.io;

import java.io.IOException;

public interface SeekableOutput extends SeekableIO {
	public void setLength(long length) throws IOException ;
	public void write(int b) throws IOException ;
	public void write(byte[] b, int off, int len) throws IOException ;
	public void write(byte[] b) throws IOException ;
}
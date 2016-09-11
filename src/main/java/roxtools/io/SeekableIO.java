package roxtools.io;

import java.io.IOException;

public interface SeekableIO {
	public void seek(long pos) throws IOException ;
	public long position() throws IOException ;
	public long length() throws IOException ;
}
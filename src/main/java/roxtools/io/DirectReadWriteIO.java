package roxtools.io;

import java.io.IOException;

public interface DirectReadWriteIO extends SeekableInputOutput {

	public int read(long pos) throws IOException;
	public int read(long pos, byte[] buffer) throws IOException;
	public int read(long pos, byte[] buffer, int offset, int length) throws IOException;

	public void write(long pos, int b) throws IOException;
	public int write(long pos, byte[] buffer) throws IOException;
	public int write(long pos, byte[] buffer, int offset, int length) throws IOException;
	
	public void dispose() ;
	
}

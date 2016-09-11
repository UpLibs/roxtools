package roxtools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

final class RandomAccessInput extends InputStream implements SeekableInput {
	final private RandomAccessFile inOut ;

	public RandomAccessInput(RandomAccessFile inOut) {
		this.inOut = inOut;
	}

	public void seek(long pos) throws IOException {
		inOut.seek(pos);
	}
	
	@Override
	public long position() throws IOException {
		return inOut.getFilePointer() ;
	}
	
	@Override
	public long length() throws IOException {
		return inOut.length();
	}
	
	@Override
	public int read() throws IOException {
		return inOut.read() ;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return inOut.read(b, off, len) ;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return inOut.read(b, 0, b.length) ;
	}
}
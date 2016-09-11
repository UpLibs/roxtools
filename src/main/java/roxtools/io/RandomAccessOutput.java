package roxtools.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

final class RandomAccessOutput extends OutputStream implements SeekableOutput {
	final private RandomAccessFile inOut ;

	public RandomAccessOutput(RandomAccessFile inOut) {
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
	public void write(int b) throws IOException {
		inOut.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		inOut.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		inOut.write(b, 0, b.length);
	}
	
}
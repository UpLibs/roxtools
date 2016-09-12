package roxtools.io;

import java.io.IOException;
import java.io.RandomAccessFile;

final public class RandomAccessInputOutput implements DirectReadWriteIO {

	private RandomAccessFile inOut ;

	public RandomAccessInputOutput(RandomAccessFile inOut) {
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


	@Override
	public long length() throws IOException {
		return inOut.length();
	}
	
	@Override
	public void setLength(long length) throws IOException {
		inOut.setLength(length);
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


	@Override
	public void flush() throws IOException {
		
	}


	@Override
	public int read(long pos) throws IOException {
		seek(pos);
		return inOut.read() ;
	}


	@Override
	public int read(long pos, byte[] buffer) throws IOException {
		seek(pos);
		return inOut.read(buffer) ;
	}


	@Override
	public int read(long pos, byte[] buffer, int offset, int length) throws IOException {
		seek(pos);
		return inOut.read(buffer, offset, length);
	}


	@Override
	public void write(long pos, int b) throws IOException {
		seek(pos);
		inOut.write(b);
	}


	@Override
	public int write(long pos, byte[] buffer) throws IOException {
		seek(pos);
		inOut.write(buffer);
		return buffer.length ;
	}


	@Override
	public int write(long pos, byte[] buffer, int offset, int length) throws IOException {
		seek(pos);
		inOut.write(buffer, offset, length);
		return length ;
	}


	@Override
	public void dispose() {
		
	}

}

package roxtools.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayInputOutput implements SeekableInput , SeekableOutput {

	private byte[] data ;
	private int size ;
	
	public ByteArrayInputOutput() {
		this(128) ;
	}
	
	public ByteArrayInputOutput(int capacity) {
		this.data = new byte[capacity];
		this.size = 0 ;
	}
	
	public ByteArrayInputOutput(byte[] data) {
		this(data, data.length) ;
	}
	
	public ByteArrayInputOutput(byte[] data, int size) {
		this.data = data;
		this.size = size ;
	}
	
	public void reset() {
		this.size = 0;
		this.pos = 0;
	}
	
	public int capacity() {
		return data.length ;
	}
	
	@Override
	public void setLength(long length) {
		int size = (int) length ;
		if (size < 0) throw new IllegalArgumentException("Negative size: "+ size) ;
		
		this.size = size ;
		
		ensureCapacity(size);
		
		if ( size < data.length/2 ) {
			int desiredCapacity = ((size/1024)+1)*1024 ;
			
			byte[] data2 = new byte[desiredCapacity] ;
			
			int lng = Math.min( desiredCapacity ,data.length) ;
			System.arraycopy(data, 0, data2, 0, lng);
			
			this.data = data2 ;
		}
	}
	
	@Override
	public long length() {
		return size;
	}
	
	private int pos = 0 ;
	
	@Override
	public long position() {
		return pos ;
	}

	@Override
	public void seek(long pos) throws IOException {
		this.pos = (int) pos ;
	}
	
	private void ensureCapacity(int capacity) {
		if (capacity > this.data.length) {
			int increase = this.data.length ;
			if (increase > 1024*1024) increase = 1024*1024 ;
			
			int newCapacity = this.data.length + increase ;
			byte[] data2 = new byte[newCapacity] ;
			System.arraycopy(this.data, 0, data2, 0, this.data.length);
			
			this.data = data2 ;
		}
	}

	@Override
	public void write(int b) throws IOException {
		ensureCapacity(pos+1);
		data[pos] = (byte) b ;
		pos++ ;
		if (pos > size) size = pos ;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ensureCapacity(pos+len);
		
		for (int i = 0; i < len; i++) {
			data[pos++] = b[off+i] ;
		}
		
		if (pos > size) size = pos ;
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public int read() throws IOException {
		if ( pos >= size ) throw new EOFException() ;
		return data[pos++] ;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (pos >= size && len > 0) return -1 ;
		
		int read = 0 ;
		
		while (len > 0) {
			b[off++] = data[pos++] ;
			read++ ;
			
			if (pos >= size) break ;
			len--;
		}
		
		return read ;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length) ;
	}
	
	public byte[] toByteArray() {
		return toByteArray(0, size);
	}
	
	public byte[] toByteArray(int offset, int length) {
		byte[] a = new byte[length] ;
		System.arraycopy(data, offset, a, 0, length);
		return a ;
	}
	
	public void copyTo(int offset, int length, byte[] dest, int destOffset) {
		System.arraycopy(data, offset, dest, destOffset, length);
	}
	
	public void writeTo(OutputStream out) throws IOException {
		writeTo(0, size, out);
	}
	
	public void writeTo(int offset, int length, OutputStream out) throws IOException {
		out.write(data, offset, length);
	}
	
	@Override
	public String toString() {
		return getClass().getName()+"["+position()+"/"+length()+"/"+capacity()+"]";
	}
	
}
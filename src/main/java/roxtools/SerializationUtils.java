package roxtools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

final public class SerializationUtils {

	static final public Charset CHARSET_LATIN1 = Charset.forName("ISO-8859-1") ;
	static final public Charset CHARSET_UTF8 = Charset.forName("UTF-8") ;
	
	static public byte[] string2bytes_latin1(String str) {
		return str.getBytes(CHARSET_LATIN1) ;
	}
	
	static public String bytes2string_latin1(byte[] data) {
		return new String(data, CHARSET_LATIN1) ;
	}
	
	static public byte[] string2bytes_utf8(String str) {
		return str.getBytes(CHARSET_UTF8) ;
	}
	
	static public String bytes2string_utf8(byte[] data) {
		return new String(data, CHARSET_UTF8) ;
	}
	
	static public byte[] readFull(InputStream in, int size) throws IOException {
		byte[] buffer = new byte[size] ;
		readFull(in, buffer, size) ;
		return buffer ;
	}
	
	static public void readFull(InputStream in, byte[] buffer, int size) throws IOException {
		int read = 0 ;
		
		while (read < size) {
			int r = in.read(buffer, read, size-read) ;
			
			if (r < 0) throw new EOFException() ;
			
			read += r ;
		}
	}
	
	static public void readFull(InputStream in, OutputStream out, int size) throws IOException {
		byte[] buffer = new byte[1024*8] ;
		
		int read = 0 ;
		
		while (read < size) {
			int lng = size-read ;
			if (lng > buffer.length) lng = buffer.length ;
			
			int r = in.read(buffer, 0, lng) ;
			
			if (r < 0) throw new EOFException() ;
			
			out.write(buffer, 0, r);
			
			read += r ;
		}
	}
	
	static public void skip(InputStream in, int size) throws IOException {
		int read = 0 ;
		
		while (read < size) {
			long r = in.skip(size-read) ;
			
			if (r < 0) throw new EOFException() ;
			
			read += r ;
		}
	}
	
	static public byte[] readAll(InputStream in) throws IOException {
		MyByteArrayOutputStream bout = new MyByteArrayOutputStream(in.available(), 1024) ;
		readAll(in, bout) ;
		return bout.toByteArrayUsingAllocatedBuffer() ;
	}
	
	static public byte[] readAll(InputStream in, int expectedSize, int readBufferSize) throws IOException {
		MyByteArrayOutputStream bout = new MyByteArrayOutputStream(expectedSize, 1024*8) ;
		readAll(in, bout, readBufferSize) ;
		return bout.toByteArrayUsingAllocatedBuffer() ;
	}
	
	final static private class MyByteArrayOutputStream extends ByteArrayOutputStream {
		public MyByteArrayOutputStream(int knownBytesToAdd , int desiredAllocatedBuffer) {
			super( knownBytesToAdd < desiredAllocatedBuffer ? desiredAllocatedBuffer : knownBytesToAdd ) ;
		}
		
		public byte[] toByteArrayUsingAllocatedBuffer() {
			if ( this.buf.length == this.count ) return this.buf ;
			return toByteArray() ;
		}
	}
	
	static public void readAll(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024*8] ;
		
		int r ;
		while ( ( r = in.read(buffer, 0, buffer.length) ) >= 0 ) {
			out.write(buffer, 0, r) ;
		}
	}
	
	static public void readAll(InputStream in, OutputStream out, int readBufferSize) throws IOException {
		byte[] buffer = new byte[readBufferSize] ;
		
		int r ;
		while ( ( r = in.read(buffer, 0, buffer.length) ) >= 0 ) {
			out.write(buffer, 0, r) ;
		}
	}
	
	static public void writeInt(int v, OutputStream out) throws IOException {
		byte[] buff = new byte[4] ;
		writeInt(v, buff, 0) ;
		out.write(buff) ;
	}
	
	static public void writeInt(int v, OutputStream out, byte[] tmpBuffer) throws IOException {
		writeInt(v, tmpBuffer, 0) ;
		out.write(tmpBuffer,0,4) ;
	}
	
	static public void writeInt(int v, byte[] buffer, int off) {
		buffer[off] = (byte) (v >>> 24);
		buffer[off+1] = (byte) (v >>> 16);
		buffer[off+2] = (byte) (v >>> 8);
		buffer[off+3] = (byte) (v);
	}
	
	static public void writeInt24(int v, OutputStream out) throws IOException {
		byte[] buff = new byte[3] ;
		writeInt24(v, buff, 0) ;
		out.write(buff) ;
	}
	
	static public void writeInt24(int v, byte[] buffer, int off) {
		buffer[off+0] = (byte) (v >>> 16);
		buffer[off+1] = (byte) (v >>> 8);
		buffer[off+2] = (byte) (v);
	}
	
	static public void writeInt16(int v, OutputStream out) throws IOException {
		byte[] buff = new byte[2] ;
		writeInt16(v, buff, 0) ;
		out.write(buff) ;
	}
	
	static public void writeInt16(int v, byte[] buffer, int off) {
		buffer[off+0] = (byte) (v >>> 8);
		buffer[off+1] = (byte) (v);
	}
	
	static public void writeLong(long v, OutputStream out) throws IOException {
		byte[] buf = new byte[8] ;
		writeLong(v, buf, 0) ;
		out.write(buf) ;
	}
	
	static public void writeLong(long v, OutputStream out, byte[] tmpBuffer) throws IOException {
		writeLong(v, tmpBuffer, 0) ;
		out.write(tmpBuffer,0,8) ;
	}

	static public void writeLong(long v, byte[] buffer, int off) {
		buffer[off] = (byte) (v >>> 56);
		buffer[off+1] = (byte) (v >>> 48);
		buffer[off+2] = (byte) (v >>> 40);
		buffer[off+3] = (byte) (v >>> 32);
		buffer[off+4] = (byte) (v >>> 24);
		buffer[off+5] = (byte) (v >>> 16);
		buffer[off+6] = (byte) (v >>> 8);
		buffer[off+7] = (byte) (v);
	}
	

	static public void writeFloat(float v, OutputStream out) throws IOException {
		byte[] buf = new byte[4] ;
		writeFloat(v, buf, 0) ;
		out.write(buf) ;
	}
	
	static public void writeFloat(float v, OutputStream out, byte[] tmpBuffer) throws IOException {
		writeFloat(v, tmpBuffer, 0) ;
		out.write(tmpBuffer,0,4) ;
	}
	
	static public void writeFloat(float v, byte[] buffer, int off) {
		writeInt( Float.floatToIntBits(v) , buffer, off) ;
	}
	
	static public void writeDouble(double v, OutputStream out) throws IOException {
		byte[] buf = new byte[8] ;
		writeDouble(v, buf, 0) ;
		out.write(buf) ;
	}
	
	static public void writeDouble(double v, OutputStream out, byte[] tmpBuffer) throws IOException {
		writeDouble(v, tmpBuffer, 0) ;
		out.write(tmpBuffer,0,8) ;
	}
	
	static public void writeDouble(double v, byte[] buffer, int off) {
		writeLong( Double.doubleToLongBits(v) , buffer, off) ;
	}
	
	static public int readInt(InputStream in) throws IOException {
		return readInt( readFull(in, 4) , 0 ) ;
	}
	
	static public int readInt(InputStream in, byte[] buffer) throws IOException {
		readFull(in, buffer, 4) ;
		return readInt(buffer , 0) ;
	}
	
	static public int readInt(byte[] buffer, int off) {
		return (( (buffer[off] & 0xFF) << 24) + ( (buffer[off+1] & 0xFF) << 16) + ( (buffer[off+2] & 0xFF) << 8) + ( (buffer[off+3] & 0xFF) ));
	}
	
	static public int readInt24(InputStream in) throws IOException {
		return readInt24( readFull(in, 3) , 0 ) ;
	}
	
	static public int readInt24(InputStream in, byte[] buffer) throws IOException {
		readFull(in, buffer, 3) ;
		return readInt24(buffer, 0) ;
	}
	
	static public int readInt24(byte[] buffer, int off) {
		return (( (buffer[off] & 0xFF) << 16) + ( (buffer[off+1] & 0xFF) << 8) + ( (buffer[off+2] & 0xFF) ));
	}
	
	static public int readInt16(InputStream in) throws IOException {
		return readInt16( readFull(in, 2) , 0 ) ;
	}
	
	static public int readInt16(InputStream in, byte[] buffer) throws IOException {
		readFull(in, buffer, 2) ;
		return readInt16(buffer, 0) ;
	}
	
	static public int readInt16(byte[] buffer, int off) {
		return (( (buffer[off+0] & 0xFF) << 8) + ( (buffer[off+1] & 0xFF) ));
	}
	
	static public long readLong(InputStream in) throws IOException {
		return readLong( readFull(in, 8) , 0 ) ;
	}

	static public long readLong(byte[] buffer, int off) {
		return (((long) buffer[off] << 56) + ((long) (buffer[off+1] & 255) << 48) + ((long) (buffer[off+2] & 255) << 40) + ((long) (buffer[off+3] & 255) << 32) +
				((long) (buffer[off+4] & 255) << 24) + ((buffer[off+5] & 255) << 16) + ((buffer[off+6] & 255) << 8) + ((buffer[off+7] & 255) ));
	}
	
	static public float readFloat(InputStream in) throws IOException {
		return readFloat( readFull(in, 4) , 0 ) ;
	}
	
	static public float readFloat(InputStream in, byte[] buffer) throws IOException {
		readFull(in, buffer, 4) ;
		return readFloat(buffer, 0) ;
	}

	static public float readFloat(byte[] buffer, int off) {
		return Float.intBitsToFloat(readInt(buffer, off));
	}
	
	static public double readDouble(InputStream in) throws IOException {
		return readDouble( readFull(in, 8) , 0 ) ;
	}
	
	static public double readDouble(InputStream in, byte[] buffer) throws IOException {
		readFull(in, buffer, 8) ;
		return readDouble(buffer, 0) ;
	}
	
	static public double readDouble(byte[] buffer, int off) {
		return Double.longBitsToDouble(readLong(buffer, off));
	}

	//////////////////////////////////////////////////////
	
	static public void writeInts(int[] ns, byte[] buffer, int off) {
		for (int i = 0; i < ns.length; i++) {
			int v = ns[i] ;
			
			buffer[off++] = (byte) (v >>> 24);
			buffer[off++] = (byte) (v >>> 16);
			buffer[off++] = (byte) (v >>> 8);
			buffer[off++] = (byte) v ;
		}
	}
	
	static public void writeInts(int[] ns, OutputStream out) throws IOException {
		byte[] buff = new byte[ns.length * 4] ;
		
		writeInts(ns, buff, 0) ; 
		
		out.write(buff);
	}
	
	static public void writeIntsBlock(int[] ns, OutputStream out) throws IOException {
		byte[] buff = new byte[4 + ns.length * 4] ;
		
		writeInt(ns.length, buff, 0) ;
		
		writeInts(ns, buff, 4) ; 
		
		out.write(buff);
	}
	
	static public void writeIntsMatrix(int[][] ns, OutputStream out) throws IOException {
		int lines = ns.length ;
		int cols = ns[0].length ;
		
		byte[] buff = new byte[4+4 + (lines * cols * 4) ] ;
		
		writeInt(lines, buff, 0) ;
		writeInt(cols, buff, 4) ;
		
		int off = 8 ;
		int lineSize = cols*4 ;
		
		for (int i = 0; i < lines; i++) {
			int[] line = ns[i] ;
			writeInts(line, buff, off) ;
			off += lineSize ;
		}
		
		out.write(buff);
	}
	
	static public void writeIntsArraysBlock(int[][] ns, OutputStream out) throws IOException {

		SerializationUtils.writeInt(ns.length , out) ;
		
		for (int i = 0; i < ns.length; i++) {
			int[] line = ns[i];
			
			writeIntsBlock(line, out) ;
		}

	}
	
	static public int[] readInts(int totalInts, InputStream in) throws IOException {
		byte[] buff = readFull(in, totalInts * 4) ;
		return readInts(buff, 0, buff.length) ;
	}
	
	static public int[] readIntsBlock(InputStream in) throws IOException {
		int totalInts = readInt(in) ;
		return readInts(totalInts, in) ;
	}
	
	static public int[] readInts(byte[] buff, int off, int lng) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(buff, off, lng) ;

		IntBuffer intBuffer = byteBuffer.asIntBuffer() ;
		
		int sz = intBuffer.remaining() ;
		
		int[] ns = new int[sz] ;
		
		for (int i = 0; i < sz; i++) {
			ns[i] = intBuffer.get() ;
		}
		
		return ns ;
	}
	
	static public int[][] readIntsMatrix(byte[] buff, int off, int lng) {
		int lines = readInt(buff, off) ;
		off += 4 ;
		int cols = readInt(buff, off) ;
		off += 4 ;
		
		return readIntsMatrix(buff, off, lines, cols);
	}
	
	static public int[][] readIntsArraysBlock(InputStream in) throws IOException {
		int lines = readInt(in) ;
		
		int[][] ns = new int[lines][] ;
		
		for (int i = 0; i < ns.length; i++) {
			ns[i] = readIntsBlock(in) ;
		}
		
		return ns ;
	}

	static public int[][] readIntsMatrix(InputStream in) throws IOException {
		byte[] bufferHeader = readFull(in, 4+4) ;
		int lines = readInt(bufferHeader, 0) ;
		int cols = readInt(bufferHeader, 4) ;

		int nsBytesSize = lines * cols * 4 ;
		byte[] buffer = readFull(in, nsBytesSize) ;

		return readIntsMatrix(buffer, 0, lines, cols);
	}

	private static int[][] readIntsMatrix(byte[] buff, int off, int lines, int cols) {
		int lineSize = cols*4 ;
		
		int[][] ns = new int[lines][] ; 
		
		for (int i = 0; i < lines; i++) {
			int[] line = readInts(buff, off, lineSize) ;
			off += lineSize ;
			ns[i] = line ;
		}
		
		return ns ;
	}	

	static public void writeFloats(float[] ns, byte[] buffer, int off) {
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, off, ns.length*4) ;
		
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;
		
		for (int i = 0; i < ns.length; i++) {
			floatBuffer.put( ns[i] ) ;
		}
	}
	
	static public int writeFloats(float[] ns, int offset, int length, byte[] buffer, int buffOffset) {
		int bytesToWrite = length * 4 ;
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, buffOffset, bytesToWrite) ;
		
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;
		
		int limit = offset+length ;
		for (int i = offset; i < limit; i++) {
			floatBuffer.put( ns[i] ) ;
		}
		
		return bytesToWrite ;
	}
	
	static public void writeFloats(float[] ns, OutputStream out) throws IOException {
		writeFloats(ns, out, 1024*32) ;
	}
	
	static public void writeFloats(float[] ns, OutputStream out, int maxBufferAllocation) throws IOException {
		int buffSize = ns.length * 4 ;
		if (buffSize > maxBufferAllocation) buffSize = maxBufferAllocation ;
		
		byte[] buff = new byte[buffSize] ;
		int nPerBuff = buffSize / 4 ;
		
		int nsSz = ns.length ;
		
		for (int i = 0; i < nsSz; i+= nPerBuff) {
			int lng = nsSz - i ;
			if (lng > nPerBuff) lng = nPerBuff ;
			
			int w = writeFloats(ns, i, lng, buff, 0) ; 
			out.write(buff, 0, w);
		}
		
	}
	
	static public float[] readFloats(int totalFloats, InputStream in) throws IOException {
		byte[] buff = readFull(in, totalFloats * 4) ;
		return readFloats(buff, 0, buff.length) ;
	}
	
	static public float[] readFloats(byte[] buff, int off, int lng) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(buff, off, lng) ;

		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;
		
		int sz = floatBuffer.remaining() ;
		
		float[] ns = new float[sz] ;
		
		for (int i = 0; i < sz; i++) {
			ns[i] = floatBuffer.get() ;
		}
		
		return ns ;
	}
	
	static public void readFloats(byte[] buff, int off, float[][] dest) {
		int lng = dest.length * ( dest[0].length * 4 ) ;
		readFloats(buff, off, lng, dest) ;
	}
	
	static public void readFloats(byte[] buff, int off, int lng, float[][] dest) {
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(buff, off, lng) ;

		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;
		
		int destSz = dest.length ;
		
		for (int i = 0; i < destSz; i++) {
			float[] destFs = dest[i] ;
			
			int destFsSz = destFs.length ;
			
			for (int j = 0; j < destFsSz; j++) {
				destFs[j] = floatBuffer.get() ;
			}
			
		}
	}
	
	static public void readFloats(byte[] buff, int off, float[][][] dest) {
		int lng = dest.length * dest[0].length * dest[0][0].length * 4 ;
		readFloats(buff, 0, lng, dest) ;
	}
	
	static public void readFloats(byte[] buff, int off, int lng, float[][][] dest) {
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(buff, off, lng) ;

		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer() ;
		
		int destSz = dest.length ;
		
		for (int i = 0; i < destSz; i++) {
			float[][] destFs = dest[i] ;
			int destFsSz = destFs.length ;
			
			for (int j = 0; j < destFsSz; j++) {
				float[] fs = destFs[j];
				int fsSz = fs.length ;
				
				for (int k = 0; k < fsSz; k++) {
					fs[k] = floatBuffer.get() ;	
				}
			}
		}
		
	}
	
	//////////////////////////////////////////////////////
	
	static public void writeFloatsBlock(float[] fs, OutputStream out) throws IOException {
		int fsBytesSize = fs.length * 4 ;
		byte[] buffer = new byte[ 4 + fsBytesSize ] ;
		
		writeInt(fsBytesSize, buffer, 0) ;
		writeFloats(fs, buffer, 4) ;
		
		out.write(buffer) ;
	}
	
	static public int writeFloatsBlock(float[] fs, byte[] buffer, int off) throws IOException {
		int fsBytesSize = fs.length * 4 ;
		
		writeInt(fsBytesSize, buffer, off) ;
		off += 4 ;
		
		writeFloats(fs, buffer, off) ;
		off += fsBytesSize ;
		
		return off ;
	}
	
	static public void writeFloatsArraysBlock(float[][] fs, OutputStream out) throws IOException {
		writeInt(fs.length, out) ;
		
		for (int i = 0; i < fs.length; i++) {
			writeFloatsBlock(fs[i], out) ;
		}
	}
	
	static public void writeFloatsMatrixBlock(float[][] fs, OutputStream out) throws IOException {
		int lines = fs.length ;
		int cols = fs[0].length ;
		
		int fsBytesSize = lines * cols * 4 ;
		byte[] buffer = new byte[ 4 + 4 + fsBytesSize ] ;
		
		writeInt(lines, buffer, 0) ;
		writeInt(cols, buffer, 4) ;
		
		int off = 8 ;
		
		for (int i = 0; i < lines; i++) {
			float[] line = fs[i] ;
			
			writeFloats(line, buffer, off) ;
			off += cols * 4 ;
		}
		
		out.write(buffer) ;
	}
	
	static public void writeFloatsCubeBlock(float[][][] fs, OutputStream out) throws IOException {
		int linesSz = fs.length ;
		int colsSz = fs[0].length ;
		int valsSz = fs[0][0].length ;
		
		int fsBytesSize = linesSz * colsSz * valsSz * 4 ;
		byte[] buffer = new byte[ 4 + 4 + 4 + fsBytesSize ] ;
		
		writeInt(linesSz, buffer, 0) ;
		writeInt(colsSz, buffer, 4) ;
		writeInt(valsSz, buffer, 8) ;
		
		int off = 4*3 ;
		
		for (int i = 0; i < linesSz; i++) {
			float[][] line = fs[i] ;
			
			for (int j = 0; j < colsSz; j++) {
				float[] col = line[j] ;
				
				for (int k = 0; k < valsSz; k++) {
					float v = col[k] ;
					
					writeFloat(v, buffer, off) ;
					off += 4 ;
				}
			}
		}
		
		out.write(buffer) ;
	}
	
	//////////
	
	static public float[] readFloatsBlock(InputStream in) throws IOException {
		int buffSz = readInt(in) ;
		
		byte[] buffer = readFull(in, buffSz) ;
		
		return readFloats(buffer, 0, buffer.length) ;
	}
	
	static public float[] readFloatsBlock(byte[] buffer, int off) throws IOException {
		int buffSz = readInt(buffer, off) ;
		
		return readFloats(buffer, 4, buffSz) ;
	}
	
	static public float[][] readFloatsArraysBlock(InputStream in) throws IOException {
		int sz = readInt(in) ;
		
		float[][] fs = new float[sz][] ;
		
		for (int i = 0; i < sz; i++) {
			fs[i] = readFloatsBlock(in) ;
		}
		
		return fs ;
	}
	
	static public float[][] readFloatsMatrixBlock(InputStream in) throws IOException {
		byte[] bufferHeader = readFull(in, 4+4) ;
		int lines = readInt(bufferHeader, 0) ;
		int cols = readInt(bufferHeader, 4) ;

		int fsBytesSize = lines * cols * 4 ;
		byte[] buffer = readFull(in, fsBytesSize) ;
		
		float[][] fs = new float[lines][cols] ;
		
		readFloats(buffer, 0, fsBytesSize, fs) ;
		
		return fs ;
	}
	
	static public float[][][] readFloatsCubeBlock(InputStream in) throws IOException {
		byte[] bufferHeader = readFull(in, 4+4+4) ;
		int linesSz = readInt(bufferHeader, 0) ;
		int colsSz = readInt(bufferHeader, 4) ;
		int valsSz = readInt(bufferHeader, 8) ;

		int fsBytesSize = linesSz * colsSz * valsSz * 4 ;
		byte[] buffer = readFull(in, fsBytesSize) ;
		
		float[][][] fs = new float[linesSz][colsSz][valsSz] ;
		
		readFloats(buffer, 0, fsBytesSize, fs) ;
		
		return fs ;
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	
	static public byte[] serializeFloatArray(float[] fs) throws IOException {
		MyByteArrayOutputStream bout = new MyByteArrayOutputStream( 4 + fs.length * 4 , 1024 ) ;
		writeFloatsBlock(fs, bout) ;
		return bout.toByteArrayUsingAllocatedBuffer() ;
	}
	
	static public byte[] serializeFloatMatrix(float[][] fs) throws IOException {
		MyByteArrayOutputStream bout = new MyByteArrayOutputStream( 4 + 4 + fs.length * fs[0].length * 4 , 1024 ) ;
		writeFloatsMatrixBlock(fs, bout) ;
		return bout.toByteArrayUsingAllocatedBuffer() ;
	}
	
	static public float[] unserializeFloatArray(byte[] serial) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(serial) ;
		float[] fs = readFloatsBlock(in) ;
		return fs ;
	}
	
	static public float[][] unserializeFloatMatrix(byte[] serial) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(serial) ;
		float[][] fs = readFloatsMatrixBlock(in) ;
		return fs ;
	}

	/////////////////////////////////////////////////////////////////////////////
	
	static public void writeObject( File file , Serializable obj , boolean compressed ) throws IOException {
		FileOutputStream fout = new FileOutputStream(file) ;
	
		BufferedOutputStream buffOut = new BufferedOutputStream(fout, 1024*8) ;
		
		try {
			if (compressed) writeObjectCompressed(buffOut, obj) ;
			else 			writeObject(buffOut, obj) ;
		}
		finally {
			buffOut.close() ;
		}
	}
	
	static public void writeObjectCompressed( OutputStream out , Serializable obj ) throws IOException {
		GZIPOutputStream gzOut = new GZIPOutputStream(out, 1024*8) ;
		writeObject(gzOut, obj) ;
		gzOut.flush() ;
		gzOut.finish() ;
	}
	
	static public void writeObject( OutputStream out , Serializable obj ) throws IOException {
		ObjectOutputStream objOut = new ObjectOutputStream(out) ;
		objOut.writeObject(obj);
		objOut.flush() ;
	}
	
	static public Object readObject( File file , boolean compressed ) throws IOException {
		FileInputStream fin = new FileInputStream(file) ;
		
		BufferedInputStream buffIn = new BufferedInputStream(fin, 1024*8) ;
		
		try {
			if (compressed) {
				try {
					return readObjectCompressed(buffIn) ; 	
				}
				catch (ZipException e) {
					buffIn.close() ;
					
					fin = new FileInputStream(file) ;
					buffIn = new BufferedInputStream(fin, 1024*8) ;
					return readObject(fin) ;
				}
				
			}
			else {
				return readObject(buffIn) ;
			}
		}
		finally {
			buffIn.close() ;
		}
	}
	
	static public Object readObjectCompressed( InputStream in ) throws IOException {
		GZIPInputStream gzIn = new GZIPInputStream(in, 1024*8) ;
		return readObject(gzIn) ;
	}
	
	static public Object readObject( InputStream in ) throws IOException {
		ObjectInputStream objIn = new ObjectInputStream(in) ;
		try {
			return objIn.readObject() ;
		} catch (ClassNotFoundException e) {
			throw new IOException(e) ;
		}
	}
	
	//////////////////////////////////////////////////////////////////
	
	static public void writeFile(File file, byte[] data) throws IOException {
		FileOutputStream fout = new FileOutputStream(file) ;
		fout.write(data);
		fout.close();
	}
	
	static public byte[] readFile(File file) throws IOException {
		FileInputStream fin = new FileInputStream(file) ;

		byte[] data = readAll(fin) ;
		
		fin.close();
		
		return data ;
	}

	//////////////////////////////////////////////////////////////////
	
	static public void writeBlock(byte[] block, OutputStream out) throws IOException {
		writeInt(block.length, out);
		out.write(block);
	}
	
	static public byte[] readBlock(InputStream in) throws IOException {
		int sz = readInt(in) ;
		return readFull(in, sz) ;
	}
	
	static public int readBlock(InputStream in, OutputStream out) throws IOException {
		int sz = readInt(in) ;
		readFull(in, out, sz);
		return sz ;
	}
	
	//////////////////////////////////////////////////////////////////
	
	static public void writeStringUTF8( String str , OutputStream out ) throws IOException {
		writeString(str, out, CHARSET_UTF8);
	}
	
	static public void writeStringLATIN1( String str , OutputStream out ) throws IOException {
		writeString(str, out, CHARSET_LATIN1);
	}
	
	static public void writeString( String str , OutputStream out , Charset charset ) throws IOException {
		byte[] bs = str.getBytes(charset) ;
		writeInt(bs.length, out);
		out.write(bs);
	}
	
	static public String readStringUTF8( InputStream in ) throws IOException {
		return readString(in, CHARSET_UTF8) ;
	}
	
	static public String readStringLATIN1( InputStream in ) throws IOException {
		return readString(in, CHARSET_LATIN1) ;
	}
	
	static public String readString( InputStream in , Charset charset ) throws IOException {
		int sz = readInt(in) ;
		byte[] bs = readFull(in, sz) ;
		return new String(bs, charset) ;
	}
	
}

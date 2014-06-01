package roxtools.io.vdisk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import roxtools.SerializationUtils;

final public class VDMetaData implements Comparable<VDMetaData> {
	
	static String readKeyFromSerial( InputStream in ) throws IOException {
		int sz = SerializationUtils.readInt(in) ;
		
		byte[] keysBytes = SerializationUtils.readFull(in, sz) ;
		
		String key = new String(keysBytes , CHARSET) ;
		
		return key ;
	}
	
	static String readKeyFromSerial( VDFile vdFile ) throws IOException {
		
		byte[] buff = new byte[4] ;
		
		vdFile.seek(0) ;
		vdFile.read(buff) ;
		
		int sz = SerializationUtils.readInt(buff,0) ;
		
		byte[] keysBytes = new byte[sz] ;
		
		vdFile.read(keysBytes) ;
		
		String key = new String(keysBytes , CHARSET) ;	
		
		return key ;
	}
	
	static final private Charset CHARSET = Charset.forName("iso-8859-1") ;
	
	static final protected byte[] dummyData = new byte[0] ;
	
	static final public int MAX_KEY_SIZE = 256 ;
	
	private String key ;
	private byte[] data ;

	public VDMetaData(String key) {
		this(key, dummyData) ;
	}
	
	public VDMetaData(String key, byte[] data) {
		if (key == null) throw new NullPointerException("null key") ;
		if (data == null) throw new NullPointerException("null data") ;
		
		if (key.length() > MAX_KEY_SIZE) throw new IllegalArgumentException("Key too long: "+ key.length() +" > "+ MAX_KEY_SIZE +" <"+key+">") ;
		
		this.key = key;
		this.data = data;
	}
	
	public String getKey() {
		return key;
	}
	
	public byte[] getData() {
		return data.clone() ;
	}
	
	public int copyData(byte[] buff, int off) {
		System.arraycopy(data, 0, buff, off, data.length) ;
		return data.length ;
	}
	
	public int getDataSize() {
		return data.length ;
	}
	
	public void writeDataTo(OutputStream out) throws IOException {
		out.write(data) ;
	}
	
	public VDMetaData( byte[] serial ) throws IOException {
		this( new ByteArrayInputStream(serial) )  ;
	}
	
	public VDMetaData( InputStream in ) throws IOException {
		readFrom(in) ;
	}
	
	private void readFrom( InputStream in ) throws IOException {
		
		DataInputStream din = new DataInputStream(in) ;
		
		int sz = din.readInt() ;
		byte[] keysBytes = new byte[sz] ;
		din.readFully(keysBytes) ;
		
		sz = din.readInt() ;
		byte[] data = new byte[sz] ;
		din.readFully(data) ;
		 
		this.key = new String(keysBytes , CHARSET) ;
		this.data = data ;
		
	}
	
	public byte[] getSerial() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream( 4 + key.length() + 4 + data.length ) ;
		
		writeTo(bout) ;
		
		return bout.toByteArray() ;
	}
	
	public void writeTo( OutputStream out ) throws IOException {
	
		DataOutputStream dOut = new DataOutputStream(out) ;
		

		byte[] keyBytes = this.key.getBytes(CHARSET) ;
		
		dOut.writeInt(keyBytes.length) ;
		dOut.write(keyBytes) ;
	
		dOut.writeInt(data.length) ;
		dOut.write(data) ;
	
		
	}
	
	private int parentBlockIndex ;
	private int parentBlockSector ;
	
	public boolean hasParent() {
		return parentBlockIndex > 0 && parentBlockSector > 0 ;
	}
	
	public String getParentID() {
		if (!hasParent()) return null ;
		
		return VDBlock.toStringIdent(getParentBlockIndex(), getParentBlockSector()) ;
	}
	
	public int getParentBlockIndex() {
		return parentBlockIndex -1;
	}
	
	public int getParentBlockSector() {
		return parentBlockSector -1;
	}
	
	protected void setParentBlock(int parentBlockIndex, int parentBlockSector) {
		this.parentBlockIndex = parentBlockIndex +1;
		this.parentBlockSector = parentBlockSector +1;
	}
	
	protected void clearParentBlock() {
		this.parentBlockIndex = 0 ;
		this.parentBlockSector = 0 ;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder() ;
		
		str.append( this.getClass().getName() ) ;
		
		if (hasParent()) {
			str.append("#") ;
			str.append( getParentID() ) ;
		}
		

		str.append("<") ;
		str.append(key) ;
		str.append(">") ;
		
		str.append( Arrays.toString(data) ) ;
		
		return str.toString();
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() ;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (obj.getClass() == String.class) {
			String objKey = (String) obj ;
			
			return this.key.equals(objKey) ;
		}
		
		if (getClass() != obj.getClass()) return false;
		
		VDMetaData other = (VDMetaData) obj;
		
		return this.key.equals(other.key) ;
	}

	@Override
	public int compareTo(VDMetaData o) {
		return this.key.compareTo(o.key) ;
	}
	
}

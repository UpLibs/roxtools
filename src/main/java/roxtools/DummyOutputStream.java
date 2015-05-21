package roxtools;

import java.io.IOException;
import java.io.OutputStream;

final public class DummyOutputStream extends OutputStream {

	@Override
	public void write(byte[] b) throws IOException {}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {}
	
	@Override
	public void write(int b) throws IOException {}

}

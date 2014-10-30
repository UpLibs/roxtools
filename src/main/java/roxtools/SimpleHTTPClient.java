package roxtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SimpleHTTPClient {

	static public String getAsString(URL url) throws IOException {
		byte[] data = get(url) ;
		return new String(data) ;
	}
	
	static public byte[] get(URL url) throws IOException {
		InputStream in = url.openStream();

		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		
		byte[] buffer = new byte[1024 * 4];
		int r = 0;
		while ( (r = in.read(buffer)) >= 0 ) {
			bout.write(buffer, 0, r);
		}

		return bout.toByteArray() ;
	}

}

package roxtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SimpleHTTPClient {

	static public String getAsString(URL url) throws IOException {
		return getAsString(url, null) ;
	}
	
	static public String getAsString(URL url, String userAgent) throws IOException {
		byte[] data = get(url, userAgent) ;
		return new String(data) ;
	}
	
	static public byte[] get(URL url) throws IOException {
		return get(url, null) ;
	}
	
	static public byte[] get(URL url, String userAgent) throws IOException {
		URLConnection con = url.openConnection();
		
		con.setConnectTimeout(60000);
		con.setReadTimeout(60000);
		
		if (userAgent != null && !userAgent.isEmpty()) {
			con.setRequestProperty("User-Agent", userAgent) ;
		}
		
		InputStream in = con.getInputStream() ;
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
		
		byte[] buffer = new byte[1024 * 4];
		int r = 0;
		while ( (r = in.read(buffer)) >= 0 ) {
			bout.write(buffer, 0, r);
		}
		
		try {
			in.close();
		} catch (Exception e) {}

		return bout.toByteArray() ;
	}

}

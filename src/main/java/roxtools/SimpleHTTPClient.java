package roxtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SimpleHTTPClient {

    private static final int DEFAULT_TIMEOUT = 60000;

    // Hides the implicit public constructor
    private SimpleHTTPClient() {

    }

    static public String getAsString(URL url) throws IOException {
		return getAsString(url, null) ;
	}

	static public String getAsString(URL url, String userAgent) throws IOException {
        return getAsString(url, null, DEFAULT_TIMEOUT , DEFAULT_TIMEOUT) ;
	}

    static public String getAsString(URL url, String userAgent, int connectTimeout, int readTimeout) throws IOException {
        byte[] data = get(url, userAgent, connectTimeout , readTimeout) ;
        return new String(data) ;
    }

    static public byte[] get(URL url) throws IOException {
		return get(url, null, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT) ;
	}

    static public byte[] get(URL url, String userAgent) throws IOException {
        return get( url, userAgent, DEFAULT_TIMEOUT , DEFAULT_TIMEOUT);
    }

	static public byte[] get( URL url, String userAgent, int connectTimeout, int readTimeout ) throws IOException {
		URLConnection con = url.openConnection();

		con.setConnectTimeout(connectTimeout);
		con.setReadTimeout(readTimeout);

		if (userAgent != null && !userAgent.isEmpty()) {
			con.setRequestProperty("User-Agent", userAgent) ;
		}

        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        try ( InputStream in = con.getInputStream() ) {
            byte[] buffer = new byte[1024 * 4];
            int r = 0;
            while (( r = in.read( buffer )) >= 0 ) {
                bout.write( buffer, 0, r );
            }
        }

        return bout.toByteArray();
    }

}

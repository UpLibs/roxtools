package roxtools;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

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
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        downloadToOutput(url, userAgent, connectTimeout, readTimeout, bout) ;
        return bout.toByteArray();
    }
	
	static public long downloadToFile( URL url, String userAgent, int connectTimeout, int readTimeout , File file , boolean allowResume) throws IOException {
		return downloadToFile(url, userAgent, connectTimeout, readTimeout, file, allowResume, 10) ;
	}
	
	static public long downloadToFile( URL url, String userAgent, int connectTimeout, int readTimeout , File file , boolean allowResume, int maxRetries) throws IOException {
		
		if (maxRetries < 1) maxRetries = 1 ;
		
		for (int retry = 0; retry < maxRetries; retry++) {
			try {
				return downloadToFileImplem(url, userAgent, connectTimeout, readTimeout, file, allowResume) ;	
			}
			catch (IOException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
			}
		}
		
		throw new IOException("Can't fully download file. File size: "+ file.length()) ;
	}
	
	static private long downloadToFileImplem( URL url, String userAgent, int connectTimeout, int readTimeout , File file , boolean allowResume) throws IOException {
		
		FileOutputStream fout ;
		long fileOffset = 0 ;
		
		if ( allowResume ) {
			fout = new FileOutputStream(file, true) ;
			FileChannel channel = fout.getChannel() ;
			long pos = channel.position() ;
			if (pos > 0) {
				fileOffset = pos ;
			}
		}
		else {
			fout = new FileOutputStream(file) ;
		}
		
		BufferedOutputStream foutBuff = new BufferedOutputStream(fout, 1024*64) ;
		
		long downloadedBytes = -1 ;
		
        try {
        	downloadedBytes = downloadToOutput(url, userAgent, connectTimeout, readTimeout, foutBuff, fileOffset) ;
        }
        finally {
        	foutBuff.close();
		}

        return downloadedBytes ;
    }
	
	static public long downloadToOutput( URL url, String userAgent, int connectTimeout, int readTimeout , OutputStream out) throws IOException {
		return downloadToOutput(url, userAgent, connectTimeout, readTimeout, out, 0) ;
	}
	
	static public long downloadToOutput( URL url, String userAgent, int connectTimeout, int readTimeout , OutputStream out , long dataOffset) throws IOException {
		URLConnection conn = url.openConnection();

		if (connectTimeout > 0) conn.setConnectTimeout(connectTimeout);
		if (readTimeout > 0) conn.setReadTimeout(readTimeout);

		if (userAgent != null && !userAgent.isEmpty()) {
			conn.setRequestProperty("User-Agent", userAgent) ;
		}
		
		long downloadedBytes = 0 ;
		
		if (dataOffset > 0) {
			conn.setRequestProperty("Range", "bytes="+dataOffset+"-");
			downloadedBytes = dataOffset ;
		}
		
        try ( InputStream in = conn.getInputStream() ) {
            byte[] buffer = new byte[1024 * 4];
            int r = 0;
            while (( r = in.read( buffer )) >= 0 ) {
            	out.write( buffer, 0, r );
            	downloadedBytes += r ;
            }
        }
        catch (IOException e) {
        	if ( e.getMessage().contains("erver returned HTTP response code: 416") ) {
        		// If using header "Range" and returned code 416,
        		// this means that the file was already fully downloaded:
        		if (dataOffset > 0) {
        			return downloadedBytes ;
        		}
        	}
        	throw e ;
		}

        return downloadedBytes ;
    }
	
}

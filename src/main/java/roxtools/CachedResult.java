package roxtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class CachedResult {
	
	static public interface ResultSerializer {
		public void writeTo(ObjectOutputStream out , Serializable result) throws IOException ;
		public Serializable readFrom(ObjectInputStream in) throws IOException ;
	}

	static public class Config implements Serializable {
		private static final long serialVersionUID = 7770924314405702746L;
		
		final private double version ;
		final private Serializable[] params ;

		public Config(double version, Serializable... params) {
			this.version = version;
			this.params = params;
		}
		
		public double getVersion() {
			return version;
		}
		
		public Serializable[] getParams() {
			return params;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(params);
			long temp;
			temp = Double.doubleToLongBits(version);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			Config other = (Config) obj;
			
			if (Double.doubleToLongBits(version) != Double.doubleToLongBits(other.version))
				return false;
			
			if (!Arrays.equals(params, other.params))
				return false;
			
			return true;
		}


	}

	
	//////////////////////////////////////////////////////////////////////
	
	final private Config config ;
	private Serializable result ;

	final private File storeFile ;
	
	final private ResultSerializer resultSerializer ;
	
	public CachedResult(File storeFile, Config config, Serializable result) throws IOException {
		this(storeFile, config, result, null) ;
	}
	
	public CachedResult(File storeFile, Config config, Serializable result, ResultSerializer resultSerializer) throws IOException {
		this.storeFile = storeFile ;
		this.config = config;
		this.result = result;
		this.loaded = true ;
		
		this.resultSerializer = resultSerializer ;
		
		save() ;
	}
	
	public CachedResult(File storeFile, Config config) throws IOException {
		this(storeFile, config, (ResultSerializer)null) ;
	}
	
	public CachedResult(File storeFile, Config config, ResultSerializer resultSerializer) throws IOException {
		this.storeFile = storeFile ;
		this.config = config ;
		
		this.resultSerializer = resultSerializer ;
		
		load() ;
	}
	
	public ResultSerializer getResultSerializer() {
		return resultSerializer;
	}
	
	public File getStoreFile() {
		return storeFile;
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Serializable getResult() {
		return result;
	}
	
	public void writeTo(OutputStream out) throws IOException {
		ObjectOutputStream objOut = new ObjectOutputStream(out) ;
		
		objOut.writeObject(config) ;
		
		if (resultSerializer != null) {
			resultSerializer.writeTo(objOut, result) ;	
		}
		else {
			objOut.writeObject(result) ;
		}
		
		
		objOut.flush() ;
	}
	
	public boolean readFrom(InputStream in) throws IOException {
		ObjectInputStream objIn = new ObjectInputStream(in) ;
		
		
		try {
			Config config = (Config) objIn.readObject();
		
			if ( !this.config.equals(config) ) {
				return false ;
			}
			
			if (resultSerializer != null) {
				this.result = resultSerializer.readFrom(objIn) ;
			}
			else {
				this.result = (Serializable) objIn.readObject() ;
			}
			
			return true ;
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e) ;
		}
		
	}
	
	private boolean loaded = false ;
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public boolean load() throws IOException {
		FileInputStream fin ;
		try {
			fin = new FileInputStream(storeFile);	
		}
		catch (IOException e) {
			return false ;
		}
		
		boolean ok = readFrom( fin ) ;
		this.loaded = ok ;
		return ok ;
	}
	
	public void save() throws IOException {
	
		FileOutputStream fout = new FileOutputStream(storeFile) ;
		
		writeTo(fout) ;
		
		fout.close() ;
		
	}
	
	public void setResult(Serializable result) throws IOException {
		this.result = result ;
		save() ;
	}
	
}

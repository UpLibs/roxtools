package roxtools.compiler;

import java.net.URL;

public class ResourceLocation {
	final private URL resourceURL ;
	final private String resourceFileRelativePath ;
	final private String resourceFileFullPath ;
	
	public ResourceLocation(URL resourceURL, String resourceFileRelativePath, String resourceFileFullPath) {
		this.resourceURL = resourceURL;
		this.resourceFileRelativePath = resourceFileRelativePath;
		this.resourceFileFullPath = resourceFileFullPath;
	}

	public URL getResourceURL() {
		return resourceURL;
	}
	
	public String getResourceFileRelativePath() {
		return resourceFileRelativePath;
	}
	
	public String getResourceFileFullPath() {
		return resourceFileFullPath;
	}
	
}
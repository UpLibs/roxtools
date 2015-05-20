package roxtools.compiler;

import java.net.URL;

public interface ClassLoaderPackageAware {

	public URL[] getPackageClassesURLs(String packageName) ;
	
	public ClassLocation[] getPackageClassesLocations(String packageName) ;
	
}

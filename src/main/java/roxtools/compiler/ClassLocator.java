package roxtools.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassLocator {
	
	final private ClassLoader classLoader ;
	
	public ClassLocator(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

    public List<ClassLocation> getClassesFromPackage(String packageName, boolean recurse) throws IOException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        
        ArrayList<ClassLocation> list = new ArrayList<ClassLocation>() ;
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            List<ClassLocation> classLocations = getClassesFromPackage(packageName, resource, recurse) ;
            list.addAll(classLocations) ;
        }
        
        return list ;
    }
    

    public List<ResourceLocation> getResourcesFromPackage(String packageName, String resoucesExtension, boolean recurse) throws IOException {
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        
        ArrayList<ResourceLocation> list = new ArrayList<ResourceLocation>() ;
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            List<ResourceLocation> rscLocations = getResourcesFromPackage(packageName, resource, resoucesExtension, recurse) ;
            list.addAll(rscLocations) ;
        }
        
        return list ;
    }
	
    public List<ClassLocation> getClassesFromPackage(String packName, URL packURL, boolean recurse) throws IOException {
    	
    	ArrayList<ClassLocation> classLocations = new ArrayList<ClassLocation>() ;
    	
    	ClassLoaderPackageAware classLoaderPackageAware = null ;
    	if ( classLoader instanceof ClassLoaderPackageAware ) {
    		classLoaderPackageAware = (ClassLoaderPackageAware) classLoader ;
    	}
    	else if ( classLoader.getParent() instanceof ClassLoaderPackageAware ) {
    		classLoaderPackageAware = (ClassLoaderPackageAware) classLoader.getParent() ;
    	}
    	
    	/////////////////////////////////////////////
    	
    	boolean ignoreUnknowProtocols = false ;
    	
    	if (classLoaderPackageAware != null) {
    		ClassLocation[] locations = classLoaderPackageAware.getPackageClassesLocations(packName) ;
    		
    		Collections.addAll(classLocations, locations) ;
    		
    		ignoreUnknowProtocols = true ;
    	}
    	
    	/////////////////////////////////////////////
    	
    	String protocol = packURL.getProtocol() ;
    	
    	if ( protocol.equals("file") ) {
    		File file;
			try {
				file = new File( packURL.toURI() );
			}
			catch (URISyntaxException e) {
				throw new IOException(e) ;
			}

    		if ( !file.isDirectory() ) throw new IOException("Package URL not a directory: "+ packURL) ;
    		
    		File[] listFiles = file.listFiles() ;
    		
    		for (File classFile : listFiles) {
    			if ( classFile.getName().endsWith(".class") ) {
        			URL url = classFile.toURI().toURL() ;
        			String className = url.getPath().replaceFirst("^.*?([^\\\\/]+)$", "$1") ;
        			if (packName.length() > 0) className = packName +"."+ className ;
        			className = className.replaceFirst(".class$", "") ;
        			
        			String classFileFullPath = url.getPath() ;
        			
        			ClassLocation classLocation = new ClassLocation(className, url, classFileFullPath) ;
        			classLocations.add(classLocation) ;
    			}	
			}
    	}
    	else if ( protocol.equals("jar") ) {
    		
    		String classPackPrefix = packName+"." ;
    		
    		String jarExternalForm = packURL.toExternalForm().replaceFirst("^jar:", "").replaceFirst("!.*", "") ;
    		
    		URL jarURL = new URL( jarExternalForm ) ;

    		List<String> jarClassesEntries = getJarClassesEntries(jarURL) ;
    		
    		for (String entryName : jarClassesEntries) {
    			if ( !entryName.endsWith(".class") ) continue ;
    			
				String className = entryName.replaceFirst(".class$", "").replaceAll("[\\\\/]", ".") ;
				
				boolean match = false ;
				
				if ( recurse ) {
					match = className.startsWith(classPackPrefix)  ;
				}
				else {
					String classPack = className.replaceFirst("\\.[^\\.]+$", "") ;
					
					match = classPack.equals(packName) ;
				}
				
				if (match) {
					URL classURL = new URL("jar:"+jarExternalForm+"!/"+entryName) ;

					ClassLocation classLocation = new ClassLocation(className, classURL, entryName) ;
        			classLocations.add(classLocation) ;
				}
    		}
    		
    		
    	}
    	else {
    		if (!ignoreUnknowProtocols) {
    			throw new UnsupportedOperationException("Unsupported protocol: "+ protocol) ;
    		}
    	}
    	
    	return classLocations ;
    }
    

    public List<ResourceLocation> getResourcesFromPackage(String packName, URL packURL, String resourceExtension, boolean recurse) throws IOException {
    	
    	ArrayList<ResourceLocation> rscLocations = new ArrayList<ResourceLocation>() ;
    	
    	String protocol = packURL.getProtocol() ;
    	
    	if ( protocol.equals("file") ) {
    		File file;
			try {
				file = new File( packURL.toURI() );
			}
			catch (URISyntaxException e) {
				throw new IOException(e) ;
			}

    		if ( !file.isDirectory() ) throw new IOException("Package URL not a directory: "+ packURL) ;
    		
    		File[] listFiles = file.listFiles() ;
    		
    		for (File rscFile : listFiles) {
    			if ( rscFile.getName().endsWith(resourceExtension) ) {
        			URL url = rscFile.toURI().toURL() ;
        			String filePath = url.getPath() ;
        			
        			String fileRelativePath = packName.replaceAll("\\.", "/") +"/"+ rscFile.getName() ;
        			
        			ResourceLocation location = new ResourceLocation(url, fileRelativePath, filePath) ;
        			rscLocations.add(location) ;
    			}	
			}
    	}
    	else if ( protocol.equals("jar") ) {
    		
    		String classPackPrefix = packName+"." ;
    		
    		String jarExternalForm = packURL.toExternalForm().replaceFirst("^jar:", "").replaceFirst("!.*", "") ;
    		
    		URL jarURL = new URL( jarExternalForm ) ;

    		List<String> jarClassesEntries = getJarClassesEntries(jarURL) ;
    		
    		for (String entryName : jarClassesEntries) {
    			if ( !entryName.endsWith(resourceExtension) ) continue ;
    			
				boolean match = false ;
				
				if ( recurse ) {
					String rscName = entryName.replaceAll("[\\\\/]", ".") ;
					match = rscName.startsWith(classPackPrefix)  ;
				}
				else {
					String entryPack = entryName.replaceFirst("[\\\\/][^\\\\/]+$", "").replaceAll("[\\\\/]", ".") ;
					match = entryPack.equals(packName) ;
				}
				
				if (match) {
					URL classURL = new URL("jar:"+jarExternalForm+"!/"+entryName) ;

					ResourceLocation location = new ResourceLocation(classURL, entryName, entryName) ;
        			rscLocations.add(location) ;
				}
    		}
    		
    	}
    	else {
    		throw new UnsupportedOperationException("Unsupported protocol: "+ protocol) ;
    	}
    	
    	return rscLocations ;
    }
    

    private HashMap<URL, List<String>> jarEntriesCache = new HashMap<URL, List<String>>() ;
    
    private List<String> getJarClassesEntries(URL jarURL) throws IOException {
    	
    	synchronized (jarEntriesCache) {
    		List<String> cachedEntries = jarEntriesCache.get(jarURL) ;
			
    		if (cachedEntries != null) return cachedEntries ;
    		
	    	ZipInputStream zin = new ZipInputStream( jarURL.openStream() ) ;
	
	    	ArrayList<String> entries = new ArrayList<String>() ;
	    	
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				String entryName = entry.getName() ;
				entries.add(entryName) ;
			}
			
			jarEntriesCache.put(jarURL, entries) ;
			
			return entries ;
    	}
    }
    
    
}

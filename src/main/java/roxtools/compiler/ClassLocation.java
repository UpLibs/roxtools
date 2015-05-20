package roxtools.compiler;

import java.net.URL;

public class ClassLocation {
	final private String className ;
	final private URL classURL ;
	final private String classFileFullPath ;
	
	public ClassLocation(String className, URL classURL, String classFileFullPath) {
		this.className = className;
		this.classURL = classURL;
		this.classFileFullPath = classFileFullPath;
	}
	
	public String getClassName() {
		return className;
	}
	
	static public String parseClassLastName(String className) {
		int idx = className.lastIndexOf('.') ;
		if (idx < 0) return className ;
		return className.substring(idx+1);
	}
	
	public String getClassLastName() {
		return parseClassLastName(className) ;
	}
	
	static public String parseClassPackageName(String className) {
		int idx = className.lastIndexOf('.') ;
		if (idx < 0) return "" ;
		return className.substring(0,idx);
	}
	
	public String getPackageName() {
		return parseClassPackageName(className) ;
	}
	
	public URL getClassURL() {
		return classURL;
	}
	
	public String getClassRelativePath() {
		return className.replaceAll("\\.", "/") + ".class" ;
	}
	
	public String getClassFileFullPath() {
		return classFileFullPath;
	}
	
}
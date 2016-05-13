package roxtools.ipc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roxtools.ArrayUtils;
import roxtools.FileUtils;
import roxtools.ipc.ProcessRunner.OutputConsumer;
import roxtools.ipc.ProcessRunner.OutputConsumerListener;

public class JVMRunner {
	
	static public String[] filesToPath(File[] files) {
		String[] strs = new String[files.length] ;
		
		for (int i = 0; i < strs.length; i++) {
			strs[i] = files[i].getPath() ;
		}
		
		return strs ;
	}
	
	static public File[] getClassesClasspath(boolean ignoreStandartJavaClasses, Class<?>... classes) {
		LinkedHashMap<File, File> cpTable = new LinkedHashMap<>() ;
		
		for (Class<?> clazz : classes) {
			File[] cps = getClassHierarchyClasspath(ignoreStandartJavaClasses, clazz) ;
			
			for (File file : cps) {
				cpTable.put(file, file) ;
			}
		}
		
		File[] allCps = new File[ cpTable.size() ] ;
		int allCpsSz = 0 ;
		
		for (File file : cpTable.keySet()) {
			allCps[allCpsSz++] = file ;
		}
		
		return allCps ;
	}
	
	static public File[] getClassHierarchyClasspath(boolean ignoreStandartJavaClasses, Class<?> clazz) {
		ArrayList<File> files = new ArrayList<>() ;
		
		while ( clazz != null ) {
			File cp = getClassClasspath(clazz) ;
			files.add(cp) ;
			
			clazz = clazz.getSuperclass() ;
			if ( clazz == null ) break ;
			
			if ( ignoreStandartJavaClasses && clazz.getName().startsWith("java.lang.")) break ;
		}
		
		return files.toArray( new File[files.size()] ) ;
	}
	
	static public File getClassClasspath(Class<?> clazz) {
		if (clazz == null) throw new NullPointerException("Null clazz") ;
		
		String name = clazz.getName() ;
		
		String path = "/"+name.replaceAll("\\.", "/") +".class" ;
		
		URL rsc = JVMRunner.class.getResource(path) ;
		
		String protocol = rsc.getProtocol().toLowerCase() ;
		
		if ( protocol.equals("file") ) {
			String fullPath = rsc.toString() ;
			String base = fullPath.substring(0, fullPath.length()-path.length()) ;
			
			try {
				File file = new File( new URL(base).toURI() ) ;
				return file ;
			}
			catch (MalformedURLException | URISyntaxException e) {
				throw new IllegalStateException(e) ;
			}
		}
		else if ( protocol.equals("jar") ) {
			String fullPath = rsc.toString() ;
			String base = fullPath.split("!",2)[0] ;
			base = base.replaceFirst("^jar:", "") ;
			
			try {
				File file = new File( new URL(base).toURI() ) ;
				return file ;
			}
			catch (MalformedURLException | URISyntaxException e) {
				throw new IllegalStateException(e) ;
			}
		}
		
		return null ;
	}
	
	static public String getClassPathDelimiter() {
		return File.pathSeparator ;
	}

	static public String getDefaultJVMBinary() {
		String javaHome = System.getProperty("java.home") ;
		
		if (javaHome == null || javaHome.isEmpty()) throw new IllegalStateException("Can't find property 'java.home'.") ;
		
		String bin = javaHome + File.separator + "bin" + File.separator +"java" ;
		
		return bin ;
	}
	
	static public String[] getDefaultMainClasspath(boolean addCurrentDirectory) {
		
		ArrayList<String> classPath = new ArrayList<>() ;
		
		String classPathDelimiter = getClassPathDelimiter() ;
		
		String javaClassPath = System.getProperty("java.class.path") ;
		
		if (javaClassPath != null && !javaClassPath.isEmpty()) {
			String[] parts = javaClassPath.split(classPathDelimiter) ;
			
			for (String part : parts) {
				part = part.trim() ;
				if ( !classPath.contains(part) ) classPath.add(part) ;		
			}
		}
		
		if (addCurrentDirectory) {
			String currentDirectory = FileUtils.getCurrentDirectory().toString() ;
			if ( !classPath.contains(currentDirectory) ) classPath.add(currentDirectory) ;
		}
		
		String[] cp = classPath.toArray(new String[classPath.size()]) ;
		return cp ;
	}
	
	private String[] classPath ;
	private String mainClass ;
	private String[] arguments ;
	
	public JVMRunner(String mainClass, String... arguments) {
		this( getDefaultMainClasspath(true), mainClass, arguments) ;
	}
	
	public JVMRunner(File[] classPath, String mainClass, String... arguments) {
		this(filesToPath(classPath), mainClass, arguments) ;
	}
	
	public JVMRunner(String[] classPath, String mainClass, String... arguments) {
		if (classPath == null || classPath.length == 0 ) throw new IllegalArgumentException("Invalid classPath: "+ Arrays.toString(classPath)) ;
		if (mainClass == null || mainClass.isEmpty() ) throw new IllegalArgumentException("Invalid mainClass: "+ mainClass) ;
		
		this.classPath = classPath ;
		this.mainClass = mainClass ;
		this.arguments = arguments != null ? arguments : new String[0] ;
	}
	
	private String jvmBinary = null ;
	
	public String getJvmBinary() {
		if (jvmBinary == null) return getDefaultJVMBinary() ;
		return jvmBinary ;
	}
	
	public boolean isDefaultJVMBinary() {
		return jvmBinary == null || jvmBinary.equals(getDefaultJVMBinary()) ;
	}
	
	public void setJvmBinary(String jvmBinary) {
		this.jvmBinary = jvmBinary;
	}
	
	public String[] getClassPath() {
		return classPath;
	}
	
	public String getMainClass() {
		return mainClass;
	}
	
	public String[] getArguments() {
		return arguments;
	}
	
	private ArrayList<String> vmArguments = new ArrayList<>() ;
	
	@SuppressWarnings("unchecked")
	synchronized public List<String> getVmArguments() {
		return (List<String>) vmArguments.clone() ;
	}
	
	synchronized public boolean addVmArguments(String... vmArguments) {
		boolean allOk = true ;
		for (String arg : vmArguments) {
			boolean ok = addVmArgument(arg) ;
			allOk &= ok ;
		}
		return allOk ;
	}
	
	synchronized public boolean addVmArgument(String vmArgument) {
		if ( !vmArguments.contains(vmArgument) ) {
			vmArguments.add(vmArgument) ;
			return true ;
		}
		else {
			return false ;
		}
	}
	
	synchronized public boolean containsVmArgument(String vmArgument) {
		return vmArguments.contains(vmArgument) ;
	}
	
	synchronized public boolean removeVmArgument(String vmArgument) {
		return vmArguments.remove(vmArgument) ;
	}
	
	synchronized public void clearVmArguments() {
		vmArguments.clear();
	}

	synchronized public String[] getVmArgumentsArray() {
		String[] vmArgs = ArrayUtils.toArray(vmArguments, new String[vmArguments.size()]) ;
		return vmArgs;
	}

	private HashMap<String, String> vmProperties = new HashMap<>() ;
	
	@SuppressWarnings("unchecked")
	public Map<String, String> getVmProperties() {
		return (Map<String, String>) vmProperties.clone() ; 
	}
	
	synchronized public void setVMProperty(String key) {
		setVMProperty(key, "");
	}
	
	synchronized public void setVMProperty(String key, String value) {
		vmProperties.put(key, value) ;
	}

	synchronized public String getVMProperty(String key) {
		return vmProperties.get(key) ;
	}
	
	synchronized public boolean containsVMProperty(String key) {
		return vmProperties.containsKey(key) ;
	}
	
	synchronized public String removeVMProperty(String key) {
		return vmProperties.remove(key) ;
	}
	
	synchronized public void clearVMProperties() {
		vmProperties.clear();
	}
	
	synchronized public String[] getVmPropertiesArray() {
		String[] vmProps = new String[ vmProperties.size() ] ;
		int vmPropsSz = 0 ;
		
		for (Entry<String, String> entry : vmProperties.entrySet()) {
			String value = entry.getValue() ;
			
			if (value == null || value.isEmpty()) {
				vmProps[vmPropsSz++] = "-D"+ entry.getKey() ;	
			}
			else {
				vmProps[vmPropsSz++] = "-D"+ entry.getKey() +"="+ value;		
			}
			
		}
		return vmProps;
	}
	
	synchronized public String[] getJVMProcessArguments() {
		String[] vmArgs = getVmArgumentsArray();
		
		String[] vmProps = getVmPropertiesArray();
		
		String[] processArgs = ArrayUtils.joinToStrings(
			vmArgs ,
			vmProps ,
			"-cp" , ArrayUtils.joinInSingleString( getClassPathDelimiter() , getClassPath() ) ,
			mainClass ,
			arguments
		) ;
		
		return processArgs;
	}

	
	private ProcessRunner processRunner ;
	
	synchronized public ProcessRunner getProcessRunner() {
		return processRunner;
	}
	
	synchronized public boolean isRunning() {
		return processRunner != null ;
	}
	
	public void execute() throws IOException {
		execute(false);
	}
	
	synchronized public void execute( boolean redirectErrorToNormalOutput ) throws IOException {
		execute(redirectErrorToNormalOutput, null);
	}
	
	synchronized public void execute( boolean redirectErrorToNormalOutput, OutputConsumerListener outputListener ) throws IOException {
		
		String[] processArgs = getJVMProcessArguments();
		
		this.processRunner = new ProcessRunner(getJvmBinary() , processArgs) ;
		
		if (outputListener != null) {
			this.processRunner.setOutputConsumerListener(outputListener);
		}
		
		this.processRunner.execute(redirectErrorToNormalOutput) ;
		
	}


	public boolean isRunningProcess() {
		synchronized (this) {
			if (processRunner == null) return false ;
		}
		
		return processRunner.isRunningProcess();
	}

	public boolean isExecutionFinished() {
		synchronized (this) {
			if (processRunner == null) return false ;
		}
		
		return processRunner.isExecutionFinished();
	}

	public OutputConsumer getOutputConsumer() {
		synchronized (this) {
			if (processRunner == null) return null ;
		}
		
		return processRunner.getOutputConsumer();
	}

	public OutputConsumer getErrorConsumer() {
		synchronized (this) {
			if (processRunner == null) return null ;
		}
		
		return processRunner.getErrorConsumer();
	}

	public OutputStream getProcessInput() {
		ProcessRunner processRunner = this.processRunner ;
		return processRunner != null ? processRunner.getProcessInput() : null ;
	}
	
	public int waitForProcess() throws InterruptedException {
		synchronized (this) {
			if (processRunner == null) return -1 ;
		}
		
		return processRunner.waitForProcess();
	}

	public int waitForProcess(boolean waitForOutputConsumers) throws InterruptedException {
		synchronized (this) {
			if (processRunner == null) return -1 ;
		}
		
		return processRunner.waitForProcess(waitForOutputConsumers);
	}

	public boolean destroyProcess() {
		synchronized (this) {
			if (processRunner == null) return false ;
		}
		
		return processRunner.destroyProcess();
	}
	
	
	
	
	
}

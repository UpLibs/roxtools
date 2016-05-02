package roxtools.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import roxtools.FileUtils;
import roxtools.SerializationUtils;

public class RoxCompiler<T> {
	
	static final public String ROX_COMPILER_CACHE_ROOT = System.getProperty("ROX_COMPILER_CACHE_ROOT") ;
	static final public boolean IGNORE_ROX_COMPILER_CACHE_ROOT = System.getProperty("IGNORE_ROX_COMPILER_CACHE_ROOT") != null ;

	static {
		FileUtils.setTemporaryFileToClassicPath();
	}
	
	///////////////////////////////////////////////////

	static final private ArrayList<MyRef> instances = new ArrayList<MyRef>() ;
	
	@SuppressWarnings("rawtypes")
	static private class MyRef extends WeakReference<RoxCompiler>{

		final private File compilerClassPathCache ;
		
		public MyRef(RoxCompiler referent) {
			super(referent);
			
			compilerClassPathCache = referent.getCompilerClassPathCache() ;
		}
		
		public File getCompilerClassPathCache() {
			return compilerClassPathCache;
		}
	}
	
	
	///////////////////////////////////////////////////
	
	private final int id ;
	private final ClassLoaderImpl classLoader;
	private final File compilerClassPathCache ;
	private final JavaCompiler compiler;
	private final List<String> options;

	private DiagnosticCollector<JavaFileObject> diagnostics;
	private final FileManagerImpl javaFileManager;

	static private AtomicInteger idCounter = new AtomicInteger(0) ;
	
	public RoxCompiler(ClassLoader loader) {
		this(loader, new String[0]) ;
	}
	
	public RoxCompiler(ClassLoader loader, String... options) {
		this(loader,  null, options) ;
	}
	
	public RoxCompiler(ClassLoader loader, URL[] classpaths, String... options) {
		if (classpaths == null) {
			if ( loader instanceof URLClassLoader ) {
				URLClassLoader urlCp = (URLClassLoader) loader ;
				classpaths = urlCp.getURLs() ;
			}
		}
		
		compiler = ToolProvider.getSystemJavaCompiler();
		
		if (compiler == null) {
			throw new IllegalStateException("Cannot find the system Java compiler. Check that your class path includes tools.jar");
		}
		
		this.id = idCounter.incrementAndGet() ;
		
		File compilerClassPathCache = null ;
		
		if (!IGNORE_ROX_COMPILER_CACHE_ROOT) {
			if ( ROX_COMPILER_CACHE_ROOT != null && !ROX_COMPILER_CACHE_ROOT.isEmpty() ) {
				File cacheRoot = new File(ROX_COMPILER_CACHE_ROOT) ;
				compilerClassPathCache = new File(cacheRoot , "roxcompiler-classpath-"+ System.currentTimeMillis() +"-"+ id +"-cache") ;
				compilerClassPathCache.mkdirs() ;
			}
			
			if (compilerClassPathCache == null) {
				try {
					compilerClassPathCache = FileUtils.createTempDirectory("roxcompiler-classpath-", "-"+id+"-cache") ;
				}
				catch (IOException e1) {
					e1.printStackTrace(); 
				}
			}
		}
		
		this.compilerClassPathCache = compilerClassPathCache ;
		
		classLoader = new ClassLoaderImpl(loader);
		diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		JavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		
		javaFileManager = new FileManagerImpl(fileManager, classLoader, compilerClassPathCache);
		
		this.options = new ArrayList<String>();
		for (String option : options) {
			this.options.add(option);
		}
		
		String optionsClassPath = "" ;
		String pathSep = System.getProperty("path.separator") ;
		
		
		if ( classpaths != null && classpaths.length > 0 ) {
			StringBuilder str = new StringBuilder() ;
			
			for (URL cp : classpaths) {
				try {
					URI uri = cp.toURI() ;
					
					String scheme = uri.getScheme() ;
					
					if ( scheme == null || !scheme.equalsIgnoreCase("file") ) {
						System.err.println("-- "+ RoxCompiler.class.getName() +"> Can't handle URI: "+ uri);
						continue ;
					}
					
					File cpFile = new File( cp.toURI() ) ;

					if (str.length() > 0) str.append(pathSep) ;
					str.append( cpFile.toString() ) ;
				}
				catch (Exception e) {
					new IllegalStateException("Error handling claspath, should be a file: "+ cp, e).printStackTrace(); 
				}
			}
			
			if ( str.length() > 0 ) {
				String sysClassPath = System.getProperty("java.class.path") ;
				optionsClassPath += sysClassPath + pathSep + str.toString() ;
			}
		}
		
		if (this.compilerClassPathCache != null) {
			if (!optionsClassPath.isEmpty()) optionsClassPath += pathSep ;
			optionsClassPath += this.compilerClassPathCache.toString() ;
		}
		
		
		this.options.add("-classpath") ;
		this.options.add(optionsClassPath) ;
		
		synchronized (instances) {
			instances.add( new MyRef(this) ) ;
		}
	}

	public int getId() {
		return id;
	}
	
	public File getCompilerClassPathCache() {
		return compilerClassPathCache;
	}
	
	/**
	 * <p>Compile Java source in <var>javaSource</var> and return the resulting
	 * class.</p>
	 * 
	 * Thread safety: this method is thread safe if the <var>javaSource</var>
	 * and <var>diagnosticsList</var> are isolated to this thread.
	 * 
	 * @param qualifiedClassName
	 *            The fully qualified class name.
	 * @param javaSource
	 *            Complete java source, including a package statement and a
	 *            class, interface, or annotation declaration.
	 *            
	 * @return a Class which is generated by compiling the source
	 * @throws RoxCompilerException
	 *             if the source cannot be compiled - for example, if it
	 *             contains syntax or semantic errors or if dependent classes
	 *             cannot be found.
	 * @throws ClassCastException
	 *             if the generated class is not assignable to all the optional
	 *             <var>types</var>.
	 */
	public synchronized Class<T> compile(String qualifiedClassName, CharSequence javaSource) throws RoxCompilerException, ClassCastException {
		Map<String, CharSequence> classes = new HashMap<String, CharSequence>(1);
		classes.put(qualifiedClassName, javaSource);
		
		Map<String, Class<T>> compiled = compile(classes);
		Class<T> newClass = compiled.get(qualifiedClassName);
		
		return newClass ;
	}

	/**
	 * Compile multiple Java source strings and return a Map containing the
	 * resulting classes.
	 * <p>
	 * Thread safety: this method is thread safe if the <var>classes</var> and
	 * <var>diagnosticsList</var> are isolated to this thread.
	 * 
	 * @param classes
	 *            A Map whose keys are qualified class names and whose values
	 *            are the Java source strings containing the definition of the
	 *            class. A map value may be null, indicating that compiled class
	 *            is expected, although no source exists for it (it may be a
	 *            non-public class contained in one of the other strings.)
	 * 
	 * @return A mapping of qualified class names to their corresponding
	 *         classes. The map has the same keys as the input
	 *         <var>classes</var>; the values are the corresponding Class
	 *         objects.
	 * @throws RoxCompilerException
	 *             if the source cannot be compiled
	 */
	public synchronized Map<String, Class<T>> compile(Map<String, CharSequence> classes) throws RoxCompilerException {
		
		if (compilerClassPathCache != null && !compilerClassPathCache.exists()) {
			compilerClassPathCache.mkdirs() ;
		}
		
		List<JavaFileObject> sources = new ArrayList<JavaFileObject>();
		
		for (Entry<String, CharSequence> entry : classes.entrySet()) {
			String qualifiedClassName = entry.getKey();
			CharSequence javaSource = entry.getValue();
			if (javaSource != null) {
				int dotPos = qualifiedClassName.lastIndexOf('.');
				String classLastName = dotPos == -1 ? qualifiedClassName : qualifiedClassName.substring(dotPos + 1);
				String packageName = dotPos == -1 ? "" : qualifiedClassName.substring(0, dotPos);
				JavaFileObjectMemorySource source = new JavaFileObjectMemorySource(qualifiedClassName, classLastName+Kind.SOURCE.extension, javaSource);
				sources.add(source);

				javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, classLastName + Kind.SOURCE.extension, source);
			}
		}
		
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		CompilationTask task = compiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
		Boolean result = task.call();
		
		// Retry compilation with cache populated:
		if ( (result == null || !result.booleanValue()) && this.compilerClassPathCache != null ) {
			task = compiler.getTask(null, javaFileManager, diagnostics, options, null, sources);
			result = task.call();
		}
		
		if (result == null || !result.booleanValue()) {
			throw new RoxCompilerException("Compilation failed.", classes.keySet(), diagnostics);
		}
		
		try {
			Map<String, Class<T>> compiled = new HashMap<String, Class<T>>();
			for (String qualifiedClassName : classes.keySet()) {
				Class<T> newClass = loadClass(qualifiedClassName);
				compiled.put(qualifiedClassName, newClass);
			}
			return compiled;
		} catch (ClassNotFoundException e) {
			throw new RoxCompilerException(classes.keySet(), e, diagnostics);
		} catch (IllegalArgumentException e) {
			throw new RoxCompilerException(classes.keySet(), e, diagnostics);
		} catch (SecurityException e) {
			throw new RoxCompilerException(classes.keySet(), e, diagnostics);
		}
	}

	@SuppressWarnings("unchecked")
	public Class<T> loadClass(String qualifiedClassName) throws ClassNotFoundException {
		return (Class<T>) classLoader.loadClass(qualifiedClassName);
	}

	static URI toURIClassName(String className, Kind kind) {
		return toURI( className.replaceAll("\\.", "/") + kind.extension ) ;
	}
	
	static URI toURI(String path) {
		try {
			return new URI(path);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public ClassLoader getClassLoader() {
		return javaFileManager.getClassLoader();
	}
	

    ///////////////////////////////////////////////////////////////////////////////

	static final class FileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {
		final private ClassLoaderImpl classLoader;
		final private File compilerClassPathCache;
		final private ClassLocator classLocator ;
	    
		private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

		public FileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader, File compilerClassPathCache) {
			super(fileManager);
			this.classLoader = classLoader;
			this.classLocator = new ClassLocator(classLoader) ;
			this.compilerClassPathCache = compilerClassPathCache ;
		}

		public ClassLoader getClassLoader() {
			return classLoader;
		}

		@Override
		public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
			FileObject o = fileObjects.get(uri(location, packageName, relativeName));
			if (o != null) return o;
			return super.getFileForInput(location, packageName, relativeName);
		}

		public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject objFile) {
			URI key = uri(location, packageName, relativeName) ;
			
			synchronized (fileObjects) {
				if (fileObjects.containsKey(key)) return ;
				fileObjects.put(key, objFile);	
			}
			
			if (
					compilerClassPathCache != null
					&& relativeName.endsWith(".class")
					&& objFile.getKind() == Kind.CLASS
					&& !(objFile instanceof JavaFileObjectMemorySource)
					)
			{
				String classNamePath = packageName.replaceAll("\\.", "/") +"/"+ relativeName ;
				
				File classFileInCache = new File( compilerClassPathCache , classNamePath ) ;
				
				try {
					System.out.println("-- Saved in cache: "+classFileInCache +" > "+ objFile.getClass() +" > "+ objFile);
					byte[] classBytes = SerializationUtils.readAll( objFile.openInputStream() ) ;
					
					File parentFile = classFileInCache.getParentFile() ;
					if (parentFile != null) parentFile.mkdirs() ;
					
					SerializationUtils.writeFile(classFileInCache, classBytes);
					
					classFileInCache.deleteOnExit();
				}
				catch (Exception e) {
					e.printStackTrace(); 
				}
			}
		}

		private URI uri(Location location, String packageName, String relativeName) {
			return RoxCompiler.toURI(location.getName() + '/' + packageName + '/' + relativeName);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject outputFile) throws IOException {
			String classLastName = ClassLocation.parseClassLastName(qualifiedName) ;
			
			JavaFileObject file = new JavaFileObjectMemorySource(qualifiedName, classLastName+kind.extension, kind);
			classLoader.add(qualifiedName, file);
			
			return file;
		}
		
		@Override
		public ClassLoader getClassLoader(JavaFileManager.Location location) {
			return classLoader;
		}
		
		@Override
		public String inferBinaryName(Location loc, JavaFileObject file) {
			try {
				String result;
				if (file instanceof JavaFileObjectMemorySource) result = file.getName();
				else if (file instanceof JavaFileObjectURLClass) result = file.getName();
				else result = super.inferBinaryName(loc, file);
				return result;
			} catch (RuntimeException e) {
				System.err.println("inferBinaryName error: "+ loc +" > "+ file);
				throw e ;
			}
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
			Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
			ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();
			
			if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
				for (JavaFileObject file : fileObjects.values()) {
					if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
						files.add(file);
					}
				}
				files.addAll(classLoader.files());
			}
			else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
				for (JavaFileObject file : fileObjects.values()) {
					if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
						files.add(file);
					}
				}
			}
			
			for (JavaFileObject file : result) {
				files.add(file);
			}
			
			if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
				List<JavaFileObject> classes = getClasses(packageName, recurse) ;	
				
				for (JavaFileObject javaObj : classes) {
					if ( !containsComatibleJavaObjName(files, javaObj) ) {
						files.add(javaObj) ;
					}	
				}
			}
			
			for (JavaFileObject javaFileObject : files) {
				if ( javaFileObject instanceof MyJavaFileObject ) {
					MyJavaFileObject myJavaFileObject = (MyJavaFileObject) javaFileObject ;
					
					String className = myJavaFileObject.getClassName() ;
					String classLastName = myJavaFileObject.getClassLastName() ;
					String packName = myJavaFileObject.getPackageName() ;
					
					classLoader.add(className, javaFileObject);
					
					putFileForInput(StandardLocation.CLASS_PATH, packName, classLastName + Kind.CLASS.extension, javaFileObject);	
				}
				
				
			}
			
			return files;
		}
		
		private boolean containsComatibleJavaObjName(ArrayList<JavaFileObject> files , JavaFileObject javaObj) {
			String name = javaObj.getName().replaceFirst("\\.class$", "").replaceFirst("^.*?([^\\.]+)$", "$1") ;
			
			for (JavaFileObject javaFileObject : files) {
				String name2 = javaFileObject.getName().replaceFirst("\\.class$", "").replaceFirst("^.*?([^\\.]+)$", "$1") ;
					
				if ( name.equals(name2) || name2.endsWith("/"+name) ) return true ;
			}
			
			return false ;
		}
		
	    private List<JavaFileObject> getClasses(String packageName, boolean recurse) throws IOException {
	    	
	    	List<ClassLocation> classLocations = classLocator.getClassesFromPackage(packageName, recurse) ;

	    	ArrayList<JavaFileObject> javaObjs = new ArrayList<JavaFileObject>() ;
	    	
	    	for (ClassLocation classLocation : classLocations) {
	    		JavaFileObjectURLClass javaFileObjectURLClass = new JavaFileObjectURLClass(classLocation) ;
    			javaObjs.add(javaFileObjectURLClass) ;
			}
	    	
	    	return javaObjs;
	    }
	    
	    
	}
	
	abstract static class MyJavaFileObject extends SimpleJavaFileObject {

		private String className ;
		
		public MyJavaFileObject(String className, String classFileName, Kind kind) {
			super(toURIClassName(className, kind), kind);
			this.className = className ;
		}
		
		public String getClassName() {
			return className;
		}
		
		public String getClassLastName() {
			String packageName = getPackageName() ;
			if (packageName.isEmpty()) return className ;
			return className.substring(packageName.length()+1) ;
		}
		
		public String getPackageName() {
			return className.replaceFirst("\\.?[^\\.]+$", "") ;
		}
		
    	abstract public byte[] getByteCode() ;
		
	}
	
    static final class JavaFileObjectMemorySource extends MyJavaFileObject {
    	private ByteArrayOutputStream byteCode;
    	private final CharSequence source;

    	JavaFileObjectMemorySource(String className, String classFileName, CharSequence source) {
    		super(className, classFileName, Kind.SOURCE);
    		this.source = source;
    	}

    	JavaFileObjectMemorySource(String className, String classFileName, Kind kind) {
    		super(className, classFileName, kind);
    		source = null;
    	}

    	@Override
    	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws UnsupportedOperationException {
    		if (source == null) throw new UnsupportedOperationException("getCharContent()");
    		return source;
    	}

    	@Override
    	public InputStream openInputStream() {
    		return new ByteArrayInputStream(getByteCode());
    	}

    	@Override
    	public OutputStream openOutputStream() {
    		byteCode = new ByteArrayOutputStream();
    		return byteCode;
    	}
    	
    	public byte[] getByteCode() {
    		return byteCode.toByteArray();
    	}
    }

    static final class JavaFileObjectURLClass extends MyJavaFileObject {
    	private final URL classURL;

    	JavaFileObjectURLClass(ClassLocation classLocation) {
    		this( classLocation.getClassName() , classLocation.getClassLastName()+".class", classLocation.getClassURL() ) ;
    	}
    	
    	JavaFileObjectURLClass(String className, String classFileName, URL classURL) {
    		super(className, classFileName, Kind.CLASS);
    		this.classURL = classURL;
    	}
    	
    	@Override
    	public InputStream openInputStream() {
    		try {
    			return this.classURL.openStream() ;
    		}
    		catch (Exception e) {
    			throw new RuntimeException(e) ;
    		}
    	}
    	
    	public byte[] getByteCode() {
    		try {
    			byte[] bytes = SerializationUtils.readAll( openInputStream() ) ;
        		return bytes ;
			}
    		catch (IOException e) {
				throw new IllegalStateException(e) ;
			}
    	}
    	
    	@Override
    	public String toString() {
    		return super.toString() +"@"+ this.classURL;
    	}
    }

    static final class ClassLoaderImpl extends ClassLoader {
    	private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();
    	
    	ClassLoaderImpl(ClassLoader parentClassLoader) {
    		super(parentClassLoader);
    	}

		Collection<JavaFileObject> files() {
			synchronized (classes) {
				return Collections.unmodifiableCollection(classes.values());	
			}
    	}

    	@Override
    	protected Class<?> findClass(String qualifiedClassName) throws ClassNotFoundException {
    		JavaFileObject file ;
    		
    		synchronized (classes) {
    			file = classes.get(qualifiedClassName);
			}
    		
    		if (file != null) {
    			byte[] bytes = ((MyJavaFileObject) file).getByteCode();
    			return defineClass(qualifiedClassName, bytes, 0, bytes.length);
    		}
    		
    		// Workaround for "feature" in Java 6
    		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
    		try {
    			Class<?> c = Class.forName(qualifiedClassName);
    			return c;
    		} catch (ClassNotFoundException nf) {
    			// Ignore and fall through
    		}
    		
    		return super.findClass(qualifiedClassName);
    	}

    	void add(String qualifiedClassName, JavaFileObject javaFile) {
    		synchronized (classes) {
    			if ( !classes.containsKey(qualifiedClassName) ) {
    				classes.put(qualifiedClassName, javaFile);	
    			}
			}
    	}

    	@Override
    	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    		return super.loadClass(name, resolve);
    	}

    	@Override
    	public InputStream getResourceAsStream(String name) {
    		if (name.endsWith(".class")) {
    			String qualifiedClassName = name.substring(0, name.length() - ".class".length()).replace('/', '.');
    			
    			MyJavaFileObject file ;
    			
    			synchronized (classes) {
    				file = (MyJavaFileObject) classes.get(qualifiedClassName);
				}
    			
    			if (file != null) {
    				return new ByteArrayInputStream(file.getByteCode());
    			}
    		}
    		
    		return super.getResourceAsStream(name);
    	}

    	
    }
    
    public void clearCompilerClassPathCacheFiles() {
    	clearCompilerClassPathCacheFiles(true);
    }
    
    public void clearCompilerClassPathCacheFiles(boolean deleteCacheDir) {
    	if (compilerClassPathCache == null) return ;
    	
    	deleteCompilerClassPathCache(compilerClassPathCache, deleteCacheDir) ;
    }
    

	static private void deleteCompilerClassPathCache(File dir, boolean deletetDir) {
		if (dir == null) return ;
		
		File[] files = dir.listFiles() ;
		if (files == null) return ;
		
		for (File file : files) {
			if ( file.isDirectory() && !file.isFile() ) {
				deleteCompilerClassPathCache(file, false);
			}
			
			file.delete() ;
		}
		
		if (deletetDir) {
			dir.delete();
		}
	}
	
	///////////////////////////////////////////////
	
	static {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				clearInstances();
			}
		});
		
	}
	
	static private void clearInstances() {
		
		synchronized (instances) {
			for (MyRef myRef : instances) {
				deleteCompilerClassPathCache( myRef.getCompilerClassPathCache() , true );
			} 
			
			instances.clear();
		}
		
	}

	
}



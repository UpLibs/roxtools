package roxtools.compiler;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.slf4j.Logger;

import roxtools.SerializationUtils;

final public class JavaSourceRunner {
	
	private static Logger logger ;
	
	static private void logError(String msg, Throwable e) {
		Logger log = log() ;
		if (log == null) {
			System.err.println("[ERROR]: "+msg);
			if (e != null) e.printStackTrace();
			return ;
		}
		log.error(msg, e);
	}
	
	static private Logger log() {
		if ( logger == null ) {
			synchronized (JavaSourceRunner.class) {
				if ( logger != null ) return logger ;
				
				try {
					logger = getLogger(JavaSourceRunner.class);	
				}
				catch (Throwable e) {
					return null;
				}
					
			}
		}
		return logger ;
	}
	
	static private class Source {
		
		private File codeFile ;
		private String code ;
		
		public Source(File codeFile) throws IOException {
			this( new String( SerializationUtils.readFile(codeFile) ) ) ;
			this.codeFile = codeFile ;
		}
		
		public Source(String code) {
			this.code = code ;
		}
		
		public String getCode() {
			return code;
		}
		
		public String parsePackage() {
			Pattern pattern = Pattern.compile("package[ \\t]+([^\\s;]+)") ;
			
			Matcher matcher = pattern.matcher(code) ;
			
			if ( matcher.find() ) {
				return matcher.group(1) ;
			}
			else {
				return null ;
			}
		}
		
		public String parseClassName() {
			Pattern pattern = Pattern.compile("\\sclass[ \\t]+(\\S+)") ;
			
			Matcher matcher = pattern.matcher(code) ;
			
			if ( matcher.find() ) {
				return matcher.group(1) ;
			}
			else {
				return null ;
			}
		}
		
		public String getFullClassName() {
			return parsePackage() +"."+ parseClassName() ;
		}
		
		@Override
		public String toString() {
			return this.getClass().getName()+"["+ ( codeFile != null ? "codeFile: "+codeFile.toString() +" ; codeSize: "+ codeFile.length() : "codeSize: "+ code.length()) +" ; "+ getFullClassName() +"]" ;
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("rawtypes")
	final static private RoxCompiler<?> COMPILER = new RoxCompiler(JavaSourceRunner.class.getClassLoader()) ;
	
	static public boolean execute( Source source , String[] args ) {
		
		try {
			Class<?> clazz = COMPILER.compile( source.getFullClassName() , source.getCode() ) ;
			
			try {
				Method method = clazz.getDeclaredMethod("main", String[].class) ;
				method.setAccessible(true);
				
				if ( !Modifier.isStatic( method.getModifiers() ) ) {
					logError("Found main method that is not static: "+ method +" Source: "+ source, null);
				}
				
				try {
					if ( args == null ) {
						method.invoke(null, new Object[] { new String[0] }) ;
					}
					else {
						method.invoke(null, new Object[] { args }) ;	
					}
					
					return true ;
				}
				catch (IllegalAccessException | IllegalArgumentException e) {
					logError("Can't execute main method class in code: "+ source, e);
					return false ;
				}
				catch (InvocationTargetException e) {
					logError("Can't execute main method class in code: "+ source, e);
					logError("Execution cause: "+ source, e.getCause());
					return false ;
				}
			}
			catch (NoSuchMethodException | SecurityException e) {
				
				try {
					Method method = clazz.getDeclaredMethod("run") ;
					method.setAccessible(true);
					
					Object object;
					try {
						object = clazz.newInstance();
					}
					catch (InstantiationException | IllegalAccessException e2) {
						logError("Can't create class instance to call run() method: "+ source, e2);
						return false ;
					}
					
					try {
						method.invoke(object) ;
						return true ;
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						logError("Can't execute run() method class in code: "+ source, e1);
						return false ;
					}
				}
				catch (NoSuchMethodException | SecurityException e1) {
					logError("Can't find main(String[]) or run() method in class: "+ source, e1);
					return false ;
				}
				
			}
		}
		catch (ClassCastException e) {
			logError("Can't compile source: "+ source, e);
			
			return false ;
		}
		catch (RoxCompilerException e) {
			logError("Can't compile source: "+ source, e);
			
			DiagnosticCollector<JavaFileObject> diagnostics = e.getDiagnostics() ;
			
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				logError("Compile diagnostic: "+ diagnostic, null);
			}
			
			if ( e.getCause() != null ) {
				logError("Compile cause", e.getCause());	
			}
			
			return false ;
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////

	private static void showHelp() {
		System.out.println("--------------------------------------------------------------");
		System.out.println( JavaSourceRunner.class.getName() );
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -h       Help.");
		System.out.println("  -e       Execute code.");
		System.out.println("  -f       Execute file.");
		System.out.println();
		System.out.println("Example 1:");
		System.out.println("  $> java "+ JavaSourceRunner.class.getName() +" -e \"...CODE TO COMPILE AND RUN...\"");
		System.out.println();
		System.out.println("Example 2:");
		System.out.println("  $> java "+ JavaSourceRunner.class.getName() +" -f file/path/ClassFoo.java");
		System.out.println();
		System.out.println("--------------------------------------------------------------");
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		
		if ( args.length == 0 || args[0].equals("-h") ) {
			showHelp();
		}
		else if ( args[0].equals("-e") && args.length >= 2 ) {
			Source source = new Source(args[1]) ;
			
			String[] codeArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0] ;
			
			execute(source, codeArgs) ;
		}
		else if ( args[0].equals("-f") && args.length >= 2 ) {
			Source source = new Source(new File(args[1])) ;
			
			String[] codeArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0] ;
			
			execute(source, codeArgs) ;
		}
		else {
			showHelp();
		}
		
	}

	
}

package roxtools;

import java.util.Map;
import java.util.Map.Entry;

final public class JVMInfos {

	static public String getInfos() {
		StringBuilder str = new StringBuilder() ;
		
		str.append("MEMORY: ") ;
		str.append( getMemoryInfos() ) ;
		str.append("\n") ;
		
		str.append("\nJVM THREADS: \n\n") ;
		str.append( getThreadInfos() ) ;
		
		return str.toString() ;
	}
	
	static public String getMemoryInfos() {
		Runtime runtime = Runtime.getRuntime() ;
		
		long totalMemory = runtime.totalMemory() ;
		long freeMemory = runtime.freeMemory() ;
		long usedMemory = totalMemory - freeMemory ;
		
		return "used:"+ usedMemory +" + free:"+ freeMemory +" / total:"+ totalMemory ;
	}
	
	static public String getThreadInfos() {
		StringBuilder str = new StringBuilder() ;
		
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces() ;
		
		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
			
			Thread thread = entry.getKey() ;
			
			str.append(thread) ;
			str.append(": ") ;
			str.append( thread.getState() ) ;
			
			str.append("\n") ;
			
			StackTraceElement[] stack = entry.getValue() ;
			
			for (StackTraceElement stackTraceElement : stack) {
				str.append("    ") ;
				str.append( stackTraceElement.toString() ) ;
				str.append("\n") ;
			}
			
			str.append("\n") ;
			
		}
		
		return str.toString() ;
	}
	
	public static void main(String[] args) {
		
		System.out.println( getInfos() );
		
	}
	
}

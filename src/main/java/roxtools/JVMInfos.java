package roxtools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

final public class JVMInfos {

	static public String getInfos() {
		StringBuilder str = new StringBuilder() ;
		
		str.append("MEMORY:\n\n    ") ;
		str.append( getMemoryInfos() ) ;
		str.append("\n") ;
		
		str.append("\nJVM: \n\n") ;
		str.append("    java.vm.name: ") ;
		str.append( System.getProperty("java.vm.name") ) ;
		str.append("\n") ;
		
		str.append("    java.version: ") ;
		str.append( System.getProperty("java.version") ) ;
		str.append("\n") ;
		
		str.append("    java.vendor: ") ;
		str.append( System.getProperty("java.vendor") ) ;
		str.append("\n") ;
		
		str.append("\nTHREADS: \n\n") ;
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
		
		ArrayList<Thread> threads = new ArrayList<Thread>() ;
		
		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
			threads.add( entry.getKey() ) ;
		}
		
		Collections.sort(threads , new Comparator<Thread>() {
			@Override
			public int compare(Thread o1, Thread o2) {
				return Long.compare( o1.getId() , o2.getId() );
			}
		});
		
		Thread currentThread = Thread.currentThread() ;
		
		for (Thread thread : threads) {
			
			if ( thread == currentThread ) str.append("** ") ;
			
			str.append(thread) ;
			str.append(": ") ;
			str.append( thread.getState() ) ;
			
			str.append("\n") ;
			
			StackTraceElement[] stack = allStackTraces.get(thread) ;
			
			for (StackTraceElement stackTraceElement : stack) {
				str.append("    ") ;
				str.append( stackTraceElement.toString() ) ;
				str.append("\n") ;
			}
			
			str.append("\n") ;
			
		}
		
		return str.toString() ;
	}

}

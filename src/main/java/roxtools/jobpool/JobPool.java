package roxtools.jobpool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * 
 * <h1>Jobs Pool:</h1>
 * <p>
 * A proxy over generic objects for remote and parallel execution.
 * 
 * @author gracilianomp
 *
 */
public class JobPool {
	
	final static private class MyMethodFilter implements MethodFilter {
		@Override
		public boolean isHandled(Method method) {
			if ( method.getDeclaringClass() == Object.class ) return false ;
			if ( method.getName().equals("toString") && method.getParameterTypes().length == 0 ) return false ;
			return true ;
		}
	}

	///////////////////////////////
	
	private final JobPoolExecutor executor ;
	
	public JobPool( JobPoolExecutor executor ) {
		this.executor = executor ;
	}
	
	private ArrayList<JobCall> callsQueue = new ArrayList<JobCall>() ;
	
	final private class MyMethodHandler implements MethodHandler {

		final private Class<?> codeClass ;
		
		public MyMethodHandler(Class<?> codeClass) {
			this.codeClass = codeClass;
		}

		@Override
		public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
			JobCall call = new JobCall(codeClass, self, thisMethod, proceed, args) ;
			
			synchronized (callsQueue) {
				callsQueue.add(call) ;
			}
			
			if ( proceed.getReturnType() == JobResult.class ) {
				return call.getResult() ;
			}
			else {
				return null ;
			}
		}
		
	}
	
	public void clearCallsQueue() {
		synchronized (callsQueue) {
			callsQueue.clear() ;
		}
	}
	
	public int getCallsQueueSize() {
		synchronized (callsQueue) {
			return callsQueue.size() ;
		}
	}
	
	///////////////////////////////
	
	final private HashMap<Class<?>, MyMethodHandler> methodHandlers = new HashMap<Class<?>, MyMethodHandler>() ;
	
	public MyMethodHandler getMethodHandler(Class<?> clazz) {
		synchronized (methodHandlers) {
			MyMethodHandler methodHandler = methodHandlers.get(clazz) ;
			if (methodHandler != null) return methodHandler ;
		}
		
		synchronized (clazz) {
			synchronized (methodHandlers) {
				MyMethodHandler methodHandler = methodHandlers.get(clazz) ;
				if (methodHandler != null) return methodHandler ;
			}
			
			MyMethodHandler methodHandler = new MyMethodHandler(clazz) ;
			
			synchronized (methodHandlers) {
				methodHandlers.put(clazz, methodHandler) ;
			}
			
			return methodHandler ;
		}
	}
	
	///////////////////////////////
	
	final private HashMap<Class<?>, ProxyFactory> proxyFactories = new HashMap<Class<?>, ProxyFactory>() ;
	
	private ProxyFactory getProxyFactory(Class<?> clazz) {
		synchronized (proxyFactories) {
			ProxyFactory proxyFactory = proxyFactories.get(clazz) ;
			if (proxyFactory != null) return proxyFactory ;
		}
		
		synchronized (clazz) {
			synchronized (proxyFactories) {
				ProxyFactory proxyFactory = proxyFactories.get(clazz) ;
				if (proxyFactory != null) return proxyFactory ;
			}
			
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setSuperclass(clazz);

			proxyFactory.setFilter(new MyMethodFilter());
		
			synchronized (proxyFactories) {
				proxyFactories.put(clazz, proxyFactory) ;	
			}
			
			return proxyFactory ;
		}
	}
	
	public <T> T newJob(Class<T> clazz) {
		ProxyFactory proxyFactory = getProxyFactory(clazz) ;
		
		MyMethodHandler methodHandler = getMethodHandler(clazz) ;
		
		try {
			@SuppressWarnings("unchecked")
			T job = (T) proxyFactory.create(new Class<?>[0], new Object[0], methodHandler) ;
			return job ;
		}
		catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e) ;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public JobResult[] executeJobs() {
		JobCall[] calls ;
		
		synchronized (this.callsQueue) {
			calls = this.callsQueue.toArray( new JobCall[this.callsQueue.size()] ) ;
			
			this.callsQueue.clear(); 
		}
		
		JobResult[] jobResults = new JobResult[calls.length] ;
		
		for (int i = 0; i < calls.length; i++) {
			JobCall call = calls[i];
			JobResult result = call.getResult() ;
			result.setDispatched();
			jobResults[i] = result ;
		}
		
		executor.executeJobs(calls) ;
		
		return jobResults ;
		
	}

}

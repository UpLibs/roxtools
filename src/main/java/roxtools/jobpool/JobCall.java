package roxtools.jobpool;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final public class JobCall implements Serializable , Runnable {
	private static final long serialVersionUID = -5375821922993700225L;
	
	final private Object obj;
	final private Method method ;
	final private Object[] args ;
	
	@SuppressWarnings("rawtypes")
	final private JobResult result ;
	
	@SuppressWarnings("rawtypes")
	protected JobCall(Object obj, Method method, Object[] args) {
		this.obj = obj;
		this.method = method;
		this.args = args;
		
		this.result = new JobResult() ;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	@SuppressWarnings("rawtypes")
	public JobResult getResult() {
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setResult(Object result) {
		if (result != null && result.getClass() == JobResult.class) {
			result = ((JobResult)result).getResult() ;
		}
		
		this.result.setResult(result);
	}
	
	public void setError(Throwable error) {
		result.setError(error);
	}

	public Object invokeLocal() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return method.invoke(obj, args) ;
	}
	
	@Override
	public void run() {
		try {
			Object res = invokeLocal() ;
			setResult(res);
		}
		catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace() ;
			setError(e);
		}
		catch (InvocationTargetException e) {
			e.printStackTrace() ;
			setError(e);
		}	
	}
	
}
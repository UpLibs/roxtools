package roxtools.threadpool;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RoxTaskPool {
	
	private final ArrayList<RoxTask> tasks = new ArrayList<RoxTask>() ;
	
	private final ExecutorService threadPool ;

	public RoxTaskPool() {
		this( RoxThreadPool.DEFAULT_THREAD_POOL ) ;
	}
	
	public RoxTaskPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}
	
	public <T extends RoxTask> void executeTask( T task ) {
		synchronized (this.tasks) {
			allTasksFinished = false ;
			
			this.tasks.add(task) ;
		}
		
		threadPool.execute(task);
	}
	
	public <T extends RoxTask> void executeTasks( @SuppressWarnings("unchecked") T... tasks ) {
		
		synchronized (this.tasks) {
			allTasksFinished = false ;
			
			for (T task : tasks) {
				this.tasks.add(task) ;	
			}
		}
		
		for (T task : tasks) {
			threadPool.execute(task);	
		}
		
	}
	
	public <T extends RoxTask> void executeTasks( List<T> tasks ) {
		
		synchronized (this.tasks) {
			allTasksFinished = false ;
			
			for (T task : tasks) {
				this.tasks.add(task) ;	
			}
		}
		
		for (T task : tasks) {
			threadPool.execute(task);	
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public List<RoxTask> getTasks() {
		synchronized (tasks) {
			return (List<RoxTask>) tasks.clone() ;
		}
	}
	
	public int getTasksSize() {
		synchronized (tasks) {
			return tasks.size() ;
		}
	}
	
	public void waitTasksVerbose(String message) {
		System.out.println("-- Waiting tasks["+ getTasksSize() +"]: "+ message);
		waitTasks();
		System.out.println("-- Finished tasks: "+ message);
	}
	
	private boolean allTasksFinished = false ;
	
	public void waitTasks() {
		synchronized (tasks) {
			if (allTasksFinished) return ;
			
			for (RoxTask roxTask : tasks) {
				roxTask.waitFinished() ;
			}
			
			allTasksFinished = true ;
		}
	}
	
	public void clearFinishedTasks() {
		synchronized (tasks) {
			allTasksFinished = false ;
			
			for (int i = 0; i < tasks.size() ; i++) {
				RoxTask task = tasks.get(i) ;
				
				if (task.isFinished()) {
					tasks.remove(i) ;
				}
				else {
					i++ ;
				}
			}
			
		}
	}

	public Object[] grabTasksResults() {
		synchronized (tasks) {
			return grabTasksResults( new Object[ tasks.size() ] ) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] grabTasksResults(T[] results) {
		synchronized (tasks) {
			waitTasks();
			
			int sz = tasks.size() ;
			
			if (results == null || results.length != sz) {
				Class<? extends Object[]> type = results.getClass() ;
				
				T[] array =
							( (Object)type == (Object)Object[].class )
							?
							(T[]) new Object[sz]
							:
							(T[]) Array.newInstance(type.getComponentType(), sz)
							;
							
				results = array ;
			}
			
			for (int i = 0; i < sz; i++) {
				results[i] = (T) tasks.get(i).getResult() ;
			}
			
			return results ;
		}
	}
	
	public List<Throwable> grabTasksErros() {
		synchronized (tasks) {
			waitTasks();
			
			int sz = tasks.size() ;
			
			ArrayList<Throwable> errors = new ArrayList<Throwable>() ;
			
			for (int i = 0; i < sz; i++) {
				Throwable error = tasks.get(i).getError() ;
				if (error != null) errors.add(error) ;
			}
			
			return errors ;
		}
	}
	
	public boolean printTasksErros() {
		synchronized (tasks) {
			waitTasks();
			
			int sz = tasks.size() ;
			
			boolean hasErrors = false ;
			
			for (int i = 0; i < sz; i++) {
				Throwable error = tasks.get(i).getError() ;
				if (error != null) {
					hasErrors = true ;
					error.printStackTrace(); 
				}
			}
			
			return hasErrors ;
		}
	}
	
	public boolean hasTasksErros() {
		synchronized (tasks) {
			waitTasks();
			
			int sz = tasks.size() ;
			
			for (int i = 0; i < sz; i++) {
				if ( tasks.get(i).hasError() ) return true ;
			}
			
			return false ;
		}
	}
	
	public int countFinishedTasks() {
		synchronized (tasks) {
			int count = 0 ;
			
			for (RoxTask roxTask : tasks) {
				if ( roxTask.isFinished() ) count++ ;
			}
			
			return count ;
		}
	}
	

	private final HashMap<String, Object> properties = new HashMap<String, Object>() ;
	
	public Object getProperty(String key) {
		synchronized (properties) {
			return properties.get(key) ;
		}
	}
	
	public Object removeProperty(String key) {
		synchronized (properties) {
			return properties.remove(key) ;
		}
	}
	
	public void setProperty(String key, Object val) {
		synchronized (properties) {
			properties.put(key, val) ;
			properties.notifyAll(); 
		}
	}
	
	public Object waitProperty(String key) {
		synchronized (properties) {
			while ( !properties.containsKey(key) ) {
				try {
					properties.wait();
				} catch (InterruptedException e) {}
			}
			return properties.get(key) ;
		}
	}
	
	public Object waitProperty(String key, long timeout) {
		synchronized (properties) {
			long init = System.currentTimeMillis() ;
			
			while ( !properties.containsKey(key) ) {
				long delay = System.currentTimeMillis() - init ;
				long timeLeft = timeout - delay ;
				
				if (timeLeft > 0) {
					try { properties.wait(timeLeft) ;} catch (InterruptedException e) {}
				}
				else {
					break ;
				}
			}
			
			return properties.get(key) ;
		}
	}
	
	
}

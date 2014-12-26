package roxtools.threadpool;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

final public class RoxTaskPool {
	
	private final ArrayList<RoxTask> tasks = new ArrayList<RoxTask>() ;
	
	private final ExecutorService threadPool ;

	public RoxTaskPool() {
		this( RoxThreadPool.DEFAULT_THREAD_POOL ) ;
	}
	
	public RoxTaskPool(int maxThreads) {
		this( RoxThreadPool.newThreadPool(maxThreads) ) ;
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
	

	public boolean isAllTasksFinished() {
		synchronized (tasks) {
			if (allTasksFinished) return true ;
			
			for (RoxTask roxTask : tasks) {
				if ( !roxTask.isFinished() ) return false ;
			}
			
			allTasksFinished = true ;
			return true ;
		}
	}
	
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
	
	public long getFirstTaskInitTime() {
		synchronized (tasks) {
			if ( tasks.isEmpty() ) return 0 ;
			
			long minTime = Long.MAX_VALUE ;
			
			for (RoxTask roxTask : tasks) {
				if (!roxTask.isFinished()) continue ;
				
				long time = roxTask.getInitTime() ;
				if (time < minTime) minTime = time ;
			}
			
			return minTime ;
		}
	}
	
	public long getLastTaskEndTime() {
		synchronized (tasks) {
			if ( tasks.isEmpty() ) return 0 ;
			
			long maxTime = Long.MIN_VALUE ;
			
			for (RoxTask roxTask : tasks) {
				if (!roxTask.isFinished()) continue ;
				
				long time = roxTask.getEndTime() ;
				if (time > maxTime) maxTime = time ;
			}
			
			return maxTime ;
		}
	}
	
	public long[] getTasksInitEndTime() {
		synchronized (tasks) {
			if ( tasks.isEmpty() ) return new long[3] ;
			
			long minTime = Long.MAX_VALUE ;
			long maxTime = Long.MIN_VALUE ;
			long totalExecutedTasks = 0 ;
			
			for (RoxTask roxTask : tasks) {
				if (!roxTask.isFinished()) continue ;
				
				totalExecutedTasks++ ;
				
				long time = roxTask.getEndTime() ;
				
				if (time < minTime) minTime = time ;
				if (time > maxTime) maxTime = time ;
			}
			
			if (totalExecutedTasks == 0) {
				minTime = maxTime = 0 ;
			}
			
			return new long[] { minTime, maxTime, totalExecutedTasks } ;
		}
	}
	
	public int getTotalExecutedTasks() {
		synchronized (tasks) {
			int total = 0 ;
			
			for (RoxTask roxTask : tasks) {
				if ( roxTask.isFinished() ) total++ ;
			}
			
			if (total == tasks.size()) {
				allTasksFinished = true ;
			}
			
			return total ;
		}
	}
	
	public long[] getTasksExecutionTimes() {
		synchronized (tasks) {
			long[] times = new long[tasks.size()] ;
			int timesSz = 0 ;
			
			for (RoxTask roxTask : tasks) {
				if (!roxTask.isFinished()) continue ;
				
				times[timesSz++] = roxTask.getExecutionTime() ;
			}
			
			if (times.length != timesSz) times = Arrays.copyOf(times, timesSz) ;
			
			return times ;
		}
	}

	public long getTasksExecutionTimeToExecuteAll() {
		synchronized (tasks) {
			long timeToExecuteAll = getLastTaskEndTime() - getFirstTaskInitTime() ;
			
			return timeToExecuteAll ;
		}
	}
	
	public long getTasksExecutionTotalThreadTime() {
		synchronized (tasks) {
			long[] executionTime = getTasksExecutionTimes() ;
			
			long total = 0 ;
			
			for (int i = 0; i < executionTime.length; i++) {
				total += executionTime[i] ;
			}
			
			return total ;
		}
	}
	
	public long getTasksExecutionTimeAverage() {
		synchronized (tasks) {
			long[] times = getTasksInitEndTime() ;
			
			long init = times[0] ;
			long end = times[1] ;
			long total = times[2] ;
			
			long timeToExecuteAll = end - init ;
			
			return timeToExecuteAll / total ;
		}
	}
	
	public double getTasksExecutionSpeed() {
		synchronized (tasks) {
			long[] times = getTasksInitEndTime() ;
			
			long init = times[0] ;
			long end = times[1] ;
			long total = times[2] ;
			
			long timeToExecuteAll = end - init ;
			
			double secs = timeToExecuteAll / 1000d ; 
			
			return total / secs ;
		}
	}
	
	public void printExecutionInfos() {
		printExecutionInfos(false);
	}
	
	static public String formatTimeDuration(long time) {
		return (time / (1000L*60)) +"min "+ ( (time / 1000L) % 60 ) +"sec "+ (time % 1000) +"ms"  ;
	}
	
	public void printExecutionInfos(boolean waitAllTasks) {
		synchronized (tasks) {
			if (waitAllTasks) waitTasks() ;
			
			int tasksSize = getTasksSize() ;
			int totalExecutedTasks = getTotalExecutedTasks() ;
			long firstTaskInitTime = getFirstTaskInitTime() ;
			long lastTaskEndTime = getLastTaskEndTime() ;
			long tasksExecutionTimeToExecuteAll = getTasksExecutionTimeToExecuteAll() ;
			long tasksExecutionTotalThreadTime = getTasksExecutionTotalThreadTime() ;
			long tasksExecutionTimeAverage = getTasksExecutionTimeAverage() ;
			double tasksExecutionSpeed = getTasksExecutionSpeed() ;
			
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("Total tasks: "+ tasksSize);
			System.out.println("Total executed tasks: "+ totalExecutedTasks + ( tasksSize == totalExecutedTasks ? " (all executed)" : ""));
			System.out.println();
			System.out.println("Start time (first task): "+ firstTaskInitTime +" > "+ new Date(firstTaskInitTime));
			System.out.println("End time (last task): "+ lastTaskEndTime +" > "+ new Date(lastTaskEndTime));
			System.out.println();
			System.out.println("Time to execute all tasks: "+ tasksExecutionTimeToExecuteAll +"ms ("+ formatTimeDuration(tasksExecutionTimeToExecuteAll) +")");
			System.out.println("Total thread time: "+ tasksExecutionTotalThreadTime +"ms ("+ formatTimeDuration(tasksExecutionTotalThreadTime) +")");
			System.out.println();
			System.out.println("Average time per task: "+ tasksExecutionTimeAverage +"ms ("+ formatTimeDuration(tasksExecutionTimeAverage) +")");
			System.out.println("Tasks execution speed: "+ tasksExecutionSpeed +"/s");
			System.out.println("--------------------------------------------------------------------------------");
			
		}
	}
	
}

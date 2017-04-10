package roxtools.threadpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import roxtools.RoxDeque;

final public class RoxChainTaskPool<Iinit,Ofinal> {
	
	private final ExecutorService threadPool ;
	
	public RoxChainTaskPool() {
		this( RoxThreadPool.DEFAULT_THREAD_POOL ) ;
	}
	
	public RoxChainTaskPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	static public class Chain<I,O> {
		private final RoxChainTaskPool<?,?> chainTaskPool ;

		private Chain(RoxChainTaskPool<?,?> chainTaskPool) {
			this.chainTaskPool = chainTaskPool;
		}
		
		public <O2> Chain<O, O2> add(RoxChainTask<I,O> chainTask) {
			return chainTaskPool.add(chainTask);
		}
		
		@SuppressWarnings("unchecked")
		public <I2,O2> Chain<I2, O2> returnsMultipleElements(Class<I2> outputType) {
			chainTaskPool.getFinalTask().setReturnsMultipleElements(true) ;
			return (Chain<I2, O2>) this ;
		}
	}
	
	private RoxDeque<RoxChainTask<?,?>> tasksChain = new RoxDeque<>() ;
	
	private Chain<?,?> chain = new Chain<>(this) ;
	
	@SuppressWarnings("unchecked")
	public <I,O,O2> Chain<O,O2> add( RoxChainTask<I,O> chainTask ) {
		synchronized (tasksChain) {
			if (started) throw new IllegalStateException("Already started") ;
			
			if (!tasksChain.contains(chainTask)) {
				RoxChainTask<?,I> prev = (RoxChainTask<?, I>) tasksChain.peekLast() ;
				
				if (prev != null) prev.setNext(chainTask);
				chainTask.setPrevious(prev);
				
				tasksChain.addLast(chainTask) ;
			}
		}
		
		return (Chain<O, O2>) chain ;
	}
	
	@SuppressWarnings("unchecked")
	public <O> RoxChainTask<Iinit,O> getInitialTask() {
		synchronized (tasksChain) {
			return (RoxChainTask<Iinit, O>) tasksChain.peekFirst() ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <I> RoxChainTask<I,Ofinal> getFinalTask() {
		synchronized (tasksChain) {
			return (RoxChainTask<I, Ofinal>) tasksChain.peekLast() ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addInitialInputs(Iinit... initialInputs) {
		RoxChainTask<Iinit,?> initialTask = getInitialTask() ;
		
		initialTask.addToInputQueue( Arrays.asList(initialInputs) );
	}
	
	public void addInitialInputs(List<Iinit> initialInputs) {
		RoxChainTask<Iinit,?> initialTask = getInitialTask() ;
		initialTask.addToInputQueue(initialInputs);
	}
	
	public boolean isChainFinished() {
		RoxChainTask<?,Ofinal> finalTask = getFinalTask() ;
		return finalTask.isFinished();
	}
	
	public void waitChainFinished() {
		RoxChainTask<?,Ofinal> finalTask = getFinalTask() ;
		finalTask.waitFinished();
	}
	
	public List<Ofinal> getChainFinalOutput() {
		RoxChainTask<?,Ofinal> finalTask = getFinalTask() ;
		
		finalTask.waitFinished();
		
		RoxDeque<Ofinal> finalResults = finalTask.getFinalResults() ;
		
		return new ArrayList<>( finalResults ) ; 
	}
	
	private boolean started = false ;
	
	public boolean isStarted() {
		synchronized (tasksChain) {
			return started;
		}
	}
	
	public void start() {
		synchronized (tasksChain) {
			if (started) return ;
			started = true ;
			
			for (RoxChainTask<?, ?> roxChainTask : tasksChain) {
				threadPool.execute(roxChainTask);
			}
		}
		
	}
	
}

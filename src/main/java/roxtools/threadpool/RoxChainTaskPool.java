package roxtools.threadpool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import roxtools.RoxDeque;

public class RoxChainTaskPool<Iinit,Ofinal> {
	
	private final ExecutorService threadPool ;
	
	public RoxChainTaskPool() {
		this( RoxThreadPool.DEFAULT_THREAD_POOL ) ;
	}
	
	public RoxChainTaskPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	private RoxDeque<RoxChainTask<?,?>> chain = new RoxDeque<>() ;
	
	public <I,O> void add( RoxChainTask<I,O> chainTask ) {
		synchronized (chain) {
			if (started) throw new IllegalStateException("Already started") ;
			
			if (!chain.contains(chainTask)) {
				@SuppressWarnings("unchecked")
				RoxChainTask<?,I> prev = (RoxChainTask<?, I>) chain.peekLast() ;
				
				if (prev != null) prev.setNext(chainTask);
				chainTask.setPrevious(prev);
				
				chain.addLast(chainTask) ;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <O> RoxChainTask<Iinit,O> getInitialTask() {
		synchronized (chain) {
			return (RoxChainTask<Iinit, O>) chain.peekFirst() ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <I> RoxChainTask<I,Ofinal> getFinalTask() {
		synchronized (chain) {
			return (RoxChainTask<I, Ofinal>) chain.peekLast() ;
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
		synchronized (chain) {
			return started;
		}
	}
	
	public void start() {
		synchronized (chain) {
			if (started) return ;
			started = true ;
			
			for (RoxChainTask<?, ?> roxChainTask : chain) {
				threadPool.execute(roxChainTask);
			}
		}
		
	}
	
}

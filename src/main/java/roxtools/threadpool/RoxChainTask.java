package roxtools.threadpool;

import java.util.List;

import roxtools.Mutex;
import roxtools.RoxDeque;

abstract public class RoxChainTask<I,O> implements Runnable {
	
	static private boolean isQueueEmpty(RoxDeque<?> queue) {
		synchronized (queue) {
			return queue.isEmpty() ;
		}
	}
	
	static private <T> T consumeFromQueue(RoxDeque<T> queue) {
		synchronized (queue) {
			while ( queue.isEmpty() ) {
				try {
					queue.wait();
				} catch (InterruptedException e) {}
			}
			
			T ret = queue.removeFirst() ;
			
			queue.notifyAll();
			
			return ret ;
		}
	}
	
	static private <T> void addToQueue(RoxDeque<T> queue, int limit, T element) {
		synchronized (queue) {
			queue.add(element) ;
			
			if (queue.size() > limit) {
				while (queue.size() > limit/2) {
					try {
						queue.wait();
					} catch (InterruptedException e) {}
				}	
			}
			
			queue.notifyAll(); 
		}
	}
	

	final private int outputQueueLimit ;
	
	public RoxChainTask() {
		this(2);
	}
	
	public RoxChainTask(int outputQueueLimit) {
		this.outputQueueLimit = outputQueueLimit > 1 ? outputQueueLimit : 1 ;
	}
	
	public int getOutputQueueLimit() {
		return outputQueueLimit;
	}
	
	private RoxChainTask<?,I> previous ;
	
	protected void setPrevious(RoxChainTask<?,I> previous) {
		this.previous = previous;
	}

	private RoxChainTask<O,?> next ;
	
	protected void setNext(RoxChainTask<O,?> next) {
		this.next = next;
	}
	

	private RoxDeque<I> inputQueue = new RoxDeque<>() ;
	
	protected void addToInputQueue(I element) {
		synchronized (inputQueue) {
			inputQueue.add(element) ;
			inputQueue.notifyAll(); 
		}
	}
	
	protected void addToInputQueue(List<I> elements) {
		synchronized (inputQueue) {
			inputQueue.addAll(elements) ;
			inputQueue.notifyAll(); 
		}
	}
	
	private RoxDeque<O> finalResults ;
	
	public RoxDeque<O> getFinalResults() {
		return finalResults;
	}
	
	@Override
	public void run() {
		
		RoxChainTask<O,?> next = this.next ;
		
		RoxDeque<O> outputQueue = null ;
		
		if (next != null) {
			outputQueue = next.inputQueue ;	
		}
		else {
			this.finalResults = new RoxDeque<>() ;
		}
		
		while ( !isPrevTaskFinished() || !isQueueEmpty(inputQueue) ) {
			
			I input = consumeFromQueue(inputQueue) ;
			
			O output = task(input) ;
		
			if ( outputQueue != null ) {
				addToQueue(outputQueue, outputQueueLimit, output);	
			}
			else {
				this.finalResults.add(output) ;
			}
		}
		
		notifyFinished();
	}
	
	private final Mutex finishMUTEX = new Mutex() ;
	private boolean finished = false ;
	
	private void notifyFinished() {
		synchronized (finishMUTEX) {
			this.finished = true ;
			finishMUTEX.notifyAll();
		}
	}
	
	
	
	public boolean isFinished() {
		synchronized (finishMUTEX) {
			return this.finished;	
		}
	}
	
	public void waitFinished() {
		synchronized (finishMUTEX) {
			while (!finished) {
				try {
					finishMUTEX.wait();
				} catch (InterruptedException e) {}
			}
		}
	}
	
	private boolean isPrevTaskFinished() {
		if (previous == null) return true ;
		return previous.isFinished() ;
	}

	abstract public O task(I input) ;
	
}

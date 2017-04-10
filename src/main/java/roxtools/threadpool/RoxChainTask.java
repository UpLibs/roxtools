package roxtools.threadpool;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roxtools.Mutex;
import roxtools.RoxDeque;

abstract public class RoxChainTask<I,O> implements Runnable {
	
	final private int outputQueueLimit ;
	
	public RoxChainTask() {
		this(2);
	}
	
	public RoxChainTask(int outputQueueLimit) {
		this.outputQueueLimit = outputQueueLimit > 1 ? outputQueueLimit : 1 ;
	}
	
	final public int getOutputQueueLimit() {
		return outputQueueLimit;
	}
	
	private RoxChainTask<?,I> previous ;
	
	final protected void setPrevious(RoxChainTask<?,I> previous) {
		this.previous = previous;
	}

	private RoxChainTask<O,?> next ;
	
	final protected void setNext(RoxChainTask<O,?> next) {
		this.next = next;
	}
	

	private RoxDeque<I> inputQueue = new RoxDeque<>() ;
	private volatile int inputQueueAddedElements = 0 ;
	private volatile int inputsConsumed = 0 ;
	
	final public int getInputQueueAddedElements() {
		synchronized (inputQueue) {
			return inputQueueAddedElements;
		}
	}
	
	final public int getInputsConsumed() {
		synchronized (inputQueue) {
			return inputsConsumed;
		}
	}

	final public boolean isInputQueueEmpty() {
		synchronized (inputQueue) {
			return inputQueue.isEmpty() ;
		}
	}
	
	final public int getInputQueueSize() {
		synchronized (inputQueue) {
			return inputQueue.size() ;
		}
	}
	
	final protected void addToInputQueue(I element) {
		synchronized (inputQueue) {
			inputQueueAddedElements++ ;
			inputQueue.add(element) ;
			inputQueue.notifyAll(); 
		}
	}
	
	final protected void addToInputQueue(I element, int limit) {
		synchronized (inputQueue) {
			inputQueueAddedElements++ ;
			
			inputQueue.add(element) ;
			
			if (inputQueue.size() >= limit) {
				int minQueue = limit/2 ;
				while (inputQueue.size() > minQueue) {
					try {
						inputQueue.wait();
					} catch (InterruptedException e) {}
				}	
			}
			
			inputQueue.notifyAll();
		}
	}
	
	final protected void addToInputQueue(List<I> elements) {
		synchronized (inputQueue) {
			inputQueueAddedElements += elements.size() ;
			inputQueue.addAll(elements) ;
			inputQueue.notifyAll(); 
		}
	}
	

	private I consumeInputQueue() {
		synchronized (inputQueue) {
			while ( inputQueue.isEmpty() ) {
				try {
					inputQueue.wait();
				} catch (InterruptedException e) {}
			}
			
			I element = inputQueue.removeFirst() ;
			
			inputsConsumed++ ;
			inputQueue.notifyAll();
			
			return element ;
		}
	}
	
	
	private RoxDeque<O> finalResults ;
	
	final public RoxDeque<O> getFinalResults() {
		return finalResults;
	}
	
	private boolean returnsMultipleElements = false ; 
	
	final public RoxChainTask<I, O> setReturnsMultipleElements(boolean returnsMultipleElements) {
		this.returnsMultipleElements = returnsMultipleElements;
		
		return this ;
	}
	
	final public boolean getReturnsMultipleElements() {
		return returnsMultipleElements;
	}
	
	@Override
	final public void run() {
		
		if (next == null) {
			this.finalResults = new RoxDeque<>() ;
		}
		
		while ( !isPrevTaskFinished() || !isInputQueueEmpty() ) {
			
			I input = consumeInputQueue() ;
			
			O output = task(input) ;
			
			dispatchOutput(output);
			
		}
		
		O lastOutput = onFinishTask() ;
		
		dispatchOutput(lastOutput);
		
		notifyFinished();
	}
	

	private void dispatchOutput(O output) {
		if (output == null) return ;
	
		if ( next != null ) {
			
			if ( returnsMultipleElements ) {
				List<?> list = asList(output) ;
				
				@SuppressWarnings("unchecked")
				RoxChainTask<Object, ?> next2 = (RoxChainTask<Object, ?>) next ;
				
				for (Object out : list) {
					next2.addToInputQueue(out, outputQueueLimit);		
				}
			}
			else {
				next.addToInputQueue(output, outputQueueLimit);	
			}
				
		}
		else {
			
			if ( returnsMultipleElements ) {
				List<O> list = asList(output) ;
				
				for (O out : list) {
					this.finalResults.add(out) ;
				}
			}
			else {
				this.finalResults.add(output) ;	
			}
			
		}	
	
	}
	
	@SuppressWarnings("unchecked")
	private <T> List<T> asList(T obj) {
		if ( obj instanceof List ) {
			return (List<T>) obj ;
		}
		else if ( obj.getClass().isArray() ) {
			
			int length = Array.getLength(obj) ;
			
			ArrayList<T> list = new ArrayList<>() ;
			
			for (int i = 0; i < length; i++) {
				T elem = (T) Array.get(obj, i) ;
				list.add(elem) ;
			}
		
			return list ;
		}
		else {
			return Arrays.asList(obj) ;	
		}
		
	}
	
	private final Mutex finishMUTEX = new Mutex() ;
	private boolean finished = false ;
	
	private void notifyFinished() {
		synchronized (finishMUTEX) {
			this.finished = true ;
			finishMUTEX.notifyAll();
		}
	}
	
	final public boolean isFinished() {
		synchronized (finishMUTEX) {
			return this.finished;	
		}
	}
	
	final public void waitFinished() {
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
	
	public O onFinishTask() {
		return null ;
	}
	
}

package roxtools.ipc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import roxtools.ArrayUtils;

public class ProcessRunner {
	
	private String command ;
	private String[] arguments ;
	
	public ProcessRunner(String command, String... arguments) {
		if ( command == null || command.trim().isEmpty() ) throw new IllegalArgumentException("Invalid command: "+ command) ;
		
		this.command = command;
		this.arguments = arguments == null ? new String[0] : arguments ;
	}

	public String getCommand() {
		return command;
	}
	
	public String[] getArguments() {
		return arguments;
	}
	
	Process runningProcess ;
	
	synchronized public boolean isRunningProcess() {
		return runningProcess != null ;
	}
	
	synchronized public Process getRunningProcess() {
		return runningProcess;
	}
	
	private boolean executionFinished = false ;
	
	synchronized public boolean isExecutionFinished() {
		return executionFinished;
	}
	
	private OutputConsumer outputConsumer ;
	private OutputConsumer errorConsumer ;
	
	synchronized public OutputConsumer getOutputConsumer() {
		return outputConsumer;
	}
	
	synchronized public OutputConsumer getErrorConsumer() {
		return errorConsumer;
	}
	
	synchronized public void execute() throws IOException {
		execute(false);
	}
	
	synchronized public void execute( boolean redirectErrorToNormalOutput ) throws IOException {
		if ( isRunningProcess() ) throw new IllegalStateException("Already running process: "+ runningProcess) ;
		
		String[] cmdFull = ArrayUtils.join(new String[] {command} , arguments) ;
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmdFull) ;
		
		if (redirectErrorToNormalOutput) { 
			processBuilder.redirectErrorStream(true) ;
		}
		
		this.runningProcess = processBuilder.start() ;
		
		this.outputConsumer = new OutputConsumer( this.runningProcess.getInputStream() , true ) ;
		this.errorConsumer = !redirectErrorToNormalOutput ? new OutputConsumer( this.runningProcess.getErrorStream() , false ) : null ;
		
	}
	
	public int waitForProcess() throws InterruptedException {
		synchronized (this) {
			if ( this.runningProcess == null ) return -1 ; 
		}
		
		return this.runningProcess.waitFor() ;
	}
	
	public int waitForProcess(boolean waitForOutputConsumers) throws InterruptedException {
		int exitCode = waitForProcess() ;
		
		if (waitForOutputConsumers) {
			this.outputConsumer.waitFinished() ;
			if (this.errorConsumer != null) this.errorConsumer.waitFinished() ;
		}
		
		return exitCode ;
	}
	
	public boolean destroyProcess() {
		synchronized (this) {
			if ( this.runningProcess == null ) return false ; 
		}
		
		this.runningProcess.destroy();
		
		return true ;
	}
	
	public class OutputConsumer implements Runnable {
		final private InputStream in ;
		final private boolean mainOutput ;
		
		private OutputConsumer(InputStream in, boolean mainOutput) {
			this.in = in;
			this.mainOutput = mainOutput ;
			
			new Thread(this).start();
		}

		final private ByteArrayOutputStream output = new ByteArrayOutputStream(1024) ; 
		
		public byte[] getOutput() {
			synchronized (output) {
				return output.toByteArray() ;	
			}
		}
		
		public String getOutputAsString() {
			return new String( getOutput() ) ; 
		}
		
		public String getOutputAsString( Charset charset ) {
			return new String( getOutput() , charset ) ; 
		}
		
		private volatile boolean finished = false ;
		
		public boolean isFinished() {
			return finished ;
		}
		
		public OutputConsumer waitFinished() {
			synchronized (output) {
				while ( !finished ) {
					try {
						output.wait();
					} catch (InterruptedException e) {}
				}
			}
			
			return this ;
		}
		
		@Override
		public void run() {
			
			byte[] buffer = new byte[1024*4] ;
			int r ;
			
			try {
				while (true) {
					r = in.read(buffer) ;
					
					if (r < 0) break ;
					
					synchronized (output) {
						output.write(buffer, 0, r);	
					}
				}
			}
			catch (IOException e) {
				
			}
			
			try {
				in.close();
			} catch (Exception e) {}
			
			synchronized (output) {
				finished = true ;
				
				if (mainOutput) {
					synchronized (ProcessRunner.this) {
						ProcessRunner.this.executionFinished = true ;	
					}
				}
				
				output.notifyAll(); 
			}
			
		}
	}

}
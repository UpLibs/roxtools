package roxtools.ipc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

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
	
	private OutputStream processInput ;
	
	public OutputStream getProcessInput() {
		return processInput;
	}
	
	private OutputConsumerListener outputConsumerListener ;
	
	synchronized public void setOutputConsumerListener(OutputConsumerListener outputListener) {
		this.outputConsumerListener = outputListener ;
	}
	
	synchronized public OutputConsumerListener getOutputConsumerListener() {
		return outputConsumerListener;
	}
	
	private File workingDirectory ;
	
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	
	synchronized public void execute() throws IOException {
		execute(false);
	}
	
	synchronized public void execute( boolean redirectErrorToNormalOutput ) throws IOException {
		if ( isRunningProcess() ) throw new IllegalStateException("Already running process: "+ runningProcess) ;
		
		String[] cmdFull = ArrayUtils.join(new String[] {command} , arguments) ;
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmdFull) ;
		
		if (workingDirectory != null) {
			processBuilder.directory(workingDirectory) ;
		}
		
		if (redirectErrorToNormalOutput) { 
			processBuilder.redirectErrorStream(true) ;
		}
		
		this.runningProcess = processBuilder.start() ;
		
		this.outputConsumer = new OutputConsumer( this.runningProcess.getInputStream() , true , outputConsumerListener ) ;
		this.errorConsumer = !redirectErrorToNormalOutput ? new OutputConsumer( this.runningProcess.getErrorStream() , false , outputConsumerListener ) : null ;
		
		this.processInput = this.runningProcess.getOutputStream() ;
		
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
	
	static public interface OutputConsumerListener {
		
		public void onReadBytes(OutputConsumer outputConsumer, byte[] bytes, int length) ;

		public void onReadLine(OutputConsumer outputConsumer, String line);
		
	}
	
	public class OutputConsumer implements Runnable , OutputConsumerListener {
		final private InputStream in ;
		final private boolean mainOutput ;
		
		private OutputConsumer(InputStream in, boolean mainOutput) {
			this(in, mainOutput, null) ;
		}
		
		private OutputConsumer(InputStream in, boolean mainOutput, OutputConsumerListener listener) {
			this.in = in;
			this.mainOutput = mainOutput ;
			this.listener = listener != null ? listener : this ;
			
			new Thread(this).start();
		}
		
		@Override
		public void onReadBytes(OutputConsumer outputConsumer, byte[] bytes, int length) {}
		@Override
		public void onReadLine(OutputConsumer outputConsumer, String line) {}
		
		private OutputConsumerListener listener = this ;
		
		public void setListener(OutputConsumerListener listener) {
			if (listener == null) throw new NullPointerException("null listener") ;
			this.listener = listener;
		}
		
		public OutputConsumerListener getListener() {
			return listener;
		}

		private long maxOutputSize = 1024*1024*20 ;
		
		public void setMaxOutputSize(long maxOutputSize) {
			synchronized (output) {
				this.maxOutputSize = maxOutputSize;	
			}
		}
		
		public long getMaxOutputSize() {
			synchronized (output) {
				return maxOutputSize;
			}
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
				
				byte[] lineBuffer = new byte[1024*2] ;
				int lineBufferSz = 0 ;
				
				while (true) {
					r = in.read(buffer) ;
					
					if (r < 0) break ;
					
					synchronized (output) {
						output.write(buffer, 0, r);	
						
						if (maxOutputSize > 0 && output.size() > maxOutputSize) {
							checkMaxOutputSize() ;
						}
					}
					
					try {
						listener.onReadBytes(this, buffer, r);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					
					
					for (int i = 0; i < r; i++) {
						byte b = buffer[i] ;
						
						
						if (lineBufferSz >= lineBuffer.length) {
							lineBuffer = Arrays.copyOf(lineBuffer, lineBufferSz+(1024*2) ) ;
						}
						
						lineBuffer[lineBufferSz++] = b ;
						
						char c = (char) (b & 0xFF) ;
						if (c == '\n') {
							String line = new String(lineBuffer,0,lineBufferSz) ;
							listener.onReadLine(this, line) ;
							lineBufferSz = 0 ;
						}
					}
					
				}
				
				if (lineBufferSz > 0) {
					String line = new String(lineBuffer,0,lineBufferSz) ;
					listener.onReadLine(this, line) ;
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

		private void checkMaxOutputSize() {
			synchronized (output) {
				if (maxOutputSize <= 0) return ;
				
				byte[] bs = output.toByteArray() ;
				
				output.reset();
				
				int remainSize = bs.length/2 ;
				
				if (remainSize > 0) {
					output.write(bs, bs.length-remainSize, remainSize);
				}
			}
		}
	}

}

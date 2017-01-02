package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Runs an operating system command. This is a high level abstraction around Runtime.exec.
 * It handles the input and output stream of the process, in a simple way for the client.
 * 
 * In a typical use case, you will set up the command, run the command and then do something
 * with the output, or exit status of the command.
 * Exec's methods return "this", which makes chaining method calls easy (Fluent API). 
 * 
 * By default, stdout and stderr are thrown away. If you want the results of stdout/stderr you
 * call {@link #stdout()} and/or {@link #stderr()} with a {@link Sink}.
 * The simplest is to use {@link StringBufferSink}, but this is not practical for large amounts of output.
 * Alternatively you can send the command's output to a file using {@link FileSink}.
 * Finally, you could subclass {@link SimpleSink} and process the output on the fly.
 * If you want to pipe the output of one command into the input of another, use {@link Pipe}.
 * 
 * Example code to list a directory and redirect the output to a file, throwing an exception if the command fails.
 * <pre><code>
 * 	new Exec( "ls", "/home" )
 * 		.stdout( new FileSink( "/tmp/myoutput.txt" ) )
 * 		.throwOnError()
 * 		.run()
 * </code></pre>
 */
public class Exec
{

	/**
	 * The state of an Exec, initial value is CREATED, then once the run method is called
	 * it progresses to RUNNING, then through to TIMED_OUT or COMPLETED. 
	 */
	public enum State { CREATED, RUNNING, TIMED_OUT, COMPLETED };

	/**
	 * Uses bash's -c option to run a bash command.
	 * This allows access to all of bash goodness, such as pipes and redirection, but has the down side that
	 * you need to be very careful to escape arguments correctly.
	 * 
	 * Note this does NOT run the command, to run it :
	 * <pre><code>
	 * Exec.bash( "ls | sort" ).run();
	 * </code></pre>
	 * 
	 * @param commandString
	 * @return A new instance of Exec.
	 */
	public static Exec bash( String commandString )
	{
		return new Exec( "bash", "-c", commandString );
	}
	
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program )
	{
		return new Exec( new String[] { program } );
	}
	
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1 )
	{
		return new Exec( new String[] { program, arg1 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2 )
	{
		return new Exec( new String[] { program, arg1, arg2 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4, arg5 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4, arg5, arg6 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4, arg5, arg6, arg7 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7, String arg8 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8 } );
	}
	/**
	 * @deprecated
	 * A bodge because Beanshell currently doesn't support varargs
	 */
	public static Exec create( String program, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7, String arg8, String arg9 )
	{
		return new Exec( new String[] { program, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 } );
	}
	
	
	private State _state = State.CREATED;
	
	private List<String> _commandArray;
	
	private File _workingDirectory;
	
	private Map<String,String> _env = null;
	
	private int _exitStatus;

	private Source _inSource = new PrintSource();
	
	private Sink _outSink = new SimpleSink();            
	private Sink _errSink = new SimpleSink();  

	private StringBuffer _outBuffer;
	private StringBuffer _errBuffer;

	private long _timeoutMillis;

	private boolean _throwOnError = false;
	
	
	public Exec( String... cmdArray )
	{
		_commandArray = new ArrayList<String>( cmdArray.length );
		for ( int i = 0; i < cmdArray.length; i ++ ) {
			_commandArray.add( cmdArray[ i ] );
		}
	}
	
	public Exec add( String argument )
	{
		_commandArray.add( argument );
		return this;
	}
	
	/**
	 * Removes null values from the list of command arguments.
	 * This is handy if some values are optional, which you don't want to wrap in individual ifs.
	 * @return this
	 */
	public Exec removeNullArgs()
	{
		for ( Iterator<String> i = _commandArray.iterator(); i.hasNext(); ) {
			String val = i.next();
			if ( val == null ) {
				i.remove();
			}
		}
		return this;
	}

	/**
	 * Set the working directory for the new process.
	 * @param directory
	 * @return this
	 */
	public Exec dir( File directory )
	{
		this._workingDirectory = directory;
		return this;
	}
	
	/**
	 * Sets an environment variable for the new process.
	 * If this is the first call to var, then the existing environment variables are 
	 * @param name
	 * @param value
	 * @return
	 */
	public Exec var( String name, String value )
	{
		if ( _env == null ) {
			_env = new HashMap<String,String>( System.getenv() );
		}
		_env.put( name,  value );
		
		return this;
	}
	
	/**
	 * Clears all of the environment variables for the new process. You must call this if you
	 * want to start with a clean slate, as the var method will only add to the existing
	 * environment variables.
	 * @return
	 */
	public Exec clearEnv()
	{
		_env = new HashMap<String,String>();
		return this;
	}
	
	public Exec stdin( Source source )
	{
		_inSource = source;
		return this;
	}
	
	public Exec stdin( final String input )
	{
		_inSource = new PrintSource() {
			public void run()
			{
				_out.print( input );
				_out.close();
			}
		};
		
		return this;
	}
	
	public Exec stdout( Sink sink )
	{
		_outSink = sink;
		return this;
	}
	public Exec stderr( Sink sink )
	{
		_errSink = sink;
		return this;
	}
	
	/**
	 * Buffers stdout to a StringBuffer. Don't use this if large amounts of output will be created,
	 * instead create a subclass of StreamSink and pass it to outSink().
	 * @param buffer
	 * @return this
	 */
	public Exec stdout()
	{
		_outBuffer = new StringBuffer();
		_outSink = new StringBufferSink( _outBuffer );
		return this;
	}
	
	/**
	 * Buffers stderr to a StringBuffer. Don't use this if large amounts of output will be created,
	 * instead create a subclass of StreamSink and pass it to errSink().
	 * @param buffer
	 * @return this
	 */
	public Exec stderr()
	{
		_errBuffer = new StringBuffer();
		_errSink = new StringBufferSink( _errBuffer );
		return this;
	}

	/**
	 * Similar to a redirect such as mycommand > file
	 * @param file
	 * @return this
	 */
	public Exec stdout( File file )
		throws IOException
	{
		stdout( file, false );
		return this;
	}
	
	/**
	 * Similar to a redirect such as mycommand >> file (when append is true).
	 * @param file
	 * @param append
	 * @return this
	 */
	public Exec stdout( File file, boolean append )
		throws IOException
	{
		_outSink = new FileSink( file, append );
		return this;
	}
	
	/**
	 * Similar to a redirect such as mycommand > file
	 * @param file
	 * @return this
	 */
	public Exec stderr( File file )
		throws IOException
	{
		stderr( file, false );
		return this;
	}
	
	/**
	 * Similar to a redirect such as mycommand >> file (when append is true).
	 * @param file
	 * @param append
	 * @return this
	 */
	public Exec stderr( File file, boolean append )
		throws IOException
	{
		_errSink = new FileSink( file, append );
		return this;
	}
	
	/**
	 * Pipes the output stream from the command to an existing output stream.
	 * A common paramater is System.out, which will cause the command's output to be
	 * streamed to the running java process's stdout.
	 * @param out
	 * @return this
	 */
	public Exec stdout( OutputStream out )
	{
		_outSink = new CopySink( out );
		return this;
	}

	/**
	 * Pipes the error stream from the command to an existing output stream.
	 * A common paramater is System.err, which will cause the command's output to be
	 * streamed to the running java process's stderr.
	 * @param out
	 * @return this
	 */
	public Exec stderr( OutputStream out )
	{
		_errSink = new CopySink( out );
		return this;
	}
	
	/**
	 * Similar to stdout(), but combines the contents of stdout and stderr into a single buffer.
	 * @return
	 */
	public Exec combineStdoutStderr()
	{
		_outBuffer = new StringBuffer();
		_outSink = new StringBufferSink( _outBuffer );
		_errSink = new StringBufferSink( _outBuffer );
		
		return this;
	}
	
	/**
	 * If you have buffered the output using stdout(), then this will return that buffer as
	 * a string.
	 * 
	 * @return The contents of the buffered output.
	 * @throws NullPointeException if stdout() was not previously called.
	 */
	public String getStdout()
	{
		return _outBuffer.toString();
	}

	public String getStdoutLine()
	{
		return Util.firstLine( getStdout() );
	}
	
	public String getStderrLine()
	{
		return Util.firstLine( getStderr() );
	}
	
	/**
	 * If you have buffered the output using stderr(), then this will return that buffer as
	 * a string.
	 * 
	 * @return The contents of the buffered output.
	 * @throws NullPointeException if stderr() was not previously called.
	 */
	public String getStderr()
	{
		return _errBuffer.toString();
	}

	/**
	 * If you have buffered the output using stdout(), then this will return that buffer as
	 * a string.
	 * 
	 * @return The contents of the buffered output, with each line of stdout as one element
	 * in the array.
	 * @throws NullPointeException if bufferOutput was not previously called.
	 */
	public String[] getStdoutAsArray()
	{
		return _outBuffer.toString().split("\\r?\\n");
	}
	
	/**
	 * If you have buffered the output using stderr(), then this will return that buffer as
	 * a string.
	 * 
	 * @return The contents of the buffered output, with each line of stderr as one element
	 * in the array.
	 * @throws NullPointeException if bufferOutput was not previously called.
	 */
	public String[] getStderrAsArray()
	{
		return _errBuffer.toString().split("\\r?\\n");
	}

		/**
	 * 
	 * @return What was/will be passed to Runtime.exec as its cmdarray parameter
	 */
	public String[] getCommandArray()
	{
		return _commandArray.toArray( new String[_commandArray.size()] );
	}

	/**
	 * @return What was/will be passed to Runtime.exec as its env parameter
	 */
	public String[] getEnvironment()
	{
		String[] environment = null;

		if ( _env != null ) {
			ArrayList<String> variables = new ArrayList<String>();
			for ( String name : _env.keySet() ) {
				variables.add( name + "=" + _env.get( name ) );
			}
			environment = variables.toArray( new String[ variables.size()] );
		}

		return environment;
	}
	
	/**
	 * 
	 * @param millis
	 * @return this
	 */
	public Exec timeout( long millis )
	{
		_timeoutMillis= millis;
		return this;
	}
	
	public Exec throwOnError()
	{
		_throwOnError = true;
		return this;
	}
	
	/**
	 * Runs the command. This method will block until the process is complete, so if the process
	 * hangs, so will this method call. However, if you set a timeout using {@link #timeout(long)}, then
	 * a hung process will not hand this method call.
	 * 
	 * @return this
	 */
	public Exec run()
		throws ExecException
	{		
		_state = State.RUNNING;

		try {
			final Process process = Runtime.getRuntime().exec( getCommandArray(), getEnvironment(), _workingDirectory );

			if ( _timeoutMillis > 0 ) {

				new Thread() {
					public void run()
					{
						try {
							Thread.sleep( _timeoutMillis );
						} catch (Exception e) {
						}
						if ( _state == Exec.State.RUNNING ) {
							_state = Exec.State.TIMED_OUT;
							process.destroy();
						}
					}
				}.start();
			}
			
			
			_inSource.setStream( process.getOutputStream() );
			
			_outSink.setStream( process.getInputStream() );
			_errSink.setStream( process.getErrorStream() );
	        
			new Thread(_inSource ).start();
			Thread outSinkThread = new Thread( _outSink );
			Thread errSinkThread = new Thread( _errSink );
			errSinkThread.start();
			outSinkThread.start();
			
			try {
				if ( _state != State.TIMED_OUT ) {
					_exitStatus = process.waitFor();
					outSinkThread.join();
					errSinkThread.join();
				}
			} catch (InterruptedException e) {
				// Do nothing
			}

			if ( _throwOnError && (_exitStatus != 0) ) {
				throw new ExecException( this, "Non zero return value" );
			}

		} catch (ExecException e) {
			if ( _throwOnError ) {
				throw e;
			}		
		} catch (Exception e) {
			throw new ExecException( this, e );
			
		} finally {
			if ( _state != State.TIMED_OUT ) {
				_state = State.COMPLETED;
			}
		}
		
		return this;
	}
	
	public int getExitStatus()
	{
		return _exitStatus;
	}
	
	public State getState()
	{
		return _state;
	}
	
	public String toString()
	{
		List<String> env = (getEnvironment() == null) ? null : Arrays.asList( getEnvironment() );
		return "Exec" +
				"\n  State : " + _state +
				"\n  Command : " + Arrays.asList( getCommandArray() ) +
				( ( _state != State.COMPLETED ) ? "" : "\n  Exit Status : " + _exitStatus ) +
				( (_outBuffer == null) ? "" : "\n  Stdout : " + Util.abbreviate(_outBuffer.toString()) ) +
				( (_errBuffer == null) ? "" : "\n  Stderr : " + Util.abbreviate(_errBuffer.toString()) ) +
				"\n  Env : " + env +
				"\n  Dir : " + _workingDirectory;
	}
	
}

package uk.co.nickthecoder.jguifier.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Runs an operating system command. This is a high level abstraction around Runtime.exec.
 * (Actually, it now uses ProcessBuilder, but ProcessBuilder wasn't available when this was first written).
 * It handles the input and output stream of the process, in a simple way for the client.
 * <p>
 * In a typical use case, you will set up the command, run the command and then do something with the output, or exit
 * status of the command. Exec's methods return "this", which makes chaining method calls easy (Fluent API).
 * <p/>
 * <p>
 * By default, stdout and stderr are thrown away. If you want to buffer the results of stdout/stderr call
 * {@link #stdout()} and/or {@link #stderr()}. However this is not practical for large amounts of output, so instead
 * redirect the output to a file using {@link #stdout(File)} and {@link #stderr(File)}, or pipe the output to the input
 * of another Exec using {@link #stdout(Exec)}).
 * </p>
 * <p>
 * Example code to list a directory and redirect the output to a file, throwing an exception if the command fails.
 * </p>
 * 
 * <pre>
 * <code>
 * 	new Exec( "ls", "/home" )
 * 		.stdout( new File( "/tmp/myoutput.txt" ) )
 * 		.throwOnError()
 * 		.run()
 * </code>
 * </pre>
 * 
 */
public class Exec implements Runnable
{

    /**
     * The state of an Exec, initial value is CREATED, then once the run method is called
     * it progresses to RUNNING, then through to TIMED_OUT or COMPLETED.
     * 
     * @priority 5
     */
    public enum State
    {
        CREATED, RUNNING, TIMED_OUT, COMPLETED
    };

    /**
     * Uses bash's -c option to run a bash command.
     * This allows access to all of bash goodness, such as pipes and redirection, but has the down side that
     * you need to be very careful to escape arguments correctly.
     * 
     * Note this does NOT run the command, to run it :
     * 
     * <pre>
     * <code>
     * Exec.bash( "ls | sort" ).run();
     * </code>
     * </pre>
     * 
     * @param commandString
     * @return A new instance of Exec.
     */
    public static Exec bash(String commandString)
    {
        return new Exec("bash", "-c", commandString);
    }

    private State _state = State.CREATED;

    private List<String> _commandArray;

    private File _workingDirectory;

    private Map<String, String> _env = null;

    private Source _inSource = new NullSource();

    private Sink _outSink = new SimpleSink();
    private Sink _errSink = new SimpleSink();

    private long _timeoutMillis;

    private boolean _throwOnError = false;

    private Process _process;

    private boolean _mergeStderr = false;

    private Thread _outSinkThread;
    private Thread _errSinkThread;

    public Exec(String... cmdArray)
    {
        _commandArray = new ArrayList<String>(cmdArray.length);
        for (String element : cmdArray) {
            if (element != null) {
                _commandArray.add(element);
            }
        }
    }

    public Exec add(String argument)
    {
        _commandArray.add(argument);
        return this;
    }

    /**
     * Removes null values from the list of command arguments.
     * This is handy if some values are optional, which you don't want to wrap in individual ifs.
     * 
     * @return this
     */
    public Exec removeNullArgs()
    {
        for (Iterator<String> i = _commandArray.iterator(); i.hasNext();) {
            String val = i.next();
            if (val == null) {
                i.remove();
            }
        }
        return this;
    }

    /**
     * Set the working directory for the new process.
     * 
     * @param directory
     * @return this
     */
    public Exec dir(File directory)
    {
        this._workingDirectory = directory;
        return this;
    }

    /**
     * Sets an environment variable for the new process.
     * 
     * @param name
     * @param value
     * @return this
     */
    public Exec var(String name, String value)
    {
        if (_env == null) {
            _env = new HashMap<String, String>(System.getenv());
        }
        _env.put(name, value);

        return this;
    }

    /**
     * Clears all of the environment variables for the new process. You must call this if you
     * want to start with a clean slate, as the var method will only add to the existing
     * environment variables.
     * 
     * @return this
     */
    public Exec clearEnv()
    {
        _env = new HashMap<String, String>();
        return this;
    }

    public Exec stdin(Source source)
    {
        _inSource = source;
        return this;
    }

    public Exec stdin(final String input)
    {
        _inSource = new SimplePrintSource()
        {
            @Override
            public void setStream(OutputStream os)
            {
                super.setStream(os);
                out.print(input);
                out.close();
            }
        };

        return this;
    }

    public Exec stdout(Sink sink)
    {
        _outSink = sink;
        return this;
    }

    public Exec mergeStderr(boolean value)
    {
        _mergeStderr = value;
        return this;
    }

    public Exec mergeStderr()
    {
        return mergeStderr(true);
    }

    public Exec stderr(Sink sink)
    {
        _errSink = sink;
        return this;
    }

    /**
     * Buffers stdout to a StringBuffer. Don't use this if large amounts of output will be created.
     * Consider using {@link #stdout(File)} or {@link #stdout(Sink)}.
     * 
     * @param buffer
     * @return this
     */
    public Exec stdout()
    {
        _outSink = new StringBufferSink();
        return this;
    }

    /**
     * Buffers stderr to a StringBuffer. Don't use this if large amounts of output will be created.
     * Consider using {@link #stderr(File)}.
     * 
     * @param buffer
     * @return this
     */
    public Exec stderr()
    {
        _errSink = new StringBufferSink();
        return this;
    }

    /**
     * Similar to a redirect such as mycommand > file
     * 
     * @param file
     * @return this
     */
    public Exec stdout(File file)
        throws IOException
    {
        stdout(file, false);
        return this;
    }

    /**
     * Similar to a redirect such as mycommand >> file (when append is true).
     * 
     * @param file
     * @param append
     * @return this
     */
    public Exec stdout(File file, boolean append)
        throws IOException
    {
        _outSink = new FileSink(file, append);
        return this;
    }

    /**
     * Similar to a redirect such as mycommand > file
     * 
     * @param file
     * @return this
     */
    public Exec stderr(File file)
        throws IOException
    {
        stderr(file, false);
        return this;
    }

    /**
     * Similar to a redirect such as mycommand >> file (when append is true).
     * 
     * @param file
     * @param append
     * @return this
     */
    public Exec stderr(File file, boolean append)
        throws IOException
    {
        _errSink = new FileSink(file, append);
        return this;
    }

    /**
     * Pipes the output stream from the command to an existing output stream.
     * A common parameter is System.out, which will cause the command's output to be
     * streamed to the running java process's stdout.
     * 
     * @param out
     * @return this
     */
    public Exec stdout(OutputStream out)
    {
        _outSink = new CopySink(out);
        return this;
    }

    /**
     * Pipes the standard output of this Exec into the stdin of the taget Exec
     * 
     * @param target
     *            The Exec that will receive the output
     * @return this
     */
    public Exec stdout(Exec target)
    {
        _outSink = new Pipe(target);
        return this;
    }

    /**
     * Pipes the error stream from the command to an existing output stream.
     * A common paramater is System.err, which will cause the command's output to be
     * streamed to the running java process's stderr.
     * 
     * @param out
     * @return this
     */
    public Exec stderr(OutputStream out)
    {
        _errSink = new CopySink(out);
        return this;
    }

    /**
     * The Sink for stdout. If you buffered the output using {@link #stdout()}, then the easiest way to get the
     * output is : <code><pre>
     *      myExec.getStdout().toString();
     * </pre></code>
     * 
     * @return The output Sink.
     */
    public Sink getStdout()
    {
        return _outSink;
    }

    /**
     * The Sink for stderr. If you buffered the output using {@link #stderr()}, then the easiest way to get the
     * output is : <code><pre>
     *      myExec.getStderr().toString();
     * </pre></code>
     * 
     * @return The error Sink.
     */
    public Sink getStderr()
    {
        return _errSink;
    }

    /**
     * Assuming you have buffered stdout, using {@link #stdout()}
     * 
     * @return The first line of output
     */
    public String getStdoutLine()
    {
        return Util.firstLine(getStdout().toString());
    }

    /**
     * Assuming you have buffered stderr, using {@link #stderr()}
     * 
     * @return The first line of output
     */
    public String getStderrLine()
    {
        return Util.firstLine(getStderr().toString());
    }

    /**
     * If you have buffered the output using {@link #stdout()}, then this will return that buffer as
     * an array of strings.
     * 
     * @return The contents of the buffered output, with each line of stdout as one element
     *         in the array.
     */
    public String[] getStdoutAsArray()
    {
        return getStdout().toString().split("\\r?\\n");
    }

    /**
     * If you have buffered the output using {@link #stderr()}, then this will return that buffer as
     * a string.
     * 
     * @return The contents of the buffered output, with each line of stderr as one element
     *         in the array.
     * @throws NullPointerException
     *             if bufferOutput was not previously called.
     */
    public String[] getStderrAsArray()
    {
        return getStderr().toString().split("\\r?\\n");
    }

    /**
     * 
     * @return What was/will be passed to Runtime.exec as its cmdarray parameter
     */
    public String[] getCommandArray()
    {
        return _commandArray.toArray(new String[_commandArray.size()]);
    }

    /**
     * @return What was/will be passed to Runtime.exec as its env parameter
     */
    public String[] getEnvironment()
    {
        String[] environment = null;

        if (_env != null) {
            ArrayList<String> variables = new ArrayList<String>();
            for (String name : _env.keySet()) {
                variables.add(name + "=" + _env.get(name));
            }
            environment = variables.toArray(new String[variables.size()]);
        }

        return environment;
    }

    /**
     * 
     * @param millis
     * @return this
     */
    public Exec timeout(long millis)
    {
        _timeoutMillis = millis;
        return this;
    }

    public Exec throwOnError()
    {
        _throwOnError = true;
        return this;
    }

    public Exec go()
    {
        go();
        return this;
    }

    public BufferedReader runBuffered() throws IOException
    {
        return new BufferedReader(new InputStreamReader(runStreaming()));
    }

    public InputStream runStreaming() throws IOException
    {
        _outSink = null;
        runWithoutWaiting();

        return _process.getInputStream();
    }

    public Process getProcess()
    {
        return _process;
    }

    /**
     * Runs the command. This method will block until the process is complete, so if the process
     * hangs, so will this method call. However, if you set a timeout using {@link #timeout(long)}, then
     * a hung process will not hand this method call.
     */
    public void run()
    {
        // System.out.println( "Running Exec" );

        try {
            int _exitStatus = -1;

            runWithoutWaiting();

            if (_timeoutMillis > 0) {
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            Thread.sleep(_timeoutMillis);
                        } catch (Exception e) {
                        }
                        if (_state == Exec.State.RUNNING) {
                            _state = Exec.State.TIMED_OUT;
                            _process.destroy();
                        }
                    }
                }.start();
            }

            try {
                if (_state != State.TIMED_OUT) {
                    _exitStatus = _process.waitFor();
                    if (_outSinkThread != null) {
                        _outSinkThread.join();
                    }
                    if (_errSinkThread != null) {
                        _errSinkThread.join();
                    }
                }
            } catch (InterruptedException e) {
                // Do nothing
            }

            if (_throwOnError && (_exitStatus != 0)) {
                throw new ExecException(this, "Non zero return value");
            }

        } catch (ExecException e) {
            if (_throwOnError) {
                throw e;
            }
        } catch (Exception e) {
            throw new ExecException(this, e);

        } finally {
            if (_state != State.TIMED_OUT) {
                _state = State.COMPLETED;
            }
        }

    }

    /**
     * Runs the command, without waiting for it to finish.
     * 
     * @return The process
     * @throws IOException
     */
    public Process runWithoutWaiting() throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(getCommandArray());
        if (_env != null) {
            processBuilder.environment().putAll(_env);
        }
        if (_workingDirectory != null) {
            processBuilder.directory(_workingDirectory);
        }
        processBuilder.redirectErrorStream(_mergeStderr);

        _process = processBuilder.start();
        _state = State.RUNNING;

        if (_outSink != null) {
            _outSink.setStream(_process.getInputStream());
            _outSinkThread = new Thread(_outSink);
            _outSinkThread.start();
        }
        _inSource.setStream(_process.getOutputStream());

        if (!_mergeStderr) {
            _errSink.setStream(_process.getErrorStream());
            _errSinkThread = new Thread(_errSink);
            _errSinkThread.start();
        }

        return _process;
    }

    /**
     * Attempts to end the command by calling {@link Process#destroy()}.
     */
    public void stop()
    {
        _process.destroy();
    }

    public int getExitStatus()
    {
        try {
            return _process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public State getState()
    {
        if (_state == State.RUNNING) {
            try {
                _process.exitValue();
                _state = State.COMPLETED;
            } catch (IllegalThreadStateException e) {
                // Do nothing
            }
        }

        return _state;
    }

    @Override
    public String toString()
    {
        List<String> env = (getEnvironment() == null) ? null : Arrays.asList(getEnvironment());
        return "Exec" +
            "\n  State : " + _state +
            "\n  Command : " + Arrays.asList(getCommandArray()) +
            ((_state != State.COMPLETED) ? "" : "\n  Exit Status : " + getExitStatus()) +
            "\n  Stdout : " + Util.abbreviate(getStdout().toString()) +
            "\n  Stderr : " + Util.abbreviate(getStderr().toString()) +
            "\n  Env : " + env +
            "\n  Dir : " + _workingDirectory;
    }

}

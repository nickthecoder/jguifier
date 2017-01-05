package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;

/**
 * Used by the {@link Exec} class to handle the stdout from the process.
 * Each of the streams (stdout, stderr, stdin) have their own thread, which is responsible for reading/writing
 * to the stream.
 * @priority 4
 */
public interface Source extends Runnable
{

    public abstract void setStream(OutputStream os);

    @Override
    public abstract void run();

}

package uk.co.nickthecoder.jguifier.util;

import java.io.InputStream;

/**
 * Used by class Exec, to deal with stdout and stderr when executing commands.
 * 
 * If a command produces output, it has to go somewhere, and if the output isn't read, then the command will
 * stall. There are a few common types of Sink :
 * <ul>
 * <li>{@link SimpleSink} - Throws the output away</li>
 * <li>{@link StringBufferSink} - Appends the output to a StringBuffer - Impractical if there is a lot of output!</li>
 * <li>{@link FileSink} - Writes the output to a file</li>
 * <li>{@link Pipe} - Sends the output to the input of another command, in a similar manner to command line pipes |</li>
 * </ul>
 * @priority 4
 */
public interface Sink extends Runnable
{
    public void setStream(InputStream in);

    @Override
    public void run();

}

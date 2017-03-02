package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A thread (actually a Runnable) to feed to an Exec's stdin via a PrintStream.
 * @priority 5
 */
public class PrintSource implements Source
{

    protected PrintStream _out;

    @Override
    public void setStream(OutputStream os)
    {
        _out = new PrintStream(os);
    }

    @Override
    public void run()
    {
        //System.out.println( "Started PrintSource" );
        _out.close();
        //System.out.println( "Ended PrintSource" );
    }
}

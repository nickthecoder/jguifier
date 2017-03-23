package uk.co.nickthecoder.jguifier.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A thread (actually a Runnable) to feed to an Exec's stdin via a PrintStream.
 * @priority 5
 */
public class NullSource implements Source
{
    @Override
    public void setStream(OutputStream os)
    {
        try {
            os.close();
        } catch (IOException e) {
        }
    }
}

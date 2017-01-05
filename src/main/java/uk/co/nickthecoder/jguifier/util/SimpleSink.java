package uk.co.nickthecoder.jguifier.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.co.nickthecoder.jguifier.Task;

/**
 * Throws the output away. This is the default sink for a {@link Task}'s stdout and stderr.
 * 
 * @priority 5
 */
public class SimpleSink implements Sink
{

    protected int _bufferSize = 1024;

    InputStreamReader _reader;

    @Override
    public void setStream(InputStream in)
    {
        _reader = new InputStreamReader(in);
    }

    @Override
    public void run()
    {
        try {
            char[] buffer = new char[_bufferSize];

            int amount = 0;
            while ((amount = _reader.read(buffer, 0, _bufferSize)) != -1) {
                sink(buffer, amount);
            }
        } catch (IOException e) {
            sinkError(e);
        } finally {
            try {
                _reader.close();
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    protected void sink(char[] buffer, int len) throws IOException
    {
        // Does nothing - throws away the output
    }

    protected void sinkError(IOException e)
    {
        System.err.println("Sink error : " + e);
        e.printStackTrace();
    }

}

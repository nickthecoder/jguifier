package uk.co.nickthecoder.jguifier.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies the data from an OutputStream to an InputSteam.
 * Used as the basis for {@link CopySink}, and {@link Pipe}.
 * 
 * @priority 4
 */
public class CopySink implements Sink
{
    protected int _bufferSize = 1024;

    protected OutputStream _out;

    protected InputStream _in;

    public CopySink()
    {
    }

    public CopySink(OutputStream out)
    {
        _out = out;
    }

    public void setStream(OutputStream out)
    {
        _out = out;
    }

    @Override
    public void setStream(InputStream in)
    {
        _in = in;
    }

    @Override
    public void run()
    {

        byte[] buffer = new byte[_bufferSize];

        int len = 0;

        try {
            while ((len = _in.read(buffer, 0, _bufferSize)) != -1) {
                _out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            sinkError(e);
        }

    }

    protected void sinkError(IOException e)
    {
        System.err.println("Copy Sink Error : " + e);
        e.printStackTrace();
    }

}

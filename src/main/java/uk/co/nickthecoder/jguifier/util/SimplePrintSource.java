package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class SimplePrintSource implements Source
{
    public PrintStream out;

    @Override
    public void setStream(OutputStream os)
    {
        out = new PrintStream(os);
    }
}

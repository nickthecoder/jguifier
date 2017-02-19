package uk.co.nickthecoder.jguifier.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NullOutputStream extends OutputStream
{
    public static PrintStream nullPrintStream = new PrintStream(new NullOutputStream());

    @Override
    public void write(int b) throws IOException
    {
    }
}

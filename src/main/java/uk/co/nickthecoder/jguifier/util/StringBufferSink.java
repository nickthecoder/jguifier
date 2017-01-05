package uk.co.nickthecoder.jguifier.util;

/**
 * Buffers the output into a String. This is only practical for small amounts of output.
 * 
 * @priority 4
 */
public class StringBufferSink extends SimpleSink
{
    private StringBuffer _stringBuffer;

    public StringBufferSink()
    {
        this(new StringBuffer());
    }

    public StringBufferSink(StringBuffer stringBuffer)
    {
        _stringBuffer = stringBuffer;
    }

    @Override
    protected void sink(char[] buffer, int len)
    {
        _stringBuffer.append(buffer, 0, len);
    }

    /**
     * @return The StringBuffer used to store the text being sinked.
     */
    public StringBuffer getStringBuffer()
    {
        return _stringBuffer;
    }

    /**
     * @return The contents of the StringBuffer
     */
    @Override
    public String toString()
    {
        return _stringBuffer.toString();
    }
}

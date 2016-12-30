package uk.co.nickthecoder.jguifier.util;

import uk.co.nickthecoder.jguifier.ReaderSink;

public class StringBufferSink extends ReaderSink
{
	StringBuffer _stringBuffer;
	
	public StringBufferSink( StringBuffer stringBuffer )
	{
		_stringBuffer = stringBuffer;
	}
	
	protected void sink( char[] buffer, int len )
	{
		_stringBuffer.append( buffer, 0, len );
	}
}

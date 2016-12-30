package uk.co.nickthecoder.jguifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.co.nickthecoder.jguifier.util.Sink;

public class ReaderSink implements Sink {

	protected int _bufferSize = 1024;
	
	InputStreamReader _reader;

	public void setStream( InputStream in )
	{
		_reader = new InputStreamReader( in );
	}

	public void run()
	{
		try {
			char[] buffer = new char[ _bufferSize ];
			
			int amount = 0;
			while ( (amount = _reader.read( buffer, 0, _bufferSize )) != -1 ) {
				sink( buffer, amount );
			}
		} catch (IOException e) {
			System.out.println( "Sink error" );
			sinkError( e );
		} finally {
			try {
				_reader.close();
			} catch (Exception e) {
			}
		}
	}
    
    protected void sink( char[] buffer, int len ) throws IOException
    {
    	//Does nothing - throws away the output
    }

    protected void sinkError( IOException e )
    {
    }

}

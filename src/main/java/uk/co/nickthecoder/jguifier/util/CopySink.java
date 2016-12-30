package uk.co.nickthecoder.jguifier.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class CopySink implements Sink
{
	protected int _bufferSize = 1024;
	
	protected OutputStream _out;
	
	protected InputStream _in;
	
	public CopySink()
	{
	}
	
	public CopySink( OutputStream out )
	{
		_out = out;
	}

	public void setStream( OutputStream out )
	{
		_out = out;
	}
	
	@Override
	public void setStream( InputStream in )
	{
		_in = in;
	}


	@Override
	public void run()
	{
		//System.out.println( "CopySink running" );
		//System.out.println( "IS " + _in );
		//System.out.println( "OS " + _out );
				
		byte[] buffer = new byte[ _bufferSize ];
		
		int len = 0;
		
		try {
			while ( (len = _in.read( buffer, 0, _bufferSize )) != -1 ) {
				_out.write( buffer, 0, len );
				//System.out.println( "Copied " + len );
			}
		} catch (IOException e) {
			sinkError( e );
		}
		
		//System.out.println( "CopySink finished" );

	}
	
	protected void sinkError( IOException e )
	{
		System.err.println( "Copy Sink Error" );
		e.printStackTrace();
	}
	
}

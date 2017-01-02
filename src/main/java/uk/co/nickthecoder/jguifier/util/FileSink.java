package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class FileSink extends CopySink
{
	private OutputStream _out;
	

    public FileSink( String filename )
        throws IOException
    {
        this( new File( filename ) );
    }
    
    public FileSink( String filename, boolean append )
        throws IOException
    {
        this( new File( filename ), append );
    }
    
	public FileSink( File file )
		throws IOException
	{
		this( file, false );
	}
	
	public FileSink( File file, boolean append )
		throws IOException
	{
		this( new FileOutputStream( file, append ) );
	}

	private FileSink( OutputStream out )
	{
		super( out );
		_out = out;
	}
	
	@Override
    public void run()
	{
		try {
			super.run();
		} finally {
			try {
				_out.close();
			} catch (Exception e) {
				//Do nothing
			}
		}
	}
}

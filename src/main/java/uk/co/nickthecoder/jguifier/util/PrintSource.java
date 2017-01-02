package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;
import java.io.PrintStream;


public class PrintSource implements Source {

    protected PrintStream _out; 
    
    @Override
    public void setStream( OutputStream os )
    {
    	_out = new PrintStream( os );
    }

    @Override
    public void run()
    {
    	_out.close();
    }
}

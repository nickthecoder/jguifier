package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;
import java.io.PrintStream;


public class PrintSource implements Source {

    protected PrintStream _out; 
    
    public void setStream( OutputStream os )
    {
    	_out = new PrintStream( os );
    }

    public void run()
    {
    	_out.close();
    }
}

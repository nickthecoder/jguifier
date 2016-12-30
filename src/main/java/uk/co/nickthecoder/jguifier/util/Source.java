package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;

public interface Source extends Runnable
{
	
    public abstract void setStream( OutputStream os );

    public abstract void run();
    
}

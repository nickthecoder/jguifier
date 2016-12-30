package uk.co.nickthecoder.jguifier.util;

import java.io.InputStream;

public interface Sink extends Runnable
{    	
	public void setStream( InputStream in );

	public void run();

}

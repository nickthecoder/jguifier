package uk.co.nickthecoder.jguifier.util;

import java.util.Arrays;


public class ExecException extends RuntimeException
{
	static final long serialVersionUID = 1;
	
	private Exec _exec;
	
	public ExecException( Exec exec, String message )
	{
		super( message );
		_exec = exec;
	}
	
	public ExecException( Exec exec, Exception e )
	{
		super( e );
		_exec = exec;
	}
	
	public Exec getExec()
	{
		return _exec;
	}
	
	@Override
    public String getMessage()
	{
		return super.getMessage() + " " + Arrays.asList( _exec.getCommandArray() );
	}
}

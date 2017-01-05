package uk.co.nickthecoder.jguifier.util;

import java.util.Arrays;

/**
 * Thrown by an {@link Exec} if the exec failed, or if it returned a non-zero return result, and
 * {@link Exec#throwOnError()} was called.
 * 
 * @priority 4
 */
public class ExecException extends RuntimeException
{
    static final long serialVersionUID = 1;

    private Exec _exec;

    public ExecException(Exec exec, String message)
    {
        super(message);
        _exec = exec;
    }

    public ExecException(Exec exec, Exception e)
    {
        super(e);
        _exec = exec;
    }

    public Exec getExec()
    {
        return _exec;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + " " + Arrays.asList(_exec.getCommandArray());
    }
}

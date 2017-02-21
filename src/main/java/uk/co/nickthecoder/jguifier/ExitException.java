package uk.co.nickthecoder.jguifier;

public class ExitException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private int exitStatus;

    public ExitException(int exitStatus)
    {
        this.exitStatus = exitStatus;
    }

    public int getExitStatus()
    {
        return exitStatus;
    }

    public String toString()
    {
        return "ExitException " + this.exitStatus;
    }
}

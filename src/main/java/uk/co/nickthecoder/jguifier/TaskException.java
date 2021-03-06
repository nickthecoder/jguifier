package uk.co.nickthecoder.jguifier;

/**
 * Thrown by {@link Task}.
 *
 * @priority 4
 */
public class TaskException
    extends RuntimeException
{
    private static final long serialVersionUID = 1;

    public TaskException(String message)
    {
        super(message);
    }
}

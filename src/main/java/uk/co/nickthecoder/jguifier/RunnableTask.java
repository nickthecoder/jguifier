package uk.co.nickthecoder.jguifier;

/**
 * A {@link Task}, which is given a {@link Runnable}, which can make scripts simpler, because they can
 * send a Runnable (such as a Closure) to this class, rather than define a new Task subclass.
 */
public class RunnableTask extends Task
{
    private Runnable runnable = null;

    @Override
    public void guessName()
    {
        guessName( this.runnable );
    }
    
    public RunnableTask action( Runnable action )
    {
        this.runnable = action;
        return this;
    }
    
    protected void run()
    {
        runnable.run();
    }
}

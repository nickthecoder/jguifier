package uk.co.nickthecoder.jguifier;

/**
 * A {@link Task}, which is given a {@link Runnable}. Designed for use with scripts, because they can
 * send a Runnable (such as a groovy Closure) to this class, rather than define a new Task subclass.
 * This can make your scripts smaller and neater than sub-classing Task directly. Both choices have their pros and cons.
 * <p>
 * Here's an example groovy script, passing a closure to RunnableTask.
 * <p>
 * 
 * <pre>
 * <code>
 * 
 * #!/usr/bin/env jguifier
 * import uk.co.nickthecoder.jguifier.*
 * import uk.co.nickthecoder.jguifier.util.*
 * 
 * def foo = new StringParameter( "foo" );
 * def bar = new IntegerParameter( "bar").range( 0, 10 );
 * new RunnableTask()
 *     .parameters( mustExist, mustNotExist, mayExist,  directory, file, file2, either,  optional, writable )
 *     .action {
 *         // Program logic goes here!
 *         println( foo.value )
 *         println( bar.value )
 *     }
 * .go(args)
 * 
 * </code>
 * </pre>
 */
public class RunnableTask extends Task
{
    private Runnable runnable = null;

    /**
     * The task's name is taken from the source code where the runnable was defined. This will be the groovy script
     * file, so fingers crossed, the guess will be correct! If not, set the Task's name directly using
     * {@link #setName(String)}.
     */
    @Override
    public void guessName()
    {
        guessName(this.runnable);
    }

    public RunnableTask action(Runnable action)
    {
        this.runnable = action;
        return this;
    }

    public void run()
    {
        runnable.run();
    }
}

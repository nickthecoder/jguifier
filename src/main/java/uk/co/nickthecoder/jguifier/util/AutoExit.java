package uk.co.nickthecoder.jguifier.util;

/**
 * Exits the JVM when a count returns to zero.
 * Increment the count when you show a window, and decrement it when you close it.
 *
 */
public class AutoExit
{
    private static int count = 0;
    

    public static void setVisible( boolean show )
    {
        if (show) {
            inc();
        } else {
            dec();
        }
    }

    public static void inc()
    {
        count ++;
    }
    
    public static void dec()
    {
        count --;
        if (count == 0) {
            System.exit(0);
        }
    }
}

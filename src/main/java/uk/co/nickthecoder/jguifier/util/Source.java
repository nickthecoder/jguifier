package uk.co.nickthecoder.jguifier.util;

import java.io.OutputStream;

/**
 * Used by the {@link Exec} class to handle the stdin of the process.
 * @priority 4
 */
public interface Source
{
    public abstract void setStream(OutputStream os);
}

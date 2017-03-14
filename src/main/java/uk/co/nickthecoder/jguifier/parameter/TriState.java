package uk.co.nickthecoder.jguifier.parameter;

/**
 * Three states, true, false and maybe.
 * Created for use by FileParameter (but may be useful elsewhere) to specify if a file must exist (true),
 * must not exist (false), or don't care (maybe).
 * 
 * @priority 4
 */
public enum TriState
{
    TRUE, FALSE, MAYBE
}

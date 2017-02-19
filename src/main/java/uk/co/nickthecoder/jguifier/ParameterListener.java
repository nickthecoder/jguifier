package uk.co.nickthecoder.jguifier;

/**
 * A listener, which is notified when a parameter changes its value.
 * This can be used for one parameter to have an effect on another parameter before the Task is run.
 * 
 * For example, a FileParameter (for a directory) notifies, a ChoiceParameter when the directory is changed;
 * the choice parameter, then changes its set of choices based on the contents of the directory.
 */
public interface ParameterListener
{
    public void changed(Parameter source);
}

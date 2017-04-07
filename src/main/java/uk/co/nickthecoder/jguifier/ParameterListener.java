package uk.co.nickthecoder.jguifier;

import uk.co.nickthecoder.jguifier.parameter.Parameter;
import uk.co.nickthecoder.jguifier.parameter.ValueParameter;

/**
 * A listener, which is notified when a parameter changes its value.
 * This can be used for one parameter to have an effect on another parameter before the Task is run.
 * 
 * For example, a FileParameter (for a directory) notifies, a ChoiceParameter when the directory is changed;
 * the choice parameter, then changes its set of choices based on the contents of the directory.
 * 
 * Note, the Parameter may have an invalid value, for example, {@link ValueParameter#getValue()} may be null
 * even for non-optional parameters.
 */
public interface ParameterListener
{
    public void changed(Parameter source);
}

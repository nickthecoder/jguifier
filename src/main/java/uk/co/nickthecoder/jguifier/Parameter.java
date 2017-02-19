package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * The base class for parameters. A parameter stores both the metadata
 * (such as a label, is the parameter is required etc) as well as the parameter's value.
 *
 * @priority 3
 */
public abstract class Parameter
{

    private String _name;

    private String _label;

    private List<ParameterListener> _listeners;

    public Parameter(String name)
    {
        _name = name;
        _label = Util.uncamel(name);
    }

    /**
     * @return The name of the parameter.
     */
    public String getName()
    {
        return _name;
    }

    /**
     * @return The label as it will appear when prompted using the GUI.
     */
    public String getLabel()
    {
        return _label;
    }

    /**
     * 
     * @return A one line description of this parameter.
     */
    public String getHelp()
    {
        return "--" + _name + " (" + _label + ")";
    }

    /**
     * The default implementation returns the same as getHelp.
     * 
     * @return A verbose description of this parameter, which may span many lines.
     */
    public String getDescription()
    {
        return getHelp();
    }

    /**
     * Checks that the current value is valid. Used before a Task is run, to ensure that all the parameters are correct.
     * Note, that it is possible for parameters to be invalid, despite their values being check when they are set.
     * This is because the deafult value may be invalid, and also because a valid value may <b>become</b> invalid,
     * because
     * the parameter's meta data is changed, e.g. when a IntegerParameter's range is changed.
     * 
     * @throws ParameterException
     */
    public void check() throws ParameterException
    {
        // Do nothing
    }

    /**
     * Creates an AWT Component object when a Task is displayed using the GUI.
     * 
     * @param taskPrompter
     * @return The GUI component
     */
    public abstract Component createComponent(ParametersPanel panel);

    /**
     * Should the component be stretched to the maximum width of the container?
     * 
     * @return true iff the component should fill all the available width
     */
    public boolean isStretchy()
    {
        return false;
    }

    public void addListener(ParameterListener listener)
    {
        if (_listeners == null) {
            _listeners = new ArrayList<ParameterListener>();
        }
        _listeners.add(listener);
    }

    public void remvoveListener(ParameterListener listener)
    {
        if (_listeners != null) {
            _listeners.remove(listener);
        }
    }

    /**
     * Call implementations of Parameter should call this whenever their value changes.
     */
    protected void fireChangeEvent(Parameter source)
    {
        if (_listeners != null) {
            for (ParameterListener pl : _listeners) {
                pl.changed(this);
            }
        }
    }

    protected void fireChangeEvent()
    {
        fireChangeEvent(this);
    }

    /**
     * Called by {@link Task#autocomplete(String[])} when a parameter's value is being tab-completed.
     * Outputs the possible values to stdout, or does nothing if no tab completions.
     * For choice parameters, this will list the set of possible choices.
     * For {@link FileParameter}s, it will list the set of possible filenames.
     * 
     * @param cur
     *            What the user has typed in so far, and should be used to filter the values.
     */
    public void autocomplete(String cur)
    {
        // Default does nothing.
    }

    @Override
    public String toString()
    {
        return _name;
    }
}

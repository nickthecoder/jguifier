package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * The base class for parameters. A parameter stores both the metadata
 * (such as a label, is the parameter is required etc) as well as the parameter's value.
 *
 * @priority 1
 */
public abstract class Parameter
{

    private String _name;

    private String _label;

    private String _description;

    private List<ParameterListener> _listeners;

    /**
     * 
     * @param name
     *            The name of the parameter. Prefixed by "--" when specified on the command line.
     */
    public Parameter(String name)
    {
        _name = name;
        _label = Util.uncamel(name);
    }

    /**
     * @return The name of the parameter.
     * 
     * @priority 3
     */
    public String getName()
    {
        return _name;
    }

    /**
     * @return The label as it will appear when prompted using the GUI.
     * 
     * @priority 3
     */
    public String getLabel()
    {
        return _label;
    }

    /**
     * Simple setter
     */
    public void setDescription(String value)
    {
        _description = value;
    }

    /**
     * Simple getter
     * 
     * @return The description
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * A fluent API for {@link #setDescription(String)}.
     * 
     * @param value
     * @return this
     */
    public Parameter description(String value)
    {
        setDescription(value);
        return this;
    }

    /**
     * 
     * @return A one line description of this parameter.
     */
    public String getHelp()
    {
        int padLength = 18 - _name.length();
        if (padLength < 1)
            padLength = 1;

        String padding = new String(new char[padLength]).replace("\0", " ");

        String desc = getDescription();
        if (Util.empty(desc)) {
            return "--" + _name;
        } else {
            return "--" + _name + padding + ": " + desc;
        }
    }

    /**
     * Checks that the current value is valid. Used before a Task is run, to ensure that all the parameters are correct.
     * Note, that it is possible for parameters to be invalid, despite their values being check when they are set.
     * This is because the deafault value may be invalid, and also because a valid value may <b>become</b> invalid,
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
     * Creates a Swing Component object, used by the GUI when a Task is prompted.
     * 
     * @param taskPrompter
     *            Used by the created component to set and clear error messages on the ParametersPanel.
     * @return The Swing component
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

    /**
     * The listener will be notified whenever this parameter's value is changed.
     * 
     * @param listener
     * 
     * @see #remvoveListener(ParameterListener)
     * @priority 3
     */
    public void addListener(ParameterListener listener)
    {
        if (_listeners == null) {
            _listeners = new ArrayList<ParameterListener>();
        }
        _listeners.add(listener);
    }

    /**
     * Removes a listener
     * 
     * @param listener
     * 
     * @see #addListener(ParameterListener)
     * @priority 4
     */
    public void remvoveListener(ParameterListener listener)
    {
        if (_listeners != null) {
            _listeners.remove(listener);
        }
    }

    /**
     * To fire an change event, normally use : {@link #fireChangeEvent()}. This version
     * allows the source to be a <b>difference</b> parameter. It was created so that {@link GroupParameter}s
     * can notify their listeners whenever any of their child Parameters change.
     * In this case, the source will be the child parameter, but the listeners notified will be those added to the
     * Group.
     * 
     * @priority 5
     */
    protected void fireChangeEvent(Parameter source)
    {
        if (_listeners != null) {
            for (ParameterListener pl : _listeners) {
                pl.changed(this);
            }
        }
    }

    /**
     * All subclasses of Parameter should call this whenever their value changes.
     * It will notify each listener in turn.
     * 
     * @see ParameterListener#changed(Parameter)
     * @priority 4
     */
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
     * @priority 5
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

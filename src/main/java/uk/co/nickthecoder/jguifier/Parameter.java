package uk.co.nickthecoder.jguifier;

import java.awt.Component;

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

    private boolean _required;

    /**
     * @param name
     *            A unique (per Task) identifier, used when setting values using the command line.
     * @param label
     *            A human readable version of the name, used when setting values via a GUI.
     */
    public Parameter(String name, String label)
    {
        _name = name;
        _label = label;
        _required = true;
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

    public Parameter required(boolean value)
    {
        setRequired(value);
        return this;
    }

    /**
     * Make the parameter required (i.e. cannot be left blank).
     * Not usually needed, because Parameters are required by default.
     */
    public Parameter required()
    {
        setRequired(true);
        return this;
    }

    /**
     * Make the parameter optional (i.e. it CAN be left blank).
     */
    public Parameter optional()
    {
        setRequired(false);
        return this;
    }

    /**
     * Parameters are required by default, so you need to call this with false for optional parameters.
     * Its expected that most parameters will have suitable default values, so even though the parameter
     * is required, the user will not have to enter a value unless they wish to override the default.
     */
    public void setRequired(boolean value)
    {
        _required = value;
    }

    /**
     * @return true iff the Parameter must be have a value (this is the default).
     */
    public boolean isRequired()
    {
        return _required;
    }

    /**
     * @return true iff the Parameter must be have a value (this is the default).
     */
    public boolean getRequired()
    {
        return _required;
    }

    /**
     * 
     * @return A one line description of this parameter.
     */
    public String getHelp()
    {
        return "--" + _name + " (" + _label + ")" + (_required ? "" : " optional");
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
     * Sets the parameter from a string representation of a value, used when setting a value from the command line,
     * or other string-only sources. To set the value using the correct type, each subclass should define its own
     * setValue method.
     * 
     * Sub classes should parses the parameter, attempt to coerce it into the required type, and then
     * call check().
     * 
     * @throws ParameterException
     *             If the parse fails, or that value is invalid.
     */
    public abstract void setStringValue(String value) throws ParameterException;

    /**
     * @return A string representation of this parameter. The result should be symmetric with setStringValue,
     *         i.e. param.setStringValue( param.getStringValue() ) should have no effect.
     */
    public abstract String getStringValue();

    /**
     * Throws an exception if the parameter does not hold a valid value.
     * Parameters are checked when setValue and setStringValue are called, but are also checked
     * again before a Task is executed. This second check is to ensure that any values which haven't
     * been explicitly set by the user (either via the command line, or the GUI), are valid.
     * Needed to catch parameter's whose default values are not valid (such as required parameters
     * with a null default value).
     * 
     * @throws ParameterException
     */
    public abstract void check() throws ParameterException;

    /**
     * Creates an AWT Component object when a Task is displayed using the GUI.
     * 
     * @param taskPrompter
     * @return The GUI component
     */
    public abstract Component createComponent(TaskPrompter taskPrompter);

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

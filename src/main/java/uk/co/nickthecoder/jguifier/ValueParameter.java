package uk.co.nickthecoder.jguifier;

/**
 * 
 * @param <T>
 *            The Type of value, for example Double for DoubleParameters
 */
public abstract class ValueParameter<T> extends Parameter implements Cloneable
{

    private T _value;

    private boolean _required;

    /**
     * The default value will be null, but may be overridden by user-defined defaults using {@link Task#readDefaults()}.
     * 
     * @param name
     *            The name of the parameter. Prefixed by "--" when specified on the command line.
     */
    public ValueParameter(String name)
    {
        this(name, null);
    }

    /**
     * @param name
     *            The name of the parameter, is prefixed by "--" when specified on the command line.
     * @param value
     *            The default value. However this value may be overridden by user-defined defaults using
     *            {@link Task#readDefaults()}.
     */
    public ValueParameter(String name, T value)
    {
        super(name);
        _value = value;
        _required = true;
    }

    /**
     * 
     * @return A one line description of this parameter.
     */
    public String getHelp()
    {
        return super.getHelp() + (_required ? "" : "(optional) ");
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
    public String getStringValue()
    {
        if (getValue() == null) {
            return null;
        }
        return getValue().toString();
    }

    /**
     * Sets the value of the parameter, without testing if it is valid, and without firing any change events.
     * 
     * @param value
     */
    public void setDefaultValue(T value)
    {
        _value = value;
    }

    /**
     * Set the value of the parameter.
     * The parameter is checked, throwing a ParameterException when invalid.
     * However, the value is set even when an exception is thrown. This allows parameters to be set to
     * invalid values in the GUI.
     * <p>
     * If the value is valid, and has changed, then a message is fired to all {@link ParameterListener}s.
     * </p>
     * 
     * @param value
     * @throws ParameterException
     */
    public void setValue(T value)
    {
        boolean changed = (value != _value);
        _value = value;

        String reason = valid(value);
        if (reason != null) {
            throw new ParameterException(this, reason);
        }

        if (changed) {
            fireChangeEvent();
        }
    }

    public T getValue()
    {
        return _value;
    }

    /**
     * Checks that the current value is valid. Used before a Task is run, to ensure that all the parameters are correct.
     * Note, that it is possible for parameters to be invalid, despite their values being check when they are set.
     * This is because the deafult value may be invalid, and also because a valid value may <b>become</b> invalid,
     * because
     * the parameter's meta data is changed, e.g. when a IntegerParameter's range is changed.
     * 
     * @throws ParameterException
     * @Override
     */
    public void check() throws ParameterException
    {
        String reason = valid(_value);

        if (reason != null) {
            throw new ParameterException(this, reason);
        }
    }

    /**
     * Checks if the value is valid. Used by {@link #setValue(Object)} and {@link #setStringValue(String)}, which
     * will throw an exception when this method returns false.
     * 
     * @param value
     *            The value to be checked
     * @return null iff the value is allowed, otherwise a string explaining the problem.
     *         If there are more than one reason then just one may be returned.
     */
    public String valid(T value)
    {
        if (getRequired() && (value == null)) {
            return ParameterException.REQUIRED_MESSAGE;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public ValueParameter<T> clone()
    {
        ValueParameter<T> result;
        try {
            result = (ValueParameter<T>) super.clone();
            return result;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String toString()
    {
        return super.toString() + " = " + (_value == null ? "null" : _value.toString());
    }

    /**
     * @param <B>
     *            The Builder
     * @param <P>
     *            The Parameter
     * @param <T>
     *            The Type held by the parameter, eg Double for DoubleParameters
     */
    public abstract static class Builder<B extends Builder<B, P, T>, P extends ValueParameter<T>, T>
        extends Parameter.Builder<B, P>
    {
        public B value(T value)
        {
            making.setDefaultValue(value);
            return me();
        }

        public B required()
        {
            making.setRequired(true);
            return me();
        }

        public B optional()
        {
            making.setRequired(false);
            return me();
        }

        public MultipleParameter<P, T> multipleParameter()
        {
            return new MultipleParameter<P, T>(making, making.getName());
        }

    }
}

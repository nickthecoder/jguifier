package uk.co.nickthecoder.jguifier;


public abstract class ValueParameter<T> extends Parameter
{

    private T _value;

    private boolean _required;

    /**
     * @param name
     *            A unique (per Task) identifier, used when setting values using the command line.
     * @param label
     *            A human readable version of the name, used when setting values via a GUI.
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
        return super.getHelp() + (_required ? "" : " optional");
    }

    public ValueParameter<T> required(boolean value)
    {
        setRequired(value);
        return this;
    }

    /**
     * Make the parameter required (i.e. cannot be left blank).
     * Not usually needed, because Parameters are required by default.
     */
    public ValueParameter<T> required()
    {
        setRequired(true);
        return this;
    }

    /**
     * Make the parameter optional (i.e. it CAN be left blank).
     */
    public ValueParameter<T> optional()
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

    public void setValue(T value)
    {
        String reason = valid(value);
        if (reason != null) {
            throw new ParameterException(this, reason);
        }

        if (value != _value) {
            _value = value;
            fireChangeEvent();
        }
    }

    public T getValue()
    {
        return _value;
    }
    
    public ValueParameter<T> value( T value )
    {
        setValue( value );
        return this;
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

    @Override
    public String toString()
    {
        return super.toString() + " = " + (_value==null ? "null" : _value.toString());
    }
}

package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JTextField;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * The simplest type of {@link Parameter}, which can be any text.
 * 
 */
public class StringParameter
    extends TextParameter<String>
{

    public StringParameter(String name, String label)
    {
        super(name, label);
    }

    public StringParameter(String name, String label, String defaultValue)
    {
        super(name, label, defaultValue);
    }

    public StringParameter stretch()
    {
        setStretchy(true);
        return this;
    }

    @Override
    public void setStringValue(String value)
        throws ParameterException
    {
        setValue(value);
    }

    public StringParameter value(String value)
        throws ParameterException
    {
        try {
            setValue(value);
        } catch (Exception e) {
            // Do nothing - let the default value be illegal
        }
        return this;
    }

    public StringParameter columns(int value)
    {
        setColumns(value);
        return this;
    }

    @Override
    public String valid( String value )
    {
        if (isRequired() && (Util.empty(value))) {
            return super.valid(null);
        }
        return null;
    }

    @Override
    public Component createComponent(final ParametersPanel parametersPanel)
    {
        final JTextField component = new JTextField(getValue() == null ? "" : getValue());
        textField( component, parametersPanel );
        
        return component;
    }

}

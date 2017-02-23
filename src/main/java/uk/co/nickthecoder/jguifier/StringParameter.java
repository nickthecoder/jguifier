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
    public int maxLength = Integer.MAX_VALUE;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public StringParameter(String name)
    {
        super(name);
        setStretchy(true);
    }

    @Override
    public void setStringValue(String value)
        throws ParameterException
    {
        setValue(value);
    }

    public void setMaxLength( int value )
    {
        this.maxLength = value;
        if ( value <= 30 ) {
            setColumns( value );
        }
    }
    
    @Override
    public String valid(String value)
    {
        if (isRequired() && (Util.empty(value))) {
            return super.valid(null);
        }
        if ((value != null) && (value.length() > maxLength) ) {
            return "Too long. Maximum of " + maxLength + " characters";
        }
        return null;
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        final JTextField component = new JTextField(getValue() == null ? "" : getValue());
        textField(component, holder);

        return component;
    }

    public static final class Builder extends TextParameter.Builder<Builder, StringParameter, String>
    {
        public Builder(String name)
        {
            making = new StringParameter(name);
        }

        public Builder stretch()
        {
            making.setStretchy(true);
            return this;
        }
        
        public Builder maxLength( int maxLength )
        {
            making.setMaxLength(maxLength);
            return this;
        }

    }
}

package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import uk.co.nickthecoder.jguifier.util.Util;

public class StringParameter
    extends Parameter
{
    private String _value = null;
    
    
    public StringParameter( String name, String label )
    {
        super( name, label );
    }
    public StringParameter( String name, String label, String defaultValue )
    {
        super( name, label );
        _value = defaultValue;
    }
    
    public void setStringValue( String value )
        throws ParameterException
    {
    	setValue( value );
    }
    
    public String getStringValue()
    {
        return _value;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue( String value )
    	throws ParameterException
    {
        _value = value;
        check();
    }

    public StringParameter value( String value )
    	throws ParameterException
    {
    	try {
    		setValue( value );
    	} catch ( Exception e ) {
    		// Do nothing - let the default value be illegal
    	}
    	return this;
    }
    
    public void check()
        throws ParameterException
    {
        if ( isRequired() && ( Util.empty( getValue() ) ) ) {
            throw new ParameterException( this, ParameterException.REQUIRED_MESSAGE );
        }
    }
    
    public Component createComponent( final TaskPrompter taskPrompter )
    {
        final JTextField component = new JTextField( _value == null ? "" : _value );
        component.setColumns( 30 );
        
        component.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValue();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValue();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValue();
            }

            public void checkValue() {
                try {
                    setStringValue( component.getText() );
                    taskPrompter.clearError( StringParameter.this );
                } catch (Exception e) {
                    taskPrompter.setError( StringParameter.this, e.getMessage() );
                }
            }
        });

        return component;
    }
    
    public String toString()
    {
        return super.toString() + " = " + _value;
    }

}



package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import uk.co.nickthecoder.jguifier.util.Util;
	
public class IntegerParameter
    extends Parameter
{
    private Integer _value = null;
    
    private int _minimum = Integer.MIN_VALUE;

    private int _maximum = Integer.MAX_VALUE;

    public IntegerParameter( String name, String label )
    {
        super( name, label );
    }

    public IntegerParameter( String name, String label, Integer defaultValue )
    {
        super( name, label );
        _value = defaultValue;
    }
    
    
    
    public IntegerParameter range( Integer min, Integer max )
    {
        setRange( min, max );
        return this;
    }
        
    public IntegerParameter setRange( Integer min, Integer max )
    {
        if ( min == null ) {
            _minimum = Integer.MIN_VALUE;
        } else {
            _minimum = min;
        }
        
        if ( max == null ) {
            _maximum = Integer.MAX_VALUE;
        } else {
            _maximum = max;
        }
        
        return this;
    }
    
    public int getMinimumValue()
    {
        return _minimum;
    }
    
    public int getMaximumValue()
    {
        return _maximum;
    }
    
        
    public void setValue( Integer value )
    {
        _value = value;
    }
    
    public void setStringValue( String string )
        throws ParameterException
    {
        if ( Util.empty( string ) ) {
            _value = null;
            
        } else {
        
            try {
            
                int value = Integer.parseInt( string );
                _value = value;
                
            } catch ( Exception e ) {
                throw new ParameterException( this, "Not a whole number" );
            }
        }
        
        check();        
    }
    
    public Integer getValue()
    {
        return _value;
    }
    
    public String getStringValue()
    {
        if (_value == null) {
            return null;
        }
        return _value.toString();
    }

    public void check()
        throws ParameterException
    {
        if ( getValue() == null ) {
            if ( isRequired() ) {
                throw new ParameterException( this, ParameterException.REQUIRED_MESSAGE );
            }
            return;
        }

        int value = getValue();

        if ( value < _minimum ) {
            throw new ParameterException( this, "Minimum value is " + _minimum );
        }
        if ( value > _maximum ) {
            throw new ParameterException( this, "Maximum value is " + _maximum );
        }
    }
    
    public Component createComponent( final TaskPrompter taskPrompter )
    {
        final JTextField component = new JTextField( _value == null ? "" : _value.toString() );
        component.setColumns( 6 );
        
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
                    taskPrompter.clearError( IntegerParameter.this );
                } catch (NumberFormatException e) {
                    taskPrompter.setError( IntegerParameter.this, "Not a whole number" );                    
                } catch (Exception e) {
                    taskPrompter.setError( IntegerParameter.this, e.getMessage() );
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



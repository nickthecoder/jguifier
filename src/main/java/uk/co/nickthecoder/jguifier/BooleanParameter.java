package uk.co.nickthecoder.jguifier;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import java.awt.Component;
import javax.swing.JCheckBox;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;


public class BooleanParameter
    extends uk.co.nickthecoder.jguifier.Parameter
{
    private static Set<String> TRUE_VALUES = new HashSet<String>( Arrays.asList("true", "1", "yes" ) );
    private static Set<String> FALSE_VALUES = new HashSet<String>( Arrays.asList("false", "0", "no" ) );
    
    private Boolean _value = null;
    
    private String _oppositeName = null;
    

    public BooleanParameter( String name, String label )
    {
        this( name, label, null );
    }

    public BooleanParameter( String name, String label, Boolean defaultValue )
    {
        super( name, label );
        _value = defaultValue;
    }
    
    
    
    public BooleanParameter oppositeName( String name )
    {
        setOppositeName( name );
        return this;
    }

    public void setOppositeName( String name )
    {
        assert ! getName().equals(_oppositeName );
        _oppositeName = name;
    }
    
    public String getOppositeName()
    {
        return _oppositeName;
    }
       
    
    
    public void setValue( Boolean value )
    {
        _value = value;
    }
    
    public void setStringValue( String value )
    {
        value = value.toLowerCase();
        if ( TRUE_VALUES.contains( value ) ) {
            _value = true;
        } else if ( FALSE_VALUES.contains( value ) ) {
            _value = false;
        } else {
            throw new NumberFormatException( "Parameter " + this.getName() + " must be false/true (or 0/1)" );
        }
          
    }
    
    public Boolean getValue()
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
        if ( getRequired() && ( getValue() == null) ) {
            throw new ParameterException( this, ParameterException.REQUIRED_MESSAGE );
        }
    }
    

    public Component createComponent( final TaskPrompter taskPrompter )
    {
        final JCheckBox component = new JCheckBox();
        if ( Boolean.TRUE == _value ) {
            component.setSelected( true );
        } else if ( _value == null ) {
        	// We can't distinguish between an null value and a false value using a GUI,
        	// so nulls must become false.
        	_value = false;
        }
        
        component.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    setValue( component.getModel().isSelected() );
                    taskPrompter.clearError( BooleanParameter.this );
                } catch (Exception e) {
                    taskPrompter.setError( BooleanParameter.this, e.getMessage() );
                }
            }
        });
        
        return component;
    }
    
    public String toString()
    {
        return super.toString() + " = " + _value;
    }

    public String getHelp()
    {
        if ( _oppositeName != null ) {
            return "--" + getName() + " , --" + _oppositeName + " (" + getLabel() + ")" + ( getRequired() ? "" : " optional" );
        } else {
            return super.getHelp();
        }
    }
    
    public String getDescription()
    {
        return super.getDescription() + " [true|false]";
    }
    
    public void autocomplete( String cur )
    {
    	Task.autocompleteFilter( "0", cur );
    	Task.autocompleteFilter( "1", cur );
    	Task.autocompleteFilter( "true", cur );
    	Task.autocompleteFilter( "false", cur );
    }
    
}



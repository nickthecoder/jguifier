package uk.co.nickthecoder.jguifier;

import java.util.ArrayList;
import java.util.List;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/**
 * A parameter whose value must be one from a predefines list of values.
 * The GUI will display this as a combobox.
 *  * 
 * The GUI may optionally display more human readable text for each of the options. For example,
 * a male/female parameter may use the Strings "m" and "f", but the GUI can display "Male" and "Female".
 * To do this use addChoice( "m", "Male" ) and addChoice( "f", "Female" ). You can also use the
 * ChoiceParameter's convenience methods :
 * <pre><code>
 * myParameter.choice( "m", "Male" ).choice( "f", "Female" )
 * </code></pre>
 */
public abstract class AbstractChoiceParameter<T> extends Parameter
{

	private List<T> _keys = new ArrayList<T>();
	
	private List<String> _keysAsString = new ArrayList<String>();
	
	private List<String> _labels = null;
	
	private T _value;
	
	
	public AbstractChoiceParameter( String name, String label )
	{
		super( name, label );
	}

	@Override
	public String getStringValue() {
		return _value.toString();
	}

	public void addChoice( T key, String label )
	{
		if ( _labels == null ) {
			_labels = new ArrayList<String>();
			for ( T k : _keys ) {
				_labels.add( k.toString() );
			}
		}
		_keys.add( key );
		_keysAsString.add( key.toString() );
		_labels.add( label );
	}
	
	public void addChoice( T key )
	{
		_keys.add( key );
		_keysAsString.add( key.toString() );
		if ( _labels != null ) {
			_labels.add( key.toString() );
		}
	}
	
	public void addChoices( T[] keys )
	{
		for ( T key : keys ) {
			addChoice( key );
		}
	}
	
	@Override
	public void check() throws ParameterException
	{
		if ( isRequired() && ( _value == null ) ) {
			throw new ParameterException( this, ParameterException.REQUIRED_MESSAGE );
		}
	}
	
	public void setValue( T value )
		throws ParameterException
	{
		_value = value;
		check();
	}
	
	public T getValue()
	{
		return _value;
	}
	
	@Override
	public Component createComponent( final TaskPrompter taskPrompter)
	{
		String[] labels = ((_labels == null) ? _keys : _labels).toArray( new String[0] );
		
		final JComboBox<String> combo = new JComboBox<String>( labels );
		
		combo.addActionListener (new ActionListener () {
			
		    public void actionPerformed(ActionEvent event) {
                try {
                	int index = combo.getSelectedIndex();
                    setValue( _keys.get( index ) );
                    taskPrompter.clearError( AbstractChoiceParameter.this );
                } catch (Exception e) {
                    taskPrompter.setError( AbstractChoiceParameter.this, e.getMessage() );
                }
		    	
		    }
		});

		// Select the correct item from the combobox.
		int index = _keys.indexOf( _value );
		if ( index >= 0 ) {
			combo.setSelectedIndex( index );
		}

		/*
		 *  It seems that JCombobox cannot be displayed such that no item is selected
		 *  (I don't want to have a JComboBox with text entry), then I guess we need to update
		 *  the parameter's value based on what the user will see.
		 */
		if ( _value == null ) {
			if ( _keys.size() > 0 ) {
				_value = _keys.get( 0 );
			}
		}
		
		return combo;
	}

	public void setStringValue( String string )
			throws ParameterException
	{
		int index = _keysAsString.indexOf( string );
		
		if ( index >= 0 ) {
			setValue( _keys.get( index ) );
		} else {
			throw new ParameterException( this, "Value not found" );
		}
	}
	

    public void autocomplete( String cur )
    {
    	for ( String possible : _keysAsString ) {
    		Task.autocompleteFilter( possible, cur );
    	}
    }
}

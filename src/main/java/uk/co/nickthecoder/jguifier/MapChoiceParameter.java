package uk.co.nickthecoder.jguifier;

import java.util.HashMap;
import java.util.Map;

public class MapChoiceParameter<K,V> extends AbstractChoiceParameter<K>
{

	private Map<K,V> _choicesMap = new HashMap<K,V>();
	
	public MapChoiceParameter( String name, String label )
	{
		super( name, label );
	}
	
	public MapChoiceParameter<K,V> choice( K key, V value, String label )
	{
		addChoice( key, value, label );
		return this;
	}
	public MapChoiceParameter<K,V> choice( K key, V value )
	{
		addChoice( key, value );
		return this;
	}
	public void addChoice( K key, V value )
	{
		_choicesMap.put( key, value );
		addChoice( key );
	}
	public void addChoice( K key, V value, String label )
	{
		_choicesMap.put( key, value);
		addChoice( key, label );
	}
	
	public V getMappedValue()
	{
		if ( getValue() == null ) {
			return null;
		}
		return _choicesMap.get( getValue() );
	}
	
	public void setMappedValue( V value )
		throws ParameterException
	{
		for ( K key : _choicesMap.keySet() ) {
			if ( _choicesMap.get( key ).equals( value ) ) {
				setValue( key );
				return;
			}
		}
		
		throw new ParameterException( this, "Value not found" );
	}
	
	public MapChoiceParameter<K,V> value( V value )
	{
		try {
			setMappedValue( value );
			return this;
		} catch (ParameterException e) {
			throw new RuntimeException( e );	
		}
	}
	
}

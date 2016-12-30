package uk.co.nickthecoder.jguifier;

public class ChoiceParameter<T> extends AbstractChoiceParameter<T>
{

	public ChoiceParameter( String name, String label )
	{
		super( name, label );
	}

	public ChoiceParameter<T> choice( T value )
	{
		addChoice( value );
		return this;
	}

	public ChoiceParameter<T> choice( T value, String label )
	{
		addChoice( value, label );
		return this;
	}
	
	public ChoiceParameter<T> choices( T[] values )
	{
		addChoices( values );
		return this;
	}
	
	public ChoiceParameter<T> value( T value )
	{
		try {
			setValue( value );
			return this;
		} catch (ParameterException e) {
			throw new RuntimeException( e );	
		}
	}
	
}

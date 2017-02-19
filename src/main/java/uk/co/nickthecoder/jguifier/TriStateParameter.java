package uk.co.nickthecoder.jguifier;

public class TriStateParameter extends ChoiceParameter<TriState>
{

    public TriStateParameter(String name)
    {
        this( name, TriState.MAYBE);
    }
    
    public TriStateParameter(String name, TriState value )
    {
        super(name, value);
        addChoice("true", TriState.TRUE);
        addChoice("false", TriState.FALSE);
        addChoice("1", TriState.TRUE);
        addChoice("0", TriState.FALSE);
        addChoice("maybe", TriState.MAYBE);
    }

}

package uk.co.nickthecoder.jguifier;

public class TriStateParameter extends ChoiceParameter<TriState>
{

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public TriStateParameter(String name)
    {
        this( name, TriState.MAYBE);
    }
    
    /**
     * @see ValueParameter#ValueParameter(String)
     */
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

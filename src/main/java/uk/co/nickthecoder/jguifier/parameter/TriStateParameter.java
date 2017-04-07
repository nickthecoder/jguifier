package uk.co.nickthecoder.jguifier.parameter;

public class TriStateParameter extends ChoiceParameter<TriState>
{

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public TriStateParameter(String name)
    {
        super(name);
        addChoice("true", TriState.TRUE);
        addChoice("false", TriState.FALSE);
        addChoice("1", TriState.TRUE);
        addChoice("0", TriState.FALSE);
        addChoice("maybe", TriState.MAYBE);
    }

    public static final class Builder extends ChoiceParameter.ChoiceBuilder<Builder,TriStateParameter, TriState>
    {
        public Builder(String name)
        {
            making = new TriStateParameter(name);
        }
    }
}

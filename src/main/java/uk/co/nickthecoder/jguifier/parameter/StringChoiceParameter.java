package uk.co.nickthecoder.jguifier.parameter;

/**
 * A {@link ChoiceParameter}, with Strings for keys.
 */
public class StringChoiceParameter extends ChoiceParameter<String>
{
    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public StringChoiceParameter(String name)
    {
        super(name);
    }
    
    public static final class Builder extends ChoiceParameter.ChoiceBuilder<Builder,StringChoiceParameter,String>
    {
        public Builder(String name)
        {
            making = new StringChoiceParameter(name);
        }

        public Builder choices(String... keys)
        {
            for (String key : keys) {
                making.addChoice(key, key);
            }
            return this;
        }
        
    }

}

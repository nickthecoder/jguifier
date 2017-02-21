package uk.co.nickthecoder.jguifier;

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

    /**
     * Adds a whole bunch of choices, where the keys, value and labels are identical.
     * 
     * @param keys
     *            A set of String used as the keys, labels and values.
     * @return this
     */
    public StringChoiceParameter choices(String... keys)
    {
        for (String key : keys) {
            addChoice(key, key);
        }
        return this;
    }

    public StringChoiceParameter choice(String key)
    {
        addChoice(key, key);
        return this;
    }
}

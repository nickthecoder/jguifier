package uk.co.nickthecoder.jguifier;

/**
 * A {@link ChoiceParameter}, with Strings for keys.
 * 
 * Does exactly the same thing as
 */
public class StringChoiceParameter extends ChoiceParameter<String>
{

    public StringChoiceParameter(String name, String label)
    {
        super(name, label);
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

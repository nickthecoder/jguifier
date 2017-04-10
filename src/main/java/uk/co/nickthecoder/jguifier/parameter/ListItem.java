package uk.co.nickthecoder.jguifier.parameter;

/**
 * Used by {@link ListParameter} to hold the possible values, as well as the chosen values.
 */
public interface ListItem<T>
{
    public T getValue();

    public String getStringValue();

    /**
     * Used to convert from a string representation.
     * Is the opposite of {@link #getStringValue()}.
     * i.e.
     * <code><pre>
     * myListChoice.getValue().equals( myListChoice.parse(myListChoice.getStringValue()) )
     * </code>
     * </pre>
     * 
     */
    public T parse(String stringValue);

    /**
     * This is the how the item will appear to the user in the JLists
     */
    public String toString();

}

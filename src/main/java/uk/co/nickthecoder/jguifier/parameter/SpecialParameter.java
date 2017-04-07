package uk.co.nickthecoder.jguifier.parameter;

/**
 * Like {@link ExtraSpecialParameter}, but the types of the special values, are the same as the regular values.
 * This allows the {@link #getValue()} method to be clever.
 * 
 * @param <P>
 *            The sub-class of the regular {@link Parameter}s, such as StringParameter
 * @param <T>
 *            The type of values held by the regular parameters, such as String.
 */
public class SpecialParameter<P extends ValueParameter<T>, T>
    extends ExtraSpecialParameter<T, P, T>
{
    public SpecialParameter(String name)
    {
        super(name);
    }

    /**
     * @return The special value, if a special value has been chosen, otherwise, the regular value.
     */
    @Override
    public T getValue()
    {
        T result = super.getValue();
        return result == null ? getRegularParameter().getValue() : result;
    }

    public static final class Builder<P2 extends ValueParameter<T2>, T2>
        extends ChoiceBuilder<Builder<P2, T2>, SpecialParameter<P2, T2>, T2>
    {
        public Builder(String name)
        {
            making = new SpecialParameter<P2, T2>(name);
        }

        public Builder<P2, T2> regular(P2 regularParameter)
        {
            making.setRegularParameter(regularParameter);
            return this;
        }

        public Builder<P2, T2> prefix(String prefix)
        {
            making.specialPrefix = prefix;
            return this;
        }

        public Builder<P2, T2> suffix(String suffix)
        {
            making.specialSuffix = suffix;
            return this;
        }
    }
}

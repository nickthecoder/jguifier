package uk.co.nickthecoder.jguifier;

public class EnumParameter<E extends Enum<E>> extends ChoiceParameter<E>
{
    private Class<E> klass;

    public EnumParameter(Class<E> klass, String name)
    {
        super(name);
        this.klass = klass;
    }

    @Override
    public void setStringValue(String value) throws ParameterException
    {
        setValue(Enum.<E> valueOf(klass, value));
    }

    public static final class Builder<E2 extends Enum<E2>> extends
        ChoiceParameter.ChoiceBuilder<Builder<E2>, EnumParameter<E2>, E2>
    {
        public Builder(Class<E2> klass, String name)
        {
            making = new EnumParameter<E2>(klass, name);

            for (E2 e : klass.getEnumConstants()) {
                making.addChoice(e.name(), e);
            }
        }
    }

}

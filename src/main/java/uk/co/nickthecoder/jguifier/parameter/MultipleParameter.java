package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParametersPanel;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A parameter which can be given multiple values.
 * The values are stored in a List. When representing the value as a string, the inidivual string values are separated
 * by a new line character. Therefore, this cannot be used with a ValueParameter which also ma include a new line
 * character
 * in their string value. This shouldn't be a problem though, because new line characters aren't normally allowed.
 * <p>
 * To pass a MultipleParameter value as a command line, simply repeat the --name=value pairs for each value. For example
 * :
 * <p/>
 * <code><pre>
 * myCommand --myParam=abc --myParam=def --myParam=xyz
 * </pre></code>
 * 
 * To create a MultipleParameter using a fluent API (the builder pattern), first create a parameter as if it were to
 * hold a single value, and then instead of called "parameter()", call multipleParameter() instead. For example :
 * <code><pre>
         MultipleParameter<IntegerParameter, Integer> manyInts = new IntegerParameter.Builder("manyInts")
            .range(0, 100).description("blah...")
            .multipleParameter();
 * </pre></code>
 * 
 * @param <P>
 *            The Parameter for a single value
 * @param <T>
 *            The type of value for a single value
 */
public class MultipleParameter<P extends ValueParameter<T>, T> extends ValueParameter<List<T>>
{
    /**
     * Used to convert a single value to/from its string representation.
     * Also used to create new parameter objects using the clone method, when editing values using the TaskPrompter.
     */
    private P prototypeParameter;

    public MultipleParameter(P prototype, String name)
    {
        super(name);
        this.prototypeParameter = prototype;
    }

    @Override
    public String getStringValue()
    {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (T value : getValue()) {
            prototypeParameter.setValue(value);
            buffer.append(prototypeParameter.getStringValue());
            if (first) {
                first = false;
            } else {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    @Override
    public void setStringValue(String value)
    {
        List<T> list = new ArrayList<T>();

        String[] stringValues = value.split("\n");
        for (String stringValue : stringValues) {
            prototypeParameter.setStringValue(stringValue);
            list.add(prototypeParameter.getValue());
        }
    }

    public void setSingleStringValue(String value)
    {
        List<T> list = getValue();
        prototypeParameter.setStringValue(value);
        list.add(prototypeParameter.getValue());
    }

    /**
     * @return multiple --name=value pairs, one for each item in the list of values.
     * @see GroupParameter#getCommandString()
     */
    public String getCommandArguments()
    {
        StringBuffer buffer = new StringBuffer();

        for (T value : getValue()) {
            prototypeParameter.setValue(value);
            String text = prototypeParameter.getStringValue();

            buffer.append(" --");
            buffer.append(getName());
            buffer.append("=");
            if (text != null) {
                if (text.matches("[a-zA-Z0-9./]*")) {
                    buffer.append(text);
                } else {
                    buffer.append(Util.quote(text));
                }
            }
        }

        return buffer.toString();
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        return new MultipleParameterComponent();
    }

    class MultipleParameterComponent extends ParametersPanel
    {
        private static final long serialVersionUID = 1L;

        public MultipleParameterComponent()
        {
            addComponents();
        }

        public void addComponents()
        {
            for (T value : getValue()) {
                addComponent(value);
            }
        }

        public void addComponent(T value)
        {
            @SuppressWarnings("unchecked")
            P parameter = (P) prototypeParameter.clone();
            parameter.setValue(value);
            Component component = parameter.createComponent(this);
            MultipleParameterComponent.this.add(component);
        }
    }

}

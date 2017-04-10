package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Holds a list
 */
public class ListParameter<T extends ListItem<?>> extends ValueParameter<List<T>>
    implements Boxed
{
    private List<T> possibleItems;

    private Map<String, T> possibleMap;

    public int height = 150;

    public ListParameter(String name)
    {
        super(name);
        possibleItems = new ArrayList<>();
        possibleMap = new HashMap<>();

        setDefaultValue(new ArrayList<T>());
    }

    public boolean isStretchy()
    {
        return true;
    }

    public List<T> getPossibleItems()
    {
        return possibleItems;
    }

    @Override
    public String getStringValue()
    {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (T value : getValue()) {
            if (first) {
                first = false;
            } else {
                buffer.append("\n");
            }
            String str = value.getStringValue();

            if (str != null) {
                if (str.matches("[a-zA-Z0-9./]*")) {
                    buffer.append(str);
                } else {
                    buffer.append(Util.quote(str));
                }
            }
        }
        return buffer.toString();
    }

    @Override
    public void setStringValue(String value) throws ParameterException
    {
        List<T> list = new ArrayList<T>();

        String[] stringValues = value.split("\n");
        for (String line : stringValues) {
            String strVal = Util.unescapeNewLines(line);

            T item = findItem(strVal);
            if (item == null) {
                throw new ParameterException(this, "Unexpected value: " + strVal);
            }
            list.add(item);
        }
        setValue(list);
    }

    /**
     * @return multiple --name=value pairs, one for each item in the list of values.
     * @see GroupParameter#getCommandString()
     */
    public String getCommandArguments()
    {
        StringBuffer buffer = new StringBuffer();

        for (T value : getValue()) {
            String text = value.getStringValue();
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

    public T findItem(String str)
    {
        return possibleMap.get(str);
    }

    public void addPossibleValue(T item)
    {
        possibleItems.add(item);
        possibleMap.put(item.getStringValue(), item);
        fireChangeEvent();
    }

    public void removePossibleValue(T item)
    {
        possibleItems.remove(item);
        possibleMap.remove(item.getStringValue());
        fireChangeEvent();
    }

    public void add(T item)
    {
        assert (possibleItems.contains(item));
        if (!getValue().contains(item)) {
            getValue().add(item);
            fireChangeEvent();
        }
    }

    public void add(String str)
    {
        T item = findItem(str);
        if (!getValue().contains(item)) {
            getValue().add(item);
            fireChangeEvent();
        }
    }

    public void remove(String str)
    {
        T item = findItem(str);
        if (getValue().contains(item)) {
            getValue().remove(item);
            fireChangeEvent();
        }
    }

    public void remove(T item)
    {
        if (getValue().contains(item)) {
            getValue().remove(item);
            fireChangeEvent();
        }
    }

    @Override
    public Component createComponent(ParameterHolder holder)
    {
        return new ListComponent<T>(this, holder);
    }

    public static final class Builder<TT extends ListItem<?>>
        extends ValueParameter.Builder<Builder<TT>, ListParameter<TT>, List<TT>>
    {
        public Builder(String name)
        {
            making = new ListParameter<TT>(name);
        }

        public Builder<TT> add(TT item)
        {
            making.addPossibleValue(item);
            return this;
        }

        public Builder<TT> height(int height)
        {
            making.height = height;
            return this;
        }
    }
}

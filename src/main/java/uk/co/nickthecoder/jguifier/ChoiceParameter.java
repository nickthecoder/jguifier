package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A parameter whose value must be one from a predefines list of values.
 * The GUI will display this as a combobox.
 * <p>
 * Each entry has three pieces of data, the key, the value and the label. The key is a String, which is used in the
 * command line. The value can be any object (of generic type T). The label is an optional String, and if specified is
 * used in the GUI's combobox. If the label isn't specified, then the label is the same as the key.
 * </p>
 * For example, if we want a parameter to specify either {@link System#out} or {@link System#err}, then
 * we could do something like :
 * 
 * <code><pre>
        ChoiceParameter<PrintStream> foo = new ChoiceParameter.Builder<PrintStream>("theName")
            .choice("out", System.out, "Standard Output" )
            .choice("err", System.err )
            .parameter();
 * </pre></code>
 * 
 * Note only the first choice has been given a nice label, so the GUI would display a JComboBox with items
 * "Standard Output" and "err".
 * 
 * <br/>
 * 
 * The choice labels are not used on the command line. We would set the parameter from the command line like so:
 * <code>--foo=out</code>
 * 
 * @see Builder
 */
public class ChoiceParameter<T> extends ValueParameter<T>
{
    /**
     * Maps keys to values. Used to convert a key into its corresponding value.
     */
    protected Map<String, T> _mapping = new HashMap<String, T>();

    /**
     * Maps keys to labels. Used to store the labels used by the GUI.
     */
    protected Map<String, String> _labelMapping = new HashMap<String, String>();

    /**
     * A list of the possible keys. This is needed to order the options correctly (Maps are not ordered).
     */
    protected List<String> _keys;

    private JComboBox<String> _comboBox;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public ChoiceParameter(String name)
    {
        super(name);
        _mapping = new HashMap<String, T>();
        _labelMapping = new HashMap<String, String>();
        _keys = new ArrayList<String>();
        _comboBox = null;
    }

    /**
     * Removes all choices (rarely used).
     */
    public void clearChoices()
    {
        _keys.clear();
        _labelMapping.clear();
        _mapping.clear();
        updateComboBox();
    }

    /**
     * Add a choice.
     * 
     * @param key
     *            The key (Used on the command line : --parameterName=key)
     * @param value
     *            The value, as returned by {@link #getValue()}
     * @param label
     *            The label seen in the GUI pull-down combobox. Not used when using the command line.
     */
    public void addChoice(String key, T value, String label)
    {
        _keys.add(key);
        _mapping.put(key, value);
        if (label != null) {
            _labelMapping.put(key, label);
        } else {
            _labelMapping.put(key, key);
        }

        updateComboBox();
    }

    /**
     * Add a choice. The label will be the same as the key.
     * 
     * @param key
     *            The key (Used on the command line : --parameterName=key)
     * @param value
     *            The value, as returned by {@link #getValue()}
     * 
     */
    public void addChoice(String key, T value)
    {
        addChoice(key, value, key);
    }

    public void addChoice(T value)
    {
        addChoice(value.toString(), value);
    }

    /**
     * Checks that the value is one of the choice values.
     */
    public String valid(T value)
    {
        if (!_mapping.values().contains(value)) {
            return "Not a valid choice";
        }
        return super.valid(value);
    }

    /**
     * Sets the value using the key value
     * 
     * @param key
     */
    @Override
    public void setStringValue(String key)
        throws ParameterException
    {
        if (_mapping.containsKey(key)) {
            setValue(_mapping.get(key));
        } else {
            throw new ParameterException(this, "Key not found : " + key);
        }
    }

    @Override
    public String getStringValue()
    {
        for (String key : _mapping.keySet()) {
            if (Util.equals(_mapping.get(key), getValue())) {
                return key;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc} Implemented using a JComboBox
     */
    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        _comboBox = new JComboBox<String>();
        updateComboBox();

        _comboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                try {
                    int index = _comboBox.getSelectedIndex();
                    setStringValue(_keys.get(index));
                    holder.clearError(ChoiceParameter.this);

                } catch (Exception e) {
                    holder.setError(ChoiceParameter.this, e.getMessage());
                }

            }
        });

        addListener(new ParameterListener() {
            @Override
            public void changed(Parameter source)
            {
                String oldKey = _keys.get(_comboBox.getSelectedIndex() );
                String newKey = getStringValue();
                if ( (oldKey == null) || ! oldKey.equals(newKey) ) {
                    _comboBox.setSelectedItem(_labelMapping.get(newKey));
                }
            }
        });
        
        return _comboBox;
    }

    private void updateComboBox()
    {
        if (_comboBox == null) {
            return;
        }

        _comboBox.removeAllItems();
        String stringValue = getStringValue();

        for (String key : _keys) {
            String label = _labelMapping.get(key);
            _comboBox.addItem(label);
            if (key.equals(stringValue)) {
                _comboBox.setSelectedIndex(_comboBox.getItemCount() - 1);
            }
        }

        /*
         * It seems that JCombobox cannot be displayed such that no item is selected
         * (I don't want to have a JComboBox with text entry), then I guess we need to update
         * the parameter's value based on what the user will see.
         */
        if (getValue() == null) {
            if (_keys.size() > 0) {
                try {
                    setStringValue(_keys.get(0));
                } catch (ParameterException e) {
                    // Do nothing
                }
            }
        }
    }

    @Override
    public void autocomplete(String cur)
    {
        for (String possible : _keys) {
            Task.autocompleteFilter(possible, cur);
        }
    }

    /**
     * Note, there are TWO builder classes within ChoiceParameter.
     * ChoiceBuilder is abstract, and is used for sub-classes of ChoiceParameter, such as StringChoiceParameter.
     * <p>
     * {@link Builder} is final, and is the one used to build ChoiceParameters.
     * </p>
     */
    protected abstract static class ChoiceBuilder<B extends ChoiceBuilder<B, P, T2>, P extends ChoiceParameter<T2>, T2>
        extends ValueParameter.Builder<B, P, T2>
    {
        public B choice(T2 value)
        {
            making.addChoice(value);
            return me();
        }
        
        public B choice(String key, T2 value)
        {
            making.addChoice(key, value);
            return me();
        }

        public B choice(String key, T2 value, String label)
        {
            making.addChoice(key, value, label);
            return me();
        }
    }

    public static final class Builder<T2> extends ChoiceBuilder<Builder<T2>, ChoiceParameter<T2>, T2>
    {
        public Builder(String name)
        {
            making = new ChoiceParameter<T2>(name);
        }
    }

}

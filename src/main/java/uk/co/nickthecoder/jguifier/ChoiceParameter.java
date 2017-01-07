package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;

/**
 * A parameter whose value must be one from a predefines list of values.
 * The GUI will display this as a combobox.
 * 
 * Each entry has three pieces of data, the key, the value and the label.
 * The key is a String, which is used in the command line.
 * The value can be any object (of generic type T).
 * The label is an optional String, and if specified is used in the GUI's combobox. If this isn't specified, then the
 * key is used as the label.
 * 
 */
public class ChoiceParameter<T> extends Parameter
{
    private Map<String, T> _mapping = new HashMap<String, T>();

    private Map<String, String> _labelMapping = new HashMap<String, String>();

    private List<String> _keys;

    private T _value;

    /**
     * 
     * @param name
     *            The name/id of the parameter, is prefixed by "--" when specified on the command line.
     * @param label
     *            A human readable label used within the GUI
     */
    public ChoiceParameter(String name, String label)
    {
        super(name, label);
        _mapping = new HashMap<String, T>();
        _labelMapping = new HashMap<String, String>();
        _keys = new ArrayList<String>();
    }

    public void addChoice(String key, T value, String label)
    {
        _keys.add(key);
        _mapping.put(key, value);
        if (label != null) {
            _labelMapping.put(key, label);
        } else {
            _labelMapping.put(key, key);
        }
    }

    public void addChoice(String key, T value)
    {
        addChoice(key, value, key);
    }

    public ChoiceParameter<T> choice(String key, T value)
    {
        addChoice(key, value);
        return this;
    }

    public ChoiceParameter<T> choice(String key, T value, String label)
    {
        addChoice(key, value, label);
        return this;
    }

    @Override
    public void check() throws ParameterException
    {
        if (isRequired() && (_value == null)) {
            throw new ParameterException(this, ParameterException.REQUIRED_MESSAGE);
        }
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
            _value = _mapping.get(key);
        } else {
            throw new ParameterException(this, "Key not found : " + key);
        }
    }

    @Override
    public String getStringValue()
    {
        for (String key : _mapping.keySet()) {
            if (_mapping.get(key).equals(_value)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Sets the value, this must be one of the allowable values, or null.
     * 
     * @param value
     * @throws ParameterException
     */
    public void setValue(T value)
        throws ParameterException
    {
        if ((value != null) || (_mapping.values().contains(value))) {
            _value = value;
        } else {
            throw new ParameterException(this, "Not a valid value");
        }
    }

    /**
     * A fluent version of {@link #setValue(Object)}.
     * Note that if a Parameter is thrown by {@link #setValue(Object)}, it will be quietly ignored.
     * 
     * @param value
     *            The new value
     * @return this
     */
    public ChoiceParameter<T> value(T value)
    {
        try {
            setValue(value);
        } catch (ParameterException e) {
            // Do nothing
        }
        return this;
    }

    public T getValue()
    {
        return _value;
    }

    @Override
    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JComboBox<String> combo = new JComboBox<String>();
        for (String key : _keys) {
            String label = _labelMapping.get(key);
            combo.addItem(label);
        }

        combo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                try {
                    int index = combo.getSelectedIndex();
                    setStringValue(_keys.get(index));
                    taskPrompter.clearError(ChoiceParameter.this);
                } catch (Exception e) {
                    taskPrompter.setError(ChoiceParameter.this, e.getMessage());
                }

            }
        });

        // Select the correct item from the combobox.
        int index = _keys.indexOf(_value);
        if (index >= 0) {
            combo.setSelectedIndex(index);
        }

        /*
         * It seems that JCombobox cannot be displayed such that no item is selected
         * (I don't want to have a JComboBox with text entry), then I guess we need to update
         * the parameter's value based on what the user will see.
         */
        if (_value == null) {
            if (_keys.size() > 0) {
                try {
                    setStringValue(_keys.get(0));
                } catch (ParameterException e) {
                    // Do nothing
                }
            }
        }

        return combo;
    }

    @Override
    public void autocomplete(String cur)
    {
        for (String possible : _keys) {
            Task.autocompleteFilter(possible, cur);
        }
    }
}

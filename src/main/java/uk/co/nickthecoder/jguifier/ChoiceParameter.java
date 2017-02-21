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
 * <p>
 * Each entry has three pieces of data, the key, the value and the label. The key is a String, which is used in the
 * command line. The value can be any object (of generic type T). The label is an optional String, and if specified is
 * used in the GUI's combobox. If the label isn't specified, then the label is the same as the key.
 * </p>
 * For example, if we want a parameter to specify either {@link System#out} or {@link System#err}, then
 * we could do something like :
 * 
 * <code><pre>
 * ChoiceParameter output = new ChoiceParameter<PrintWriter>( "output" );
 * output.addChoice( "stdout", System.out, "Standard Output" );
 * output.addChoice( "stderr", System.err );
 * </pre></code>
 * 
 * As only stdout was given a nice label, the GUI will show a pull down list containing "Standard Output"
 * and "stderr".
 * <p>
 * We could have chosen to use the fluent API like so :
 * </p>
 * 
 * <code><pre>
 * ChoiceParameter output = new ChoiceParameter<PrintWriter>( "output" )
 *     .choice( "stdout", System.out, "Standard Output" );
 *     .choice( "stderr", System.err );
 * </pre></code>
 * 
 */
public class ChoiceParameter<T> extends ValueParameter<T>
{
    /**
     * Maps keys to values. Used to convert a key into its corresponding value.
     */
    private Map<String, T> _mapping = new HashMap<String, T>();

    /**
     * Maps keys to labels. Used to store the labels used by the GUI.
     */
    private Map<String, String> _labelMapping = new HashMap<String, String>();

    /**
     * A list of the possible keys. This is needed to order the options correctly (Maps are not ordered).
     */
    private List<String> _keys;

    private JComboBox<String> _comboBox;

    /**
     * @see Parameter#Parameter(String)
     */
    public ChoiceParameter(String name)
    {
        this(name, null);
    }

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public ChoiceParameter(String name, T value)
    {
        super(name, value);
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

    /**
     * Add a choice, using a fluent API. The label will be the same as the key.
     * 
     * @param key
     *            The key (Used on the command line : --parameterName=key)
     * @param value
     *            The value, as returned by {@link #getValue()}
     * @return
     *         this
     */
    public ChoiceParameter<T> choice(String key, T value)
    {
        addChoice(key, value);
        return this;
    }

    /**
     * Add a choice, using a fluent API.
     * 
     * @param key
     *            The key (Used on the command line : --parameterName=key)
     * @param value
     *            The value, as returned by {@link #getValue()}
     * @param label
     *            The label seen in the GUI pull-down combobox. Not used when using the command line.
     * @return
     *         this
     */
    public ChoiceParameter<T> choice(String key, T value, String label)
    {
        addChoice(key, value, label);
        return this;
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
            if (_mapping.get(key).equals(getValue())) {
                return key;
            }
        }
        return null;
    }

    /**
     * A fluent version of {@link #setValue(Object)}.
     * Note that if a ParameterException is thrown by {@link #setValue(Object)}, it will be quietly ignored.
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

    /**
     * {@inheritDoc}
     * Implemented using a JComboBox
     */
    @Override
    public Component createComponent(final ParametersPanel parametersPanel)
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
                    parametersPanel.clearError(ChoiceParameter.this);

                } catch (Exception e) {
                    parametersPanel.setError(ChoiceParameter.this, e.getMessage());
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

        for (String key : _keys) {
            String label = _labelMapping.get(key);
            _comboBox.addItem(label);
        }

        // Select the correct item from the combobox.
        int index = _keys.indexOf(getValue());
        if (index >= 0) {
            _comboBox.setSelectedIndex(index);
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
}

package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A parameter holding values true, false and null.
 * From the command line a BooleanParameter can be given using the text "true", "1", "yes", and "false", "0", "no".
 * I have no intension of allowing non-english translations of any of these - sorry.
 */
public class BooleanParameter
    extends uk.co.nickthecoder.jguifier.Parameter
{
    private static Set<String> TRUE_VALUES = new HashSet<String>(Arrays.asList("true", "1", "yes"));
    private static Set<String> FALSE_VALUES = new HashSet<String>(Arrays.asList("false", "0", "no"));

    private Boolean _value = null;

    private String _oppositeName = null;

    public BooleanParameter(String name, String label)
    {
        this(name, label, null);
    }

    public BooleanParameter(String name, String label, Boolean defaultValue)
    {
        super(name, label);
        _value = defaultValue;
    }

    public BooleanParameter oppositeName(String name)
    {
        setOppositeName(name);
        return this;
    }

    public void setOppositeName(String name)
    {
        assert !getName().equals(_oppositeName);
        _oppositeName = name;
    }

    public String getOppositeName()
    {
        return _oppositeName;
    }

    public void setValue(Boolean value)
    {
        _value = value;
    }

    @Override
    public void setStringValue(String value)
    {
        value = value.toLowerCase();
        if (TRUE_VALUES.contains(value)) {
            _value = true;
        } else if (FALSE_VALUES.contains(value)) {
            _value = false;
        } else {
            throw new NumberFormatException("Parameter " + this.getName() + " must be false/true (or 0/1)");
        }

    }

    public Boolean getValue()
    {
        return _value;
    }

    @Override
    public String getStringValue()
    {
        if (_value == null) {
            return null;
        }
        return _value.toString();
    }

    @Override
    public void check()
        throws ParameterException
    {
        if (getRequired() && (getValue() == null)) {
            throw new ParameterException(this, ParameterException.REQUIRED_MESSAGE);
        }
    }

    @Override
    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JCheckBox component = new JCheckBox();
        if (Boolean.TRUE == _value) {
            component.setSelected(true);
        } else if (_value == null) {
            // We can't distinguish between an null value and a false value using a GUI,
            // so nulls must become false.
            _value = false;
        }

        component.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent changeEvent)
            {
                try {
                    setValue(component.getModel().isSelected());
                    taskPrompter.clearError(BooleanParameter.this);
                } catch (Exception e) {
                    taskPrompter.setError(BooleanParameter.this, e.getMessage());
                }
            }
        });

        return component;
    }

    @Override
    public String toString()
    {
        return super.toString() + " = " + _value;
    }

    @Override
    public String getHelp()
    {
        if (_oppositeName != null) {
            return "--" + getName() + " , --" + _oppositeName + " (" + getLabel() + ")"
                + (getRequired() ? "" : " optional");
        } else {
            return super.getHelp();
        }
    }

    @Override
    public String getDescription()
    {
        return super.getDescription() + " [true|false]";
    }

    @Override
    public void autocomplete(String cur)
    {
        Task.autocompleteFilter("0", cur);
        Task.autocompleteFilter("1", cur);
        Task.autocompleteFilter("true", cur);
        Task.autocompleteFilter("false", cur);
    }

}

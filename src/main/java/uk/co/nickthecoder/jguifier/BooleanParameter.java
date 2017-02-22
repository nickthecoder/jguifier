package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A parameter holding values true, false or null (if the parameter is {@link #optional()}).
 * <p>
 * Examples of how to pass boolean values from the command line. If the parameter name is "foo" :
 * </p>
 * 
 * <pre>
 * <code>
 * --foo=true --foo=false
 * or just :
 * --foo (sets the value to true)
 * </code>
 * </pre>
 * 
 * If you prefer the shortened version, you should set an {@link #oppositeName(String)}.
 * If the opposite name is "bar", then :
 * 
 * <pre>
 * <code>
 * --bar (sets the foo's value to false).
 * </code>
 * </pre>
 * 
 */
public class BooleanParameter
    extends ValueParameter<Boolean>
{
    private static Set<String> TRUE_VALUES = new HashSet<String>(Arrays.asList("true", "1", "yes"));
    private static Set<String> FALSE_VALUES = new HashSet<String>(Arrays.asList("false", "0", "no"));

    private String _oppositeName = null;

    public BooleanParameter(String name, Boolean value)
    {
        super(name, value);
    }

    /**
     * @param name
     */
    public void setOppositeName(String name)
    {
        assert !getName().equals(_oppositeName);
        _oppositeName = name;
    }

    public String getOppositeName()
    {
        return _oppositeName;
    }

    @Override
    public void setStringValue(String value)
    {
        value = value.toLowerCase();
        if (TRUE_VALUES.contains(value)) {
            setValue(true);
        } else if (FALSE_VALUES.contains(value)) {
            setValue(false);
        } else {
            throw new NumberFormatException("Parameter " + this.getName() + " must be false/true (or 0/1)");
        }

    }

    @Override
    public Component createComponent(final ParametersPanel parametersPanel)
    {
        final JCheckBox component = new JCheckBox();
        if (Boolean.TRUE == getValue()) {
            component.setSelected(true);
        } else if (getValue() == null) {
            // We can't distinguish between an null value and a false value using a GUI,
            // so nulls must become false.
            setValue(false);
        }

        component.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent changeEvent)
            {
                try {
                    setValue(component.getModel().isSelected());
                    parametersPanel.clearError(BooleanParameter.this);
                } catch (Exception e) {
                    parametersPanel.setError(BooleanParameter.this, e.getMessage());
                }
            }
        });

        return component;
    }

    @Override
    public String getHelp()
    {
        if (_oppositeName != null) {
            return super.getHelp() + "\n    --" + _oppositeName;
        } else {
            return super.getHelp();
        }
    }

    @Override
    public String getDescription()
    {
        if (_oppositeName == null) {
            String sup = super.getDescription();
            return (sup == null ? "" : sup) + " [true|false]";
        } else {
            return super.getDescription();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @priority 5
     */
    @Override
    public void autocomplete(String cur)
    {
        Task.autocompleteFilter("0", cur);
        Task.autocompleteFilter("1", cur);
        Task.autocompleteFilter("true", cur);
        Task.autocompleteFilter("false", cur);
    }

    public static final class Builder extends ValueParameter.Builder<BooleanParameter.Builder, BooleanParameter, Boolean>
    {
        public Builder(String name)
        {
            making = new BooleanParameter(name, null);
        }

        public Builder oppositeName(String oppositeName)
        {
            making.setOppositeName(oppositeName);
            return this;
        }
    }

}

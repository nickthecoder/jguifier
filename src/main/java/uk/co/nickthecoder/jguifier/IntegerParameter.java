package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A {@link Parameter}, which must be an integer value, or may be null.
 *
 */
public class IntegerParameter
    extends ValueParameter<Integer>
{
    private int _minimum = Integer.MIN_VALUE;

    private int _maximum = Integer.MAX_VALUE;

    public IntegerParameter(String name, String label)
    {
        super(name, label);
    }

    public IntegerParameter(String name, String label, Integer defaultValue)
    {
        super(name, label, defaultValue);
    }

    public IntegerParameter range(Integer min, Integer max)
    {
        setRange(min, max);
        return this;
    }

    public IntegerParameter setRange(Integer min, Integer max)
    {
        if (min == null) {
            _minimum = Integer.MIN_VALUE;
        } else {
            _minimum = min;
        }

        if (max == null) {
            _maximum = Integer.MAX_VALUE;
        } else {
            _maximum = max;
        }

        return this;
    }

    public int getMinimumValue()
    {
        return _minimum;
    }

    public int getMaximumValue()
    {
        return _maximum;
    }

    @Override
    public void setStringValue(String string)
        throws ParameterException
    {
        if (Util.empty(string)) {
            setValue(null);

        } else {

            try {

                int value = Integer.parseInt(string);
                setValue(value);

            } catch (Exception e) {
                try {
                    Double.parseDouble(string);
                    throw new ParameterException(this, "Not a whole number");
                } catch (NumberFormatException nfe) {
                    throw new ParameterException(this, "Not a number");
                }

            }
        }

        check();
    }

    @Override
    public void check()
        throws ParameterException
    {
        if (getValue() == null) {
            if (isRequired()) {
                throw new ParameterException(this, ParameterException.REQUIRED_MESSAGE);
            }
            return;
        }

        int value = getValue();

        if (value < _minimum) {
            throw new ParameterException(this, "Minimum value is " + _minimum);
        }
        if (value > _maximum) {
            throw new ParameterException(this, "Maximum value is " + _maximum);
        }
    }

    @Override
    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JTextField component = new JTextField(getValue() == null ? "" : getValue().toString());
        component.setColumns(6);

        component.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                checkValue();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                checkValue();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                checkValue();
            }

            public void checkValue()
            {
                try {
                    setStringValue(component.getText());
                    taskPrompter.clearError(IntegerParameter.this);
                } catch (Exception e) {
                    taskPrompter.setError(IntegerParameter.this, e.getMessage());
                }
            }
        });

        return component;
    }
}

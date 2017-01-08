package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A parameter which accepts floating point numbers as well as null.
 * Can optionally specify a minimum and maximum value.
 * 
 */
public class DoubleParameter
    extends ValueParameter<Double>
{
    private double _minimum = Double.MIN_VALUE;

    private double _maximum = Double.MAX_VALUE;

    public DoubleParameter(String name, String label)
    {
        super(name, label);
    }

    public DoubleParameter(String name, String label, Double defaultValue)
    {
        super(name, label, defaultValue);
    }

    public DoubleParameter range(Double min, Double max)
    {
        setRange(min, max);
        return this;
    }

    public void setRange(Double min, Double max)
    {
        if (min == null) {
            _minimum = Double.MIN_VALUE;
        } else {
            _minimum = min;
        }

        if (max == null) {
            _maximum = Double.MAX_VALUE;
        } else {
            _maximum = max;
        }
    }

    @Override
    public void setStringValue(String string)
        throws ParameterException
    {

        if (Util.empty(string)) {
            setValue(null);
        } else {
            try {
                double value = Double.parseDouble(string);
                setValue(value);

            } catch (Exception e) {
                throw new ParameterException(this, "Not a number");
            }
        }
        check();

    }

    @Override
    public String valid(Double value)
        throws ParameterException
    {
        if (value == null) {
            return super.valid(value);
        }

        if (value < _minimum) {
            return "Minimum value is " + _minimum;
        }
        if (value > _maximum) {
            return "Maximum value is " + _maximum;
        }

        return null;
    }

    @Override
    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JTextField component = new JTextField(getValue() == null ? "" : getValue().toString());
        component.setColumns(10);

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
                    check();
                    taskPrompter.clearError(DoubleParameter.this);
                } catch (NumberFormatException e) {
                    taskPrompter.setError(DoubleParameter.this, "Not an integer");
                } catch (Exception e) {
                    taskPrompter.setError(DoubleParameter.this, e.getMessage());
                }
            }
        });

        return component;
    }
}

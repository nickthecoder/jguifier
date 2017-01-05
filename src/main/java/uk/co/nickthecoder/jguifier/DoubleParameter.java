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
    extends Parameter
{
    private Double _value = null;

    private double _minimum = Double.MIN_VALUE;

    private double _maximum = Double.MAX_VALUE;

    public DoubleParameter(String name, String label)
    {
        super(name, label);
    }

    public DoubleParameter(String name, String label, Double defaultValue)
    {
        super(name, label);
        _value = defaultValue;
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

    public DoubleParameter setValue(Double value)
        throws ParameterException
    {
        _value = value;
        check();
        return this;
    }

    @Override
    public void setStringValue(String string)
        throws ParameterException
    {

        if (Util.empty(string)) {
            _value = null;
        } else {
            try {
                double value = Double.parseDouble(string);
                _value = value;

            } catch (Exception e) {
                throw new ParameterException(this, "Not a number");
            }
        }
        check();

    }

    public Double getValue()
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
        if (getValue() == null) {
            if (isRequired()) {
                throw new ParameterException(this, ParameterException.REQUIRED_MESSAGE);
            }
            return;
        }

        double value = getValue();

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
        final JTextField component = new JTextField(_value == null ? "" : _value.toString());
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

    @Override
    public String toString()
    {
        return super.toString() + " = " + _value;
    }

}

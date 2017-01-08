package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A parameter which accepts floating point numbers as well as null.
 * Can optionally specify a minimum and maximum value.
 * 
 */
public class DoubleParameter
    extends TextParameter<Double>
{
    private double _minimum = Double.MIN_VALUE;

    private double _maximum = Double.MAX_VALUE;

    public DoubleParameter(String name, String label)
    {
        super(name, label);
        _columns = 8;
    }

    public DoubleParameter(String name, String label, Double defaultValue)
    {
        super(name, label, defaultValue);
        _columns = 8;
    }

    public double getMinimumValue()
    {
        return _minimum;
    }

    public double getMaximumValue()
    {
        return _maximum;
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
        Double value = getValue();
        if (value == null) {
            value = 0.0;
        }
        if (value < getMinimumValue()) {
            value = getMinimumValue();
        }
        if (value > getMaximumValue()) {
            value = getMaximumValue();
        }
        // If value has been changed, then update the parameter's, to prevent "Required" error message,
        // when the default was not set.
        setValue( value );
        
        final SpinnerNumberModel model = new SpinnerNumberModel(value, (Double) getMinimumValue(),
            (Double) getMaximumValue(), (Double) 1.0);
        final JSpinner component = new JSpinner(model);
        final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) component.getEditor();
        final JTextField textField = editor.getTextField();

        textField(textField, taskPrompter);

        return component;
    }
}

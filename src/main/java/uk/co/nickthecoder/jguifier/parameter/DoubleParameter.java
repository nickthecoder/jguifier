package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A {@link Parameter}, which must be a double value, or may be null.
 * Can be constrained by a range.
 * 
 * Create using the corresponding {@link Builder} class, like so :
 * 
 * <pre>
 * <code>
 * new DoubleParameter.Builder( "theName" ).range( 0.0, 10.0 ).description( "blah" ).parameters();
 * </pre>
 * 
 * @see Builder
 *      </code>
 */
public class DoubleParameter
    extends TextParameter<Double>
{
    private double _minimum = Double.MIN_VALUE;

    private double _maximum = Double.MAX_VALUE;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public DoubleParameter(String name)
    {
        super(name);
        _columns = 8;
    }

    /**
     * @return The minimum allowable value, or {@link Double#MIN_VALUE}
     * @see {@link #setRange(Double, Double)}, {@link Builder#range(Double, Double)}.
     * @priority 3
     */
    public double getMinimumValue()
    {
        return _minimum;
    }

    /**
     * @return The maximum allowable value, or {@link Double#MAX_VALUE}
     * @see {@link #setRange(Double, Double)}, {@link Builder#range(Double, Double)}.
     * @priority 3
     */
    public double getMaximumValue()
    {
        return _maximum;
    }

    /**
     * Sets the range of valid values.
     * 
     * @param min
     *            Use {@link Double#MIN_VALUE} if you do not want a lower bound.
     * @param max
     *            Use {@link Double#MAX_VALUE} if you do not want an upper bound.
     * @see {@link Builder#range(Double, Double)}
     * @priority 1
     */
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
            double value;
            try {
                value = Double.parseDouble(string);

            } catch (Exception e) {
                throw new ParameterException(this, "Not a number");
            }
            setValue(value);
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
    public Component createComponent(final ParameterHolder holder)
    {
        Component component;
        JTextField textField;
        Double value = getValue();

        if (isRequired()) {
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
            setValue(value);

            SpinnerNumberModel model = new SpinnerNumberModel(value, (Double) getMinimumValue(),
                (Double) getMaximumValue(), (Double) 1.0);
            JSpinner spinner = new JSpinner(model);
            
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            textField = editor.getTextField();
            component = spinner;

        } else {
            // Optional DoubleParameters cannot use a JSpinner, because it doesn't allow the number to be blank.
            // Could implement my own version of JSpinner
            textField = new JTextField();
            textField.setText(getValue() == null ? "" : getStringValue());
            component = textField;
        }

        textField(component, textField, holder);

        return component;
    }

    public static final class Builder extends ValueParameter.Builder<Builder, DoubleParameter, Double>
    {
        public Builder(String name)
        {
            making = new DoubleParameter(name);
        }

        public Builder range(Double min, Double max)
        {
            making.setRange(min, max);
            return this;
        }
    }
}

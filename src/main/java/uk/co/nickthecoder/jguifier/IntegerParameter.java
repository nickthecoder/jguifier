package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A {@link Parameter}, which must be an integer value, or may be null.
 * Can be constrained by a range.
 * 
 * Create using the corresponding {@link Builder} class, like so :
 * 
 * <pre>
 * <code>
 * new IntegerParameter.Builder( "theName" ).range( 0, 10 ).description( "blah" ).parameters();
 * </pre>
 * 
 * </code>
 */
public class IntegerParameter
    extends TextParameter<Integer>
{
    private int _minimum = Integer.MIN_VALUE;

    private int _maximum = Integer.MAX_VALUE;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public IntegerParameter(String name)
    {
        super(name);
        _columns = 6;
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
    public Component createComponent(final ParameterHolder holder)
    {
        Component component;
        JTextField textField;
        Integer value = getValue();

        if (isRequired()) {
            if (value == null) {
                value = 0;
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

            SpinnerNumberModel model = new SpinnerNumberModel(value, (Integer) getMinimumValue(),
                (Integer) getMaximumValue(), (Integer) 1);
            JSpinner spinner = new JSpinner(model);

            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            textField = editor.getTextField();
            component = spinner;
            
        } else {
            // Optional IntegerParameters cannot use a JSpinner, because it doesn't allow the number to be blank.
            // Could implement my own version of JSpinner
            textField = new JTextField();
            textField.setText(getValue() == null ? "" : getValue().toString());
            component = textField;
        }

        textField(textField, holder);

        return component;
    }

    public static final class Builder extends ValueParameter.Builder<Builder, IntegerParameter, Integer>
    {
        public Builder(String name)
        {
            making = new IntegerParameter(name);
        }

        public Builder range(Integer min, Integer max)
        {
            making.setRange(min, max);
            return this;
        }
    }
}

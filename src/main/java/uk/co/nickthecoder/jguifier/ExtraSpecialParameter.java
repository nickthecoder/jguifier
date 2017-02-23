package uk.co.nickthecoder.jguifier;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Combines a {@link ChoiceParameter}, and any other {@link ValueParameter} into one.
 * The ChoiceParameter holds a list of special values as well as a null value to indicate that a special value
 * hasn't been chosen.
 * <p>
 * When a special parameter has been chosen, then the regular parameter is ignored. However, when the special parameter
 * value is null, then the regular parameter is used instead.
 * </p>
 * 
 * <h2>Example</h2>
 * <p>
 * An <code>output</code> parameter, which can have special values of <code>System.out</code> and
 * <code>System.err</code>. In addition, you can specify any File, using a FileParameter as the regular parameter.
 * </p>
 * 
 * <pre>
 * <code>
 *      private ExtraSpecialParameter<PrintStream, FileParameter, File> _outout =
 *         new ExtraSpecialParameter.Builder<PrintStream, FileParameter, File>("special")
 *             .choice("file", null, "Output to File")
 *             .choice("out", System.out, "Standard Output")
 *             .choice("err", System.err, "Standard Error")
 *             .value(System.out)
 *             .regular(
 *                 new FileParameter.Builder("specialFile").value(new File("out.txt")).writable().parameter()
 *             )
 *             .parameter();
 * </code>
 * </pre>
 * 
 * Your tasks's {@link Task#run()} method would first check if a special value has been chosen, and if not, then
 * use the regular parameter :
 * 
 * <pre>
 * <code>
 *      PrintWriter out = _output.getValue();
 *      if ( out == null ) {
 *          out = new PrintStream( _output.getRegularParameter().getValue() );
 *      }
 * </code>
 * </pre>
 *
 * @param <S>
 *            The type of the special values, such as PrintStream
 * @param <P>
 *            The wrapped (custom) parameter, such as FileParameter
 * @param <T>
 *            The type of the wrapped parameter's values, such as File
 */
public class ExtraSpecialParameter<S, P extends ValueParameter<T>, T>
    extends ChoiceParameter<S>
{

    private P _regularParameter;

    /**
     * A prefix for all of the special value keys. The default is "@". Therefore, if you add a special choice of
     * <code>out</code>, to the <code>debug</code> parameter, then the command line may look like :
     * <code>--debug=@out</code>
     */
    public String specialPrefix = "@";

    /**
     * By default there is no suffix, this is the empty string.
     */
    public String specialSuffix = "";

    public ExtraSpecialParameter(String name)
    {
        super(name);
    }

    @Override
    public void addChoice(String key, S value, String label)
    {
        // When adding the first special choice, if it has a null value, then in is the "custom" choice,
        // otherwise, we need to create the "custom" choice first.
        if (_keys.size() == 0) {
            if (value != null) {
                addChoice("custom", null, "Custom");
            }
        }
        super.addChoice(specialPrefix + key + specialSuffix, value, label);
    }

    /**
     * A short cut to <code>getRegularParameter().getValue()</code>.
     * Note, you shouldn't care about the regular parameter's value if <code>this.getValue()</code> is not null.
     * 
     * @return The value of the regular parameter
     */
    public T getRegularValue()
    {
        return getRegularParameter().getValue();
    }

    @Override
    public String valid(S value)
    {
        if (getRequired() && (value == null)) {
            if (Util.empty(getRegularParameter().getStringValue())) {
                return ParameterException.REQUIRED_MESSAGE;
            }
        }

        return null;
    }

    public boolean isStretchy()
    {
        return _regularParameter == null ? false : _regularParameter.isStretchy();
    }

    public P getRegularParameter()
    {
        return _regularParameter;
    }

    public void setRegularParameter(P regularParameter)
    {
        this._regularParameter = regularParameter;

        // When the wrapped parameter changes, fire an event for the ExtraSpecialParameter too
        regularParameter.addListener(new ParameterListener()
        {
            @Override
            public void changed(Parameter source)
            {
                fireChangeEvent();
            }
        });
    }

    @Override
    public void setValue(S value)
    {
        try {
            super.setValue(value);
        } catch (Exception e) {
            // When the "custom" value is picked, we need to send events, even when the regular value is in error
            // Without this, the regular parameter wouldn't be made visible.
            fireChangeEvent();
        }
    }

    @Override
    public void setStringValue(String value)
    {
        if (_mapping.containsKey(value)) {
            super.setStringValue(value);
        } else {
            _regularParameter.setStringValue(value);
        }
    }

    @Override
    public String getStringValue()
    {
        if (getValue() == null) {
            return _regularParameter.getStringValue();
        } else {
            // A special value
            return super.getStringValue();
        }
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        assert (_regularParameter != null);

        JPanel both = new JPanel();
        both.setLayout(new BorderLayout());

        // We need to forward the setError and clearError messages to the real holder using THIS parameter, rather than
        // the _regularParameter.
        ParameterHolder forward = new ParameterHolder()
        {
            @Override
            public void setError(Parameter parameter, String message)
            {
                holder.setError(ExtraSpecialParameter.this, message);
            }

            @Override
            public void clearError(Parameter parameter)
            {
                holder.clearError(ExtraSpecialParameter.this);
            }
        };

        final Component specialComponent = super.createComponent(holder);
        final Component regularComponent = _regularParameter.createComponent(forward);

        both.add(specialComponent, BorderLayout.WEST);
        both.add(regularComponent, BorderLayout.CENTER);

        regularComponent.setVisible(getValue() == null);

        addListener(new ParameterListener()
        {
            @Override
            public void changed(Parameter source)
            {
                regularComponent.setVisible(getValue() == null);
            }
        });

        return both;

    }

    public static final class Builder<S2, P2 extends ValueParameter<T2>, T2>
        extends ChoiceBuilder<Builder<S2, P2, T2>, ExtraSpecialParameter<S2, P2, T2>, S2>
    {
        public Builder(String name)
        {
            making = new ExtraSpecialParameter<S2, P2, T2>(name);
        }

        public Builder<S2, P2, T2> regular(P2 regularParameter)
        {
            making.setRegularParameter(regularParameter);
            return this;
        }

        public Builder<S2, P2, T2> prefix(String prefix)
        {
            making.specialPrefix = prefix;
            return this;
        }

        public Builder<S2, P2, T2> suffix(String suffix)
        {
            making.specialSuffix = suffix;
            return this;
        }
    }

}

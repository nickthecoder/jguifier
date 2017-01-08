package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * The simplest type of {@link Parameter}, which can be any text.
 * 
 */
public class StringParameter
    extends ValueParameter<String>
{
    private int _columns = 30;

    protected boolean _stretchy = false;

    public StringParameter(String name, String label)
    {
        super(name, label);
    }

    public StringParameter(String name, String label, String defaultValue)
    {
        super(name, label, defaultValue);
    }

    public StringParameter stretch()
    {
        setStretchy(true);
        return this;
    }

    @Override
    public boolean isStretchy()
    {
        return _stretchy;
    }

    public void setStretchy(boolean value)
    {
        _stretchy = value;
    }

    @Override
    public void setStringValue(String value)
        throws ParameterException
    {
        setValue(value);
    }

    public StringParameter value(String value)
        throws ParameterException
    {
        try {
            setValue(value);
        } catch (Exception e) {
            // Do nothing - let the default value be illegal
        }
        return this;
    }

    public StringParameter columns(int value)
    {
        setColumns(value);
        return this;
    }

    public void setColumns(int value)
    {
        this._columns = value;
    }

    public int getColumns()
    {
        return _columns;
    }

    @Override
    public String valid( String value )
    {
        if (isRequired() && (Util.empty(value))) {
            return super.valid(null);
        }
        return null;
    }

    @Override
    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JTextField component = new JTextField(getValue() == null ? "" : getValue());
        component.setColumns(_columns);

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
                    taskPrompter.clearError(StringParameter.this);
                } catch (Exception e) {
                    taskPrompter.setError(StringParameter.this, e.getMessage());
                }
            }
        });

        return component;
    }

}

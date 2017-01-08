package uk.co.nickthecoder.jguifier;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameters which have a text field (where the user can type the value), such as strings, integers, doubles, files.
 * Holds common stuff based on JTextField.
 * 
 * @param <T>
 */
public abstract class TextParameter<T> extends ValueParameter<T>
{
    protected int _columns = 30;

    protected boolean _stretchy = false;

    public TextParameter(String name, String label)
    {
        super(name, label);
    }

    public TextParameter(String name, String label, T defaultValue)
    {
        super(name, label, defaultValue);
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
    public boolean isStretchy()
    {
        return _stretchy;
    }

    public void setStretchy(boolean value)
    {
        _stretchy = value;
    }

    protected void textField(final JTextField textField, final TaskPrompter taskPrompter)
    {
        textField.setColumns(_columns);

        textField.getDocument().addDocumentListener(new DocumentListener()
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
                    setStringValue(textField.getText());
                    taskPrompter.clearError(TextParameter.this);
                } catch (Exception e) {
                    taskPrompter.setError(TextParameter.this, e.getMessage());
                }
            }
        });
    }
}

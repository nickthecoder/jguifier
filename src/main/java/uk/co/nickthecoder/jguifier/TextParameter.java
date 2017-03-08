package uk.co.nickthecoder.jguifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameters which have a text field (where the user can type the value), such as strings, integers, doubles, files.
 * Holds common stuff based on {@link JTextField}.
 * <p>
 * You can probably ignore this class, unless you want to write another Parameter sub-class.
 * </p>
 * 
 * @param <T>
 *            The parameter's value type, such as Double for DoubleParameter, File for FileParameter etc.
 * @see StringParameter
 * @priority 4
 */
public abstract class TextParameter<T> extends ValueParameter<T>
{
    protected int _columns = 30;

    protected boolean _stretchy = false;

    public TextParameter(String name)
    {
        super(name);
    }

    public void setColumns(int value)
    {
        this._columns = value;
        setStretchy(false);
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

    protected boolean inNotification = false;

    protected void textField(final JTextField textField, final ParameterHolder holder)
    {
        addListener(new ParameterListener()
        {
            @Override
            public void changed(Parameter source)
            {
                if (!inNotification) {
                    textField.setText(getStringValue());
                }
            }
        });

        textField.setBackground(Color.white);
        textField.setColumns(_columns);
        textField.setMinimumSize(new Dimension(10, textField.getPreferredSize().height));

        DocumentListener documentListener = new DocumentListener()
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
                inNotification = true;
                try {
                    setStringValue(textField.getText());
                    holder.clearError(TextParameter.this);
                } catch (Exception e) {
                    holder.setError(TextParameter.this, e.getMessage());
                } finally {
                    inNotification = false;
                }
            }
        };
        textField.getDocument().addDocumentListener(documentListener);

        // A horrible bodge to get around a bug. If the text field is smaller than the length of the text,
        // then when the textfield receives focus, it isn't scrolled to the caret position, so you see the start of
        // the text, but the caret is at the end (and is not visible).
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                int oldPosition = textField.getCaretPosition();
                if (oldPosition > 0) {
                    textField.setCaretPosition(oldPosition - 1);
                    textField.setCaretPosition(oldPosition);
                }
            }
        });

        enableFocusColorChange(textField);
    }

    public abstract static class Builder<B extends Builder<B, P, T>, P extends TextParameter<T>, T>
        extends ValueParameter.Builder<B, P, T>
    {
        public B columns(int columns)
        {
            making.setColumns(columns);
            return me();
        }
    }
}

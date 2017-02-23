package uk.co.nickthecoder.jguifier;

import java.awt.Dimension;

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

    protected void textField(final JTextField textField, final ParameterHolder holder)
    {
        textField.setColumns(_columns);
        textField.setMinimumSize(new Dimension(10, textField.getPreferredSize().height));
        
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
                    holder.clearError(TextParameter.this);
                } catch (Exception e) {
                    holder.setError(TextParameter.this, e.getMessage());
                }
            }
        });
    }
    
    public abstract static class Builder<B extends Builder<B,P,T>,P extends TextParameter<T>,T>
        extends ValueParameter.Builder<B,P,T>
    {
        public B columns( int columns )
        {
            making.setColumns( columns );
            return me();
        }
    }
}

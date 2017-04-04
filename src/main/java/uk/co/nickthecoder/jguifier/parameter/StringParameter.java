package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * The simplest type of {@link Parameter}, which can be any text.
 * 
 */
public class StringParameter
    extends TextParameter<String>
{
    public int maxLength = Integer.MAX_VALUE;

    public boolean multiLine;
    
    public boolean fixedWidth;

    /**
     * Only used for multi-line. See {@link Builder#multiLine()}.
     */
    public Dimension size;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public StringParameter(String name)
    {
        super(name);
        setStretchy(true);
    }

    @Override
    public void setStringValue(String value)
        throws ParameterException
    {
        setValue(value);
    }

    public void setMaxLength(int value)
    {
        this.maxLength = value;
        if (value <= 30) {
            setColumns(value);
        }
    }

    @Override
    public String valid(String value)
    {
        if (isRequired() && (Util.empty(value))) {
            return super.valid(null);
        }
        if ((value != null) && (value.length() > maxLength)) {
            return "Too long. Maximum of " + maxLength + " characters";
        }
        return null;
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        if (multiLine) {
            JTextArea textArea = new JTextArea(getValue() == null ? "" : getValue());
            textField(textArea, holder);

            if ( fixedWidth ) {
                textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
            }
            
            JScrollPane scrollPane = new JScrollPane(textArea);

            scrollPane.setPreferredSize(size == null ? new Dimension(300, 100) : size);

            return scrollPane;

        } else {
            JTextField component = new JTextField(getValue() == null ? "" : getValue());
            textField(component, holder);
            return component;
        }
    }

    public static final class Builder extends TextParameter.Builder<Builder, StringParameter, String>
    {
        public Builder(String name)
        {
            making = new StringParameter(name);
        }

        public Builder stretch()
        {
            making.setStretchy(true);
            return this;
        }

        public Builder maxLength(int maxLength)
        {
            making.setMaxLength(maxLength);
            return this;
        }

        public Builder multiLine()
        {
            making.multiLine = true;
            return this;
        }

        public Builder fixedWidth()
        {
            making.fixedWidth = true;
            return this;
        }

        public Builder size(int width, int height)
        {
            making.size = new Dimension(width, height);
            return this;
        }
    }
}

package uk.co.nickthecoder.jguifier.parameter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Holds isRegex Pattern, but you can either use a glob, or a isRegex as the source.
 * From the command line, prefix a isRegex with <code>&quot;/&quot;</code> like so : <code>--myPattern='/.*'</code> <br/>
 * or use a glob, without any prefix : <code>--myPattern='*'</code>.
 * To allow globs beginning with '/', then if the first character is a '\', then it is ignored.
 * So if you want to be safe, then prefix globs by '\', but isn't needed most of the time. Note
 */
public class PatternParameter extends TextParameter<String>
{
    private String globOrRegex;

    private boolean isRegex;

    public static String globToRegex(String glob)
    {
        StringBuffer s = new StringBuffer(glob.length());
        // s.append('^');
        for (int i = 0, is = glob.length(); i < is; i++) {
            char c = glob.charAt(i);
            switch (c) {
            case '*':
                s.append(".*");
                break;
            case '?':
                s.append(".");
                break;
            // escape special regexp-characters
            case '(':
            case ')':
            case '[':
            case ']':
            case '$':
            case '^':
            case '.':
            case '{':
            case '}':
            case '|':
            case '\\':
                s.append("\\");
                s.append(c);
                break;
            default:
                s.append(c);
                break;
            }
        }
        // s.append('$');
        return (s.toString());
    }

    public PatternParameter(String name)
    {
        super(name);
        setStretchy(true);
        setDefaultValue("");
    }

    @Override
    public void setStringValue(String value) throws ParameterException
    {
        setValue(value);
    }

    public String getStringValue()
    {
        return getValue();
    }

    public void setValue(String value)
    {
        if (value == null) {
            isRegex = true;
            globOrRegex = "";
            super.setValue(null);
            return;
        }
        isRegex = value.startsWith("/");

        if (isRegex) {
            globOrRegex = value.substring(1);
        } else {
            if (value.startsWith("\\")) {
                globOrRegex = value.substring(1);
            } else {
                globOrRegex = value;
            }
        }

        super.setValue(value);

    }

    public void setValue(String globOrRegex, boolean isRegex)
    {
        if (isRegex) {
            setValue("/" + globOrRegex);
        } else {
            if (globOrRegex.startsWith("\\")) {
                setValue("\\" + globOrRegex);
            } else {
                setValue(globOrRegex);
            }
        }
    }

    public boolean isRegex()
    {
        return isRegex;
    }

    public boolean isGlob()
    {
        return !isRegex();
    }

    @Override
    public String valid(String value)
    {
        if (("".equals(value) || "/".equals(value)) && isRequired()) {
            return ParameterException.REQUIRED_MESSAGE;
        }

        boolean isRegex = value.startsWith("/");

        String regex;
        if (isRegex) {
            regex = value.substring(1);
        } else {
            if (value.startsWith("\\")) {
                regex = globToRegex(value.substring(1));
            } else {
                regex = globToRegex(value);
            }
        }

        try {
            Pattern.compile(regex);
        } catch (Exception e) {
            return "Not a valid pattern (" + regex + ")";
        }

        return null;
    }

    public String getRegex()
    {
        if (isRegex) {
            return globOrRegex;
        } else {
            return globToRegex(globOrRegex);
        }
    }

    public Pattern getPattern()
    {
        if (Util.empty(globOrRegex)) {
            return null;
        }

        return Pattern.compile(getRegex());
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        return new RegexComponent(holder);
    }

    public class RegexComponent extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private ParameterHolder holder;

        private JTextField textField;

        private JRadioButton regexButton;
        private JRadioButton globButton;

        public RegexComponent(ParameterHolder holderX)
        {
            this.holder = holderX;

            textField = new JTextField(globOrRegex);

            regexButton = new JRadioButton("RegEx");
            globButton = new JRadioButton("Glob");

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(regexButton);
            buttonGroup.add(globButton);

            globButton.setSelected(!isRegex);
            regexButton.setSelected(isRegex);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BorderLayout());
            buttonPanel.add(regexButton, BorderLayout.CENTER);
            buttonPanel.add(globButton, BorderLayout.SOUTH);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            setLayout(new GridBagLayout());
            add(textField, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0;
            // gbc.fill = GridBagConstraints.NONE;
            add(buttonPanel, gbc);

            addListener(new ParameterListener()
            {
                @Override
                public void changed(Parameter source)
                {
                    if (!inNotification) {
                        textField.setText(globOrRegex);
                        regexButton.setSelected(isRegex);
                        globButton.setSelected(!isRegex);
                    }
                }
            });

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

            });

            regexButton.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent event)
                {
                    inNotification = true;
                    try {
                        setValue(textField.getText(), regexButton.isSelected());
                        textField.requestFocusInWindow();
                        holder.clearError(PatternParameter.this);
                    } catch (Exception e) {
                        holder.setError(PatternParameter.this, e.getMessage());
                    } finally {
                        inNotification = false;
                    }
                }
            });
            textField(textField, holder);
        }

        private void checkValue()
        {
            inNotification = true;
            try {
                setValue(textField.getText(), regexButton.isSelected());
                holder.clearError(PatternParameter.this);
            } catch (Exception e) {
                holder.setError(PatternParameter.this, e.getMessage());
            } finally {
                inNotification = false;
            }
        }
    }

    public static final class Builder extends TextParameter.Builder<Builder, PatternParameter, String>
    {
        public Builder(String name)
        {
            making = new PatternParameter(name);
        }

        public Builder regex(String regex)
        {
            making.setValue(regex, true);
            return this;
        }

        public Builder glob(String glob)
        {
            making.setValue(glob, false);
            return this;
        }
    }
}

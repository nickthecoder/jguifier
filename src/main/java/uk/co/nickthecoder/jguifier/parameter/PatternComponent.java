package uk.co.nickthecoder.jguifier.parameter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;

public class PatternComponent extends JPanel
{
    private PatternParameter patternParameter;

    private static final long serialVersionUID = 1L;

    private ParameterHolder holder;

    private JTextField textField;

    private JRadioButton regexButton;
    private JRadioButton globButton;

    public PatternComponent(final PatternParameter patternParameter, ParameterHolder holderX)
    {
        this.patternParameter = patternParameter;
        this.holder = holderX;

        textField = new JTextField(patternParameter.globOrRegex);

        regexButton = new JRadioButton("RegEx");
        globButton = new JRadioButton("Glob");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(regexButton);
        buttonGroup.add(globButton);

        globButton.setSelected(!patternParameter.isRegex);
        regexButton.setSelected(patternParameter.isRegex);

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

        patternParameter.addListener(new ParameterListener()
        {
            @Override
            public void changed(Object initiator, Parameter source)
            {
                if (initiator != PatternComponent.this) {
                    textField.setText(patternParameter.globOrRegex);
                    regexButton.setSelected(patternParameter.isRegex);
                    globButton.setSelected(!patternParameter.isRegex);
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

        ActionListener radioListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                try {
                    patternParameter.initiator(PatternComponent.this);
                    patternParameter.setValue(textField.getText(), regexButton.isSelected());
                    textField.requestFocusInWindow();
                    holder.clearError(patternParameter);
                } catch (Exception e) {
                    holder.setError(patternParameter, e.getMessage());
                }
            }
        };
        regexButton.addActionListener(radioListener);
        globButton.addActionListener(radioListener);

        patternParameter.textField(this, textField, holder);
    }

    private void checkValue()
    {
        try {
            patternParameter.initiator(this);
            patternParameter.setValue(textField.getText(), regexButton.isSelected());
            holder.clearError(patternParameter);
        } catch (Exception e) {
            holder.setError(patternParameter, e.getMessage());
        }
    }
}
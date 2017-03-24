package uk.co.nickthecoder.jguifier;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import uk.co.nickthecoder.jguifier.parameter.Parameter;

public abstract class AbstractParameterPanel extends JPanel implements ParameterHolder
{
    private static final long serialVersionUID = 1L;

    protected Map<String, JLabel> _parameterErrorLabels;

    public AbstractParameterPanel()
    {
        _parameterErrorLabels = new HashMap<String, JLabel>();
    }
    
    protected JLabel createErrorLabel()
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        JLabel result = new JLabel(icon);
        result.setForeground(Color.RED);

        return result;
    }

    public JLabel getErrorLabel(Parameter parameter)
    {
        return _parameterErrorLabels.get(parameter.getName());
    }

    public void setError(Parameter parameter, String message)
    {
        if (message == null) {
            clearError(parameter);
        }

        JLabel label = _parameterErrorLabels.get(parameter.getName());
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
        }
    }

    public void clearError(Parameter parameter)
    {
        JLabel label = _parameterErrorLabels.get(parameter.getName());
        if (label != null) {
            label.setText("");
            label.setVisible(false);
        }
    }

    public boolean check(Task task)
    {
        boolean result = true;

        for (Parameter parameter : task.valueParameters()) {
            try {
                parameter.check();
            } catch (ParameterException e) {
                setError(parameter, e.getMessage());
                result = false;
            }
        }

        try {
            task.check();
        } catch (ParameterException e) {
            setError(e.getParameter(), e.getMessage());
            result = false;
        }

        return result;
    }
}

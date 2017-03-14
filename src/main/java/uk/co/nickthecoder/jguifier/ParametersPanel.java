package uk.co.nickthecoder.jguifier;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.guiutil.RowLayoutManager;
import uk.co.nickthecoder.jguifier.guiutil.TableLayoutManager;
import uk.co.nickthecoder.jguifier.parameter.BooleanParameter;
import uk.co.nickthecoder.jguifier.parameter.GroupParameter;
import uk.co.nickthecoder.jguifier.parameter.Parameter;

/**
 * Holds a set of Parameters' GUI components in a panel.
 * 
 * Each parameter has an error JLabel underneath, which is hidden, when there is no error.
 * 
 * The panel does NOT include Ok and Cancel buttons, nor does it have a vertical scroll bar.
 * These are part of {@link TaskPrompter}.
 *
 * @priority 4
 */
public class ParametersPanel extends JPanel implements ParameterHolder
{
    private static final long serialVersionUID = 1L;

    private Map<String, JLabel> _parameterErrorLabels;

    TableLayoutManager _tlm;

    public ParametersPanel()
    {
        _parameterErrorLabels = new HashMap<String, JLabel>();

        // A table of all of the task's parameters
        _tlm = new TableLayoutManager(this, 2);
        _tlm.getColumn(1).stretchFactor = 1;
        setLayout(_tlm);
        setBorder(new EmptyBorder(10, 10, 40, 10));

    }

    public void addParameters(GroupParameter group)
    {
        addParameters(group, this);
    }

    public boolean leftAlignBooleans = true;

    private void addParameters(GroupParameter group, Container container)
    {
        for (Parameter parameter : group.getChildren()) {

            JLabel parameterErrorLabel = createErrorLabel();
            _parameterErrorLabels.put(parameter.getName(), parameterErrorLabel);
            parameterErrorLabel.setVisible(false);
            parameterErrorLabel.setHorizontalAlignment(SwingConstants.LEFT);

            if (parameter instanceof GroupParameter) {
                GroupParameter subGroup = (GroupParameter) parameter;

                Container subContainer = (Container) subGroup.createComponent(this);
                subContainer.setLayout(_tlm);
                container.add(subContainer);

                addParameters(subGroup, subContainer);

            } else {

                Component component = parameter.createComponent(this);

                if ((leftAlignBooleans) && (parameter instanceof BooleanParameter)) {

                    container.add(component);

                } else {
                    JPanel row = new JPanel();
                    RowLayoutManager rlm = new RowLayoutManager(row, _tlm);
                    row.setLayout(rlm);

                    JLabel label = parameter instanceof BooleanParameter ? new JLabel()
                        : new JLabel(parameter.getLabel());
                    
                    rlm.add(label);
                    label.setBackground(Color.BLUE);

                    rlm.add(component);
                    rlm.setStretchy(parameter.isStretchy());

                    if (!parameter.visible) {
                        row.setVisible(false);
                    }
                    String desc = parameter.getDescription();
                    if (desc != null) {
                        label.setToolTipText(desc);
                    }

                    container.add(row);
                }

                container.add(parameterErrorLabel);

            }

        }

    }

    private JLabel createErrorLabel()
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

        for (Parameter parameter : task.getParameters().children()) {
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

package uk.co.nickthecoder.jguifier;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.guiutil.RowLayoutManager;
import uk.co.nickthecoder.jguifier.guiutil.TableLayoutManager;
import uk.co.nickthecoder.jguifier.parameter.BooleanParameter;
import uk.co.nickthecoder.jguifier.parameter.GroupParameter;
import uk.co.nickthecoder.jguifier.parameter.MultipleParameter;
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
public class ParametersPanel extends AbstractParameterPanel
{
    private static final long serialVersionUID = 1L;

    TableLayoutManager _tlm;

    public ParametersPanel()
    {
        this(null);
    }

    public ParametersPanel(ParameterHolder parent)
    {
        super();

        if (parent == null) {
            _tlm = new TableLayoutManager(this, 2);
        } else {
            _tlm = parent.getTableLayoutManager();
            _tlm.getColumn(1).stretchFactor = 1;
        }
        setLayout(_tlm);
        setBorder(new EmptyBorder(10, 10, 40, 10));

    }

    public TableLayoutManager getTableLayoutManager()
    {
        return _tlm;
    }

    public boolean leftAlignBooleans = true;

    @Override
    protected void addParameter(Parameter parameter, AbstractParameterPanel container, Component component)
    {
        JLabel parameterErrorLabel = createErrorLabel();
        _parameterErrorLabels.put(parameter.getName(), parameterErrorLabel);
        parameterErrorLabel.setVisible(false);
        parameterErrorLabel.setHorizontalAlignment(SwingConstants.LEFT);

        if (parameter instanceof GroupParameter) {

            container.add(component);

        } else {

            if ((leftAlignBooleans) && (parameter instanceof BooleanParameter)) {

                container.add(component);

            } else if (parameter instanceof MultipleParameter) {
                // MultipleParameters don't need a label, as they are in a panel which includes
                // the label.

                container.add(component);

            } else {
                JPanel row = new JPanel();
                RowLayoutManager rlm = new RowLayoutManager(row, _tlm);
                row.setLayout(rlm);

                JLabel label = parameter instanceof BooleanParameter ? new JLabel()
                    : new JLabel(parameter.getLabel());

                rlm.add(label);

                rlm.add(component);
                rlm.setStretchy(parameter.isStretchy());

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

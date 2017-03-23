package uk.co.nickthecoder.jguifier.parameter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;
import uk.co.nickthecoder.jguifier.Task;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.guiutil.TableLayoutManager;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A parameter which can be given multiple values.
 * The values are stored in a List. When representing the value as a string, the individual string values are separated
 * by a new line character. Therefore, this cannot be used with a ValueParameter which also may include a new line
 * character in their string value. This shouldn't be a problem though, because new line characters aren't normally
 * allowed.
 * <p>
 * To pass a MultipleParameter value as a command line, simply repeat the --name=value pairs for each value.
 * For example :
 * <p/>
 * <code><pre>
 * myCommand --myParam=abc --myParam=def --myParam=xyz
 * </pre></code>
 * 
 * To create a MultipleParameter using a fluent API (the builder pattern), first create a parameter as if it were to
 * hold a single value, and then instead of called "parameter()", call multipleParameter() instead. For example :
 * <code><pre>
         MultipleParameter<IntegerParameter, Integer> manyInts = new IntegerParameter.Builder("manyInts")
            .range(0, 100).description("blah...")
            .multipleParameter();
 * </pre></code>
 * 
 * @param <P>
 *            The Parameter for a single value
 * @param <T>
 *            The type of value for a single value
 */
public class MultipleParameter<P extends ValueParameter<T>, T> extends ValueParameter<List<T>>
{
    /**
     * Used to convert a single value to/from its string representation.
     * Also used to create new parameter objects using the clone method, when editing values using the TaskPrompter.
     */
    private P prototypeParameter;

    public MultipleParameter(P prototype, String name)
    {
        super(name);
        this.prototypeParameter = prototype;
        setValue(new ArrayList<T>());
    }

    @Override
    public String getStringValue()
    {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (T value : getValue()) {
            prototypeParameter.setValue(value);
            buffer.append(prototypeParameter.getStringValue());
            if (first) {
                first = false;
            } else {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    @Override
    public void setStringValue(String value)
    {
        List<T> list = new ArrayList<T>();

        String[] stringValues = value.split("\n");
        for (String stringValue : stringValues) {
            prototypeParameter.setStringValue(stringValue);
            list.add(prototypeParameter.getValue());
        }
    }

    public void setSingleStringValue(String value)
    {
        List<T> list = getValue();
        prototypeParameter.setStringValue(value);
        list.add(prototypeParameter.getValue());
    }

    /**
     * @return multiple --name=value pairs, one for each item in the list of values.
     * @see GroupParameter#getCommandString()
     */
    public String getCommandArguments()
    {
        StringBuffer buffer = new StringBuffer();

        for (T value : getValue()) {
            prototypeParameter.setValue(value);
            String text = prototypeParameter.getStringValue();

            buffer.append(" --");
            buffer.append(getName());
            buffer.append("=");
            if (text != null) {
                if (text.matches("[a-zA-Z0-9./]*")) {
                    buffer.append(text);
                } else {
                    buffer.append(Util.quote(text));
                }
            }
        }

        return buffer.toString();
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        return new MultipleParameterComponent();
    }

    class MultipleParameterComponent extends JPanel
    {
        public MultiplePanel parametersPanel;

        private static final long serialVersionUID = 1L;

        public MultipleParameterComponent()
        {
            Util.assertIsEDT();

            parametersPanel = new MultiplePanel();
            addComponents();

            setLayout(new BorderLayout());
            add(parametersPanel, BorderLayout.CENTER);

            JButton addButton = new JButton("+");
            add(addButton, BorderLayout.SOUTH);
            addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    newValue();
                }
            });
        }

        private void newValue()
        {
            Util.assertIsEDT();

            T value = prototypeParameter.getValue();
            getValue().add(value);
            addComponent(value, getValue().size() - 1);

            // Hmm, I was having redraw problems. validate, doLayout, repaint etc didn't work, but this did.
            // Yes, I know it's bad, and there is probably a bug somewhere. Soz.
            parametersPanel.setVisible(false);
            parametersPanel.setVisible(true);
        }

        private void addComponents()
        {
            int i = 0;
            for (T value : getValue()) {
                addComponent(value, i);
                i++;
            }
        }

        private void addComponent(T value, final int index)
        {
            Util.assertIsEDT();

            @SuppressWarnings("unchecked")
            final P parameter = (P) prototypeParameter.clone();
            parameter.setValue(value);

            parameter.addListener(new ParameterListener()
            {
                @Override
                public void changed(Parameter source)
                {
                    getValue().set(index, parameter.getValue());
                }
            });

            Component component = parameter.createComponent(parametersPanel);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            JButton removeButton = new JButton(" - ");
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    getValue().remove(index);
                    parametersPanel.clear();
                    addComponents();

                    parametersPanel.setVisible(false);
                    parametersPanel.setVisible(true);

                }
            });

            panel.add(removeButton);
            panel.add(component);

            parametersPanel.addParameter(parameter, panel);
        }
    }

    /**
     * This is very similar to ParametersPanel, but is specifically used to contain multiple
     * values of the same type of Parameter.
     * It does not have labels, not the bottom margin that ParametersPanel has.
     */
    public class MultiplePanel extends JPanel implements ParameterHolder
    {
        private static final long serialVersionUID = 1L;

        private Map<String, JLabel> _parameterErrorLabels;

        TableLayoutManager _tlm;

        public MultiplePanel()
        {
            _parameterErrorLabels = new HashMap<String, JLabel>();

            // A table of all of the task's parameters
            _tlm = new TableLayoutManager(this, 1);
            _tlm.getColumn(0).stretchFactor = 1;
            setLayout(_tlm);
            setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        public void clear()
        {
            _parameterErrorLabels.clear();
            this.removeAll();
            _tlm = new TableLayoutManager(this, 2);
        }

        public void addParameter(Parameter parameter)
        {
            Component component = parameter.createComponent(this);
            addParameter(parameter, component);
        }

        public void addParameter(Parameter parameter, Component component)
        {
            addParameter(parameter, this, component);
        }

        public void addParameters(GroupParameter group)
        {
            addParameters(group, this);
        }

        private void addParameters(GroupParameter group, Container container)
        {
            for (Parameter parameter : group.getChildren()) {
                if (!parameter.visible) {
                    continue;
                }

                Component component = parameter.createComponent(this);
                addParameter(parameter, container, component);
            }
        }

        private void addParameter(Parameter parameter, Container container, Component component)
        {
            JLabel parameterErrorLabel = createErrorLabel();
            _parameterErrorLabels.put(parameter.getName(), parameterErrorLabel);
            parameterErrorLabel.setVisible(false);
            parameterErrorLabel.setHorizontalAlignment(SwingConstants.LEFT);

            container.add(component);
            container.add(parameterErrorLabel);
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

}

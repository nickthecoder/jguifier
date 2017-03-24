package uk.co.nickthecoder.jguifier.parameter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.AbstractParameterPanel;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.guiutil.RowLayoutManager;
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

    public void setValues(Iterable<T> values)
    {
        getValue().clear();
        for (T value : values) {
            getValue().add(value);
        }
        fireChangeEvent();
    }

    public void addValue(T value)
    {
        getValue().add(value);
        fireChangeEvent();
    }

    @Override
    public String getStringValue()
    {
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (T value : getValue()) {
            prototypeParameter.setDefaultValue(value);
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
            prototypeParameter.setDefaultValue(value);
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
        return new MultipleParameterComponent(holder);
    }

    class MultipleParameterComponent extends JPanel
    {
        public MultiplePanel parametersPanel;

        private static final long serialVersionUID = 1L;

        public MultipleParameterComponent(ParameterHolder holder)
        {
            Util.assertIsEDT();

            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(getLabel()),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            parametersPanel = new MultiplePanel(holder);
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

            MultipleParameter.this.addListener(new ParameterListener()
            {

                @Override
                public void changed(Parameter source)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            parametersPanel.clear();
                            addComponents();

                            // I had trouble with redrawing, and this fixed it. Bad, I know...
                            parametersPanel.setVisible(false);
                            parametersPanel.setVisible(true);
                            
                        }
                    });
                }

            });
        }

        private void newValue()
        {
            Util.assertIsEDT();

            T value = prototypeParameter.getValue();
            getValue().add(value);
            fireChangeEvent();
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
            parameter.setDefaultValue(value);

            parameter.addListener(new ParameterListener()
            {
                @Override
                public void changed(Parameter source)
                {
                    getValue().set(index, parameter.getValue());
                }
            });

            Component component = parameter.createComponent(parametersPanel);

            parametersPanel.addParameter(parameter, component);
        }
    }

    /**
     * This is very similar to ParametersPanel, but is specifically used to contain multiple
     * values of the same type of Parameter.
     * It does not have labels, not the bottom margin that ParametersPanel has.
     */
    public class MultiplePanel extends AbstractParameterPanel
    {
        private static final long serialVersionUID = 1L;

        TableLayoutManager _tlm;

        public MultiplePanel(ParameterHolder holder)
        {
            super();

            // A table of all of the task's parameters
            _tlm = holder.getTableLayoutManager();
            _tlm.getColumn(1).stretchFactor = 1;
            setLayout(_tlm);
            setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        public TableLayoutManager getTableLayoutManager()
        {
            return _tlm;
        }

        public void clear()
        {
            _parameterErrorLabels.clear();
            this.removeAll();
        }

        @Override
        protected void addParameter(final Parameter parameter, final AbstractParameterPanel container,
            Component component)
        {
            JLabel parameterErrorLabel = createErrorLabel();
            _parameterErrorLabels.put(parameter.getName(), parameterErrorLabel);
            parameterErrorLabel.setVisible(false);
            parameterErrorLabel.setHorizontalAlignment(SwingConstants.LEFT);

            JPanel row = new JPanel();
            RowLayoutManager rlm = new RowLayoutManager(row, _tlm);
            row.setLayout(rlm);

            JButton removeButton = new JButton(" - ");
            removeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    getValue().remove(((ValueParameter<?>) parameter).getValue());
                    fireChangeEvent();
                }
            });

            rlm.add(removeButton);
            rlm.add(component);
            rlm.setStretchy(parameter.isStretchy());

            container.add(row);
            container.add(parameterErrorLabel);
        }
    }
}

package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;
import uk.co.nickthecoder.jguifier.TaskPrompter;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Groups a set of {@link Parameter}s. The {@link TaskPrompter} will show the parameters in a box.
 * For example, you may create a GroupParameter with a label of "Margins", and it's children will
 * be {@link IntegerParameter}s called top, right, bottom, left.
 * 
 * @priority 3
 */
public class GroupParameter
    extends Parameter implements ParameterListener
{
    private List<Parameter> _children = new ArrayList<Parameter>();

    public GroupParameter(String name)
    {
        super(name);
    }

    @Override
    public void check()
        throws ParameterException
    {
    }

    public Iterable<Parameter> children()
    {
        return _children;
    }

    public Iterable<ValueParameter<?>> valueParameters()
    {
        List<ValueParameter<?>> all = new ArrayList<>();
        for (Parameter p : _children) {
            if (p instanceof ValueParameter<?>) {
                all.add((ValueParameter<?>) p);
            }
            if (p instanceof GroupParameter) {
                for (ValueParameter<?> p2 : ((GroupParameter) p).valueParameters()) {
                    all.add(p2);
                }
            }
        }

        return all;
    }
    
    public Iterable<Parameter> parameters()
    {
        List<Parameter> all = new ArrayList<>();
        for (Parameter p : _children) {
            all.add((ValueParameter<?>) p);
            if (p instanceof GroupParameter) {
                for (Parameter p2 : ((GroupParameter) p).parameters()) {
                    all.add(p2);
                }
            }
        }

        return all;
    }

    public void addParameter(int position, Parameter parameter)
    {
        addChildren(position, parameter);
    }

    public void addParameter(Parameter parameter)
    {
        addChildren(_children.size(), parameter);
    }

    public List<Parameter> getChildren()
    {
        return _children;
    }

    public void addChildren(Parameter... parameters)
    {
        addChildren(_children.size(), parameters);
    }

    public void addChildren(int position, Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            _children.add(position, parameter);
            parameter.addListener(this);
            position++;
        }
    }

    public Parameter findParameter(String name)
    {
        for (Parameter parameter : _children) {
            if (parameter.getName().equals(name)) {
                return parameter;
            } else if (parameter instanceof GroupParameter) {
                Parameter result = ((GroupParameter) parameter).findParameter(name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(getLabel()),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return panel;
    }

    public String getCommandString(boolean includeHidden)
    {
        StringBuffer buffer = new StringBuffer();

        for (ValueParameter<?> parameter : valueParameters()) {
            if (parameter instanceof MultipleParameter) {

                MultipleParameter<?, ?> mp = (MultipleParameter<?, ?>) parameter;
                buffer.append(mp.getCommandArguments());

            } else {
                if (includeHidden || parameter.visible) {
                    buffer.append(" --");
                    buffer.append(parameter.getName());
                    buffer.append("=");
                    String text = parameter.getStringValue();
                    if (text != null) {
                        if (text.matches("[a-zA-Z0-9./]*")) {
                            buffer.append(text);
                        } else {
                            buffer.append(Util.quote(text));
                        }
                    }
                }
            }
        }
        return buffer.toString();

    }

    /**
     * Forwards change events from the Group's children to this Group's listeners.
     * This lets clients listen for events from all of a Task's parameters by listening to
     * the root parameter like so : <code><pre>
     * task.getRootParameter().addListener( ... );
     * </pre></code>
     */
    @Override
    public void changed(Parameter source)
    {
        fireChangeEvent(source);
    }

    @Override
    public String toString()
    {
        return "Group : " + super.toString();
    }

    public static final class Builder extends Parameter.Builder<Builder, GroupParameter>
    {
        public Builder(String name)
        {
            making = new GroupParameter(name);
        }

        public Builder child(Parameter parameter)
        {
            making.addParameter(parameter);
            return this;
        }

        public Builder children(Parameter... parameters)
        {
            making.addChildren(parameters);
            return this;
        }

    }

}

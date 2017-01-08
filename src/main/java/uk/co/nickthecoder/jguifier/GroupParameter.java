package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Groups a set of {@link Parameter}s. The {@link TaskPrompter} will show the parameters in a box.
 * For example, you may create a GroupParameter with a label of "Margins", and it's children will
 * be {@link IntegerParameter}s called top, right, bottom, left.
 * 
 * @priority 3
 */
public class GroupParameter
    extends Parameter
{
    private List<Parameter> _children = new ArrayList<Parameter>();

    public GroupParameter(String name, String label)
    {
        super(name, label);
    }

    @Override
    public void check()
        throws ParameterException
    {
    }

    public void addParameter(Parameter parameter)
    {
        addChildren(parameter);
    }

    public List<Parameter> getChildren()
    {
        return _children;
    }

    public void addChildren(Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            _children.add(parameter);
        }
    }

    public GroupParameter child(Parameter parameter)
    {
        addChildren(parameter);
        return this;
    }

    public GroupParameter children(Parameter... parameters)
    {
        addChildren(parameters);
        return this;
    }

    @Override
    public Component createComponent(final ParametersPanel parametersPanel)
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(getLabel()),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return panel;
    }

    @Override
    public String toString()
    {
        return "Group : " + super.toString();
    }

}

package uk.co.nickthecoder.jguifier;

import java.util.ArrayList;
import java.util.List;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class GroupParameter
    extends Parameter
{
    private List<Parameter> _children = new ArrayList<Parameter>();
    
    
    public GroupParameter( String name, String label )
    {
        super( name, label );
    }
    
    public void setStringValue( String value )
    {
    }
    
    public String getStringValue()
    {
        return null;
    }

    public void check()
        throws ParameterException
    {
    }

    public void addParameter( Parameter parameter )
    {
        addChildren( parameter );
    }
    
    public List<Parameter> getChildren()
    {
        return _children;
    }
    
    public void addChildren( Parameter... parameters )
    {
        for ( Parameter parameter : parameters ) {
            _children.add( parameter );
        }
    }
    public GroupParameter child( Parameter parameter )
    {
    	addChildren( parameter );
    	return this;
    }
    public GroupParameter children( Parameter... parameters )
    {
        addChildren( parameters );
        return this;
    }
    
    public Component createComponent( final TaskPrompter taskPrompter )
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder( getLabel() ),
                        BorderFactory.createEmptyBorder(5,5,5,5)));


        return panel;
    }
    
    public String toString()
    {
        return "Group : " + super.toString();
    }

}



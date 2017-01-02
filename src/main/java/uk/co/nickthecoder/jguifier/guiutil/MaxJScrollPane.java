package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import javax.swing.JScrollPane;
import java.awt.Dimension;

public class MaxJScrollPane
    extends JScrollPane
{
	private static final long serialVersionUID = 1;
	
    private int _maxWidth = Integer.MAX_VALUE;
    private int _maxHeight = Integer.MAX_VALUE;
    
    public MaxJScrollPane()
    {
        super();
    }
    public MaxJScrollPane(Component view)
    {
        super(view);
    }

    public MaxJScrollPane(Component view, int vsbPolicy, int hsbPolicy)
    {
        super( view, vsbPolicy, hsbPolicy );
    }
    public MaxJScrollPane(int vsbPolicy, int hsbPolicy)
    {
        super( vsbPolicy, hsbPolicy );
    }

    public void setMaxSize( int width, int height )
    {
        _maxWidth = width;
        _maxHeight = height;
    }
    
    public void setMaxWidth( int width )
    {
        _maxWidth = width;
    }
    
    public void setMaxHeight( int height )
    {
        _maxHeight = height;
    }
    
    public Dimension getPreferredSize()
    {
        Dimension parent = super.getPreferredSize();
        if ( ( parent.getHeight() > _maxHeight ) || ( parent.getWidth() > _maxWidth ) ) {
            Dimension result = new Dimension(
                _maxWidth < parent.getWidth()  ? _maxWidth : (int) parent.getWidth(),
                _maxHeight < parent.getHeight()  ? _maxHeight : (int) parent.getHeight()
            );
            return result;
        }

        return parent;
            
    }
}



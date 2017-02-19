package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollablePanel extends JPanel implements Scrollable
{
    private static final long serialVersionUID = 1L;

    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        Dimension size = getPreferredSize();
        return size;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if ( orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else {
            return visibleRect.width;
        }
    }

    
    private boolean tracksViewportWidth = false;
    private boolean tracksViewportHeight = false;
    
    public void setScrollableTracksViewportWidth( boolean value )
    {
        tracksViewportWidth = value;
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return tracksViewportWidth;
    }


    public void setScrollableTracksViewportHeight( boolean value )
    {
        tracksViewportHeight = value;
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return tracksViewportHeight;
    }
}


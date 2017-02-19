package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

/**
 * @priority 5
 */
public class MaxJScrollPane
    extends JScrollPane
{
    private static final long serialVersionUID = 1;

    private int _maxWidth = Integer.MAX_VALUE;
    private int _maxHeight = Integer.MAX_VALUE;


    public MaxJScrollPane(Component view, int vsbPolicy, int hsbPolicy)
    {
        super(view, vsbPolicy, hsbPolicy);
        Color srcCol = view.getBackground();
        Color col = new Color( srcCol.getRed(), srcCol.getGreen(), srcCol.getBlue());
        getViewport().setBackground(col);
    }

    public void setMaxSize(int width, int height)
    {
        _maxWidth = width;
        _maxHeight = height;
    }

    public void setMaxWidth(int width)
    {
        _maxWidth = width;
    }

    public void setMaxHeight(int height)
    {
        _maxHeight = height;
    }

    @Override
    public Dimension getPreferredSize()
    {
        Dimension parent = super.getPreferredSize();
        if ((parent.getHeight() > _maxHeight) || (parent.getWidth() > _maxWidth)) {
            Dimension result = new Dimension(
                _maxWidth < parent.getWidth() ? _maxWidth : (int) parent.getWidth(),
                _maxHeight < parent.getHeight() ? _maxHeight : (int) parent.getHeight()
                );
            return result;
        }

        return parent;
    }

}

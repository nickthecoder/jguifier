package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * A column used by {@link TableLayoutManager}.
 * 
 * @priority 5
 */
public class Column
{
    List<Component> cells;

    /**
     * How much does this column stretch, when the container is wider than the preferred widths of the children.
     */
    public float stretchFactor = 0;

    public Column()
    {
        cells = new ArrayList<Component>();
    }

    int minimumWidth;

    int preferredWidth;

    /**
     * The minimum width for this column is the maximum of the preferred size of each of its cells.
     */
    public int calculateMinimumWidth()
    {
        minimumWidth = 0;

        for (Component component : cells) {
            int width = component.getMinimumSize().width;
            if (minimumWidth < width) {
                minimumWidth = width;
            }
        }
        return minimumWidth;
    }
    
    /**
     * The minimum width for this column is the maximum of the preferred size of each of its cells.
     */
    public int calculatePreferredWidth()
    {
        preferredWidth = 0;

        for (Component component : cells) {
            int width = component.getPreferredSize().width;
            if (preferredWidth < width) {
                preferredWidth = width;
            }
        }
        return preferredWidth;
    }

}

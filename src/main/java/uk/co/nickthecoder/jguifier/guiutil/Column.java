package uk.co.nickthecoder.jguifier.guiutil;

import java.util.List;
import java.util.ArrayList;

import java.awt.Component;


public class Column
{
    List<Component> cells;
    
    public Column()
    {
        cells = new ArrayList<Component>();
    }

    /**
     * The minimum width for this column is the maximum preferred size of each of its cells.
    */
    public int getMinimumWidth()
    {
        int result = 0;
        
        for ( Component component: cells ) {
            int width = component.getPreferredSize().width;
            if (result < width) {
                result = width;
            }
        }
        return result;
    }
    
}

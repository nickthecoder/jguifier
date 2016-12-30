package uk.co.nickthecoder.jguifier.guiutil;

import java.util.List;
import java.util.ArrayList;

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;


/**
*/

public class TableLayoutManager
    implements LayoutManager
{
    
    private List<Column> _columns;
    
    private int _columnSpacing = 10;

    private int _rowSpacing = 10;
    
    
    public TableLayoutManager( Container container, int columns )
    {
        
        _columns = new ArrayList<Column>( columns );
        for ( int i = 0; i < columns; i ++ ) {
            _columns.add( new Column() );
        }
        
    }
    
    public List<Column> getColumns()
    {
        return _columns;
    }
    
    public int getColumnCount()
    {
        return _columns.size();
    }
    
    public void setRowSpaing( int value )
    {
        _rowSpacing = value;
    }
    
    public void setColumnSpacing( int value ) 
    {
        _columnSpacing = value;
    }
    
    public int getRowSpacing()
    {
        return _rowSpacing;
    }
    
    public int getColumnSpacing()
    {
        return _columnSpacing;
    }
    
    public void addLayoutComponent( String name, Component child )
    {
    }

    /**
      Called by a RowLayoutManager
    */
    public void addRowComponent( Component child, int columnIndex )
    {
        // System.out.println( "Adding cell " + child );
        _columns.get( columnIndex ).cells.add( child );
    }

    public void layoutContainer( Container parent )
    {
        Insets insets = parent.getInsets();

        int x = insets.left;
        int y = insets.top;
        
        for ( Component child : parent.getComponents() ) {
            if ( child.isVisible() ) {
                Dimension size = child.getPreferredSize();
                child.setBounds( x, y, (int) size.getWidth(), (int) size.getHeight() );
                // System.out.println( "Row Layed out : " + x + "," + y + " .. " + (int) size.getWidth() + "," + (int) size.getHeight() );
                y += size.getHeight() + _rowSpacing;
            }
        }

    }

    public Dimension minimumLayoutSize( Container parent )
    {
        Insets insets = parent.getInsets();
        
        int height = insets.top + insets.bottom;
        int width = 0;
        
        boolean atLeastOne = false;
        
        for ( Component child : parent.getComponents() ) {
            if ( child.isVisible() ) {
                atLeastOne = true;
                height += child.getMinimumSize().getHeight() + _rowSpacing;
                int rowWidth = (int) child.getMinimumSize().getWidth();
                // System.out.println( "Row width : " + rowWidth );
                if ( width < rowWidth ) {
                    width = rowWidth;
                }
            }
        }
        if (atLeastOne) {
            height -= _rowSpacing;
        }
        width += insets.left + insets.right;

        // System.out.println( "Table min size " + width + "," + height );
        return new Dimension( width, height );
    }

    public Dimension preferredLayoutSize( Container parent )
    {
        return minimumLayoutSize( parent );
    }

    /**
    Not currently implemented. As a work around, add a Container, and then add/remove children
    to/from that container.
    */
    public void removeLayoutComponent(Component comp)
    {
    }
    
}


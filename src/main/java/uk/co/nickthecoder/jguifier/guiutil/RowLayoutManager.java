package uk.co.nickthecoder.jguifier.guiutil;

import java.util.List;

import java.awt.LayoutManager;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;


/**
    Lays out components horizontally, such that they line up with components
    in OTHER containers.
    You will typically have a set of rows containers, each with its own RowLayoutManager,
    but sharing a single Columns object.
*/

public class RowLayoutManager
    implements LayoutManager
{

    public static final int DEFAULT_COLUMN_SPACING = 30;

    public static final int DEFAULT_LEFT_MARGIN = 10;

    public static final int DEFAULT_RIGHT_MARGIN = 10;

    private Container _container;

    private TableLayoutManager _tableLayoutManager;
    
    private int _nextColumn = 0;


    public RowLayoutManager( Container container, TableLayoutManager tableLayoutManager )
    {
        _container = container;
        _tableLayoutManager = tableLayoutManager;
    }

    public void addLayoutComponent( String name, Component child )
    {
        add( child );
    }
    
    public void add( Component child )
    {
        _container.add( child );
        _tableLayoutManager.addRowComponent( child, _nextColumn );
        _nextColumn ++;
    }

    public void layoutContainer( Container parent )
    {
        
        int x = 0;
        Component[] children = parent.getComponents();
        
        int height = (int) parent.getPreferredSize().getHeight();
        
        int i = 0;
        for ( Column column: _tableLayoutManager.getColumns() ) {
            Component child = children[i];
            
            Dimension size = child.getPreferredSize();
            int y = (height - (int) size.getHeight()) / 2;
            
            child.setBounds( x, y, (int) size.getWidth(), (int) size.getHeight() );
            // System.out.println( "Cell layed out : " + x + ",0 .. " + (int) size.getWidth() + "," + (int) size.getHeight() );
            
            i ++;
            x += _tableLayoutManager.getRowSpacing() + column.getMinimumWidth();
        }
    }

    public Dimension minimumLayoutSize( Container parent )
    {
        List<Column> columns = _tableLayoutManager.getColumns();
        
        int width = _tableLayoutManager.getRowSpacing() * ( columns.size() - 1 );
        
        for ( Column column: _tableLayoutManager.getColumns() ) {
            width += column.getMinimumWidth();
        }
        
        int height = 0;
        for ( Component child : parent.getComponents() ) {
            if ( height < child.getMinimumSize().getHeight() ) {
                height = (int) child.getPreferredSize().getHeight();
            }
        }
        
        // System.out.println( "Row min size " + width + "," + height );
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


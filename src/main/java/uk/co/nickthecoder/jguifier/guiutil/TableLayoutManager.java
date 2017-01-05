package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @priority 5
 */
public class TableLayoutManager
    implements LayoutManager
{

    private List<Column> _columns;

    private int _columnSpacing = 10;

    private int _rowSpacing = 10;

    public TableLayoutManager(Container container, int columns)
    {

        _columns = new ArrayList<Column>(columns);
        for (int i = 0; i < columns; i++) {
            _columns.add(new Column());
        }

    }

    public List<Column> getColumns()
    {
        return _columns;
    }

    public Column getColumn(int n)
    {
        return _columns.get(n);
    }

    public int getColumnCount()
    {
        return _columns.size();
    }

    public void setRowSpaing(int value)
    {
        _rowSpacing = value;
    }

    public void setColumnSpacing(int value)
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

    @Override
    public void addLayoutComponent(String name, Component child)
    {
    }

    /**
     * Called by a RowLayoutManager
     */
    public void addRowComponent(Component child, int columnIndex)
    {
        _columns.get(columnIndex).cells.add(child);
    }

    @Override
    public void layoutContainer(Container parent)
    {
        Insets insets = parent.getInsets();

        int x = insets.left;
        int y = insets.top;
        int width = parent.getWidth() - insets.left - insets.right;

        for (Component child : parent.getComponents()) {
            if (child.isVisible()) {
                Dimension preferredSize = child.getPreferredSize();
                child.setBounds(x, y, width, (int) preferredSize.getHeight());
                y += preferredSize.getHeight() + _rowSpacing;
            }
        }

    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        Insets insets = parent.getInsets();

        int height = insets.top + insets.bottom;
        int width = 0;

        boolean atLeastOne = false;

        for (Component child : parent.getComponents()) {
            if (child.isVisible()) {
                atLeastOne = true;
                height += child.getMinimumSize().getHeight() + _rowSpacing;
                int rowWidth = (int) child.getMinimumSize().getWidth();
                if (width < rowWidth) {
                    width = rowWidth;
                }
            }
        }
        if (atLeastOne) {
            height -= _rowSpacing;
        }
        width += insets.left + insets.right;

        return new Dimension(width, height);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        return minimumLayoutSize(parent);
    }

    /**
     * Not currently implemented. As a work around, add a Container, and then add/remove children
     * to/from that container.
     */
    @Override
    public void removeLayoutComponent(Component comp)
    {
    }

}

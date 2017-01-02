package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.List;

/**
 * Lays out components horizontally, such that they line up with components
 * in OTHER containers.
 * You will typically have a set of rows containers, each with its own RowLayoutManager,
 * but sharing a single Columns object.
 */

public class RowLayoutManager
    implements LayoutManager
{

    public static final int DEFAULT_COLUMN_SPACING = 30;

    public static final int DEFAULT_LEFT_MARGIN = 10;

    public static final int DEFAULT_RIGHT_MARGIN = 10;

    private Container _container;

    private boolean _stretchy = false;

    private TableLayoutManager _tableLayoutManager;

    private int _nextColumn = 0;

    public RowLayoutManager(Container container, TableLayoutManager tableLayoutManager)
    {
        _container = container;
        _tableLayoutManager = tableLayoutManager;
    }

    public void setStretchy(boolean value)
    {
        this._stretchy = value;
    }

    public boolean getStretchy()
    {
        return this._stretchy;
    }

    @Override
    public void addLayoutComponent(String name, Component child)
    {
        add(child);
    }

    public void add(Component child)
    {
        _container.add(child);
        _tableLayoutManager.addRowComponent(child, _nextColumn);
        _nextColumn++;
    }

    @Override
    public void layoutContainer(Container parent)
    {
        Component[] children = parent.getComponents();

        int totalWidths = 0;
        for (Column column : _tableLayoutManager.getColumns()) {
            totalWidths += column.calculateMinimumWidth() + _tableLayoutManager.getColumnSpacing();
        }
        totalWidths -= _tableLayoutManager.getColumnSpacing();
        int slack = parent.getWidth() - totalWidths - parent.getInsets().left - parent.getInsets().right;

        int height = (int) parent.getPreferredSize().getHeight();

        int x = parent.getInsets().left;
        int i = 0;
        for (Column column : _tableLayoutManager.getColumns()) {
            Component child = children[i];

            Dimension preferredSize = child.getPreferredSize();
            int preferredWidth = preferredSize.width;
            int columnWidth = column.calculateMinimumWidth();

            int y = (height - (int) preferredSize.getHeight()) / 2;

            int width = preferredWidth;
            if ((_stretchy) && (column.stretchFactor > 0)) {
                width = columnWidth + (int) (slack * column.stretchFactor);
            }

            child.setBounds(x, y, width, preferredSize.height);

            i++;
            x += _tableLayoutManager.getColumnSpacing() + columnWidth;
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        List<Column> columns = _tableLayoutManager.getColumns();

        int width = _tableLayoutManager.getRowSpacing() * (columns.size() - 1);

        for (Column column : _tableLayoutManager.getColumns()) {
            width += column.calculateMinimumWidth();
        }

        int height = 0;
        for (Component child : parent.getComponents()) {
            if (height < child.getMinimumSize().getHeight()) {
                height = (int) child.getPreferredSize().getHeight();
            }
        }

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

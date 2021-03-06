package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Based on a
 * <a href="http://stackoverflow.com/questions/9288350/adding-vertical-scroll-to-a-jpopupmenu">Stack Overflow Answer</a>
 * 
 * This is a flawed implementation - it has an extra component (the JScrollBar), which screws up all methods that
 * manipulate the menu items by index.
 */
public class ScrollPopupMenu extends JPopupMenu implements ChangeListener
{
    private static final long serialVersionUID = 1L;

    protected int maximumVisibleRows = 30;

    private JScrollBar popupScrollBar;

    public ScrollPopupMenu()
    {
        this(null);
    }

    public ScrollPopupMenu(String label)
    {
        super(label);
        setLayout(new ScrollPopupMenuLayout());

        super.add(createScrollBar());
        addMouseWheelListener(new MouseWheelListener()
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent event)
            {
                JScrollBar scrollBar = getScrollBar();
                int amount = (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
                    ? event.getUnitsToScroll() * scrollBar.getUnitIncrement()
                    : (event.getWheelRotation() < 0 ? -1 : 1) * scrollBar.getBlockIncrement();

                scrollBar.setValue(scrollBar.getValue() + amount);
                event.consume();
            }
        });
    }

    protected final JScrollBar createScrollBar()
    {
        popupScrollBar = new JScrollBar(Adjustable.VERTICAL);
        popupScrollBar.addAdjustmentListener(new AdjustmentListener()
        {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                doLayout();
                repaint();
            }
        });

        popupScrollBar.setVisible(false);
        return popupScrollBar;
    }

    protected JScrollBar getScrollBar()
    {
        return popupScrollBar;
    }

    public int getMaximumVisibleRows()
    {
        return maximumVisibleRows;
    }

    public void setMaximumVisibleRows(int maximumVisibleRows)
    {
        this.maximumVisibleRows = maximumVisibleRows;
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index)
    {
        super.addImpl(comp, constraints, index);

        if (maximumVisibleRows < getComponentCount() - 1) {
            getScrollBar().setVisible(true);
        }
    }

    @Override
    public void remove(int index)
    {
        super.remove(index);

        if (maximumVisibleRows >= getComponentCount() - 1) {
            getScrollBar().setVisible(false);
        }
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        JScrollBar scrollBar = getScrollBar();
        if (scrollBar.isVisible()) {
            int extent = 0;
            int max = 0;
            int i = 0;
            int unit = -1;
            int width = 0;
            for (Component comp : getComponents()) {
                if (comp.isVisible()) {
                    if (!(comp instanceof JScrollBar)) {
                        Dimension preferredSize = comp.getPreferredSize();
                        width = Math.max(width, preferredSize.width);
                        if (unit < 0) {
                            unit = preferredSize.height;
                        }
                        if (i++ < maximumVisibleRows) {
                            extent += preferredSize.height;
                        }
                        max += preferredSize.height;
                    }
                }
            }

            Insets insets = getInsets();
            int widthMargin = insets.left + insets.right;
            int heightMargin = insets.top + insets.bottom;
            scrollBar.setUnitIncrement(unit);
            scrollBar.setBlockIncrement(extent);
            scrollBar.setValues(0, heightMargin + extent, 0, heightMargin + max);

            width += scrollBar.getPreferredSize().width + widthMargin;
            int height = heightMargin + extent;

            setPopupSize(new Dimension(width, height));
        }

        super.show(invoker, x, y);
    }

    protected static class ScrollPopupMenuLayout implements LayoutManager
    {
        @Override
        public void addLayoutComponent(String name, Component comp)
        {
        }

        @Override
        public void removeLayoutComponent(Component comp)
        {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent)
        {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()) {
                    if (comp instanceof JScrollBar) {
                        JScrollBar scrollBar = (JScrollBar) comp;
                        visibleAmount = scrollBar.getVisibleAmount();
                    } else {
                        Dimension pref = comp.getPreferredSize();
                        dim.width = Math.max(dim.width, pref.width);
                        dim.height += pref.height;
                    }
                }
            }

            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount);

            return dim;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent)
        {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()) {
                    if (comp instanceof JScrollBar) {
                        JScrollBar scrollBar = (JScrollBar) comp;
                        visibleAmount = scrollBar.getVisibleAmount();
                    } else {
                        Dimension min = comp.getMinimumSize();
                        dim.width = Math.max(dim.width, min.width);
                        dim.height += min.height;
                    }
                }
            }

            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height + insets.top + insets.bottom, visibleAmount);

            return dim;
        }

        @Override
        public void layoutContainer(Container parent)
        {
            Insets insets = parent.getInsets();

            int width = parent.getWidth() - insets.left - insets.right;
            int height = parent.getHeight() - insets.top - insets.bottom;

            int x = insets.left;
            int y = insets.top;
            int position = 0;

            for (Component comp : parent.getComponents()) {
                if ((comp instanceof JScrollBar) && comp.isVisible()) {
                    JScrollBar scrollBar = (JScrollBar) comp;
                    Dimension dim = scrollBar.getPreferredSize();
                    scrollBar.setBounds(x + width - dim.width, y, dim.width, height);
                    width -= dim.width;
                    position = scrollBar.getValue();
                }
            }

            y -= position;
            for (Component comp : parent.getComponents()) {
                if (!(comp instanceof JScrollBar) && comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();
                    comp.setBounds(x, y, width, pref.height);
                    y += pref.height;
                }
            }
        }
    }

    /**
     * Adjusts the scroll bar to ensure that the component is wholly visible
     * @param comp The component to make visible
     */
    protected void ensureVisible(Component comp)
    {
        Insets insets = getInsets();

        int y1 = comp.getY() - insets.top;
        if (y1 < 0) {
            popupScrollBar.setValue(popupScrollBar.getValue() + y1);
        }

        int y2 = this.getHeight() - comp.getY() - comp.getHeight() - insets.bottom;
        if (y2 < 0) {
            popupScrollBar.setValue(popupScrollBar.getValue() - y2);
        }
    }

    /**
     * (Un)Register a listener when the menu it shown/hidden. The listener is used to ensure the selected menu item
     * is visible (i.e. the scroll bar is adjusted).
     */
    @Override
    public void setVisible(boolean b)
    {
        super.setVisible(b);

        // Despite JPopupMenu having its own change listeners, it doesn't seem to use them (well, not for
        // menu selection at least). Only by looking at the swing source code, did I see it using
        // MenuSelectionManager.defaultManager() to keep track of the selected menu item.
        if (b) {
            MenuSelectionManager.defaultManager().addChangeListener(this);
        } else {
            MenuSelectionManager.defaultManager().removeChangeListener(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        MenuElement[] me = MenuSelectionManager.defaultManager().getSelectedPath();
        if (me.length > 1) {
            Component component = me[1].getComponent();
            ensureVisible(component);
        }
    }

}

package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

/**
 * A JPopupMenu, whose contents can be filtered by typing text.
 */
public class FilteredPopupMenu extends ScrollPopupMenu
{
    private static final long serialVersionUID = 1L;

    private static final String TYPE_TO_FILTER = "<type to filter>";

    private JMenuItem filterComponent;

    private MenuItemFilter filter;

    private String filterText = "";

    public static FilteredPopupMenu createStartWith()
    {
        return create(new MenuItemFilter()
        {
            @Override
            public boolean accept(JMenuItem menuItem, String filterText)
            {
                if (!menuItem.isEnabled()) {
                    return true;
                }
                return menuItem.getText().toLowerCase().startsWith(filterText.toLowerCase());
            }
        });

    }

    public static FilteredPopupMenu createContains()
    {
        return create(new MenuItemFilter()
        {
            @Override
            public boolean accept(JMenuItem menuItem, String filterText)
            {
                return menuItem.getText().toLowerCase().contains(filterText.toLowerCase());
            }
        });
    }

    public static FilteredPopupMenu create(MenuItemFilter filter)
    {
        final FilteredPopupMenu result = new FilteredPopupMenu(filter);

        result.addMenuKeyListener(new MenuKeyListener()
        {

            @Override
            public void menuKeyTyped(MenuKeyEvent e)
            {
                result.editFilter(e);
            }

            @Override
            public void menuKeyPressed(MenuKeyEvent e)
            {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
                    result.delFilter();
                }
            }

            @Override
            public void menuKeyReleased(MenuKeyEvent e)
            {
            }

        });

        return result;
    }

    public FilteredPopupMenu(MenuItemFilter filter)
    {
        this.filter = filter;

        filterComponent = new JMenuItem(TYPE_TO_FILTER);
        filterComponent.setEnabled(false);
        add(filterComponent);
        addSeparator();

    }

    private void editFilter(KeyEvent e)
    {
        char c = e.getKeyChar();

        if (c >= 32) {
            setFilterText(filterText + c);
        }

    }

    private void delFilter()
    {
        if (filterText.length() > 0) {
            setFilterText(filterText.substring(0, filterText.length() - 1));
        }
    }

    public void setFilterText(String string)
    {
        filterText = string == null ? "" : string;

        boolean found = false;
        for (int i = 0; i < getComponentCount(); i++) {
            Component component = getComponent(i);
            if (component == filterComponent) {
                continue;
            }
            if (component instanceof JMenuItem) {
                JMenuItem menuItem = (JMenuItem) component;
                boolean accept = accept(menuItem, filterText);
                found |= accept;
                menuItem.setVisible(accept);
            }
        }
        if (filterText.length() == 0) {
            filterComponent.setText(TYPE_TO_FILTER);
        } else {
            filterComponent.setText(found ? filterText : filterText + " <no matches>");
        }

        this.pack();
        super.show(invoker, x, y);
    }

    private Component invoker;
    private int x;
    private int y;

    /*
     * Remembers the values, and reuses them whenever the filter changes, so that the menu stays by the component that
     * invoked it. Without this, a long menu can end up stranded higher up the screen than the invoking component.
     */
    @Override
    public void show(Component invoker, int x, int y)
    {
        this.invoker = invoker;
        this.x = x;
        this.y = y;
        super.show(invoker, x, y);
    }

    public boolean accept(JMenuItem menuItem, String filterText)
    {
        return filter.accept(menuItem, filterText);
    }

}

package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

/**
 * A JScrcollPopupMenu, whose contents can be filtered by typing text.
 * By default, menu items are selected when their label start with whatever has been typed.
 * Note, this is done case insensitively, by using toLowerCase on both.
 * You can change this behaviour by overriding {@link #accept(String)}.
 */
public class FilteredPopupMenu extends JScrollPopupMenu
{
    private static final long serialVersionUID = 1L;

    private JMenuItem filterComponent;

    public static FilteredPopupMenu create()
    {
        final FilteredPopupMenu result = new FilteredPopupMenu();

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

    public FilteredPopupMenu()
    {
        filterComponent = new JMenuItem("");
        filterComponent.setEnabled(false);
        add(filterComponent);
        addSeparator();

    }

    private void editFilter(KeyEvent e)
    {
        char c = e.getKeyChar();
        String text = filterComponent.getText();

        if (c >= 32) {
            filter(text + c);
        }

    }

    private void delFilter()
    {
        String text = filterComponent.getText();

        if (text.length() > 0) {
            filter(text.substring(0, text.length() - 1));
        }
    }

    private void filter(String string)
    {
        filterComponent.setText(string);

        for (int i = 0; i < getComponentCount(); i++) {
            Component component = getComponent(i);
            if (component instanceof JMenuItem) {
                JMenuItem menuItem = (JMenuItem) component;
                menuItem.setVisible(accept(menuItem, string));
            }
        }
        this.pack();
        super.show(invoker, x, y);
    }

    private Component invoker;
    private int x;
    private int y;

    /*
     * Remembers the values, and reuses them whenever the filter changes, so that the menu stays by the component that
     * invoked it. Wihout this, a long menu can end up stranded higher up the screen than the invoking component.
     */
    public void show(Component invoker, int x, int y)
    {
        this.invoker = invoker;
        this.x = x;
        this.y = y;
        super.show(invoker, x, y);
    }

    public boolean accept(JMenuItem menuItem, String filter)
    {
        return menuItem.getText().toLowerCase().startsWith(filter.toLowerCase());
    }

}

package uk.co.nickthecoder.jguifier.guiutil;

import javax.swing.JMenuItem;

public interface MenuItemFilter
{
    public boolean accept( JMenuItem menuItem, String filterText );
}

package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.FocusManager;

/**
 * Used for panels move focus to the next component whenever they gain focus.
 */
public class FocusNextListener implements FocusListener
{
    @Override
    public void focusGained(FocusEvent e)
    {
        FocusManager.getCurrentManager().focusNextComponent();
    }

    @Override
    public void focusLost(FocusEvent e)
    {
    }
}

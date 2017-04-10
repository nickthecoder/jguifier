package uk.co.nickthecoder.jguifier.parameter;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public interface Boxed
{
    public static void box( JPanel box, Parameter p )
    {
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(p.getLabel()),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }
}

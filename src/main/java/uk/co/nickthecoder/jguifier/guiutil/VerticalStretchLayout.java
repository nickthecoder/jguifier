package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Lays out child components vertically, allows some components to stretch to fit the container's size.
 * Ignores minimum sizes of child components, only uses their preferred size, and may add extra space
 * depending on the amount of stretch specified, and the amount of additional space available.
 *
 * Also ignores borders/insets. If you want these, add an extra JPanel around your container.
 * 
 * @priority 5
 **/

public class VerticalStretchLayout
    implements LayoutManager
{
    private Map<Component, Double> _stretches = new HashMap<Component, Double>();

    private double _totalStretch = 0.0;

    public void setStretch(Component component, double stretch)
    {
        _stretches.put(component, stretch);
        _totalStretch += stretch;
    }

    @Override
    public void layoutContainer(Container parent)
    {
        Insets insets = parent.getInsets();
        int y = insets.top;

        int availableWidth = parent.getWidth() - insets.left - insets.right;

        int additionalHeight = parent.getHeight() - (int) parent.getPreferredSize().getHeight();

        for (Component child : parent.getComponents()) {

            int height = (int) child.getPreferredSize().getHeight();
            if (_stretches.containsKey(child)) {
                height += (_stretches.get(child) / _totalStretch) * additionalHeight;
            }

            child.setBounds(insets.left, y, availableWidth, height);
            y += height;
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        Insets insets = parent.getInsets();

        int height = 0;
        int width = 0;
        for (Component child : parent.getComponents()) {
            Dimension prefSize = child.getPreferredSize();
            Dimension minSize = child.getPreferredSize();
            height += (int) prefSize.getHeight();
            if (minSize.getWidth() > width) {
                width = (int) minSize.getWidth();
            }
        }

        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        Insets insets = parent.getInsets();

        int height = 0;
        int width = 0;
        for (Component child : parent.getComponents()) {
            Dimension size = child.getPreferredSize();
            height += (int) size.getHeight();
            if (size.getWidth() > width) {
                width = (int) size.getWidth();
            }
        }

        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    @Override
    public void addLayoutComponent(String blah, Component component)
    {
    }

    @Override
    public void removeLayoutComponent(Component comp)
    {
        if (_stretches.containsKey(comp)) {
            _totalStretch -= _stretches.get(comp);
            _stretches.remove(comp);
        }
    }

}

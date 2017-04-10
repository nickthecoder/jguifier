package uk.co.nickthecoder.jguifier.parameter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.ParameterListener;

public class ListComponent<T extends ListItem<?>> extends JPanel implements ParameterListener
{
    private static final long serialVersionUID = 1L;

    private ListParameter<T> parameter;

    private JList<T> unusedJList;

    private JList<T> usedJList;

    private ListWrapperListModel<T> unusedModel;

    private ListWrapperListModel<T> usedModel;

    private DataFlavor myFlavor = createFlavor();

    private DataFlavor[] myFlavors = { myFlavor };

    private static DataFlavor createFlavor()
    {
        try {
            return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList");
        } catch (ClassNotFoundException e1) {
            return null;
        }
    }

    public ListComponent(ListParameter<T> parameter, ParameterHolder holder)
    {
        Boxed.box(this, parameter);
        this.setLayout(new BorderLayout());
        this.parameter = parameter;

        JLabel instructions = new JLabel("Drag (or double click) items to the right hand side");

        usedModel = new ListWrapperListModel<>(parameter.getValue());
        unusedModel = new ListWrapperListModel<>(new ArrayList<T>());

        unusedJList = new JList<>(unusedModel);
        usedJList = new JList<>(usedModel);

        unusedJList.setDragEnabled(true);
        unusedJList.setTransferHandler(new ListTransferHandler());
        unusedJList.setDropMode(DropMode.INSERT);

        usedJList.setDragEnabled(true);
        usedJList.setTransferHandler(new ListTransferHandler());
        usedJList.setDropMode(DropMode.INSERT);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(4, 1));

        JButton addButton = new JButton(">");
        addButton.setToolTipText("Add");
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onAdd();
            }
        });
        JButton removeButton = new JButton("<");
        removeButton.setToolTipText("Remove");
        removeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onRemove();
            }
        });

        JButton addAllButton = new JButton(">>");
        addAllButton.setToolTipText("Add All");
        addAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onAddAll();
            }
        });
        JButton removeAllButton = new JButton("<<");
        removeAllButton.setToolTipText("Remove All");
        removeAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onRemoveAll();
            }
        });
        unusedJList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() >= 2) {
                    onAdd();
                }
            }
        });
        usedJList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() >= 2) {
                    onRemove();
                }
            }
        });

        buttons.add(addAllButton);
        buttons.add(addButton);
        buttons.add(removeButton);
        buttons.add(removeAllButton);

        JScrollPane unusedScroll = new JScrollPane(unusedJList);
        JScrollPane usedScroll = new JScrollPane(usedJList);

        unusedScroll.setPreferredSize(new Dimension(100, parameter.height));
        usedScroll.setPreferredSize(new Dimension(100, parameter.height));

        JPanel columns = new JPanel();
        columns.setLayout(new DualListLayout());

        columns.add(unusedScroll);
        columns.add(buttons);
        columns.add(usedScroll);

        this.add(instructions, BorderLayout.NORTH);
        this.add(columns, BorderLayout.CENTER);

        rebuildUnusedList();
    }

    private void onAddAll()
    {
        for (T item : unusedModel) {
            usedModel.add(item);
        }
        unusedModel.clear();
        parameter.fireChangeEvent();
    }

    private void onRemoveAll()
    {
        for (T item : usedModel) {
            unusedModel.add(item);
        }
        usedModel.clear();
        parameter.fireChangeEvent();
    }

    private void onAdd()
    {
        for (T item : unusedJList.getSelectedValuesList()) {
            usedModel.add(item);
            unusedModel.remove(item);
        }
        parameter.fireChangeEvent();
    }

    private void onRemove()
    {
        for (T item : usedJList.getSelectedValuesList()) {
            usedModel.remove(item);
            unusedModel.add(item);
        }
        parameter.fireChangeEvent();
    }

    @Override
    public void changed(Object initiator, Parameter source)
    {
        if (initiator == this) {
            return;
        }

        rebuildUnusedList();
        usedJList.setModel(new ListWrapperListModel<>(parameter.getValue()));
    }

    private void rebuildUnusedList()
    {
        unusedModel.clear();
        for (T item : parameter.getPossibleItems()) {
            if (!parameter.getValue().contains(item)) {
                unusedModel.add(item);
            }
        }
    }

    /**
     * Lays components out in three columns, the middle column is used for the buttons, and
     * is squashed to its preferred width, the other two columns share the remaining width equally.
     */
    static class DualListLayout implements LayoutManager
    {
        @Override
        public void addLayoutComponent(String name, Component comp)
        {
            // Do nothing
        }

        @Override
        public void removeLayoutComponent(Component comp)
        {
            // Do nothing
        }

        @Override
        public Dimension preferredLayoutSize(Container parent)
        {
            Insets insets = parent.getInsets();

            int width = 0;
            int height = 0;
            for (Component c : parent.getComponents()) {
                Dimension d = c.getPreferredSize();
                width += d.width;
                if (height < d.height) {
                    height = d.height;
                }
            }
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent)
        {
            Insets insets = parent.getInsets();

            int width = 0;
            int height = 0;
            for (Component c : parent.getComponents()) {
                Dimension d = c.getMinimumSize();
                width += d.width;
                if (height < d.height) {
                    height = d.height;
                }
            }
            return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
        }

        @Override
        public void layoutContainer(Container parent)
        {
            Insets insets = parent.getInsets();

            Component left = parent.getComponent(0);
            Component middle = parent.getComponent(1);
            Component right = parent.getComponent(2);

            int middleWidth = middle.getPreferredSize().width;

            int availableWidth = parent.getWidth() - insets.left - insets.right - middleWidth;
            int availableHeight = parent.getHeight() - insets.top - insets.bottom;

            int x = insets.left;

            left.setBounds(x, insets.top, availableWidth / 2, availableHeight);
            x += availableWidth / 2;

            middle.setBounds(x, insets.top, middleWidth, availableHeight);
            x += middleWidth;

            right.setBounds(x, insets.top, availableWidth / 2, availableHeight);
        }
    }

    private class ListTransferHandler extends TransferHandler
    {
        private static final long serialVersionUID = 1L;

        public ListTransferHandler()
        {
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors)
        {
            for (DataFlavor df : flavors) {
                // Note. We use == rather than .equals, to ensure we ONLY transfer between OUR two JLists.
                if (df == myFlavor) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c)
        {
            return TransferHandler.MOVE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Transferable createTransferable(JComponent c)
        {
            return new ListTransfer((JList<T>) c);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferSupport info)
        {
            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            JList<T> jlist = (JList<T>) info.getComponent();
            Transferable t = info.getTransferable();

            ListWrapperListModel<T> model = (ListWrapperListModel<T>) jlist.getModel();

            int startIndex = dl.getIndex();
            int index = startIndex;
            if (index < 0) {
                index = 0;
            }

            List<T> data;
            try {
                jlist.clearSelection();

                data = (List<T>) t.getTransferData(myFlavor);
                for (T item : data) {
                    model.add(index, item);
                    index++;
                }
                jlist.addSelectionInterval(startIndex, index - 1);

                // Remove duplicate (when moving from the SAME JList)
                for (int i = model.getSize() - 1; i >= 0; i--) {
                    if ((i < startIndex) || (i >= index)) {
                        T item = model.get(i);
                        if (data.contains(item)) {
                            model.remove(i);
                            // This is a move within the same JList, so prevent exportDone removing the items.
                            info.setDropAction(NONE);
                        }
                    }
                }

            } catch (UnsupportedFlavorException | IOException e) {
                // Neither should occur
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action)
        {
            // When moving items from the SAME JList, importData will set the drop action to none, to 
            // indicate to us, that we should NOT remove the items.
            if (action != NONE) {
                @SuppressWarnings("unchecked")
                JList<T> jlist = (JList<T>) c;

                ListWrapperListModel<T> sourceModel = (ListWrapperListModel<T>) jlist.getModel();

                int[] selected = jlist.getSelectedIndices();
                for (int i = selected.length - 1; i >= 0; i--) {
                    sourceModel.remove(selected[i]);
                }
            }
        }
    }

    private class ListTransfer implements Transferable
    {
        List<T> list;

        public ListTransfer(JList<T> jlist)
        {
            list = jlist.getSelectedValuesList();
        }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return myFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return flavor == myFlavor;
        }

        @Override
        public List<T> getTransferData(DataFlavor flavor)
        {
            return list;
        }

    }
}

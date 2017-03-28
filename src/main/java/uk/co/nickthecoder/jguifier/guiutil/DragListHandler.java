package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

public class DragListHandler<T> extends TransferHandler implements Transferable
{
    private static final long serialVersionUID = 1L;

    private DragListListener<T> dragListener;

    private MouseMotionListener mouseMotionListener;

    private DataFlavor[] flavors;
    
    public DragListHandler(DragListListener<T> drag, DataFlavor[] flavors )
    {
        this.dragListener = drag;
        this.flavors = flavors;

        mouseMotionListener = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (!dragListener.getDragList().isEmpty()) {
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }
        };
    }
    

    public DragListHandler<T> draggable(JComponent component)
    {
        component.setTransferHandler(this);
        component.addMouseMotionListener(mouseMotionListener);

        return this;
    }
    
    public DragListHandler<T> draggable(JTable table)
    {
        table.setTransferHandler(this);        
        table.setDragEnabled(true);

        return this;
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return COPY;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        return this;
    }


    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(DataFlavor.javaFileListFlavor);
    }

    @Override
    public List<T> getTransferData(DataFlavor flavor)
    {
        return dragListener.getDragList();
    }


    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

}

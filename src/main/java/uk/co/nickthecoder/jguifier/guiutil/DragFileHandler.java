package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class DragFileHandler extends TransferHandler implements Transferable
{
    private static final long serialVersionUID = 1L;

    private static DataFlavor[] FLAVORS = new DataFlavor[] { DataFlavor.javaFileListFlavor };

    private DragFileListener dragListener;

    private MouseMotionListener mouseMotionListener;
    
    public DragFileHandler(DragFileListener drag)
    {
        this.dragListener = drag;

        mouseMotionListener = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (!dragListener.getDragFiles().isEmpty()) {
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.COPY);
                }
            }
        };
    }

    public DragFileHandler draggable(JComponent component)
    {
        component.setTransferHandler(this);
        component.addMouseMotionListener(mouseMotionListener);

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
    public DataFlavor[] getTransferDataFlavors()
    {
        return FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(DataFlavor.javaFileListFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
    {
        return dragListener.getDragFiles();
    }

}

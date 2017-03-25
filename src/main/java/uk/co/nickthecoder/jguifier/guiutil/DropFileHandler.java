package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

public class DropFileHandler implements DropTargetListener
{
    private DropFileListener listener;

    private JComponent primaryComponent;

    public DropFileHandler(DropFileListener listener, JComponent primary, JComponent... others)
    {
        this.listener = listener;
        primaryComponent = primary;

        new DropTarget(primary, this);
        for (JComponent component : others) {
            new DropTarget(component, this);
        }

        resetBorder();
    }

    private boolean canDrop(DropTargetDropEvent dtde)
    {
        return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    private boolean canDrop(DropTargetDragEvent dtde)
    {
        return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    private void resetBorder()
    {
        primaryComponent.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde)
    {
        if (canDrop(dtde)) {
            primaryComponent.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde)
    {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }

    @Override
    public void dragExit(DropTargetEvent dte)
    {
        resetBorder();
    }

    @Override
    public void drop(DropTargetDropEvent dtde)
    {
        try {

            if (canDrop(dtde)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

                Transferable tr = dtde.getTransferable();
                @SuppressWarnings("unchecked")
                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);

                listener.droppedFiles(fileList);

                dtde.getDropTargetContext().dropComplete(true);
            }
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
        } finally {
            resetBorder();
        }
    }
}

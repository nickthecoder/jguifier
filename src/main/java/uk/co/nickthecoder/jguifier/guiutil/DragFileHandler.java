package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.datatransfer.DataFlavor;
import java.io.File;

public class DragFileHandler extends DragListHandler<File>
{
    private static final long serialVersionUID = 1L;

    public static final DataFlavor[] FILE_LIST_FLAVORS = new DataFlavor[] { DataFlavor.javaFileListFlavor };

    public DragFileHandler(DragListListener<File> drag)
    {
        super(drag, FILE_LIST_FLAVORS);
    }
}

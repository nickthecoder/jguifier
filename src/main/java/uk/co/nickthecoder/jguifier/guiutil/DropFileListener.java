package uk.co.nickthecoder.jguifier.guiutil;

import java.io.File;
import java.util.List;

public interface DropFileListener
{
    void droppedFiles( List<File> files, int action );
}

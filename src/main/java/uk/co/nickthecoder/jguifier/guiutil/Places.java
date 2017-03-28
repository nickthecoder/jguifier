package uk.co.nickthecoder.jguifier.guiutil;

import java.io.File;

public interface Places
{
    public String getLabel();

    public Iterable<Place> getPlaces();

    public static class Place implements WithFile
    {
        public Place(File file)
        {
            this(file, file.getName());
        }
        
        public Place(File file, String label)
        {
            this.file = file;
            this.label = label;
        }

        @Override
        public File getFile()
        {
            return file;
        }
        
        public File file;

        public String label;
    }
}

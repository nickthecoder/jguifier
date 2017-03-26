package uk.co.nickthecoder.jguifier.guiutil;

import java.io.File;

public interface Places
{
    public String getLabel();

    public Iterable<Place> getPlaces();

    public static class Place
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

        public File file;

        public String label;
    }
}

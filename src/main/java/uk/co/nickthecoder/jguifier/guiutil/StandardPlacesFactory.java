package uk.co.nickthecoder.jguifier.guiutil;

import java.io.File;
import java.util.ArrayList;

import uk.co.nickthecoder.jguifier.util.Util;

public class StandardPlacesFactory implements PlacesFactory
{
    public static StandardPlacesFactory instance = new StandardPlacesFactory();

    public static final ArrayList<Places> places = new ArrayList<Places>();

    static {
        places.add(StandardPlaces.instance);

        try {
            File bookmarks = Util.createFile(Util.getHomeDirectory(), ".config", "gtk-3.0", "bookmarks");
            if (bookmarks.exists()) {
                places.add(new BookmarksPlaces(bookmarks, "Bookmarks"));
            }
        } catch (Exception e) {
        }
    }

    public Iterable<Places> getPlaces()
    {
        return places;
    }

}

package uk.co.nickthecoder.jguifier.guiutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.co.nickthecoder.jguifier.util.Util;

public class BookmarksPlaces implements Places
{
    
    private String label;
    
    private List<Places.Place> places;
    
    public BookmarksPlaces( File file, String label )
    {
        this.label = label;
        this.places = parsePlacesFile(file);
    }
    
    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public Iterable<Place> getPlaces()
    {
        return places;
    }

    public static List<Places.Place> parsePlacesFile(File store)
    {
        File directory = store.getParentFile();
        List<Places.Place> results = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(store)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    results.add(parseBookmarkLine(directory, line));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }

        return results;
    }

    public static Places.Place parseBookmarkLine(File directory, String line) throws MalformedURLException
    {
        File file;
        String name = null;

        if (line.startsWith("file://")) {
            int space = line.indexOf(' ');
            URL url;
            if (space > 0) {
                url = new URL(line.substring(0, space));
                name = line.substring(space + 1);
            } else {
                url = new URL(line);
            }
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                file = new File(url.getPath());
            }
        } else {
            if (line.equals("~")) {
                file = Util.getHomeDirectory();
            } else if (line.startsWith("~" + File.separatorChar)) {
                file = new File(Util.getHomeDirectory(), line.substring(2));
            } else {
                file = new File(line);
            }
        }
        if (name == null) {
            name = file.getName();
        }

        if (!file.isAbsolute()) {
            file = new File(directory, line);
        }

        return new Places.Place(file, name);
    }
}

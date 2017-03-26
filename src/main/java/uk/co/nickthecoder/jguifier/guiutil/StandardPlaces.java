package uk.co.nickthecoder.jguifier.guiutil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StandardPlaces implements Places
{
    public static final StandardPlaces instance = new StandardPlaces();

    public List<Places.Place> places;

    public StandardPlaces()
    {
        places = new ArrayList<>();

        for (File file : File.listRoots()) {
            places.add(new Place(file, file.getPath()));
        }

        try {
            places.add(new Place(new File(System.getProperty("user.home")), "Home"));
        } catch (Exception e) {
        }

        try {
            places.add(new Place(new File(".").getCanonicalFile(), "Current Directory"));
        } catch (Exception e) {
        }
    }

    @Override
    public String getLabel()
    {
        return "Standard Places";
    }

    @Override
    public Iterable<Place> getPlaces()
    {
        return places;
    }

}

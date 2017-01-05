package uk.co.nickthecoder.jguifier.util;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * General utility static methods.
 *
 * @priority 4
 */
public class Util
{

    public static boolean empty(String value)
    {
        if (value == null) {
            return true;
        }
        return value.equals("");
    }

    public static String DEFAULT_LOOK_AND_FEEL = "GTK+"; // "GTK"; // "Nimbus"

    public static void defaultLookAndFeel()
    {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (DEFAULT_LOOK_AND_FEEL.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    public static String removeExtension(File file)
    {
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot < 0) {
            return file.getName();
        }

        return file.getName().substring(0, lastDot);
    }

    /**
     * Abbreviates a string such that it doesn't straddle more than one line, and isn't very long.
     * Useful in toString methods where one attribute may be long, and you want to keep the output concise.
     * 
     * @param str
     *            The string which is to be abbreviated.
     * @return The abbreviated string or str if no abbreviation was needed.
     */
    public static String abbreviate(String str)
    {
        return abbreviate(str, 60);
    }

    public static String firstLine(String str)
    {
        int nl = str.indexOf('\n');
        if (nl < 0) {
            return str;
        }
        return str.substring(0, nl);
    }

    public static String abbreviate(String str, int limit)
    {
        if (str == null) {
            return null;
        }
        if (str.length() > limit) {
            str = str.substring(0, limit) + "...";
        }
        int nl = str.indexOf("\n");
        if (nl >= 0) {
            str = str.substring(0, nl) + "\\n...";
        }

        return str;
    }

}

package uk.co.nickthecoder.jguifier.util;

import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * General utility static methods.
 *
 * @priority 4
 */
public class Util
{
    public static String DEFAULT_LOOK_AND_FEEL = "GTK+"; // "GTK"; // "Nimbus"

    public static boolean equals(Object o1, Object o2)
    {
        if (o1 == o2) {
            return true;
        }

        if (o1 == null) {
            return false;
        }

        return o1.equals(o2);
    }

    public static boolean empty(String value)
    {
        if (value == null) {
            return true;
        }
        return value.equals("");
    }

    public static void assertIsEDT()
    {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("Not in the event dispatch thread");
        }
    }

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

    /**
     * Creates a JButton with an Icon from the named resource, which should be in the class loader jar file as
     * <code>klass</code>.
     * 
     * @param klass
     *            Used to specify which class loader is used to retrieve the image resource. Usually
     *            <code>yourInstance.getClass()</code> will suffice.
     * @param resource
     *            The location of the resource within the jar file.
     * @param fallbackText
     *            If the image couldn't be loaded, then use this text instead.
     * @return
     */
    public static JButton createIconButton(Class<?> klass, String resource, String fallbackText)
    {
        JButton result = new JButton();
        try {
            Image image = ImageIO.read(klass.getResource(resource));
            result.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            result.setText(fallbackText);
        }
        result.setToolTipText(fallbackText);
        return result;
    }


    public static File getHomeDirectory()
    {
        return new File(System.getProperty("user.home"));
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

    /**
     * Given a string, which is quoted with double quotes, returns the contents, without the double quotes.
     * 
     * @param value
     * @return The contents of the quoted string.
     */
    public static String unquote(String value)
    {
        if (value.startsWith("\'") && (value.endsWith("\'"))) {
            return value.substring(1, value.length() - 1).replaceAll("\\\'", "'");
            // Replace all \' slash quote with " quote
        }
        return value;
    }

    public static String undoubleQuote(String value)
    {
        if (value.startsWith("\"") && (value.endsWith("\""))) {
            return value.substring(1, value.length() - 1).replaceAll("\\\"", "\"");
            // Replace all \" slash double-quote  with " double-quote
        }
        return value;
    }

    public static String quote(String value)
    {
        return "'" + value.replaceAll("'", "\\\\\'") + "'";
    }

    public static String doubleQuote(String value)
    {
        return '"' + value.replaceAll("\"", "\\\\\"") + '"';
    }

    public static String csvQuote(String value)
    {
        return '"' + value.replaceAll("\"", "\"\"") + '"';
    }

    public static String uncsvQuote(String value)
    {
        if (value.startsWith("\"")) {
            return value.substring(1, value.length() - 1).replaceAll("\"\"", "\"");
        } else {
            return value;
        }
    }

    /**
     * Simpler version of {{@link #uncamel(String, String, boolean)},
     * where <code>sep = ' '</code> and <code>first = true</code>.
     */
    public static String uncamel(String str)
    {
        return uncamel(str, " ");
    }

    /**
     * Simpler version of {{@link #uncamel(String, String, boolean)}, where <code>first = true</code>.
     */
    public static String uncamel(String str, String sep)
    {
        return uncamel(str, sep, true);
    }

    /**
     * Takes a camel cased string, and converts it into a space separated version.
     * 
     * @param str
     *            The string to convert
     * @param sep
     *            The separation character(s) between the words
     * @param first
     *            Should the first letter be capitalised
     * @return The converted string
     */
    public static String uncamel(String str, String sep, boolean first)
    {
        StringBuffer result = new StringBuffer();
        boolean wasUpper = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (first) {
                result.append(Character.toUpperCase(c));
                first = false;
            } else {
                if (Character.isUpperCase(c) && !wasUpper) {
                    result.append(sep);
                }
                result.append(c);
            }
            wasUpper = Character.isUpperCase(c);
        }
        return result.toString();
    }

    /**
     * Creates a new File in a similar way as {@link File#File(File, String)}, but with an list of child paths.
     * 
     * @param parent
     *            The base directory
     * @param portions
     *            A list of sub-directories to navigate, ending with a directory or file.
     * @return The new file
     */
    public static File createFile(File parent, String... portions)
    {
        File result = parent;
        for (String portion : portions) {
            result = new File(result, portion);
        }
        return result;
    }
}

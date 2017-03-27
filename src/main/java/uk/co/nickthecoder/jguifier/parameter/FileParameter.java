package uk.co.nickthecoder.jguifier.parameter;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

import javax.swing.JTextField;

import uk.co.nickthecoder.jguifier.ParameterException;
import uk.co.nickthecoder.jguifier.ParameterHolder;
import uk.co.nickthecoder.jguifier.TaskPrompter;
import uk.co.nickthecoder.jguifier.ValueParameter;
import uk.co.nickthecoder.jguifier.guiutil.FileComponent;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A {@link Parameter} for a file or directory.
 * Use the {@link Builder} class like so :
 * 
 * <pre>
 * <code>
 * new FileParameter.Builder("theDirectory").mustExist().directory().writable().parameter();
 * </code>
 * </pre>
 * 
 * The {@link TaskPrompter} will show as a text field, with a "..." button on the right, which will launch an
 * "Open File" dialog box.
 * <p>
 * By default, a FileParameter expects a regular file. See {@link Builder#directory()}.
 * </p>
 * <p>
 * By default the file can, but does not have to exist. See {@link Builder#mustExist()}, {@link Builder#mustNotExist()}.
 * </p>
 * <p>
 * By default, if the file exists, it does not have to be writable. If you plan to write to the file, then use
 * {@link Builder#writable()}.
 * </p>
 * 
 * @see Builder
 */
public class FileParameter extends TextParameter<File>
{
    private TriState _exists = TriState.MAYBE;

    private TriState _isDirectory = TriState.FALSE;

    /**
     * If false (which is the default), then hidden files and directories will not be listed in the GUI.
     */
    private boolean _includeHidden = false;

    /**
     * If false (which is the default), then hidden sub-directories will not be shown in the GUI.
     */
    private boolean _enterHidden = false;

    private boolean _writable = false;

    private String _filterDescription = null;

    private String[] _filterExtensions = null;

    /**
     * @see ValueParameter#ValueParameter(String)
     */
    public FileParameter(String name)
    {
        super(name);
        _stretchy = true;
    }

    public void setStringValue(String value)
    {
        if (value == null) {
            setValue(null);
        } else {
            setValue(new File(value));
        }
    }

    public void setExists(boolean value)
    {
        setExists(value ? TriState.TRUE : TriState.FALSE);
    }

    public void setExists(TriState value)
    {
        _exists = value;
    }

    public TriState getExists()
    {
        return _exists;
    }

    public void setIsDirectory(boolean value)
    {
        setIsDirectory(value ? TriState.TRUE : TriState.FALSE);
    }

    public void setIsDirectory(TriState value)
    {
        _isDirectory = value;
    }

    public TriState getIsDirectory()
    {
        return _isDirectory;
    }

    /**
     * Hidden files are included in the list of results, only if value is true.
     * The default is to exclude hidden files.
     * 
     * @param value
     */
    public void setIncludeHidden(boolean value)
    {
        _includeHidden = value;
    }

    public boolean getIncludeHidden()
    {
        return _includeHidden;
    }

    /**
     * 
     * @param value
     */
    public void setEnterHidden(boolean value)
    {
        _enterHidden = value;
    }

    public boolean getEnterHidden()
    {
        return _enterHidden;
    }

    public void setWritable(boolean value)
    {
        _writable = value;
    }

    public boolean getWritable()
    {
        return _writable;
    }

    public void setExtensions(String description, String... extensions)
    {
        _filterDescription = description;
        _filterExtensions = extensions;
    }

    public String getExtensionsDescription()
    {
        return _filterDescription;
    }

    public String[] getExtensions()
    {
        return _filterExtensions;
    }

    @Override
    public String getDescription()
    {
        StringBuffer result = new StringBuffer();

        String sup = super.getDescription();
        if (sup != null) {
            result.append(sup);
            result.append(". ");
        }

        if (_writable) {
            result.append("writable ");
        }
        if (_isDirectory == TriState.TRUE) {
            result.append("directory ");
        } else if (_isDirectory == TriState.FALSE) {
            result.append("file ");
        } else {
            result.append("file or directory ");
        }

        if (_exists == TriState.TRUE) {
            result.append("must exist ");
        } else if (_exists == TriState.FALSE) {
            result.append("must not exist ");
        } else {
            result.append("may exist ");
        }

        if (_filterExtensions != null) {
            result.append("extensions : " + Arrays.asList(_filterExtensions) + " ");
        }

        return result.toString();
    }

    @Override
    public String valid(File value)
        throws ParameterException
    {
        if (value == null) {
            return super.valid(value);
        }

        // Exists ?
        boolean exists = value.exists();
        if ((_exists == TriState.TRUE) && !exists) {
            return "Does not exist";
        }
        if ((_exists == TriState.FALSE) && exists) {
            return "Already exists";
        }

        if ((!_includeHidden) && value.isHidden()) {
            return "Hidden files not allowed";
        }

        if (exists) {

            // Directory/File?
            boolean isDir = value.isDirectory();

            if ((_isDirectory == TriState.TRUE) && !isDir) {
                return "Not a directory";
            }
            if (_isDirectory == TriState.FALSE && isDir) {
                return "Expected a file, not a directory";
            }

            // Writable ?
            if (_writable) {
                if (value.isDirectory()) {
                    if (!value.canWrite()) {
                        return "You cannot write to this directory";
                    }
                } else {
                    if (!value.canWrite()) {
                        return "You cannot write to this file";
                    }
                    File parent = null;
                    try {
                        parent = value.getAbsoluteFile().getParentFile();
                    } catch (Exception e) {
                        // Do nothing
                    }
                    if ((parent != null) && (!parent.canWrite())) {
                        return "You cannot write to the directory";
                    }
                }
            }
        }

        // Correct file extension?
        if (!matchesExtensions(value)) {
            return "Wrong file extension (" + _filterExtensions + ")";
        }

        return null;
    }

    @Override
    public void autocomplete(String cur)
    {
        File file = new File(cur);
        File directory;

        String prefix = file.getName();

        if (file.isDirectory()) {
            prefix = "";
            directory = file;
        } else {
            directory = file.getParentFile();
            if (directory == null) {
                directory = new File(".");
            }
        }
        File[] children = directory.listFiles();

        for (File child : children) {
            if (child.getName().startsWith(prefix)) {

                if (autocompleteMatches(child)) {

                    if (child.getPath().startsWith("./")) {
                        System.out.println(child.getPath().substring(2));
                    } else {
                        System.out.println(child.getPath());
                    }
                }
            }
        }
    }

    public boolean matchesExtensions(File file)
    {
        if (_filterExtensions == null) {
            return true;
        }

        for (String ext : _filterExtensions) {
            if (file.getName().endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    public boolean autocompleteMatches(File file)
    {
        if (_filterExtensions == null) {
            return true;
        }
        if (file.isDirectory()) {
            return true;
        }

        if (matchesExtensions(file)) {
            return true;
        }

        return false;
    }

    @Override
    public Component createComponent(final ParameterHolder holder)
    {
        FileComponent fileField = new FileComponent(this, Util.getPathWithTrailingSlash(getValue()));
        JTextField textField = fileField.getTextField();

        textField(textField, holder);

        return fileField;
    }

    public static final class Builder extends ValueParameter.Builder<Builder, FileParameter, File>
    {
        public Builder(String name)
        {
            making = new FileParameter(name);
        }

        public Builder mustExist()
        {
            making.setExists(TriState.TRUE);
            return this;
        }

        public Builder mustNotExist()
        {
            making.setExists(TriState.FALSE);
            return this;
        }

        public Builder mayExist()
        {
            making.setExists(TriState.MAYBE);
            return this;
        }

        public Builder fileOrDirectory()
        {
            making.setIsDirectory(TriState.MAYBE);
            return this;
        }

        public Builder directory()
        {
            making.setIsDirectory(TriState.TRUE);
            return this;
        }

        public Builder file()
        {
            making.setIsDirectory(TriState.FALSE);
            return this;
        }

        public Builder includeHidden()
        {
            making.setIncludeHidden(true);
            return this;
        }

        public Builder enterHidden()
        {
            making.setEnterHidden(true);
            return this;
        }

        public Builder writable()
        {
            return writable(true);
        }

        public Builder writable(boolean value)
        {
            making.setWritable(value);
            return this;
        }

        public Builder extensions(String description, String... extensions)
        {
            making.setExtensions(description, extensions);
            return this;
        }
    }

}

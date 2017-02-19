package uk.co.nickthecoder.jguifier;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;

import javax.swing.JTextField;

/**
 * A {@link Parameter} for a filename or directory.
 * 
 * The {@link TaskPrompter} will show as a text field, with a "..." button on the right, which will launch an
 * "Open File" dialog box.
 *
 * By default, a FileParameter expects a regular file, change this using {@link #directory()}.
 * 
 * By default the file can, but does not have to exist; change this using {@link #exists(boolean)}.
 * 
 * By default, if the file exists, it does not have to be writable. If you plan to write to the file, then use
 * {@link #writable(boolean)}.
 * 
 */
public class FileParameter
    extends TextParameter<File>
{
    private TriState _exists;

    private TriState _isDirectory;

    /**
     * If false (which is the default), then hidden files and directories will not be listed in the GUI.
     */
    private boolean _includeHidden = false;

    /**
     * If false (which is the default), then hidden sub-directories will not be shown in the GUI.
     */
    private boolean _enterHidden = false;

    private boolean _writable;

    private String _filterDescription;

    private String[] _filterExtensions;

    public FileParameter(String name)
    {
        this( name, null);
    }
    
    public FileParameter(String name, File value)
    {
        super(name, value);

        _exists = TriState.MAYBE;
        _isDirectory = TriState.MAYBE;
        _writable = false;
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

    public FileParameter exists(boolean value)
    {
        setExists(value);
        return this;
    }

    public FileParameter exists(TriState value)
    {
        setExists(value);
        return this;
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

    public FileParameter isDirectory(boolean value)
    {
        setIsDirectory(value);
        return this;
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
    
    public FileParameter directory()
    {
        setIsDirectory(true);
        return this;
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

    public FileParameter includeHidden()
    {
        setIncludeHidden(true);
        return this;
    }

    /**
     * 
     * @return this
     */
    public FileParameter enterHidden()
    {
        setEnterHidden(true);
        return this;
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

    public FileParameter writable(boolean value)
    {
        setWritable(value);
        return this;
    }

    public void setWritable(boolean value)
    {
        _writable = value;
    }

    public boolean getWritable()
    {
        return _writable;
    }

    public FileParameter extension(String description, String extension)
    {
        extensions(description, new String[] { extension });
        return this;
    }

    public FileParameter extensions(String description, String... extensions)
    {
        setExtensions(description, extensions);
        return this;
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
    public void check()
        throws ParameterException
    {
        super.check();

        try {

            File file = getValue();
            if (!file.isAbsolute()) {
                file = file.getAbsoluteFile();
            }

            if (_exists != TriState.MAYBE) {
                if (_exists == TriState.TRUE) {
                    if (!file.exists()) {
                        throw new ParameterException(this, "File does not exist");
                    }

                    if (!file.canRead()) {
                        throw new ParameterException(this, "File is not readable");
                    }

                } else {
                    if (file.exists()) {
                        throw new ParameterException(this, "File exists");
                    }
                }
            }

            if (_isDirectory != TriState.MAYBE) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        if (_isDirectory == TriState.FALSE) {
                            throw new ParameterException(this, "Is a directory");
                        }
                    } else {
                        if (_isDirectory == TriState.TRUE) {
                            throw new ParameterException(this, "Is not a directory");
                        }
                    }
                }
            }

            if (_writable) {
                File parent = file.getParentFile();
                if (!parent.canWrite()) {
                    throw new ParameterException(this, "Directory is not writable");
                }
                if (file.exists() && !file.canWrite()) {
                    throw new ParameterException(this, "File is not writable");
                }
            }

        } catch (ParameterException e) {
            throw e;
        } catch (Exception e) {
            throw new ParameterException(this, e.getMessage());
        }
    }

    @Override
    public String getDescription()
    {
        return super.getDescription() + "\n" +
            "    Must Exist  : " + _exists + "\n" +
            "    Must be a directory : " + _isDirectory + "\n" +
            "    Must be a writable file : " + _writable + "\n" +
            (_filterExtensions == null ? "" :
                "    File Extensions : " + _filterDescription + " " + Arrays.asList(_filterExtensions) + "\n"
            );

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

    public boolean autocompleteMatches(File file)
    {
        if (_filterExtensions == null) {
            return true;
        }
        if (file.isDirectory()) {
            return true;
        }

        for (String ext : _filterExtensions) {
            if (file.getName().endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Component createComponent(final ParametersPanel panel)
    {
        FileComponent fileField = new FileComponent(this, getValue() == null ? "" : getValue().getPath());
        JTextField textField = fileField.getTextField();
        
        textField(textField, panel);

        return fileField;
    }
    
    /*
    try {
        _fileParameter.setStringValue(_textField.getText());
        parametersPanel.clearError(this);
    } catch (Exception e) {
        parametersPanel.setError(this, e.getMessage());
    }
    */

    
}

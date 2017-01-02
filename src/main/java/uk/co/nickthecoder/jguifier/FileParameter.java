package uk.co.nickthecoder.jguifier;

import java.io.File;
import java.util.Arrays;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.nickthecoder.jguifier.util.Util;

public class FileParameter
    extends StringParameter
{
    public enum TrueFalseMaybe
    {
        TRUE, FALSE, MAYBE
    }

    private TrueFalseMaybe _exists;

    private TrueFalseMaybe _isDirectory;

    private boolean _writable;

    private String _filterDescription;

    private String[] _filterExtensions;

    public FileParameter(String name, String label)
    {
        this(name, label, null);
    }

    public FileParameter(String name, String label, String defaultValue)
    {
        super(name, label, defaultValue);

        _exists = TrueFalseMaybe.MAYBE;
        _isDirectory = TrueFalseMaybe.MAYBE;
        _writable = false;
        _stretchy = true;
    }

    public FileParameter exists(boolean value)
    {
        setExists(value);
        return this;
    }

    public FileParameter exists(TrueFalseMaybe value)
    {
        setExists(value);
        return this;
    }

    public void setExists(boolean value)
    {
        setExists(value ? TrueFalseMaybe.TRUE : TrueFalseMaybe.FALSE);
    }

    public void setExists(TrueFalseMaybe value)
    {
        _exists = value;
    }

    public TrueFalseMaybe getExists()
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
        setIsDirectory(value ? TrueFalseMaybe.TRUE : TrueFalseMaybe.FALSE);
    }

    public void setIsDirectory(TrueFalseMaybe value)
    {
        _isDirectory = value;
    }

    public TrueFalseMaybe getIsDirectory()
    {
        return _isDirectory;
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

    public void check()
        throws ParameterException
    {
        super.check();

        if (Util.empty(getValue())) {
            return;
        }

        try {

            File file = new File(getValue());
            if (!file.isAbsolute()) {
                file = file.getAbsoluteFile();
            }

            if (_exists != TrueFalseMaybe.MAYBE) {
                if (_exists == TrueFalseMaybe.TRUE) {
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

            if (_isDirectory != TrueFalseMaybe.MAYBE) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        if (_isDirectory == TrueFalseMaybe.FALSE) {
                            throw new ParameterException(this, "Is a directory");
                        }
                    } else {
                        if (_isDirectory == TrueFalseMaybe.TRUE) {
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

    public Component createComponent(final TaskPrompter taskPrompter)
    {
        final JTextField textField = (JTextField) super.createComponent(taskPrompter);

        JButton pickButton = new JButton("...");
        pickButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onFileChooser(taskPrompter, textField);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(pickButton, BorderLayout.EAST);

        return panel;
    }

    private void onFileChooser(final TaskPrompter taskPrompter, JTextField textField)
    {
        JFileChooser fileChooser = new JFileChooser(textField.getText());

        int fsm = JFileChooser.FILES_AND_DIRECTORIES;
        String title = "Select a file or directory";

        if (_isDirectory == TrueFalseMaybe.TRUE) {
            fsm = JFileChooser.DIRECTORIES_ONLY;
            title = "Select a directory";
        } else if (_isDirectory == TrueFalseMaybe.FALSE) {
            fsm = JFileChooser.FILES_ONLY;
            title = "Select a file";
        }

        fileChooser.setFileSelectionMode(fsm);
        fileChooser.setDialogTitle(title);
        fileChooser.setApproveButtonText("Select");
        if (_filterExtensions != null) {
            fileChooser.setFileFilter(new FileNameExtensionFilter(_filterDescription, _filterExtensions));
        }

        int result = fileChooser.showOpenDialog(taskPrompter);
        if (result == JFileChooser.APPROVE_OPTION) {

            textField.setText(fileChooser.getSelectedFile().getPath());
            try {
                setStringValue(textField.getText());
                taskPrompter.clearError(this);
            } catch (Exception e) {
                taskPrompter.setError(this, e.getMessage());
            }
        }
    }

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
}

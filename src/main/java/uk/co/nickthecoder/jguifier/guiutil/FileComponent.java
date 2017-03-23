package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.nickthecoder.jguifier.parameter.FileParameter;
import uk.co.nickthecoder.jguifier.parameter.TriState;
import uk.co.nickthecoder.jguifier.util.FileLister;

/**
 * A Swing component for {@link FileParameter}s. Contains a TextField, where the files path can be typed, as well as
 * two buttons, "Open" and "Down". The "Open" button displays the usual JFileChooser, however, I find that dialog box a
 * little clunky to use, and I prefer to use the "Down" button. This behaves similarly to Linux's command line tab
 * completion.
 * <p>
 * Type the first part of a file/directory name, then click the "Down" button (or the down key), and a list of possible
 * matches is display in a pop-up menu. Select the one you want with the mouse or keyboard (using up/down, then enter).
 * </p>
 * <p>
 * To further ease selecting files, the keyboard "Up" key, takes you up a directory, so getting to root, is very easy,
 * you just press up lots of times!
 * </p>
 * 
 * @priority 4
 */
public class FileComponent extends JPanel
{
    private static final long serialVersionUID = 1L;

    private FileParameter _fileParameter;

    private JTextField _textField;

    private JButton _completeButton;

    private JPopupMenu _popupMenu;

    public FileFilter fileFilter;

    public FileComponent(FileParameter fileParameter, String text)
    {
        _fileParameter = fileParameter;
        _textField = new JTextField(text);
        
        _completeButton = new JButton("\u2193");
        _completeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createPopupMenu();
            }
        });

        JButton pickButton = new JButton(" … ");
        pickButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onFileChooser();
            }
        });

        _textField.getInputMap().put(KeyStroke.getKeyStroke("UP"), "upDirectory");
        _textField.getActionMap().put("upDirectory", new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (_fileParameter.getValue() != null) {
                    File parent = _fileParameter.getValue().getParentFile();
                    if (parent != null) {
                        _textField.setText(parent.getPath());
                    }
                }
            }
        });

        _textField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "directoryPopup");
        _textField.getActionMap().put("directoryPopup", new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                createPopupMenu();
            }
        });

        this.setLayout(new BorderLayout());
        this.add(_textField, BorderLayout.CENTER);
        JPanel buttons = new JPanel();

        buttons.setLayout(new BorderLayout());
        buttons.add(_completeButton, BorderLayout.WEST);
        buttons.add(pickButton, BorderLayout.EAST);

        this.add(buttons, BorderLayout.EAST);

        int buttonHeight = _textField.getPreferredSize().height;
        Dimension preferredSize = new Dimension(buttonHeight, buttonHeight); // Make it square
        _completeButton.setPreferredSize(preferredSize);
        pickButton.setPreferredSize(preferredSize);
    }

    public JTextField getTextField()
    {
        return _textField;
    }

    private void createPopupMenu()
    {
        _popupMenu = FilteredPopupMenu.createStartWith();

        File value = _fileParameter.getValue();
        if (value == null) {
            value = new File(".");
            try {
                value = value.getCanonicalFile();
            } catch (IOException e) {
            }
        }

        File parent = value.getParentFile();

        if (value.isDirectory()) {
            // Add sub-directories and matching files
            addToComboBox(value, "");
        }

        // Add files and directories that match the text currently entered
        if (parent != null) {
            addToComboBox(parent, value.getName());
        }

        if (_popupMenu.getSubElements().length > 0) {
            _popupMenu.show(_completeButton, 0, 0);
        }
    }

    private void addPopupItem(final String label, final File file)
    {
        JMenuItem menuItem = new JMenuItem(label);
        _popupMenu.add(menuItem);

        if (file.isDirectory()) {
            Font font = menuItem.getFont();
            menuItem.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
        }

        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                _textField.setText(file.getPath());
            }

        });
    }

    private void addToComboBox(File directory, String prefix)
    {
        List<File> children;

        FileLister fileLister = new FileLister().directoriesFirst().includeDirectories();
        // Never exclude hidden files if we have a prefix (as on *nix, this tells us if it is hidden or not).
        if (prefix.equals("")) {
            fileLister.setIncludeHidden( _fileParameter.getIncludeHidden() );
        } else {
            fileLister.setIncludeHidden( true );
        }
        
        children = fileLister.listFiles(directory);

        for (File child : children) {
            if (child.getName().equals(prefix)) {
                continue;
            }

            if (child.getName().startsWith(prefix)) {

                if (child.isDirectory()) {
                    if (child.isHidden() && (!_fileParameter.getEnterHidden())) {
                        // Skip over hidden directories
                        continue;
                    }
                }
                if (_fileParameter.autocompleteMatches(child)) {

                    addPopupItem(child.getName(), child);
                }
            }
        }
    }

    private void onFileChooser()
    {
        JFileChooser fileChooser = new JFileChooser(_textField.getText());

        int fsm = JFileChooser.FILES_AND_DIRECTORIES;
        String title = "Select a file or directory";

        if (_fileParameter.getIsDirectory() == TriState.TRUE) {
            fsm = JFileChooser.DIRECTORIES_ONLY;
            title = "Select a directory";
        } else if (_fileParameter.getIsDirectory() == TriState.FALSE) {
            fsm = JFileChooser.FILES_ONLY;
            title = "Select a file";
        }

        fileChooser.setFileSelectionMode(fsm);
        fileChooser.setDialogTitle(title);
        fileChooser.setApproveButtonText("Select");
        if (_fileParameter.getExtensions() != null) {
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                _fileParameter.getExtensionsDescription(),
                _fileParameter.getExtensions()));
        }

        int result = fileChooser.showOpenDialog(this.getParent());
        if (result == JFileChooser.APPROVE_OPTION) {

            _textField.setText(fileChooser.getSelectedFile().getPath());
        }
    }

}

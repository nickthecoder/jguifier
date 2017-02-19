package uk.co.nickthecoder.jguifier;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.nickthecoder.jguifier.guiutil.JScrollPopupMenu;
import uk.co.nickthecoder.jguifier.util.FileLister;

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

        _textField.addKeyListener(new KeyListener()
        {

            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    createPopupMenu();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {

                    if (_fileParameter.getValue() != null) {
                        File parent = _fileParameter.getValue().getParentFile();
                        if (parent != null) {
                            _textField.setText(parent.getPath());
                        }
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }
        });

        this.setLayout(new BorderLayout());
        this.add(_textField, BorderLayout.CENTER);
        JPanel buttons = new JPanel();

        buttons.setLayout(new BorderLayout());
        buttons.add(_completeButton, BorderLayout.WEST);
        buttons.add(pickButton, BorderLayout.EAST);

        this.add(buttons, BorderLayout.EAST);

    }

    public JTextField getTextField()
    {
        return _textField;
    }

    private void createPopupMenu()
    {
        _popupMenu = new JScrollPopupMenu();

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
            public void actionPerformed(ActionEvent e)
            {
                _textField.setText(file.getPath());
            }

        });
    }

    private void addToComboBox(File directory, String prefix)
    {
        List<File> children;
        try {
            FileLister fileLister = new FileLister().directoriesFirst().includeDirectories();
            if (_fileParameter.getIsDirectory() == TriState.TRUE) {
                fileLister.excludeFiles();
            }
            children = fileLister.listFiles(directory);
        } catch (IOException e) {
            return;
        }

        for (File child : children) {
            if (child.getName().equals(prefix)) {
                continue;
            }

            if (child.getName().startsWith(prefix)) {

                if (child.isHidden()) {
                    if (child.isDirectory()) {
                        if ((!_fileParameter.getIncludeHidden()) && (!_fileParameter.getEnterHidden())) {
                            // Skip over hidden directories
                            continue;
                        }
                    } else {
                        if (!_fileParameter.getIncludeHidden()) {
                            // Skip over hidden files.
                            continue;
                        }
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

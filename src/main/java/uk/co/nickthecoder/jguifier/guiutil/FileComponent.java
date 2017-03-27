package uk.co.nickthecoder.jguifier.guiutil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import uk.co.nickthecoder.jguifier.Task;
import uk.co.nickthecoder.jguifier.parameter.FileParameter;
import uk.co.nickthecoder.jguifier.parameter.TriState;
import uk.co.nickthecoder.jguifier.util.FileLister;
import uk.co.nickthecoder.jguifier.util.Util;

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
public class FileComponent extends JPanel implements DropFileListener, DragFileListener
{
    private static final long serialVersionUID = 1L;

    public static PlacesFactory placesFactory = StandardPlacesFactory.instance;

    private FileParameter _fileParameter;

    private JTextField _textField;

    private JPopupMenu _popupMenu;

    public FileFilter fileFilter;

    private JLabel _iconLabel;

    private Font folderFont;

    private Font otherFont;

    public FileComponent(FileParameter fileParameter, String text)
    {
        _fileParameter = fileParameter;
        _textField = new JTextField(text);
        _textField.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.isPopupTrigger()) {
                    _textField.requestFocusInWindow();
                    createPopupMenu(e.getX(), e.getY(), false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger()) {
                    _textField.requestFocusInWindow();
                    createPopupMenu(e.getX(), e.getY(), false);
                }
            }

        });

        try {
            Image image = ImageIO.read(Task.class.getResource("file.png"));
            ImageIcon icon = new ImageIcon(image);
            _iconLabel = new JLabel(icon);
        } catch (Exception e) {
            e.printStackTrace();
            _iconLabel = new JLabel("X");
        }
        _iconLabel.setToolTipText("You can drag this!");

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
                        _textField.setText(Util.getPathWithTrailingSlash(parent));
                    }
                }
            }
        });

        _textField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "directoryAutocomplete");
        _textField.getInputMap().put(KeyStroke.getKeyStroke("ctrl SPACE"), "directoryAutocomplete");
        _textField.getActionMap().put("directoryAutocomplete", new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                createPopupMenu(true);
            }
        });

        _textField.getInputMap().put(KeyStroke.getKeyStroke("CONTEXT_MENU"), "directoryPopup");
        _textField.getActionMap().put("directoryPopup", new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                createPopupMenu(false);
            }
        });

        this.setLayout(new BorderLayout());
        this.add(_textField, BorderLayout.CENTER);
        this.add(_iconLabel, BorderLayout.EAST);

        new DropFileHandler(this, this, _textField);
        new DragFileHandler(this).draggable(_iconLabel);
    }

    public JTextField getTextField()
    {
        return _textField;
    }

    private List<JMenu> createPlacesSubMenus()
    {
        List<JMenu> result = new ArrayList<>();

        for (Places places : placesFactory.getPlaces()) {
            JMenu subMenu = new JMenu(places.getLabel());
            for (Places.Place place : places.getPlaces()) {
                subMenu.add(createPopupItem(place.label, place.file));
                subMenu.setFont(otherFont);

            }
            result.add(subMenu);
        }
        return result;
    }

    private void createPopupMenu(boolean autoComplete)
    {
        createPopupMenu(_textField.getWidth(), 0, autoComplete);
    }

    private int _matchCount;

    private void createPopupMenu(int x, int y, boolean autoComplete)
    {
        _matchCount = 0;
        _popupMenu = FilteredPopupMenu.createStartWith();

        Font font = _popupMenu.getFont();
        folderFont = font.deriveFont(font.getStyle() | Font.BOLD);
        otherFont = font.deriveFont(font.getStyle() | Font.BOLD | Font.ITALIC);

        List<JMenu> placesSubMenus = createPlacesSubMenus();

        // If there are lots of sub-menus, create a "More..." sub-menu to put them all in.
        // Otherwise put them in the top-level menu
        if (placesSubMenus.size() > 3) {

            JMenu more = new JMenu("Places");
            more.setFont(folderFont);
            _popupMenu.add(more);

            for (JMenuItem item : placesSubMenus) {
                more.add(item);
            }
            more.add(createBrowseMenuItem());

        } else {
            for (JMenuItem item : placesSubMenus) {
                _popupMenu.add(item);
            }
            _popupMenu.add(createBrowseMenuItem());
        }

        _matchCount = 0;

        File value = _fileParameter.getValue();
        if ((value == null) || (value.getPath().equals(""))) {
            value = new File(".");
            try {
                value = value.getCanonicalFile();
            } catch (IOException e) {
            }
        }

        File parent = value.getParentFile();

        if (value.isDirectory()) {
            // Add sub-directories and matching files
            addPopupMenu(value, "");
        }

        if (parent != null) {
            if (value.exists()) {
                // Add siblings
                if (_matchCount > 0) {
                    _popupMenu.addSeparator();
                }
                addPopupMenu(parent, "");
            } else {
                // Add files and directories that start with the currently entered
                addPopupMenu(parent, value.getName());
            }
        }

        if (autoComplete && (_matchCount == 1)) {
            // There's only one possible auto-complete value, so use it without showing the popup menu.
            JMenuItem item = (JMenuItem) _popupMenu.getComponent(_popupMenu.getComponentCount() - 1);
            item.doClick();
            return;
        }

        _popupMenu.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "singleAutocomplete");
        _popupMenu.getActionMap().put("singleAutocomplete", new AbstractAction()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                singleAutoComplete();
            }
        });

        _popupMenu.show(_textField, x, y);

    }

    /**
     * If there is one and only one menu item, then click it
     */
    protected void singleAutoComplete()
    {
        JMenuItem item = null;
        for (Component c : _popupMenu.getComponents()) {
            if (c.isVisible() && c.isEnabled()) {
                if ((c instanceof JMenuItem) && (!(c instanceof JMenu))) {
                    if (item == null) {
                        item = (JMenuItem) c;
                    } else {
                        // Found more than one, don't autocomplete
                        return;
                    }
                }
            }
        }
        if (item != null) {
            _popupMenu.setVisible(false);
            item.doClick();
        }
    }

    protected JMenuItem createBrowseMenuItem()
    {
        JMenuItem item = new JMenuItem("Browse...");
        item.setFont(otherFont);
        item.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onFileChooser();
            }
        });
        return item;
    }

    private void addPopupItem(final String label, final File file)
    {
        _popupMenu.add(createPopupItem(label, file));
    }

    private JMenuItem createPopupItem(final String label, final File file)
    {
        JMenuItem menuItem = new JMenuItem(label);

        if (file.isDirectory()) {
            menuItem.setFont(folderFont);
        }

        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                _textField.setText(Util.getPathWithTrailingSlash(file));
            }

        });

        _matchCount++;
        return menuItem;
    }

    private void addPopupMenu(File directory, String prefix)
    {
        JMenuItem siblingsLabel = new JMenuItem("In Directory : " + directory.getName());
        siblingsLabel.setEnabled(false);
        boolean first = true;

        List<File> children;

        FileLister fileLister = new FileLister().directoriesFirst().includeDirectories();
        // Never exclude hidden files if we have a prefix (as on *nix, this tells us if it is hidden or not).
        if (prefix.equals("")) {
            fileLister.setIncludeHidden(_fileParameter.getIncludeHidden());
        } else {
            fileLister.setIncludeHidden(true);
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

                    if (first) {
                        _popupMenu.add(siblingsLabel);
                        first = false;
                    }
                    addPopupItem(child.getName(), child);
                }
            }
        }
    }

    protected void onFileChooser()
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

    @Override
    public void droppedFiles(List<File> files)
    {
        if (!files.isEmpty()) {
            _fileParameter.setValueIgnoreErrors(files.get(0));
        }
    }

    @Override
    public List<File> getDragFiles()
    {
        List<File> list = new ArrayList<>(1);
        if (_fileParameter.getValue() != null) {
            list.add(_fileParameter.getValue());
        }
        return list;
    }

}

package uk.co.nickthecoder.jguifier;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.guiutil.MaxJScrollPane;
import uk.co.nickthecoder.jguifier.guiutil.ScrollablePanel;
import uk.co.nickthecoder.jguifier.guiutil.VerticalStretchLayout;
import uk.co.nickthecoder.jguifier.parameter.Parameter;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A JFrame (a window), containing the set of {@link Task}'s parameters.
 *
 * @priority 4
 */
public class TaskPrompter
    extends JFrame implements ParameterListener
{
    private static final long serialVersionUID = 1;

    private static final int MAX_TABLE_HEIGHT = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getMaximumWindowBounds().height / 2;

    private Task _task;

    private ParametersPanel _parametersPanel;

    private JPanel _detailsPanel;

    private JPanel _defaultsPanel;

    private JTextField _commandLabel;

    private boolean _showCommandLine;

    public TaskPrompter(Task task)
    {
        this(task, false);
    }

    public TaskPrompter(Task task, boolean showCommandLine)
    {
        super(task.getName());
        _task = task;
        _showCommandLine = showCommandLine;
    }

    public Task getTask()
    {
        return _task;
    }

    public void prompt()
    {
        Util.defaultLookAndFeel();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Parameters
        _parametersPanel = new ParametersPanel();
        _parametersPanel.addParameters(getTask().getRootParameter());
        getTask().getRootParameter().addListener(this);

        // Details
        _detailsPanel = new JPanel();
        _detailsPanel.setVisible(false);
        _detailsPanel.setLayout(new BorderLayout());

        if (_showCommandLine) {
            JPanel commandPanel = new JPanel();
            commandPanel.setLayout(new BorderLayout());
            commandPanel.setBorder(BorderFactory.createTitledBorder("Command"));
            _commandLabel = new JTextField(getTask().getCommandString());
            // _commandLabel.setColumns(30);
            _commandLabel.setEditable(false);
            commandPanel.add(_commandLabel, BorderLayout.CENTER);
            _detailsPanel.add(commandPanel, BorderLayout.NORTH);

            // Copy Button
            JButton copyCommandButton = Util.createIconButton(getClass(), "editcopy.png", "Copy");
            copyCommandButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(_commandLabel.getText()), null);
                }
            });
            commandPanel.add(copyCommandButton, BorderLayout.WEST);
        }

        // Defaults File
        _defaultsPanel = new JPanel();
        _defaultsPanel.setVisible(false);
        _defaultsPanel.setLayout(new BorderLayout());
        _defaultsPanel.setBorder(BorderFactory.createTitledBorder("Defaults"));
        JTextField defaultsFileLabel = new JTextField(getTask().getDefaultsFile().getPath());
        defaultsFileLabel.setEditable(false);
        _defaultsPanel.add(defaultsFileLabel, BorderLayout.CENTER);

        // Defaults Folder Icon
        JButton defaultsFolderButton = Util.createIconButton(getClass(), "fileopen.png", "Open");

        defaultsFolderButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {
                    Desktop.getDesktop().open(getTask().getDefaultsFile().getParentFile());
                } catch (IOException e1) {
                    // Do nothing
                }
            }
        });
        _defaultsPanel.add(defaultsFolderButton, BorderLayout.WEST);

        // Save Defaults Button
        JButton saveDefaultsButton = Util.createIconButton(getClass(), "filesave.png", "Save");
        saveDefaultsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try {
                    getTask().saveDefaults();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Save Failed\n\n" + e1);
                }
            }
        });
        _defaultsPanel.add(saveDefaultsButton, BorderLayout.EAST);

        // Scrolling Panel
        ScrollablePanel scrollablePanel = new ScrollablePanel();
        scrollablePanel.setScrollableTracksViewportWidth(true);
        scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));
        scrollablePanel.add(_parametersPanel);
        scrollablePanel.add(_detailsPanel);
        scrollablePanel.add(_defaultsPanel);

        // Scroll
        MaxJScrollPane scrollPane = new MaxJScrollPane(
            scrollablePanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setMaxHeight(MAX_TABLE_HEIGHT);
        scrollPane.setMinimumSize(new Dimension(0, 0));

        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        Dimension scrollSize = scrollPane.getPreferredSize();
        if (scrollSize.getHeight() > MAX_TABLE_HEIGHT) {
            scrollPane.setPreferredSize(new Dimension((int) scrollSize.getWidth(), MAX_TABLE_HEIGHT));
        }

        VerticalStretchLayout vsl = new VerticalStretchLayout();
        vsl.setStretch(scrollPane, 1.0);
        // vsl.setStretch(scrollingPanel, 1.0);

        JPanel panel = new JPanel();
        panel.setLayout(vsl);

        panel.add(scrollPane);
        // panel.add( scrollingPanel );
        contentPane.add(panel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel rightButtonsPanel = new JPanel();
        rightButtonsPanel.setBorder(new EmptyBorder(0, 10, 5, 10));
        rightButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JPanel leftButtonsPanel = new JPanel();
        leftButtonsPanel.setBorder(new EmptyBorder(0, 10, 5, 10));
        leftButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        buttonsPanel.add(rightButtonsPanel, BorderLayout.EAST);
        buttonsPanel.add(leftButtonsPanel, BorderLayout.WEST);

        // Ok Button
        JButton okButton = new JButton("OK");
        Dimension buttonSize = new Dimension(100, okButton.getPreferredSize().height);
        okButton.setPreferredSize(buttonSize);
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        rightButtonsPanel.add(okButton);
        okButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                onOk();
            }
        });

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.setActionCommand("Cancel");
        rightButtonsPanel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                onCancel();
            }
        });

        // Details button
        final JButton detailsButton = new JButton("Details >>>");
        detailsButton.setPreferredSize(buttonSize);
        leftButtonsPanel.add(detailsButton);
        detailsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean showing = toggleDetails();
                detailsButton.setText(showing ? "Details <<<" : "Details >>>");
            }
        });

        // Complete the layout of the frame
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    public boolean toggleDetails()
    {

        boolean visible = !_detailsPanel.isVisible();
        _detailsPanel.setVisible(visible);
        _defaultsPanel.setVisible(visible);

        if (visible) {
            _detailsPanel.scrollRectToVisible(_detailsPanel.getBounds());
        }
        return visible;
    }

    public void onOk()
    {
        if (!_parametersPanel.check(getTask())) {
            return;
        }

        dispose();
        getTask().run();
    }

    public void onCancel()
    {
        dispose();
        getTask().fireAborted();
    }

    @Override
    public void changed(Parameter source)
    {
        if (_commandLabel != null) {
            _commandLabel.setText(getTask().getCommandString());
        }
    }

}

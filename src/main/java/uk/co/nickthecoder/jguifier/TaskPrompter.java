package uk.co.nickthecoder.jguifier;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.guiutil.MaxJScrollPane;
import uk.co.nickthecoder.jguifier.guiutil.VerticalStretchLayout;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * A JFrame (a window), containing the set of {@link Task}'s parameters.
 *
 * @priority 4
 */
public class TaskPrompter
    extends JFrame
{
    private static final long serialVersionUID = 1;

    private static final int MAX_TABLE_HEIGHT =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height / 2;

    private Task _task;
    
    private ParametersPanel _parametersPanel;

    public TaskPrompter(Task task)
    {
        super("Prompt");
        _task = task;
    }

    public void prompt()
    {
        Util.defaultLookAndFeel();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        _parametersPanel = new ParametersPanel();
        _parametersPanel.addParameters( _task.getRootParameter() );
        
        // Scroll
        MaxJScrollPane tableScroll = new MaxJScrollPane(
            _parametersPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.setMaxHeight(MAX_TABLE_HEIGHT);
        tableScroll.setMinimumSize(new Dimension(0, 0));
        tableScroll.setViewportBorder(BorderFactory.createEmptyBorder());

        VerticalStretchLayout vsl = new VerticalStretchLayout();
        vsl.setStretch(tableScroll, 1.0);

        JPanel panel = new JPanel();
        panel.setLayout(vsl);

        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        Dimension scrollSize = tableScroll.getPreferredSize();
        if (scrollSize.getHeight() > MAX_TABLE_HEIGHT) {
            tableScroll.setPreferredSize(new Dimension((int) scrollSize.getWidth(), MAX_TABLE_HEIGHT));
        }

        panel.add(tableScroll);
        contentPane.add(panel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(new EmptyBorder(0, 10, 5, 10));
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        // Ok Button
        JButton okButton = new JButton("OK");
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
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
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                onCancel();
            }
        });

        // on ESC key close frame
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel"); //$NON-NLS-1$
        getRootPane().getActionMap().put("Cancel", new AbstractAction() { //$NON-NLS-1$
                private static final long serialVersionUID = 1;

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    onCancel();
                }
            });

        // Complete the layout of the frame
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // table.setBackground( Color.BLUE );
        // panel.setBackground( Color.YELLOW );

    }


    public void onOk()
    {
        for (Parameter parameter : _task.getParameters()) {
            JLabel errorLabel = _parametersPanel.getErrorLabel(parameter);
            if (errorLabel.isVisible()) {
                return;
            }
        }

        boolean errors = false;
        for (Parameter parameter : _task.getParameters()) {
            try {
                parameter.check();
            } catch (ParameterException e) {
                _parametersPanel.setError(parameter, e.getMessage());
                errors = true;
            }
        }
        if (errors) {
            return;
        }

        try {
            _task.check();
        } catch (ParameterException e) {
            _parametersPanel.setError(e.getParameter(), e.getMessage());
            return;
        }

        dispose();
        try {
            _task.run();
        } catch (TaskException e) {
            System.out.println(e);
            // MORE Show and error dialog
        } catch (Exception e) {
            e.printStackTrace();
            // MORE Show and error dialog
        }
    }

    public void onCancel()
    {
        dispose();
    }

}

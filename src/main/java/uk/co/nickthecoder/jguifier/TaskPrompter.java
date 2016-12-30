package uk.co.nickthecoder.jguifier;

import java.util.Map;
import java.util.HashMap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.awt.GraphicsEnvironment;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.KeyStroke;

import javax.swing.border.EmptyBorder;

import uk.co.nickthecoder.jguifier.guiutil.MaxJScrollPane;
import uk.co.nickthecoder.jguifier.guiutil.RowLayoutManager;
import uk.co.nickthecoder.jguifier.guiutil.TableLayoutManager;
import uk.co.nickthecoder.jguifier.guiutil.VerticalStretchLayout;
import uk.co.nickthecoder.jguifier.util.Util;

public class TaskPrompter
    extends JFrame
{   
	private static final long serialVersionUID = 1;
	
    private static final int MAX_TABLE_HEIGHT =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height / 2;
    
    private Task _task;

    private Map<String,JLabel> _parameterErrorLabels;
    

    public TaskPrompter( Task task )
    {
        super( "Prompt");
        _task = task;
        _parameterErrorLabels = new HashMap<String,JLabel>();

    }
    
    public void prompt()
    {
        Util.defaultLookAndFeel();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        Container contentPane = this.getContentPane();
        VerticalStretchLayout verticalLayout = new VerticalStretchLayout();
        contentPane.setLayout( verticalLayout );

        
        // A table of all of the task's parameters
        JPanel table = new JPanel();
        TableLayoutManager tlm = new TableLayoutManager( table, 2 );
        table.setLayout( tlm );
        
        GroupParameter root = _task.getRootParameter();
        createParameters( root, table, tlm );
        
        // Scroll
        MaxJScrollPane tableScroll = new MaxJScrollPane(
            table,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        tableScroll.setMaxHeight( MAX_TABLE_HEIGHT );
        tableScroll.setMinimumSize( new Dimension( 0,0 ) );
        tableScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        Dimension scrollSize = tableScroll.getPreferredSize();
        if ( scrollSize.getHeight() > MAX_TABLE_HEIGHT ) {
            tableScroll.setPreferredSize( new Dimension( (int) scrollSize.getWidth(), MAX_TABLE_HEIGHT ) );
        }

        JPanel panel = new JPanel();
        VerticalStretchLayout vsl = new VerticalStretchLayout();
        panel.setLayout( vsl );
        vsl.setStretch( tableScroll, 1.0 );
        panel.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );
        panel.add( tableScroll );
        verticalLayout.setStretch( panel, 1.0 );
        contentPane.add( panel );
        
        // Buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder( new EmptyBorder( 5,10,5,10 ) );
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));    
        contentPane.add( buttonPane );
        
        // Ok Button
        JButton okButton = new JButton("OK");
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent event )
            {
                onOk();
            }
        } );    
    
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent event )
            {
                onCancel();
            }
        } );    
        // on ESC key close frame
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel"); //$NON-NLS-1$
        getRootPane().getActionMap().put("Cancel", new AbstractAction(){ //$NON-NLS-1$
        	private static final long serialVersionUID = 1;
        	public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        });

        // Complete the layout of the frame
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    private void createParameters( GroupParameter group, Container container, TableLayoutManager tlm )
    {
        for ( Parameter parameter : group.getChildren() ) {

            JLabel parameterErrorLabel = createErrorLabel();
            _parameterErrorLabels.put( parameter.getName(), parameterErrorLabel );
            parameterErrorLabel.setVisible( false );

            if ( parameter instanceof GroupParameter ) {
                GroupParameter subGroup = (GroupParameter) parameter;
                
                Container subContainer = (Container) subGroup.createComponent( this );
                subContainer.setLayout( tlm );
                container.add( subContainer );
                
                createParameters( subGroup, subContainer, tlm );
                
            } else {

                JPanel row = new JPanel();
                RowLayoutManager rlm = new RowLayoutManager( row, tlm );
                row.setLayout( rlm );
                
                JLabel label = new JLabel( parameter.getLabel() );
                rlm.add( label );
                            
                Component component = parameter.createComponent( this );
                rlm.add( component );
                
                container.add( row );
                container.add( parameterErrorLabel );
            }
                        
        }
        
    }

    private JLabel createErrorLabel()
    {
        Icon icon = UIManager.getIcon( "OptionPane.errorIcon" );
        JLabel result = new JLabel( icon );
        result.setForeground( Color.RED );
        
        return result;
    }
    
    public void setError( Parameter parameter, String message )
    {
        if ( message == null ) {
            clearError( parameter );
        }
        
        JLabel label = _parameterErrorLabels.get( parameter.getName() );
        label.setText( message );
        label.setVisible( true );
        pack();
    }
    
    public void clearError( Parameter parameter )
    {
        JLabel label = _parameterErrorLabels.get( parameter.getName() );
        label.setText( "" );
        label.setVisible( false );
        pack();
    }
    
    public void onOk()
    {
        for ( Parameter parameter : _task.getParameters() ) {
            JLabel errorLabel = _parameterErrorLabels.get( parameter.getName() );
            if ( errorLabel.isVisible() ) {
                return;
            }
        }

        boolean errors = false;
        for ( Parameter parameter : _task.getParameters() ) {
            try {
                parameter.check();
            } catch (ParameterException e) {
                setError( parameter, e.getMessage() );
                errors = true;
            }
        }
        if ( errors ) {
            return;
        }
        
        try {
            _task.check();
        } catch (ParameterException e ) {
            setError( e.getParameter(), e.getMessage() );
            return;
        }
                
        dispose();
        try {
            _task.run();
        } catch (TaskException e ) {
            System.out.println( e );
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


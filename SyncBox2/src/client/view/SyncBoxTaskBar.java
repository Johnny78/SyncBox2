package client.view;


import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


import client.model.constants.Path;
import client.controller.SyncControl;
import client.model.fileSystem.WatchDir;

/**
 * Displays an icon in the users taskbar
 * provides information updates about sync progress
 * @author John
 *
 */
public class SyncBoxTaskBar {
	
	private SyncControl syncControl;
	private TrayIcon trayIcon;
	
    public  SyncBoxTaskBar(SyncControl sc){
    	syncControl = sc;   	
        /* Use an appropriate Look and Feel */
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        String path = Path.SYNCBOX;

        try {       	
        	new Thread(new WatchDir(Paths.get(path), syncControl)).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    private void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        trayIcon =
                new TrayIcon(createImage("images/syncbox.png", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.setImageAutoSize(true);
        
        // Create a popup menu components
        MenuItem syncItem = new MenuItem("Synchronise");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");
        
        //Add components to popup menu
        popup.add(syncItem);
        popup.add(aboutItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
        
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            	trayIcon.displayMessage("SyncBox information",
                        "Syncing your files...", TrayIcon.MessageType.NONE);
				try {
					syncControl.synchronise();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
            }
        });
        
              
        syncItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            	trayIcon.displayMessage("SyncBox information",
                        "Syncing your files...", TrayIcon.MessageType.NONE);
				try {
					syncControl.synchronise();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
            }
        });
        
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "SyncBox is a secure cloud backup program.\nVisit our website for more information:\nhttp://syncbox.no-ip.biz:8080/syncboxweb/  ");
                JFrame frame = new JFrame("SyncBox password");
        		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		//new Thread(new ConsolePane()).start();       		
            }
        });
        
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }
    
    public void informUser(){
    	trayIcon.displayMessage("SyncBox information",
                "Syncing your files...", TrayIcon.MessageType.NONE);
    }
    
    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = SyncBoxTaskBar.class.getResource(path);
        
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}

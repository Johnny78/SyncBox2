package client.view;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import client.model.constants.Path;

/**
 * Offer the user the choice of where to put the SyncBox folder
 * @author John
 *
 */
public class FileChooser{
	
	public static String chooseDir(){
	    JFileChooser chooser = new JFileChooser();
	    chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File(Path.DESK));
	    chooser.setDialogTitle("Select a location for the SyncBox folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    int returnVal = chooser.showOpenDialog(new JPanel());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       return chooser.getSelectedFile().getAbsolutePath();
	    }
	    return null;
	}
}

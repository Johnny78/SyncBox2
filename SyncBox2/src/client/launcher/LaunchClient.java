package client.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

import client.ClientControl;
import client.constants.Path;
import client.view.PassPrompt;
import client.view.SetupScreen;
import client.view.SyncBoxTaskBar;

public class LaunchClient {

	public static void main(String[] args) throws Exception {
		char[] masterPassword = null;
		String[] credentials = null;

		//check client credentials exist
		File creds = new File(Path.CLIENT+"credentials.ser");
		if (creds.exists()){
			System.out.println("reading credentials from file");
			try{
				FileInputStream fin = new FileInputStream(creds);
				ObjectInputStream ois = new ObjectInputStream(fin);
				credentials = (String[]) ois.readObject();
				ois.close();
			}catch(Exception ex){
				ex.printStackTrace();
			} 
			//ask for masterPassword
			PassPrompt pp = new PassPrompt();
			masterPassword = pp.getUserPassword();
			pp.setVisible(false); 
			pp.close();
		}
		else{
			//welcome new user
			SetupScreen ss = new SetupScreen();
			credentials = ss.getCredentials();
			ss.setVisible(false); 
			ss.close();

			JOptionPane.showMessageDialog(null, "You must now choose your master password\n"
					+ "Choose a strong password with a mix of numbers special characters and letters\n"
					+"You will be prompted for this password on every startup of SyncBox");

			try{
				File f0 = new File(Path.SYNCBOX);
				f0.mkdirs();
				File f1 = new File(Path.CLIENT);
				f1.mkdirs();
				File f2 = new File(Path.TEMP);
				f2.mkdirs();
				FileOutputStream fout = new FileOutputStream(creds);
				ObjectOutputStream oos = new ObjectOutputStream(fout);   
				oos.writeObject(credentials);
				oos.close();
			}catch(Exception ex){ex.printStackTrace();}



			PassPrompt pp = new PassPrompt();
			masterPassword = pp.getUserPassword();
			pp.setVisible(false); 
			pp.close();
			
			JOptionPane.showMessageDialog(null, "Great you are now ready to use Syncbox\n"
					+ "- Place the files you want in the SyncBoxFolder on your desktop\n"
					+ "- Double click the padlock icon on your taskbar to synchronise with the server");
		}
		//launch taskbar
		ClientControl cc = new ClientControl(credentials[0], credentials[1], masterPassword);
		//ClientControl cc = null;
		new SyncBoxTaskBar(cc);
		
	}
}

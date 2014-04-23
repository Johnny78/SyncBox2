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

			JOptionPane.showMessageDialog(null, "You must now choose your master password, keep it safe because there will be no copies. you will be prompted for this password on every startup of SyncBox");

			try{
				FileOutputStream fout = new FileOutputStream(creds);
				ObjectOutputStream oos = new ObjectOutputStream(fout);   
				oos.writeObject(credentials);
				oos.close();
			}catch(Exception ex){ex.printStackTrace();}



			PassPrompt pp = new PassPrompt();
			masterPassword = pp.getUserPassword();
			pp.setVisible(false); 
			pp.close();
		}
		//launch taskbar
		ClientControl cc= null;// = new ClientControl(credentials[0], credentials[1], masterPassword);
		SyncBoxTaskBar tb = new SyncBoxTaskBar(cc);
	}
}

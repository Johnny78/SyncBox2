package client.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

import client.controller.ClientLauncher;
import client.model.constants.Path;
import client.model.security.PasswordBasedEncryption;
import client.view.FileChooser;
import client.view.PassPrompt;
import client.view.SetupScreen;
import client.view.SyncBoxTaskBar;
/**
 * This class deals with launching SyncBox.
 * If the installation settings file is missing
 * then the install will run.
 * otherwise prompt for master Password and
 * launch application.
 * @author John
 *
 */
public class ClientLauncher {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting SyncBox");
		char[] masterPassword = null;
		String[] credentials = null;
		PasswordBasedEncryption aesAlgo;

		//check client credentials file exist
		File creds = new File(Path.CLIENT+"credentials.ser");
		File crypCreds = new File(Path.CLIENT+"credentials.aes");
		if (crypCreds.exists()){
			System.out.println("Returning User\n\n");
			//ask for masterPassword
			PassPrompt pp = new PassPrompt();
			masterPassword = pp.getUserPassword();
			pp.setVisible(false); 
			pp.close();
			try{
				//decrypt credentials
				System.out.println("decrypting with password ");
				for (char c : masterPassword){System.out.print(c);}
				aesAlgo = new PasswordBasedEncryption(masterPassword);				
				aesAlgo.decrypt(crypCreds.getAbsolutePath(), creds.getAbsolutePath());					
				FileInputStream fin = new FileInputStream(creds);
				ObjectInputStream ois = new ObjectInputStream(fin);
				credentials = (String[]) ois.readObject();
				ois.close();
				aesAlgo.encrypt(creds.getAbsolutePath(), crypCreds.getAbsolutePath());
				
			}catch(Exception ex){
	//although the master password is not stored it was used to encrypt the credentials file so if we can't decrypt it we know the password is wrong !
				JOptionPane.showMessageDialog(null, "The Master Password is incorrect please restart SyncBox with the right password");
				System.exit(0);
			}
			Path.SYNCBOX = credentials[2];

		}
		else{
			System.out.println("New User\n\n");
			SetupScreen ss = new SetupScreen();
			credentials = ss.getCredentials();
			ss.setVisible(false); 
			ss.close();
			
			NetworkControl nc = new NetworkControl(credentials[0], credentials[1]);
			nc.isOnServer("login test");
			
			JOptionPane.showMessageDialog(null, "The next step is to choose the location of your SyncBox folder\n"
					+ "We recommend the desktop.");
			 String parentFolder = FileChooser.chooseDir();
			 File f = new File(parentFolder);
			 Path.SYNCBOX = f.getAbsolutePath()+"/SyncBox/";
			String[] settings = {credentials[0], credentials[1], Path.SYNCBOX, "test string"};
			credentials = settings;
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
			
			System.out.println("encrypting with password ");
			for (char c : masterPassword){System.out.print(c);}
			aesAlgo = new PasswordBasedEncryption(masterPassword);
			aesAlgo.encrypt(creds.getAbsolutePath(), crypCreds.getAbsolutePath());
			
			pp.setVisible(false); 
			pp.close();
			
			
			
			JOptionPane.showMessageDialog(null, "Great, you are now ready to use Syncbox\n"
					+ "Placing files into the SyncBox folder will automatically send an encrypted copy to the server.\n"
					+ "edits and deltes will also be replicated on the server.\n\n"
					+ "Don't forget to install SyncBox on your other devices to enjoy synchronisation across them all.");
			
		}
		//launch taskbar icon
		NetworkControl nc = new NetworkControl(credentials[0], credentials[1]);
		SyncControl sc = new SyncControl(nc, masterPassword);
		SyncBoxTaskBar view = new SyncBoxTaskBar(sc);
		System.out.println("loading view");
		sc.getView(view);
		
	}
}

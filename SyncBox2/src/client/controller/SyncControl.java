package client.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;

import client.model.constants.ActionType;
import client.model.constants.Path;
import client.model.metadata.FileMetadata;
import client.model.metadata.MetadataTool;
import client.model.security.PasswordBasedEncryption;
import client.view.SyncBoxTaskBar;


/**
 * This is the Client class that deals with synchronising the client and server file system
 * @author John
 *
 */
public class SyncControl {

	private NetworkControl networkCon;
	private char[] masterPassword;
	private SyncBoxTaskBar view;

	/**
	 * Constructor takes a master password used to encrypt metadata file 
	 * @param nc
	 * @param masterPassword
	 */
	public SyncControl(NetworkControl nc, char[] masterPassword){
		this.masterPassword = masterPassword;
		networkCon = nc;
		initialise();
	}
	
	public void getView(SyncBoxTaskBar view){
		this.view = view;
	}

	/**
	 * This method sets in motion the full functionality of Syncbox.
	 * Metadata will be generated to represent the files on the server and client.
	 * This will be used to check for disparities on the two file systems.
	 * If files need to be exchanged they will undergo encryption before being sent
	 * and decryption upon reception.
	 * 
	 * A master password is necessary to encrypt the metadata file
	 * all other files will have secure pseudo random passwords attributed.
	 */
	public synchronized void synchronise(){

		System.out.println("Synchronising folders");
		if (view != null){
			view.informUser();
		}
		
		networkCon.connect();
		HashMap<FileMetadata, ActionType> actions = compareMetadata();
		sync(actions);
		networkCon.disconnect();

	}


	/**
	 * This is the method that applies the actions needed to achieve synchronisation
	 * @param actions
	 */
	public void sync(HashMap<FileMetadata, ActionType> actions){
		boolean unchanged = true;
		ArrayList<FileMetadata> resultList = new ArrayList<FileMetadata>();
		ArrayList<FileMetadata> deletedList = MetadataTool.readArray(Path.CLIENT + Path.DELETED_METADATA);
		Iterator<Entry<FileMetadata, ActionType>> it = actions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<FileMetadata, ActionType> pairs = it.next();
			ActionType  action = pairs.getValue();
			if (!action.equals(ActionType.UNCHANGED)){
				System.out.println(action+" file "+pairs.getKey().getName());
			}
			switch (action){
			case UNCHANGED :
				resultList.add(pairs.getKey());
				break;
			case IMPORT:
				unchanged = false;
				networkCon.recieveFile(pairs.getKey().getCipherName());
				decrypt(pairs.getKey().getCipherName(), Path.SYNCBOX + pairs.getKey().getName(), pairs.getKey().getPassword());
				resultList.add(pairs.getKey());
				break;
			case EXPORT:
				unchanged = false;
				encrypt(Path.SYNCBOX + pairs.getKey().getName(), pairs.getKey().getCipherName(), pairs.getKey().getPassword());
				networkCon.sendFile(pairs.getKey().getCipherName());
				resultList.add(pairs.getKey());
				break;
			case DELETEFROMCLIENT:
				unchanged = false;
				networkCon.deleteFileFromClient(pairs.getKey().getName());
				break;
			case DELETEFROMSERVER:
				unchanged = false;
				networkCon.deleteFileFromServer(pairs.getKey().getCipherName());
				break;
			}	
		}	
		if (!unchanged){
			MetadataTool.writeArray(resultList, Path.CLIENT + Path.SERVER_METADATA);
			MetadataTool.writeArray(deletedList, Path.CLIENT+Path.DELETED_METADATA);
			updateServerMetadata();
		}
		clearTempFile();
	}

	/**
	 * get latest server metadata
	 * update client metadata (look for changes)
	 * compare the 2
	 * @return a list of actions needed to synchronise
	 */
	public HashMap<FileMetadata, ActionType> compareMetadata(){
		MetadataTool.updateClientMetadata();
		getServerMetadata();	
		HashMap<FileMetadata, ActionType> actions = MetadataTool.compare();
		return actions;
	}

	public void getServerMetadata(){
		System.out.println("Fetching Server metadata ...");
		networkCon.recieveFile(Path.SERVER_METADATA);
		networkCon.recieveFile(Path.DELETED_METADATA);
		decrypt(Path.SERVER_METADATA, Path.CLIENT + Path.SERVER_METADATA, masterPassword);
		decrypt(Path.DELETED_METADATA, Path.CLIENT + Path.DELETED_METADATA, masterPassword);
	}

	public void updateServerMetadata(){
		System.out.println("Updating Server metadata ...");
		encrypt(Path.CLIENT + Path.SERVER_METADATA, Path.SERVER_METADATA, masterPassword);
		encrypt(Path.CLIENT + Path.DELETED_METADATA, Path.DELETED_METADATA, masterPassword);
		networkCon.sendFile(Path.SERVER_METADATA);
		networkCon.sendFile(Path.DELETED_METADATA);
	}

	/**
	 * Encrypts a file with given password
	 * encrypted file is created in Temp dir
	 * @param fileName
	 * @param password
	 */
	public void encrypt(String path,String cipherName, SecretKey password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.encrypt(path, Path.TEMP + cipherName);		
		System.out.println(new File(path).getName()+" encrypted to "+ cipherName);
	}

	/**
	 * Decrypts a file with given password
	 * cipher file must be in TEMP dir
	 * clear flie will be made in SyncBox
	 * @param cipherName
	 * @param clearName
	 * @param password
	 */
	public void decrypt(String cipherName, String path, SecretKey password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.decrypt(Path.TEMP + cipherName, path);
		System.out.println(cipherName+" decrypted to "+ new File(path).getName());
	}

	// metadata crypto same as above but uses master password:
	public void encrypt(String path,String cipherName, char[] password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.encrypt(path, Path.TEMP + cipherName);					
	}

	public void decrypt(String cipherName, String path, char[] password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.decrypt(Path.TEMP + cipherName, path);	
	}

	/**
	 * make empty metadata files for the initial sync
	 */
	public void initialise(){
		//make folders
		File f = new File(Path.SYNCBOX);
		if (!f.exists() || !f.isDirectory()){
			f.mkdirs();
			File f1 = new File(Path.CLIENT);
			f1.mkdirs();
			File f2 = new File(Path.TEMP);
			f2.mkdirs();
		}
		//make empty metadata
		if (!(new File(Path.CLIENT +Path.SERVER_METADATA)).exists()){
			MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.CLIENT_METADATA), Path.CLIENT + Path.CLIENT_METADATA);
			MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA), Path.CLIENT + Path.SERVER_METADATA);
			MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.DELETED_METADATA), Path.CLIENT + Path.DELETED_METADATA);
			MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.SEEN_METADATA), Path.CLIENT + Path.SEEN_METADATA);
		}
		//get Server metadata
		if(networkCon.isOnServer(Path.SERVER_METADATA)){
			synchronise();
		}
		//or send empty metadata
		else{
			networkCon.connect();
			updateServerMetadata();
			networkCon.disconnect();;
		}
	}

	/**
	 * delete all temporary files (ie encrypted files on client side)
	 */
	public void clearTempFile(){
		System.out.println("cleaning metadata");
		try{
			File temp = new File(Path.TEMP);
			String files[] = temp.list();
			File f;
			for (String s : files) {
				f = new File(Path.TEMP + s); 
				f.delete();
			}			
		}
		catch(Exception e){e.printStackTrace();}
	}
}

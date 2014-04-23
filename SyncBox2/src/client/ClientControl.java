package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;

import com.sun.net.ssl.internal.ssl.Provider;

import client.constants.ActionType;
import client.constants.Path;
import client.metadata.FileMetadata;
import client.metadata.MetadataTool;
import client.security.PasswordBasedEncryption;


/**
 * This is the Client class where interactions with the server take place
 * @author John
 *
 */
public class ClientControl {

	private char[] masterPassword = "soup".toCharArray();
	private String connectionPassword = "pass";
	private String email = "joe@joe.com";

	private static final String serverAddress = "localhost";
	//private static final String serverAddress = "syncbox.no-ip.biz";
	private static final int serverPort = 20661;
	private SSLSocket clientSocket;
	private DataOutputStream outToServer;
	private DataInputStream dis;
	private BufferedReader inFromServer;

	/**
	 * Constructor
	 * Checks the server has metadata
	 * otherwise it initialises a synchronisation with empty meta files
	 * @throws Exception
	 */
	public ClientControl() throws Exception{
		//add user info in constructor
		
		if (!isOnServer(Path.SERVER_METADATA)){
			initialise();
		}
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
	 * 
	 * 
	 * 
	 */
	public void synchronise(){
		System.out.println("Synchronising folders");
		//masterPassword = password;
		HashMap<FileMetadata, ActionType> actions = compareMetadata();
		sync(actions);
		//nullify password
	}

	/**
	 * sets up a TCP connection with server
	 */
	public void connect(){
		try {
			Security.addProvider(new Provider());
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
			clientSocket = (SSLSocket)sslsocketfactory.createSocket(serverAddress, serverPort);
			System.out.println("Connecting...");
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			dis = new DataInputStream(clientSocket.getInputStream());
			
			//authentication
			System.out.println("Sending log in credentials");
			outToServer.writeBytes(email+"\n");
			outToServer.flush();
			outToServer.writeBytes(connectionPassword+"\n");
			outToServer.flush();
			String response = inFromServer.readLine();
			if (response.equals("welcome")){System.out.println("login successful");}
			else{ throw new Exception("invalid credentials");}
		}
		catch (Exception e){e.printStackTrace(); System.out.println("connection failed");}
	}

	/**
	 * This is used when the file transfer needs to be ended by closing the data stream
	 * ie no more messages can be sent until next reconnect
	 */
	public void quickDisconnect(){
		try{	
			dis.close();
			inFromServer.close();
			clientSocket.close();
		}
		catch (Exception e){e.printStackTrace(); System.out.println("connection failed");}
	}

	/**
	 * graceful disconnect
	 * informs server and waits for response
	 */
	public void disconnect(){
		try{	
			outToServer.writeBytes("quit\n");
			outToServer.flush();
			String response = inFromServer.readLine();
			System.out.println(response+"\n\n");
			dis.close();
			inFromServer.close();
			clientSocket.close();
		}
		catch (Exception e){e.printStackTrace(); System.out.println("connection failed");}
	}

	/**
	 * asks server to check its file system for a file
	 * @param fileName
	 * @return
	 */
	public boolean isOnServer(String fileName){
		String response = "";
		try{
			connect();

			outToServer.writeBytes("is on server\n");
			outToServer.writeBytes(fileName + "\n");
			outToServer.flush();
			response = inFromServer.readLine();
			System.out.println("Is file "+ fileName + " on server?");
			System.out.println("...");
			System.out.println(response);

			disconnect();
		}
		catch(Exception e ){e.printStackTrace();}
		return response.equals("yes");
	}

	/**
	 * asks server to send file
	 * 
	 * @param fileName
	 */
	public void recieveFile(String fileName){
		try{		
			connect();
			outToServer.writeBytes("send file\n");
			inFromServer.readLine();
			outToServer.writeBytes(fileName + "\n");
			outToServer.flush();
			System.out.println("Asking for file "+ fileName);
			long len = dis.readLong();
			System.out.println("preparing to recieve "+len+" bytes");
			String filePath = Path.TEMP + fileName;
			File f = new File(filePath);
			outToServer.writeBytes("ready to recieve\n");
			recieveFile(f);
			System.out.println("file "+ f.getName()+" recieved "+f.length()+"\n\n");
			quickDisconnect();
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * sends file to server
	 * the protocol is
	 * send filename, file size, file contents 
	 * @param fileName
	 */
	public void sendFile(String fileName){
		String filePath;
		if (fileName.contains("metadata")){
			filePath = Path.TEMP + fileName;
		}
		else{
			filePath = Path.TEMP + fileName;
		}
		File f = new File(filePath);
		if (!f.exists()){
			System.out.println("No such File");
		}
		else{
			try {				
				connect();
				System.out.println("Sending File...");
				outToServer.writeBytes("recieve file\n");				
				String response = inFromServer.readLine();		//wait till server is ready
				System.out.println(response);
				outToServer.writeBytes(f.getName() + "\n");	
				outToServer.flush();
				inFromServer.readLine();				//wait till server is ready
				System.out.println("sending " +f.length()+" bytes");
				outToServer.writeLong(f.length());
				outToServer.flush();
				FileInputStream fis = new FileInputStream(f);
				IOUtils.copy(fis,  outToServer);
				IOUtils.closeQuietly(fis);	
				System.out.println(f.getName()+" sent\n\n");
				quickDisconnect();
			} catch (IOException e) {e.printStackTrace();System.out.println("send file failed");}
		}
	}

	/**
	 * Sends message to server to delete a file on server
	 * @param fileName
	 */
	public void deleteFileFromServer(String fileName){
		try {
			connect();
			outToServer.writeBytes("delete file\n");	
			String response = inFromServer.readLine();		//wait till server is ready
			System.out.println(response);
			outToServer.writeBytes(fileName + "\n");		//send path
			response = inFromServer.readLine();				//wait for confirmation
			System.out.println(response);
			disconnect();
		} catch (IOException e) {e.printStackTrace();System.out.println("delete file failed");	}
	}

	/**
	 * Deletes clients file
	 * this is used when another client
	 * updates the server by deleting its version of a file 
	 * @param fileName
	 */
	public void deleteFileFromClient(String fileName){
		System.out.println("Deleting file "+ fileName + " from Client");
		File f = new File(Path.SYNCBOX+ fileName);
		if(!f.exists()){
			System.out.println("No such file");
		}
		if(!f.delete()){
			System.out.println("Delete failed");
		}
		else{
			System.out.println("Delete successful");
		}
	}

	/**
	 * copies the contents of socket data-input-stream to a file-output-stream
	 * @param targetFile
	 */
	public void recieveFile(File targetFile){
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			IOUtils.copy(dis, fos);
			IOUtils.closeQuietly(fos);						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sync(HashMap<FileMetadata, ActionType> actions){
		boolean unchanged = true;
		ArrayList<FileMetadata> resultList = new ArrayList<FileMetadata>();
		ArrayList<FileMetadata> deletedList = MetadataTool.readArray(Path.CLIENT + Path.DELETED_METADATA);
		Iterator<Entry<FileMetadata, ActionType>> it = actions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<FileMetadata, ActionType> pairs = it.next();
			ActionType  action = pairs.getValue();
			System.out.println("attempting to "+action+" file "+pairs.getKey().getName());
			switch (action){
			case UNCHANGED :
				resultList.add(pairs.getKey());
				break;
			case IMPORT:
				unchanged = false;
				recieveFile(pairs.getKey().getCipherName());
				decrypt(pairs.getKey().getCipherName(), Path.SYNCBOX + pairs.getKey().getName(), pairs.getKey().getPassword());
				resultList.add(pairs.getKey());
				break;
			case EXPORT:
				unchanged = false;
				encrypt(Path.SYNCBOX + pairs.getKey().getName(), pairs.getKey().getCipherName(), pairs.getKey().getPassword());
				sendFile(pairs.getKey().getCipherName());
				resultList.add(pairs.getKey());
				break;
			case DELETEFROMCLIENT:
				unchanged = false;
				deleteFileFromClient(pairs.getKey().getName());
				break;
			case DELETEFROMSERVER:
				unchanged = false;
				deleteFileFromServer(pairs.getKey().getCipherName());
				
				
				break;
			}	
			if (!unchanged){
				MetadataTool.writeArray(resultList, Path.CLIENT + Path.SERVER_METADATA);
				MetadataTool.writeArray(deletedList, Path.CLIENT+Path.DELETED_METADATA);
				updateServerMetadata();
			}
			clearTempFile();
		}	
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
		recieveFile(Path.SERVER_METADATA);
		recieveFile(Path.DELETED_METADATA);
		decrypt(Path.SERVER_METADATA, Path.CLIENT + Path.SERVER_METADATA, masterPassword);
		decrypt(Path.DELETED_METADATA, Path.CLIENT + Path.DELETED_METADATA, masterPassword);
	}

	public void updateServerMetadata(){
		encrypt(Path.CLIENT + Path.SERVER_METADATA, Path.SERVER_METADATA, masterPassword);
		encrypt(Path.CLIENT + Path.DELETED_METADATA, Path.DELETED_METADATA, masterPassword);
		sendFile(Path.SERVER_METADATA);
		sendFile(Path.DELETED_METADATA);
	}

	/**
	 * Encrypts a file with given password
	 * encrypted file is created in Temp dir
	 * @param fileName
	 * @param password
	 */
	public void encrypt(String path,String cipherName, SecretKey password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.encrypt(path, cipherName);		
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
		aesAlgo.decrypt(cipherName, path);
		System.out.println(cipherName+" decrypted to "+ new File(path).getName());
	}

	// metadata crypto same as above but uses master password:
	public void encrypt(String path,String cipherName, char[] password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.encrypt(path, cipherName);					
	}

	public void decrypt(String cipherName, String path, char[] password){
		PasswordBasedEncryption aesAlgo = new PasswordBasedEncryption(password);				
		aesAlgo.decrypt(cipherName, path);	
	}

	/**
	 * make empty metadata files for the initial sync
	 */
	public void initialise(){
		System.out.println("initialising metadata\n\n");
		MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.CLIENT_METADATA), Path.CLIENT + Path.CLIENT_METADATA);
		MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA), Path.CLIENT + Path.SERVER_METADATA);
		MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.DELETED_METADATA), Path.CLIENT + Path.DELETED_METADATA);
		MetadataTool.writeArray(MetadataTool.readArray(Path.CLIENT + Path.SEEN_METADATA), Path.CLIENT + Path.SEEN_METADATA);
		updateServerMetadata();
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

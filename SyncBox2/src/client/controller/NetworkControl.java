package client.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.Security;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

import client.model.constants.Path;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * This class deals with communications with the server
 * @author John
 *
 */
public class NetworkControl {

	//private static final String serverAddress = "syncbox.no-ip.biz";
	private static final String serverAddress = "localhost";
	private static final int serverPort = 20661;
	private SSLSocket clientSocket;
	private DataOutputStream outToServer;
	private DataInputStream dis;
	private BufferedReader inFromServer;
	

	private String connectionPassword;
	private String email;

	/**
	 * Constructor
	 * Checks the server has metadata
	 * otherwise it initialises a synchronisation with empty meta files
	 * @throws Exception
	 */
	public NetworkControl(String email, String password) throws Exception{
		this.email = email;
		this.connectionPassword = password;
	}
	
	
	/**
	 * sets up a SSL TCP connection with server
	 * @throws Exception 
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
			System.out.print("Sending credentials ..... ");
			outToServer.writeBytes(email+"\n");
			outToServer.flush();
			outToServer.writeBytes(connectionPassword+"\n");
			outToServer.flush();
			String response = inFromServer.readLine();
			System.out.println(response);
			if (response.equals("welcome")){System.out.println("login successful\n");}
			else if(response.equals("non-authorised user")){ JOptionPane.showMessageDialog(null, "The credentials are not recognised by the server\n"
					+ " please restart SyncBox with the correct credentials");System.exit(0);}
		}
		catch (SSLHandshakeException e){JOptionPane.showMessageDialog(null, "The SSL certificate is not recognised please add the certificate to your Java security cacerts file\n"
				+ "you will find instructions on the SyncBox website");System.exit(0);}
		catch (UnknownHostException e) {System.out.println("unknown host");}//e.printStackTrace();} 
		catch (IOException e) {System.out.println("io error");//e.printStackTrace();
		}
	}


	/**
	 * graceful disconnect
	 * informs server and waits for response
	 * then closes streams and socket
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
			System.out.print("Is file "+ fileName + " on server?");
			System.out.print(" ..... ");
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
			FileOutputStream fos = new FileOutputStream(f);
			copy(dis,fos, len);
			System.out.println("file "+ f.getName()+" recieved "+f.length()+"\n");
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
				System.out.println("Sending File...");
				outToServer.writeBytes("recieve file\n");				
				inFromServer.readLine();		//wait till server is ready
				outToServer.writeBytes(f.getName() + "\n");	
				outToServer.flush();
				inFromServer.readLine();				//wait till server is ready
				System.out.println("sending " +f.length()+" bytes");
				outToServer.writeLong(f.length());
				outToServer.flush();
				FileInputStream fis = new FileInputStream(f);
				copy(fis, outToServer, f.length());
				System.out.println(f.getName()+" sent\n");
			} catch (IOException e) {e.printStackTrace();System.out.println("send file failed");}
		}
	}

	/**
	 * Sends message to server to delete a file on server
	 * @param fileName
	 */
	public void deleteFileFromServer(String fileName){
		try {
			outToServer.writeBytes("delete file\n");	
			inFromServer.readLine();						//wait till server is ready
			outToServer.writeBytes(fileName + "\n");		//send path
			String response = inFromServer.readLine();				//wait for confirmation
			System.out.println(response+"\n");

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
	 * This is the file transfer code.
	 * Given two streams and the length in bytes 
	 * of the data being transferred it copies one
	 * stream to the other. This code is used both
	 * to send and receive on the the client and the 
	 * server.
	 * 
	 * example usage to send a file from client to server
	 * Client -> copy(fileReaderStream(File f), dataOutputStream(Socket s), f.length)
	 * Server -> copy(dataInputStream(Socket s), fileOutputStream(File f), f.length)
	 * 
	 * not only does this provide a reduction in code it also allows file transfer without
	 * closing the data stream after each transfer, thus avoiding multiple login when transferring
	 * multiple files.
	 *  
	 * @param in
	 * @param out
	 * @param length
	 * @throws IOException
	 */
	static void copy(InputStream in, OutputStream out, long length) throws IOException {
		byte[] buf;
		if (length < 8192){
			buf = new byte[(int) length];
		}
		buf = new byte[8192];
		int len = 0;
		long read = 0;
		while (length > read && (len = in.read(buf)) > -1) {
			read += len;
			out.write(buf, 0, len);
			if (length-read < 8192){
				buf = new byte[(int) (length-read)];
			}
		}
	}

}

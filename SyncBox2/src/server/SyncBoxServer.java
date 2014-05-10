package server;

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

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;





public class SyncBoxServer implements Runnable {

	/**
	 * This the server Thread class.
	 * Once a client connects an instance of this class is generated
	 * and takes requests from the client.
	 * 
	 * The first step is authentication after that an internal loop
	 * waits for synchronisation commands.
	 */
	private boolean clientConnected;
	private String userEmail;
	private String password;
	private String syncBoxDir;
	private String metaDir;

	boolean serving;
	DataInputStream dis;
	DataOutputStream outToClient;
	BufferedReader inFromClient;
	SSLServerSocket welcomeSocket;
	SSLSocket clientSocket;

	public SyncBoxServer(SSLSocket clientSocket){
		this.clientSocket = clientSocket;
	}

	public void run(){
		try{
			serving = true;
			while (serving){
				System.out.println("*************");
				System.out.println("Serving...");
				System.out.println("Client connected");

				outToClient = new DataOutputStream(clientSocket.getOutputStream());
				inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				dis = new DataInputStream(clientSocket.getInputStream());
				clientConnected = true;

				//authenticate
				System.out.print("Checking log in credentials ...");
				userEmail = inFromClient.readLine();
				System.out.println("Username :"+userEmail);
				
				password = inFromClient.readLine();
				System.out.println("pass :"+password);
				
				Boolean verified = DatabaseCheck.isUser(userEmail, password);

				if (verified){
					outToClient.writeBytes("welcome\n");
					System.out.println("   Valid Credentials");
					//make userFolder if needed
					String[] parts = userEmail.split("@");
					String userPath =  parts[0]+"/";
					syncBoxDir = "server/"+userPath+"syncbox/";
					metaDir = "server/"+userPath;
					File f = new File(syncBoxDir);
					if (!f.exists() || !f.isDirectory()){
						f.mkdirs();
					}

				}
				else{
					System.out.println("Invalid Credentials");
					outToClient.writeBytes("non-authorised user\n");
					clientConnected = false;
					outToClient.writeBytes("Ending connection\n");
					clientSocket.close();
					System.out.println("Exiting\n\n");
					serving = false;

				}

				//main loop executes commands from client
				while (clientConnected){
					String clientCommand = inFromClient.readLine();
					System.out.println("\nCommand recieved: "+clientCommand);

					switch (clientCommand){


					case "send file":
						outToClient.writeBytes("Server Ready\n");
						String fileName = inFromClient.readLine();
						if (fileName.contains("metadata")){
							fileName = metaDir + fileName;
						}
						else{
							fileName = syncBoxDir + fileName;
						}
						File f = new File(fileName);
						if (!f.exists()){
							System.out.println("No such File");
						}
						else{	
							System.out.println("preparing to send "+ f.getName());
							outToClient.writeLong(f.length());
							outToClient.flush();
							inFromClient.readLine();
							FileInputStream fis = new FileInputStream(f);
							copy(fis, outToClient, f.length());
							fis.close();
							System.out.println("File sent");

						}
						break;

						//check a file exists on the server
					case "is on server":
						fileName = inFromClient.readLine();
						f = new File(syncBoxDir + fileName);
						File fi = new File (metaDir + fileName);
						System.out.println("is "+ fileName +" on the server? "+ (f.exists() || fi.exists()));					
						if (f.exists() || fi.exists()){
							outToClient.writeBytes("yes\n");
						}
						else{
							outToClient.writeBytes("no\n");
						}
						break;



					case "recieve file":
						try{
							outToClient.writeBytes("Server Ready\n");
							fileName = inFromClient.readLine();
							System.out.println("Receiving file "+ fileName);					
							outToClient.writeBytes("Server Ready\n");
							outToClient.flush();
							long len = dis.readLong();
							System.out.println("file content is of "+len +" bytes");
							String filePath;
							if (fileName.contains("metadata")){
								filePath = metaDir + fileName;
							}
							else{
								filePath = syncBoxDir + fileName;
							}
							f = new File(filePath);
							FileOutputStream fos = new FileOutputStream(f);
							copy( dis, fos, len);
							fos.close();
							System.out.println(f.length() + " bytes recieved from file "+ f.getName());						
						}
						catch (Exception e){
							e.printStackTrace();
						}

						break;


					case "delete file":
						try{
							outToClient.writeBytes("Server Ready\n");
							fileName = inFromClient.readLine();
							System.out.println(fileName);
							File f1 = new File(syncBoxDir+fileName);

							if (f1.exists()){
								System.out.println("deleting file "+ fileName);
								if(f1.delete()){
									outToClient.writeBytes("Delete Successful\n");
								}else{
									System.out.println("Delete failed");
									outToClient.writeBytes("Delete failed\n");
								}
							}
							else{
								System.out.println("no such file available for delete");
							}
						}
						catch (Exception e){
							e.printStackTrace();
						}
						break;

						//end the connection
					case "quit":
						clientConnected = false;
						outToClient.writeBytes("Ending connection\n");
						clientSocket.close();
						System.out.println("Exiting\n\n");
						serving = false;
						break;
					}
				}
			}

		}
		catch(Exception e){e.printStackTrace();}
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
			long remaining = length - read;
			if (remaining < 8192){
				buf = new byte[(int) remaining];
			}
		}
	}

}

package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;


public class SyncBoxServer {

	/**
	 * 
	 */
	private boolean clientConnected;
	private String syncBoxDir = "testfilesystem\\server\\syncbox\\";
	private String metaDir = "testfilesystem\\server\\";

	DataInputStream dis;
	DataOutputStream outToClient;
	BufferedReader inFromClient;
	ServerSocket welcomeSocket;
	Socket clientSocket;
	public void run (int port) throws Exception{

		boolean serving = true;
		welcomeSocket = new ServerSocket(port);
		while (serving){
			System.out.println("*************");
			System.out.println("Serving...");
			clientSocket = welcomeSocket.accept();
			System.out.println("Client connected");

			outToClient = new DataOutputStream(clientSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			dis = new DataInputStream(clientSocket.getInputStream());
			clientConnected = true;

			while (clientConnected){
				String clientCommand = inFromClient.readLine();
				System.out.println(clientCommand);

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
						sendFile(f);
						System.out.println("File sent");
						outToClient.close();
						clientSocket.close();
						clientConnected = false;
					}
					break;

				case "is on server":
					boolean b = false;
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
						recieveFile(f);
						System.out.println(f.length() + " bytes recieved from file "+ f.getName());
						clientConnected = false;
						clientSocket.close();
						System.out.println("Exiting\n\n");						
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


				case "quit":
					clientConnected = false;
					outToClient.writeBytes("Ending connection\n");
					clientSocket.close();
					System.out.println("Exiting\n\n");
					break;
				}
			}
		}
		welcomeSocket.close();
	}

	public void recieveFile(File targetFile){
		try {
			FileOutputStream fos = new FileOutputStream(targetFile);
			IOUtils.copy(dis, fos);
			IOUtils.closeQuietly(fos);						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(File targetFile){
		try {
			FileInputStream fis = new FileInputStream(targetFile);
			IOUtils.copy(fis, outToClient);
			IOUtils.closeQuietly(fis);						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void disconnect() throws IOException{
		clientConnected = false;
		outToClient.writeBytes("Ending connection\n");
		clientSocket.close();
		System.out.println("Exiting\n\n");
	}
}
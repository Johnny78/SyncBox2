package server;

import java.io.IOException;
import java.security.Security;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * This class is used to start the SyncBox server
 * when a client connects via SSL a new server thread is created 
 * to respond to the clients commands.
 */

public class ServerThread extends Thread{


	public static void main(String[] args) throws IOException{

		Security.addProvider(new Provider());
		//System.setProperty("javax.net.ssl.keyStore","/usr/local/syncbox/syncboxStore.jks");
		System.setProperty("javax.net.ssl.keyStore","syncboxStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword","syncboxpwd");			
		SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket welcomeSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(20661);
		while (true) {	
			try{
				System.out.println("Waiting for a client...");
				SSLSocket clientSocket = (SSLSocket)welcomeSocket.accept();
				new Thread(new SyncBoxServer(clientSocket)).start();
			}catch(Exception e){continue;} //ignore clients attempting to connect with invalid certificates
		}

	}
}

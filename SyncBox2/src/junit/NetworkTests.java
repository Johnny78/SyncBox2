package junit;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import client.ClientControl;
import client.constants.Path;
import client.metadata.FileMetadata;
import client.metadata.MetadataTool;

/**
 * Suite of tests
 * checking send recieve functionality for files and metadata
 * Only works if server is running on same machine as client
 * @author John
 *
 */
public class NetworkTests {
	
	private static final String NAME = "myFile.txt";
	
	public void deleteMetadata(){
		File f;
		f = new File(Path.CLIENT + Path.CLIENT_METADATA);
		f.delete();
		f = new File(Path.CLIENT + Path.DELETED_METADATA);
		f.delete();
		f = new File(Path.CLIENT + Path.SERVER_METADATA);
		f.delete();
		f = new File(Path.TEMP+ Path.SERVER_METADATA);
		f.delete();
		f = new File(Path.TEMP+ Path.DELETED_METADATA);
		f.delete();
		f = new File("testfilesystem\\server\\server-metadata.ser");
		f.delete();
		f = new File("testfilesystem\\server\\deleted-server-metadata.ser");
		f.delete();
		f = new File("testfilesystem\\server\\SyncBox\\"+NAME);
		f.delete();
		f = new File(Path.SYNCBOX+NAME);
		f.delete();
		f = new File(Path.TEMP+NAME);
		f.delete();
	}
	
	public void createTestFile(){
		String content = "This is the file content stuff";
		File file = new File(Path.TEMP+NAME);
		try{
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(Exception e){ e.printStackTrace();}
	}
	
	public void createServerTestFile(){
		String content = "This is the file content stuff";
		File file = new File("testfilesystem\\server\\SyncBox\\"+NAME);
		try{
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(Exception e){ e.printStackTrace();}
	}
	
	public void createMetaFile(){
		FileMetadata fm = new FileMetadata("myFile.txt", "cryptedName.aes");
		ArrayList<FileMetadata> li = new ArrayList<>();
		li.add(fm);
		MetadataTool.writeArray(li, Path.TEMP + Path.SERVER_METADATA);
	}
	
	public void createServerMetaFile(){
		FileMetadata fm = new FileMetadata("myFile.txt", "cryptedName.aes");
		ArrayList<FileMetadata> li = new ArrayList<>();
		li.add(fm);
		MetadataTool.writeArray(li, "testfilesystem\\server\\" + Path.SERVER_METADATA);
	}

	
	@Test
	public void sendFiletest() throws Exception{
		createTestFile();
		ClientControl cc = new ClientControl();
		cc.sendFile(NAME);
		assertTrue(new File(Path.TEMP+NAME).exists() && new File("testfilesystem\\server\\SyncBox\\"+NAME).exists());
		Thread.sleep(100);
		assertEquals(new File(Path.TEMP+NAME).length(), new File("testfilesystem\\server\\SyncBox\\"+NAME).length());
		deleteMetadata();
	}
	
	@Test
	public void recieveFiletest() throws Exception{
		createServerTestFile();
		ClientControl cc = new ClientControl();
		cc.recieveFile(NAME);
		assertTrue(new File(Path.TEMP+NAME).exists() && new File("testfilesystem\\server\\SyncBox\\"+NAME).exists());
		Thread.sleep(100);
		assertEquals(new File(Path.TEMP+NAME).length(), new File("testfilesystem\\server\\SyncBox\\"+NAME).length());
		deleteMetadata();
	}
	
	
	@Test
	public void sendMetatest() throws Exception{
		createMetaFile();
		ClientControl cc = new ClientControl();
		cc.sendFile(Path.SERVER_METADATA);
		Thread.sleep(100);
		assertTrue(new File(Path.TEMP+Path.SERVER_METADATA).exists() && new File("testfilesystem\\server\\"+Path.SERVER_METADATA).exists());
		Thread.sleep(100);
		assertEquals(new File(Path.TEMP+Path.SERVER_METADATA).length(), new File("testfilesystem\\server\\"+Path.SERVER_METADATA).length());
		deleteMetadata();
	}
	
	@Test
	public void recieveMetatest() throws Exception{
		createServerMetaFile();
		ClientControl cc = new ClientControl();
		cc.recieveFile(Path.SERVER_METADATA);
		Thread.sleep(100);
		assertTrue(new File(Path.TEMP+Path.SERVER_METADATA).exists() && new File("testfilesystem\\server\\"+Path.SERVER_METADATA).exists());
		Thread.sleep(100);
		assertEquals(new File(Path.TEMP+Path.SERVER_METADATA).length(), new File("testfilesystem\\server\\"+Path.SERVER_METADATA).length());
		//deleteMetadata();
	}

}

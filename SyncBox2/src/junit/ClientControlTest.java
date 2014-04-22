package junit;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.junit.Test;

import client.ClientControl;
import client.constants.Path;
import client.metadata.FileMetadata;
import client.metadata.MetadataTool;

/**
 * Suite of tests 
 * for checking:
 * encryption, send, recieve and decrypt methods
 * @author John
 *
 */
public class ClientControlTest {
	
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
		f = new File("testfilesystem\\server\\deleted-metadata.ser");
		f.delete();
		f = new File(Path.SYNCBOX+NAME);
		f.delete();
		f = new File(Path.TEMP+"encrypted.test");
		f.delete();
	}
	
	public void createTestFile(){
		String content = "This is the file content stuff";
		File file = new File(Path.SYNCBOX+NAME);
		try{
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(Exception e){ e.printStackTrace();}
	}
	
	@Test
	public void testEncryptMetadata() throws Exception{
		ClientControl cc = new ClientControl();
		cc.initialise();
		cc.encrypt(Path.CLIENT + Path.SERVER_METADATA, Path.SERVER_METADATA, "password".toCharArray());
		cc.encrypt(Path.CLIENT + Path.DELETED_METADATA, Path.DELETED_METADATA, "password".toCharArray());
		File f = new File(Path.TEMP + Path.SERVER_METADATA);
		File f1 = new File(Path.TEMP + Path.DELETED_METADATA);
		assertTrue(f.exists() && f1.exists());
		deleteMetadata();
	}
	
	@Test
	public void testDecryptMetadata() throws Exception{		
		ClientControl cc = new ClientControl();
		cc.initialise();
		cc.encrypt(Path.CLIENT + Path.SERVER_METADATA, Path.SERVER_METADATA, "password".toCharArray());
		cc.decrypt(Path.SERVER_METADATA, Path.CLIENT + Path.SERVER_METADATA, "password".toCharArray());		
		deleteMetadata();
	}
	
	@Test
	public void testEncryptFile() throws Exception{
		ClientControl cc = new ClientControl();
		createTestFile();
		cc.encrypt(Path.SYNCBOX+NAME, "encrypted.test", client.security.PasswordBasedEncryption.genPass());
		File f = new File(Path.TEMP + "encrypted.test");
		assertTrue(f.exists());
		deleteMetadata();
		
	}
	
	@Test
	public void testdecryptFile() throws Exception{
		ClientControl cc = new ClientControl();
		createTestFile();
		SecretKey key = client.security.PasswordBasedEncryption.genPass();
		cc.encrypt(Path.SYNCBOX+NAME, "encrypted.test", key);
		File f = new File(Path.SYNCBOX + NAME);
		cc.decrypt("encrypted.test", Path.SYNCBOX + "myfile2", key);
		System.out.println(f.length());
		System.out.println(new File(Path.SYNCBOX + "myfile2").length());
		assertTrue(f.length() == new File(Path.SYNCBOX +"myfile2").length());
		deleteMetadata();
	}

	
	@Test
	public void testEncryptSendReicieveDecryptMeta() throws Exception{
		ClientControl cc = new ClientControl();
		FileMetadata fm = new FileMetadata("myFile.txt", "cryptedName.aes");
		ArrayList<FileMetadata> li = new ArrayList<>();
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.SERVER_METADATA);
		cc.encrypt(Path.CLIENT + Path.SERVER_METADATA, Path.SERVER_METADATA, "password".toCharArray());
		cc.sendFile(Path.SERVER_METADATA);
		cc.recieveFile(Path.SERVER_METADATA);
		cc.decrypt(Path.SERVER_METADATA, Path.CLIENT + Path.SERVER_METADATA, "password".toCharArray());
		ArrayList<FileMetadata> li1 = MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA);
		assertEquals(li.get(0), li1.get(0));
		deleteMetadata();
	}
	
	@Test
	public void testEncryptSendReicieveDecryptFile() throws Exception{
		createTestFile();
		ClientControl cc = new ClientControl();
		cc.encrypt(Path.SYNCBOX+ NAME, "xyz.aes", "password".toCharArray());
		cc.sendFile("xyz.aes");
		cc.recieveFile("xyz.aes");
		cc.decrypt("xyz.aes", Path.CLIENT+ NAME, "password".toCharArray());
		assertTrue(new File(Path.CLIENT+ NAME).exists() && new File(Path.TEMP + "xyz.aes").exists());
		assertEquals(new File(Path.CLIENT+ NAME).length(), new File(Path.SYNCBOX+ NAME).length());
		deleteMetadata();
	}
}

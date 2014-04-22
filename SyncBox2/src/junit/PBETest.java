package junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.crypto.SecretKey;

import org.junit.Test;

import client.constants.Path;
import client.security.PasswordBasedEncryption;

/**
 * Test Suite
 * testing encrypt decrypt in modes master-password and auto-gen secretkey 
 * @author John
 *
 */
public class PBETest {
	private static final String NAME = "myFile.txt";

	PasswordBasedEncryption pbe;
	@Test
	public void testEncrypt() {
		pbe = new PasswordBasedEncryption("password".toCharArray());
		createTestFile();
		pbe.encrypt(Path.SYNCBOX + NAME, "encrypted.aes");
		assertTrue(new File(Path.TEMP+"encrypted.aes").exists());
		deleteMetadata();
	}

	@Test
	public void testDecrypt() {
		pbe = new PasswordBasedEncryption("password".toCharArray());
		createTestFile();
		pbe.encrypt(Path.SYNCBOX + NAME, "encrypted.aes");
		assertTrue(new File(Path.TEMP+"encrypted.aes").exists());		
		pbe.decrypt("encrypted.aes", Path.CLIENT + NAME);
		assertEquals(new File(Path.CLIENT+NAME).length(), new File(Path.SYNCBOX+NAME).length());
		deleteMetadata();
	}
	
	@Test
	public void testRandomKeyDecrypt() {
		SecretKey key = PasswordBasedEncryption.genPass();
		pbe = new PasswordBasedEncryption(key);
		createTestFile();
		pbe.encrypt(Path.SYNCBOX + NAME, "encrypted.aes");
		assertTrue(new File(Path.TEMP+"encrypted.aes").exists());		
		pbe.decrypt("encrypted.aes", Path.CLIENT + NAME);
		assertEquals(new File(Path.CLIENT+NAME).length(), new File(Path.SYNCBOX+NAME).length());
		deleteMetadata();
	}

	
	public void deleteMetadata(){
		File f;
		f = new File(Path.SYNCBOX+NAME);
		f.delete();
		f = new File(Path.TEMP+"encrypted.aes");
		f.delete();
		f = new File(Path.CLIENT+NAME);
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
}

package client.model.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import client.model.constants.Path;

/**
 * This class implements Password based AES encryption as defined by the RSA PKCS #5
 * as well as Password-Based Key Derivation
 * http://tools.ietf.org/html/rfc2898
 * 
 * 
 * @author John
 *
 */
public class PasswordBasedEncryption {

	PBEKeySpec pbeKeySpec;
	PBEParameterSpec pbeParamSpec;
	SecretKeyFactory keyFac;

	// Salt
	byte[] salt = {
			(byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
			(byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	};
	// Iteration count
	int count = 65536;
	int keySize = 128;

	Cipher pbeCipher;
	SecretKey pbeKey;
	FileInputStream fis;
	FileOutputStream fos;

	/**
	 * constructor given a master password
	 * Use password based derivation function II to make AES key.
	 * used only for the metadata file encryption
	 * @param password
	 */
	public PasswordBasedEncryption(char[] password){
		try{
			keyFac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			pbeKeySpec = new PBEKeySpec(password, salt, count, keySize);
			SecretKey tempKey = keyFac.generateSecret(pbeKeySpec);
			pbeKey = new SecretKeySpec(tempKey.getEncoded(), "AES");
			pbeCipher = Cipher.getInstance("AES");
		}
		catch (Exception e){e.printStackTrace();}
	}
	/**
	 * constructor given a generated AES key
	 * each file has its own AES key to avoid known text attacks
	 * @param key
	 */
	public PasswordBasedEncryption(SecretKey key){
		try{
			pbeKey = key;
		}
		catch (Exception e){e.printStackTrace();}
	}

	public void encrypt(String filePath, String cipherName){
		try{
			File clearFile = new File(filePath);
			fis = new FileInputStream(clearFile);

			pbeCipher = Cipher.getInstance("AES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey);

			CipherInputStream cis = new CipherInputStream(fis, pbeCipher);			
			File cipherFile = new File(cipherName);
			fos = new FileOutputStream(cipherFile);
			int read;
			while((read = cis.read())!=-1)
			{
				fos.write((char)read);
				fos.flush();
			} 
			cis.close();
			fos.close();
			fis.close();
		}
		catch(Exception e ){e.printStackTrace();}
	}

	public void decrypt(String cipherName, String filePath){
		try{

			fis = new FileInputStream(cipherName);			
			File clearFile = new File(filePath);
			fos = new FileOutputStream(clearFile);

			pbeCipher = Cipher.getInstance("AES");
			pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey);
			CipherOutputStream cos = new CipherOutputStream(fos, pbeCipher);			
			int read;
			while((read = fis.read())!=-1)
			{
				cos.write(read);
				cos.flush();
			} 
			cos.close();
			fis.close();
			fos.close();
		}
		catch(Exception e ){e.printStackTrace();}
	}


	/**
	 * Generate secret password used each time a new file needs encrypting
	 * @return
	 */
	public static SecretKey genPass(){
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}

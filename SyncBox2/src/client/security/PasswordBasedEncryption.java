package client.security;

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

import client.constants.Path;

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
	int count = 20;
	
	Cipher pbeCipher;
	SecretKey pbeKey;
	FileInputStream fis;
	FileOutputStream fos;

	/**
	 * constructor given a master password
	 * used only for metadata encryption
	 * @param password
	 */
	public PasswordBasedEncryption(char[] password){
		try{
	// Create PBE parameter set
	pbeParamSpec = new PBEParameterSpec(salt, count);
	pbeKeySpec = new PBEKeySpec(password);
	keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
	pbeKey = keyFac.generateSecret(pbeKeySpec);
	// Create PBE Cipher
	pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");

		}
		catch (Exception e){e.printStackTrace();}
	}
	/**
	 * constructor given an auto-generated password key
	 * used for all files to avoid know text attacks
	 * @param key
	 */
	public PasswordBasedEncryption(SecretKey key){
		try{
	// no params needed
	pbeParamSpec = null;
	pbeKey = key;
	pbeCipher = Cipher.getInstance("AES");
		}
		catch (Exception e){e.printStackTrace();}
	}
	
	public void encrypt(String filePath, String cipherName){
		try{
			File clearFile = new File(filePath);
			fis = new FileInputStream(clearFile);
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
			CipherInputStream cis = new CipherInputStream(fis, pbeCipher);
			
			File cipherFile = new File(Path.TEMP + cipherName);
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
			
			fis = new FileInputStream(Path.TEMP + cipherName);			
			File clearFile = new File(filePath);
			fos = new FileOutputStream(clearFile);
			pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
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
	 * Generate secret password
	 * @return
	 */
	public static SecretKey genPass(){
		KeyGenerator keygen;
		try {
			keygen = KeyGenerator.getInstance("AES");
			return keygen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}

package client.model.security;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
/**
 * Hashing functions
 * @author John
 *
 */
public class HashMd5 
{
	/**
	 * returns the hash of a files contents
	 * @param path
	 * @return
	 */
    public static String generateFileHash(String path)
    {
    	byte[] mdbytes = null;
    	try{
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(path);
 
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        md.update(path.getBytes(), 0, path.getBytes().length);
        mdbytes = md.digest();
        
        fis.close();
    	}
    	catch(Exception e ){
    		e.printStackTrace();
    	}
 
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	return hexString.toString();
    }
    
    /**
     * returns the hash of a string
     * ie hash(filename + hash(fileContent))
     * @param input
     * @return
     */
    public static String generateNameHash(String input)
    {
    	byte[] mdbytes = null;
        MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

        md.update(input.getBytes(), 0, input.getBytes().length);
        mdbytes = md.digest();
 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	return hexString.toString();
    }
}

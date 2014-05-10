package client.model.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.SecretKey;

import client.model.security.*;
import client.model.constants.ActionType;
import client.model.constants.Path;
import client.model.metadata.FileMetadata;
/**
 * This class deals with generating, updating and comparing meta-data
 * @author John
 *
 */
public class MetadataTool {

	/**
	 * compares two lists of meta-data files to find necessary sync actions
	 * @param serverFiles
	 * @param clientFiles
	 * @return
	 */
	public static HashMap<FileMetadata, ActionType> compare(){
		System.out.println("Comparing server and client metadata...");
		ArrayList<FileMetadata> serverFiles = readArray(Path.CLIENT + Path.SERVER_METADATA);
		ArrayList<FileMetadata> clientFiles = readArray(Path.CLIENT + Path.CLIENT_METADATA);
		ArrayList<FileMetadata> deletedFiles = readArray(Path.CLIENT + Path.DELETED_METADATA);
		ArrayList<FileMetadata> seenFiles = readArray(Path.CLIENT + Path.SEEN_METADATA);
		
		HashMap<FileMetadata, ActionType> actions = new HashMap<>();

		for (FileMetadata fm : serverFiles){
			if (clientFiles.contains(fm)){
				actions.put(fm, ActionType.UNCHANGED);
				clientFiles.remove(fm);
				System.out.println("file "+fm.getName()+" is unchanged on the server and client");
			}
			else{
				if (seenFiles.contains(fm)){
					actions.put(fm,  ActionType.DELETEFROMSERVER);
					System.out.println("file "+fm.getName()+" has been deleted from client");
				}
				else{
				actions.put(fm, ActionType.IMPORT);
				System.out.println("file "+fm.getName()+" does not exist on client yet");
				seenFiles.add(fm);
				}
			}
		}
		writeArray(seenFiles, Path.CLIENT + Path.SEEN_METADATA);
		for (FileMetadata fm : clientFiles){
			if (deletedFiles.contains(fm)){
				actions.put(fm,  ActionType.DELETEFROMCLIENT);
				System.out.println("file "+fm.getName()+" has been deleted from the server");
			}
			else{
				actions.put(fm, ActionType.EXPORT);
				System.out.println("file "+fm.getName()+" does not exist on the server yet");
			}
		}
		System.out.println("");
		return actions;
	}

	/**
	 * Writes or overwrites metadata given a list of fileMetadata objects
	 * @param metadata
	 */
	public static void writeArray(ArrayList<FileMetadata> metadata, String path){
		try{
			FileOutputStream fout = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(metadata);
			oos.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * reads client Metadata into a list
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static  ArrayList<FileMetadata> readArray(String path){
		ArrayList<FileMetadata> metadata;
		File md = new File(path);
		if (md.exists()){
		try{
			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fin);
			metadata = (ArrayList<FileMetadata>) ois.readObject();
			ois.close();
			return metadata;
		}catch(Exception ex){
			ex.printStackTrace();
			return null;
		} 
		}
		return new ArrayList<FileMetadata>();
	}


	/**
	 * generates a metadata object from a file
	 * @param fileName
	 */
	public static FileMetadata generateMetadata(String fileName){
		File f = new File(Path.SYNCBOX+fileName);
		String cipherName;
		String hash;
		SecretKey password;
		//get hash
		hash = HashMd5.generateFileHash(f.getAbsolutePath());	
		//make password
		password = PasswordBasedEncryption.genPass();
		//make cipherName
		cipherName = HashMd5.generateNameHash(fileName+hash) + Path.EXTENTION;
		FileMetadata fm = new FileMetadata(fileName, cipherName, hash, password);
		return fm;
	}


	/**
	 * check each file in syncBox for match in seenFiles
	 * if no match make new metadata object and add it to seen files and client metadata
	 */
	public static void updateClientMetadata(){
		System.out.println("Updating client metadata...");
		ArrayList<FileMetadata> seenFiles = readArray(Path.CLIENT + Path.SEEN_METADATA);
		ArrayList<FileMetadata> clientMeta = new ArrayList<FileMetadata>();
					
		File syncBox = new File(Path.SYNCBOX);
		File[] files = syncBox.listFiles();        
		for (File f : files){
			FileMetadata tempFM = new FileMetadata(f.getName(), HashMd5.generateFileHash(f.getAbsolutePath()));
			if (seenFiles.contains(tempFM)){
				clientMeta.add(seenFiles.get(seenFiles.indexOf(tempFM)));
				System.out.println("file "+ seenFiles.get(seenFiles.indexOf(tempFM)).getName()+ " unchanged");
			}
			else{
				FileMetadata fm = generateMetadata(f.getName());
				seenFiles.add(fm);
				clientMeta.add(fm);
				System.out.println("Generating new metadata for file "+fm.getName());
			}			
		}
		writeArray(seenFiles, Path.CLIENT + Path.SEEN_METADATA);
		writeArray(clientMeta, Path.CLIENT + Path.CLIENT_METADATA);
		System.out.println("");
	}

}

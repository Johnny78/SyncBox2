/**
 * 
 */
package junit;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;

import client.constants.ActionType;
import client.constants.Path;
import client.metadata.FileMetadata;
import client.metadata.MetadataTool;

/**
 * Suite of tests for MetadataTool
 * checks functionality for
 * -generating meta data
 * -comparing metadata
 * -updating metadata
 * @author John
 *
 */

public class MetadataToolTest {
	private static final String NAME = "myFile.txt";

	public void deleteTestFiles(){
		File f;
		f = new File(Path.CLIENT + Path.SERVER_METADATA);
		f.delete();
		f = new File(Path.SYNCBOX+NAME);
		f.delete();
		f = new File(Path.SYNCBOX+NAME+"2");
		f.delete();
		f = new File(Path.CLIENT + Path.CLIENT_METADATA);
		f.delete();
		f = new File(Path.CLIENT + Path.DELETED_METADATA);
		f.delete();
	}

	public void createTestFile(){
		String content = "This is the file content";
		File file = new File(Path.SYNCBOX + NAME);
		try{
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}
		catch(Exception e){ e.printStackTrace();}
	}

	public void createTestFile2(){
		String content = "This is the file content";
		File file = new File(Path.SYNCBOX+NAME+"2");
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
	public void testGenerateMetaData() {
		createTestFile();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		assertTrue(fm.getName().equals(NAME));
		deleteTestFiles();
	}

	@Test
	public void testWriteMetaData() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.SERVER_METADATA);
		File f = new File(Path.CLIENT + Path.SERVER_METADATA);
		assertTrue(f.exists());
		deleteTestFiles();
	}

	@Test
	public void testReadMetaData() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.SERVER_METADATA);
		ArrayList<FileMetadata> deserialised = MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA);
		assertEquals(li, deserialised);
		deleteTestFiles();
	}

	@Test
	public void testCompareEqualMetaData() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		ArrayList<FileMetadata> deleted = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.SERVER_METADATA);
		MetadataTool.writeArray(deleted, Path.CLIENT + Path.DELETED_METADATA);
		
		ArrayList<FileMetadata> deserialised = MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA);	
		MetadataTool.writeArray(deserialised, Path.CLIENT + Path.CLIENT_METADATA);
		Map<FileMetadata, ActionType> map = MetadataTool.compare();
		assertTrue(map.get(fm).name().equals("UNCHANGED"));
		deleteTestFiles();
	}

//	@Ignore	//old test
//	public void testCompareDiffMetaData() {
//		createTestFile();
//		createTestFile2();
//		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
//		ArrayList<FileMetadata> li1 = new ArrayList<FileMetadata>();
//		ArrayList<FileMetadata> deleted = new ArrayList<FileMetadata>();
//		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
//		FileMetadata fm1 = MetadataTool.generateMetadata("myFile.txt2");
//		li.add(fm);
//		li1.add(fm1);		
//		Map<FileMetadata, ActionType> map = MetadataTool.compare(li, li1, deleted);
//		assertEquals(map.get(fm).name(), "IMPORT");
//		assertEquals(map.get(fm1).name(), "EXPORT");
//		deleteTestFiles();
//	}

//	@Ignore	//old test
//	public void testCompareDeletedMetaData() {
//		createTestFile();
//		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
//		ArrayList<FileMetadata> li1 = new ArrayList<FileMetadata>();
//		ArrayList<FileMetadata> deleted = new ArrayList<FileMetadata>();
//		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
//		li1.add(fm);	
//		deleted.add(fm);
//		Map<FileMetadata, ActionType> map = MetadataTool.compare(li, li1, deleted);
//		assertEquals(map.get(fm).name(), "DELETEFROMCLIENT");
//		deleteTestFiles();
//	}

	@Test
	public void testUpdateMetaDataNoChange() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.SERVER_METADATA);		
		MetadataTool.updateClientMetadata();
		ArrayList<FileMetadata> result = MetadataTool.readArray(Path.CLIENT + Path.SERVER_METADATA);
		assertEquals(li, result);
		deleteTestFiles();
	}

	@Test
	public void testUpdateMetaDataNewFile() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.CLIENT_METADATA);
		createTestFile2();
		MetadataTool.updateClientMetadata();
		ArrayList<FileMetadata> result = MetadataTool.readArray(Path.CLIENT + Path.CLIENT_METADATA);
		assertEquals(result.size(), 2);
		deleteTestFiles();
	}

	@Test
	public void testUpdateMetaDataDeletedFile() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.CLIENT_METADATA);
		File f = new File(Path.SYNCBOX+NAME);
		f.delete();
		MetadataTool.updateClientMetadata();
		ArrayList<FileMetadata> result = MetadataTool.readArray(Path.CLIENT + Path.CLIENT_METADATA);
		assertEquals(result.size(), 0);
		deleteTestFiles();
	}

	@Test
	public void testUpdateMetaDataModifiedFile() {
		createTestFile();
		ArrayList<FileMetadata> li = new ArrayList<FileMetadata>();
		FileMetadata fm = MetadataTool.generateMetadata("myFile.txt");
		li.add(fm);
		MetadataTool.writeArray(li, Path.CLIENT + Path.CLIENT_METADATA);
		File f = new File(Path.SYNCBOX+NAME);		
		try{

			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("some new file content");
			bw.close();
		}
		catch(Exception e){ e.printStackTrace();}		
		MetadataTool.updateClientMetadata();
		ArrayList<FileMetadata> result = MetadataTool.readArray(Path.CLIENT + Path.CLIENT_METADATA);
		ArrayList<FileMetadata> resultdel = MetadataTool.readArray(Path.CLIENT + Path.DELETED_METADATA);
		System.out.println(resultdel.size());
		assertEquals(result.size(), 1);
		assertFalse(result.get(0).equals(fm));
		deleteTestFiles();
	}

}

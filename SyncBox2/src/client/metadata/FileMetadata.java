package client.metadata;

import java.io.Serializable;

import javax.crypto.SecretKey;

/**
 * This class hold information about files ie meta-data
 * @author John
 *
 */
public class FileMetadata implements Serializable{

	private static final long serialVersionUID = -8879791738942139897L;
	private String name;
	private String CipherName;
	private String hash;
	private SecretKey password;
	
	/**
	 * Constructor
	 * create full metadata object
	 * @param name
	 * @param cipherName
	 * @param hash
	 * @param password
	 */
	public FileMetadata(String name, String cipherName, String hash,
			SecretKey password) {
		super();
		this.name = name;
		CipherName = cipherName;
		this.hash = hash;
		this.password = password;
	}
	
	/**
	 * Constructor
	 * create temporary metadata, just enough to generate a hash(name + hash(contents))
	 * this can then be used to check if the metadata exists already and thus avoid creating
	 * a new secret key.
	 * @param name
	 * @param hash
	 */
	public FileMetadata(String name, String hash) {
		super();
		this.name = name;
		this.hash = hash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCipherName() {
		return CipherName;
	}

	public void setCipherName(String cipherName) {
		CipherName = cipherName;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public SecretKey getPassword() {
		return password;
	}

	public void setPassword(SecretKey password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	/**
	 * equals method 
	 * two meta objects are equal when obj.name = obj1.name
	 * and obj.hash = obj1.hash
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileMetadata other = (FileMetadata) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FileMetadata [name=" + name + ", CipherName=" + CipherName
				+ ", hash=" + hash + ", password=" + password + "]";
	}
	
}

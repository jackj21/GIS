package BE.DS.Index.FeatureName;

import java.util.ArrayList;

/**
 * @author Jack Jiang
 * @version 3/22/21
 * User data type
 */
public class NameEntry implements Hashable<NameEntry> {
	String key; // GIS feature name
	ArrayList<Long> locations; // file offsets of matching records
	String pad = ":";
	
	/** Initialize a new nameEntry object with the given feature name
	* and a single file offset.
	*/
	public NameEntry(String key, Long offset) { 
		
		this.key = key;
		this.locations = new ArrayList<Long>();
		locations.add(offset);
	}
	
	
	/** Return feature name.
	*/
	public String key() { 
		return key;
	}
	
	
	/** Return list of file offsets.
	*/
	public ArrayList<Long> locations() {
		return locations;
	}
	
	
	/** Append a file offset to the existing list.
	*/
	public boolean addLocation(Long offset) { 
		return locations.add(offset);
	}
	
	
	/** Donald Knuth hash function for strings. You MUST use this.
	*/
	public int Hash() {
		int hashValue = key.length();
		for (int i = 0; i < key.length(); i++) {
			hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ key.charAt(i);
		}
		return ( hashValue & 0x0FFFFFFF );
	}
	
	
	/** Two nameEntry objects are considered equal iff they
	* hold the same feature name.
	*/
	public boolean equals(Object other) { 
		NameEntry temp = (NameEntry) other;
		if (other == null)
			return false;
		if (this == other)
			return true;
		
		if (this.getClass().equals(other.getClass())) {
			return this.key().equals(temp.key());			
		}

		return false;
	}
	
	
	/** Return a String representation of the nameEntry object in the
	* format needed for this assignment.
	*/
	public String toString() {
		return ( "[" + this.key + ", " + this.locations.toString() + "]" );
	}
}


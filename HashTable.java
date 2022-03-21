package BE.DS.Index.FeatureName;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Jack Jiang
 * @version 3/23/2021
 * HashTable data structure.
 * Implements a generic chained hash table, using an ArrayList of LinkedLists
 * for the physical table.
 *
 * The ArrayList has a default size of 256 slots, configurable via the class
 * constructor.
 *
 * The size of the ArrayList is doubled when the load factor exceeds the
 * load limit (defaulting to 0.7, but configurable via the class constructor).
 *
 * Elements inserted to the table must implement the Hashable interface:
 *
 * public int Hash();
 *
 * This allows the user to choose an appropriate hash function, rather than
 * being tied to a fixed hash function selected by the table designer.
 */	
public class HashTable<T extends Hashable<T>> {
	private ArrayList< LinkedList<T> > table; // physical basis for the hash table
	private Integer numElements = 0; // number of elements in all the chains
	private Double loadLimit = 1.0; // table resize trigger
	private final Integer defaultTableSize = 256; // default number of table slots
	private int capacity;
	
	/** Constructs an empty hash table with the following properties:
	* Pre:
	* - size is the user's desired number of lots; null for default
	* - ldLimit is user's desired load factor limit for resizing the table;
	* null for the default
	* Post:
	* - table is an ArrayList of size LinkedList objects, 256 slots if
	* size == null
	* - loadLimit is set to default (0.7) if ldLimit == null
	*/
	public HashTable(Integer size, Double ldLimit) { 
		if (size == null) {
			table = new ArrayList<LinkedList<T> >(defaultTableSize);
			this.capacity = defaultTableSize;
		}
		else {
			table = new ArrayList<LinkedList<T> >(size);
			this.capacity = size;
		}
		
		for (int i = 0; i<this.capacity; i++) {
			table.add(i, null);
		}	
		
		
	}
	
	/**
	 * Gets the capacity of the table.
	 * @return Capacity of the table.
	 */
	public final int getCapacity() {
		return this.capacity;
	}
	
	/**
	 */
	public ArrayList<Long> get(int index, String key) {
		ArrayList<Long> locations = new ArrayList<Long>();

		LinkedList<T> curr = table.get(index);
			
		if (curr != null && !curr.isEmpty()) {
			for (int j=0; j<curr.size(); j++) {
				T elem = curr.get(j);
				NameEntry temp = (NameEntry) elem;
				
				if (temp.key.equals(key)) {
					for (int i = 0; i<temp.locations().size(); i++) {
						locations.add(temp.locations().get(i));
						
					}
				
				}
		}
		}
		return locations;
	}
	
	
	
	/** Inserts elem at the front of the elem's home slot, unless that
	* slot already contains a matching element (according to the equals()
	* method for the user's data type.
	* Pre:
	* - elem is a valid user data object
	* Post:
	* - elem is inserted unless it is a duplicate
	* - if the resulting load factor exceeds the load limit, the
	* table is rehashed with the size doubled
	* Returns:
	* true iff elem has been inserted
	*/
	public boolean insert(T elem) { 
		
		double loadFactor = (double)numElements / this.capacity;	
		if (loadFactor >= loadLimit) {
			rehash(table);
		}
		
		int index = elem.Hash() % this.capacity;
		if (table.get(index) == null) {
			table.set(index, new LinkedList<T>());
			table.get(index).add(elem);
			numElements++;
			
			
			
			//System.out.println(elem + " num elemNull: " + numElements);
			return true;
		}
		
		else {
			NameEntry find = (NameEntry) this.find(elem); // find element
			NameEntry temp = (NameEntry) elem; // cast elem to NameEntry
//			LinkedList<T> list = table.get(index);
//			for (int i = 0; i<list.size(); i++) {
//				if (list.get(i).equals(elem)) {
//					list.get(i).
//					return false;
//				}
//			}
			if (find != null) {
				find.locations.add(temp.locations().get(0));
				return false;
			}
			
			
			else {
				table.get(index).add(elem);
				numElements++;
//				double loadFactor = (double)numElements / this.capacity;	
//				if (loadFactor >= loadLimit) {
//					rehash(table);
//				}
				
				//System.out.println(elem + " num elem: " + numElements);
				return true;
				
			} 
		}				
	}
	
	/**
	 * Rehashes table if capacity becomes full
	 */
	private void rehash(ArrayList<LinkedList<T>> table) {
		HashTable<T> temp = new HashTable<T>(2*this.capacity, 1.0);		
		
		for (int i = 0; i < this.numElements; i++) {
			LinkedList<T> curr = this.table.get(i);
			
			if ( curr != null && !curr.isEmpty() ) {
				for (int j = 0; j < curr.size(); j++) {
					T elem = curr.get(j);
					temp.insert(elem);	
				}
			}
		}
		
		this.capacity = temp.capacity;
		this.numElements = temp.numElements;
		this.loadLimit = temp.loadLimit;
		this.table = temp.table;
		
	}
	
	/** Searches the table for an element that matches elem (according to
	* the equals() method for the user's data type).
	* Pre:
	* - elem is a valid user data object
	* Returns:
	* reference to the matching element; null if no match is found
	*/
	public T find(T elem) { 
		int index = elem.Hash() % this.capacity;
		LinkedList<T> curr = table.get(index);
		if (curr != null) {
			for (int i = 0; i<curr.size(); i++) {
				T find = curr.get(i);
				if (elem.equals(find)) {
					return find;
				}
					
			}
		}
		return null;
	}
	
	/** Removes a matching element from the table (according to the equals()
	* method for the user's data type).
	* Pre:
	* - elem is a valid user data object
	* Returns:
	* reference to the matching element; null if no match is found
	
	public T remove(T elem) {  
		 // Not necessary for this assignment
	}*/
	
	
	/** Writes a formatted display of the hash table contents.
	* Pre:
	* - fw is open on an output file
	*/
	public void display(FileWriter fw) throws IOException {
		
		int maxSlotLength = getMaxSlotLength(); // NEED TO CHANGE LATER
		fw.write("Number of elements: " + numElements + "\n");
		fw.write("Number of slots: " + this.capacity + "\n");
		fw.write("Maximum elements in a slot: " + maxSlotLength + "\n");
		fw.write("Load limit: " + loadLimit + "\n");
		fw.write("\n");
	
		fw.write("Slot Contents\n");
		for (int idx = 0; idx < table.size(); idx++) {
	
			LinkedList<T> curr = table.get(idx);
	
			if ( curr != null && !curr.isEmpty() ) {
	
				fw.write(String.format("%5d: %s\n", idx, curr.toString()));
			}
		}
		 // Implementation above conforms to the suggested hash table design; if
		 // you make changes to the class design above, you must supply a display
		 // function that writes the same information as this one.

	}
	
	/**
	 * Gets the maximum number of elements in a slot
	 * for the HashTable.
	 * @return maximum slot length.
	 */
	private int getMaxSlotLength() {
		int max = 0;
		for (int i=0; i<this.capacity-1; i++) {
			LinkedList<T> curr = table.get(i);
			
			if ( curr != null && !curr.isEmpty() ) {
				if (curr.size() > max)
					max = curr.size();
			}
		}
		return max;
		
	}
	
	
}

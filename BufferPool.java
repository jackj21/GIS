package FE.BufferPool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Jack Jiang
 * 	  	   Referenced from https://www.geeksforgeeks.org/design-a-data-structure-for-lru-cache/
 * @version 4/16/2021
 * 
 * Buffer pool for database file to 
 * hold up to 15 records for 
 * search efficiency.
 */
public class BufferPool {
	private LinkedHashMap<Integer, String> pool;
	private final int SIZE;
	
	/**
	 * Constructor that creates a BufferPool object, initiates
	 * the pool that stores data, and sets its size.
	 * 
	 * @param size Number of records the BufferPool can hold.
	 */
	public BufferPool(int size) {
		SIZE = size;
		pool = new LinkedHashMap<Integer, String>(size, 0.75f, true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("rawtypes")
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > SIZE;
			}
		};
	}
	
	/**
	 * Getter method for the contents of the BufferPool.
	 * 
	 * @return The String for the key in the BufferPool.
	 */
	public String get(int key) {
		return pool.get(key);
	}
	
	/**
	 * Adds an element to the BufferPool if not
	 * inside the Pool. If it is, it moves it to 
	 * the front of the Pool.
	 * 
	 * @param elem Element to add to the BufferPool.
	 * 
	 * @return True if rec is in pool
	 * 		   False if rec is not in pool
	 */
	public void set(int key, String rec) {
		if (pool.get(key) != null) {
			pool.remove(key);
			pool.put(key, rec);
			return;
		}
		pool.put(key, rec);
		
		
	}	

	
	/**
	 * Prints the contents of the BufferPool.
	 * 
	 * @param out Output file to print contents of the BufferPool to.
	 */
	public void printPool(FileWriter out) {
		try {
			List<String> list = new ArrayList<String>(pool.values());
			
			out.write("MRU\n");
			for (int i=pool.size()-1; i>=0; i--) {
				String record = list.get(i);
				
				out.write("\t" + record + "\n");
				
				
			}
			out.write("LRU\n");
		} catch (IOException e) {
			System.err.println("Error displaying BufferPool to log file.\n");
		}
	}
}


	
//	/**
//	 * Private helper method to check if a String is in
//	 * the BufferPool.
//	 * 
//	 * @param rec String to check if in the BufferPool.
//	 * 
//	 * @return True if String rec found in pool.
//	 * 		   False if String rec not found in pool.
//	 */
//	private boolean has(String rec) {
//		
//	}


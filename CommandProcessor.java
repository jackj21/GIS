package FE.CMD;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import BE.DS.GISRecord;
import BE.DS.Index.FeatureName.*;
import BE.DS.Index.Coordinate.*;
import FE.BufferPool.*;



/**
 * @author Jack Jiang
 * @version 4/16/2021
 *
 * Processes Commands passed in as Command
 * objects parsed from the script file.
 */
public class CommandProcessor {
	private RandomAccessFile db;
	private long gisDBOffset;
	private Command command;
	private FileWriter log;
	private HashTable<NameEntry> nameIndex;
	private prQuadTree<Point> locationIndex;
	private long westLong, eastLong, southLat, northLat;
	private String pad = ":";
	private int importCheck = 0;
	private BufferPool bp;

	
	/**
	 * Constructor that creates a new CommandProcessor
	 * object with an output file to write to.
	 * 
	 * @param fw Output file to write output to.
	 */
	public CommandProcessor(FileWriter fw, RandomAccessFile gisDB) {
		this.log = fw;
		this.db = gisDB;
		nameIndex = new HashTable<NameEntry>(256, 1.0);
		bp = new BufferPool(15);
		
		
	}
	
	/**
	 * Setter method for the command field so
	 * the CommandProcessor knows what Command 
	 * to process.
	 * 
	 * @param cmd Command to be processed.
	 */
	public void setCommand(Command cmd) {
		this.command = cmd;
	}
	
	/**
	 * Private helper method that converts
	 * a Longitude coordinate in DMS to total seconds.
	 * @param longDMS Longitude in degrees, minutes, and seconds
	 * 				 to be converted.
	 * @return Returns a longitude coordinate in total seconds.
	 */
	private int longToSec(String longDMS) {
		int totalSeconds;
		String degrees = longDMS.substring(0,3);
		String minutes = longDMS.substring(3,5);
		String seconds = longDMS.substring(5,7);
		String direction = longDMS.substring(7);
		
		int deg;
		int min;
		int sec;
		
		if (Integer.parseInt(degrees) < 100) {
			degrees = longDMS.substring(1, 3);
		}
		
		
		if (Integer.parseInt(minutes) < 10) {
			minutes = longDMS.substring(4, 5);
		}
		
		
		if (Integer.parseInt(seconds) < 10) {
			seconds = longDMS.substring(6, 7);
		}
		
		deg = Integer.parseInt(degrees) * 3600;
		min = Integer.parseInt(minutes) * 60;
		sec = Integer.parseInt(seconds);
		totalSeconds = deg + min + sec;
		
		if (direction.equals("W"))
			totalSeconds *= -1;
		
		return totalSeconds;
		
	}
	
	/**
	 * Private helper method that converts
	 * a Latitude coordinate in DMS to total seconds.
	 * @param latDMS Latitude in degrees, minutes, and seconds
	 * 				 to be converted.
	 * @return Returns a latitude coordinate in total seconds.
	 */
	private int latToSec(String latDMS) {
		int totalSeconds;
		String degrees = latDMS.substring(0,2);
		String minutes = latDMS.substring(2,4);
		String seconds = latDMS.substring(4,6);
		String direction = latDMS.substring(6);

		int deg;
		int min;
		int sec;
		
		if (Integer.parseInt(minutes) < 10) {
			minutes = latDMS.substring(3, 4);
		}
		
		if (Integer.parseInt(seconds) < 10) {
			seconds = latDMS.substring(5, 6);
		}
		
		deg = Integer.parseInt(degrees) * 3600;
		min = Integer.parseInt(minutes) * 60;
		sec = Integer.parseInt(seconds);
		totalSeconds = deg + min + sec;
		
		if (direction.contains("W"))
			totalSeconds *= -1;
		
		return totalSeconds;
	}
	
	/**
	 * Processes the world command, setting up the
	 * boundaries of the coordinate space.
	 */
	public void processWorld() {
		ArrayList<String> instruc = command.getInstruct();
		
		String west = instruc.get(0);
		String east = instruc.get(1);
		String south = instruc.get(2);
		String north = instruc.get(3);
		
		// converts coordinates to total seconds
		this.westLong = longToSec(west);
		this.eastLong = longToSec(east);
		this.southLat = latToSec(south);
		this.northLat = latToSec(north);
		
		// Creates location index using a prQuadTree
		this.locationIndex = new prQuadTree<Point>(westLong, eastLong, southLat, northLat);
		
		try {
			// writes output in formatted manner
			log.write("\t\t\t\t" + (int)northLat + "\n");
			log.write("\t" + (int)westLong + "\t\t\t\t\t" + (int)eastLong + "\n");
			log.write("\t\t\t\t" + (int)southLat + "\n");
		} catch (IOException e) {
			System.err.println("Could not write world coordinates to log file.\n");
		}
	}
	
	/**
	 * Adds all the valid GIS records into the
	 * GIS database file and then builds the feature
	 * name index and location index and logs 
	 * the number of entries added to each index
	 * and the longest probe sequence needed when 
	 * inserting to the feature name index.
	 * 
	 * Note: A valid record is one that lies within
	 * the specified world boundaries.
	 */
	public void processImport() {
		
		ArrayList<String> instruc = command.getInstruct();
		int numFeatures = 0;		// keep track of number of features
		int numLocations = 0;		// keep track of number of locations
		int nameLength = 0;			// keep track of total length of feature names
		try {
			
			RandomAccessFile gisFile = new RandomAccessFile(instruc.get(0), "r");
			//RandomAccessFile db = new RandomAccessFile(gisDB, "rw");
			if (importCheck == 0)
				db.setLength(0); 	// truncates GIS database file
			// headers
			String gisRecHeader = gisFile.readLine();
			GISRecord header = new GISRecord(gisRecHeader);
			db.write(header.getRecord().getBytes());
			
			while (true) {
				String gisRec = gisFile.readLine();
				
				if (gisRec == null) break; 
				
				GISRecord record = new GISRecord(gisRec);
				db.writeBytes(System.getProperty("line.separator"));
				gisDBOffset = db.getFilePointer();			// get GIS record offset
				db.write(record.getRecord().getBytes());	// write GIS record to database file
				
				
				
				// Store records into HashTable and keep count
				// But first check if record is in world				
				
				// Check if coordinates exist
				if (record.getLat() == null || record.getLon() == null)
					continue;
				
				if (record.inWorld(this.westLong, this.eastLong, this.southLat, this.northLat)) {
					// create NameEntry object for GIS record
					//System.out.println("Record: " + record.getFeatureName() + "\n");
					
					// Create name and make new NameEntry object and insert into table
					String key = record.getFeatureName() + pad + record.getStateAbbrev();
					NameEntry feature = new NameEntry(key, gisDBOffset);
					this.nameIndex.insert(feature);
					nameLength += record.getFeatureName().length();
					numFeatures++;
					
					if (record.getLongSeconds() == -1 || record.getLatSeconds() == -1)
						continue;
					Point loc = new Point((long)record.getLongSeconds(), (long)record.getLatSeconds());
					loc.addOffset(gisDBOffset);
					boolean ins = locationIndex.insert(loc);
					
					if (ins) {
						numLocations++;
					}
//					System.out.println("Insert: " + ins);
//					System.out.println("Location: " + loc.toString() + numLocations);
//					System.out.println("Num Features: " + numFeatures);
//					
//					total++;
//					System.out.println("Total: " + total);
//					System.out.println("numLocations: " + numLocations);
//					System.out.println("nameLength: " + nameLength);
				}
				continue;	
			}

			// Store records into prQuadTree and keep count
			
			nameLength /= numFeatures;
			
			// Update the number of imports and write to output
			log.write("Imported Features by name: " + numFeatures + "\n");
			log.write("Imported Locations:        " + numLocations + "\n");
			log.write("Average name length:       " + nameLength + "\n");
			
			
			//db.close();
			gisFile.close();

		} catch (FileNotFoundException e) {
			System.err.println("Database file could not be created.\n");
		} catch (IOException e) {
			System.err.println("Could not find GIS file.\n");
		}
		importCheck = 1;
	}
	
	/**
	 * Processes the debug keyword with different
	 * cases for Strings "quad", "hash", and "pool."
	 * 
	 * "quad": Prints the locationIndex prQuadTree in a readable manner.
	 * "hash": Prints the nameIndex HashTable in a readable manner.
	 * "pool": Prints the contents of the BufferPool in a readable manner.
	 */
	public void processDebug() {
		// print PR quadtree
		if (command.getInstruct().get(0).equals("quad")) {
			try {
				locationIndex.display(log);
			} catch (IOException e) {
				System.err.println("Error printing out quadtree for debug.\n");
			}		
		}
		
		// print HashTable
		if (command.getInstruct().get(0).equals("hash")) {
			try {
				nameIndex.display(log);
			} catch (IOException e) {
				System.err.println("Cannot display Feature Name Index.\n");
			}
		}
		
		// print BufferPool
		if (command.getInstruct().get(0).equals("pool")) {
			bp.printPool(log);
		}
		
	}
	
	/**
	 * For every GIS record in the database file that
	 * matches the given geographic coordinate, logs the 
	 * offset at which the record was found, and the feature
	 * name, county name, and state abbreviation.
	 */
	public void processWhatIsAt() {
		try {
			//RandomAccessFile gis = new RandomAccessFile(gisDB, "r");
			
			ArrayList<String> instruct = command.getInstruct();
			String lat= instruct.get(0);
			String lon = instruct.get(1);
			
			long latDMS = getLatSeconds(lat);
			long lonDMS = getLongSeconds(lon);
			int searchCount = 0;
			
			lat = lat(lat);
			lon = lon(lon);
			
			log.write("\tThe following features were found at (" + lon + ", " +
					 lat + ")\n");
			
			// Find points inside the PR quadtree
			ArrayList<Point> loc = locationIndex.findBox(westLong, eastLong, southLat, northLat);
			
			// Iterate through points found and write them to output in formatted manner
			for (int i=0; i<loc.size(); i++) {
				if (loc.get(i).getX() == lonDMS && loc.get(i).getY() == latDMS) {
					Point p = loc.get(i);
					ArrayList<Long> offsets = p.getOffset();
					for (int j=0; j<offsets.size(); j++) {
						db.seek(offsets.get(j));
						String record = db.readLine();						
						String[] recs = record.split("\\|");
						String featName = recs[1];
						String countyName = recs[5];
						String stateAbb = recs[3];
						
						String out = offsets.get(j) + ":\t" + featName + "\t" + countyName + "\t" + 
								stateAbb + "\n";
						
						String.format("%15s", out);
						log.write("\t" + out);
						searchCount++;
					}
				}
			}
			if (searchCount == 0) {
				log.write("\tNothing was found at (" + lon + 
								", " + lat + ")\n");
			}
			
			//gis.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find GIS record file.\n");
		} catch (IOException e) {
			System.err.println("Could not seek to GIs record file location.\n");
		}
	}
	
	/**
	 * Finds GIS records that matches the given
	 * feature name and state abbreviation and 
	 * logs the offset at which the record was 
	 * found and the county name, primary latitude,
	 * and the primary longitude.
	 */
	public void processWhatIs() {
		try {
			//RandomAccessFile gis = new RandomAccessFile(gisDB, "r");
			
			// Get instructions to search for GIS record
			ArrayList<String> elem = command.getInstruct();
			String featureName = "";
			String stateAbbrev = command.getInstruct().get(elem.size()-1);
			
			// Format feature name to use in Hash function for HashTable search
			for (int i=0; i<elem.size()-2; i++) {
				featureName += elem.get(i).toString();
				featureName += " ";
			}
			featureName += elem.get(elem.size()-2);
						
			String key = featureName + pad + stateAbbrev;
			int tableSlot = Hash(key) % nameIndex.getCapacity();
			
			// Check if GIS record is stored in BufferPool
			if (bp.get(tableSlot) != null) {
				String record = bp.get(tableSlot);
				String[] recsHalf = record.split(":");
				String[] recs = recsHalf[1].split("\\|");
				
				String offset = recsHalf[0];
				String countyName = recs[5];
				String lat = lat(recs[7]);
				String lon = lon(recs[8]);
				
				String out = offset + ": " + countyName + "  (" + lon + ", " + lat + ")\n";
				log.write("\t" + out);
				return;
			}
			
			// If no records found, log output...
			ArrayList<Long> locations = nameIndex.get(tableSlot, key);
			if (locations.isEmpty()) {
				log.write("No record matches " + featureName + " and " + stateAbbrev + "\n");
				//gis.close();
				return;
			}
			
			// *****************DEBUGGING********************
			//System.out.println(featureName + " Location: " + locations);
			
			// Get the records found from the search
			for (int i=0; i<locations.size(); i++) {
				long fileOffset = locations.get(i);
				
				db.seek(fileOffset);
				String record = db.readLine();
				String[] recs = record.split("\\|");
				String countyName = recs[5];
//				
//				if (records[2] != null || !records[5].equals("Unknown")) {
//					gis.close();
//					return;
//				}
				String latDMS = recs[7];
				String longDMS = recs[8];
				String latClean = lat(latDMS);
				String longClean = lon(longDMS);
				String out = fileOffset + ":  " + countyName + "  (" + longClean + 
						", " + latClean + ")\n";
				
				// Add Record to BufferPool
				String bpRec = fileOffset + ":\t" + record;
				bp.set(tableSlot, bpRec); 	
				
				log.write("\t" + out); 
			}
			
			
			
			
			
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not find GIS record file.\n");
		} catch (IOException e) {
			System.err.println("Could not seek to GIs record file location.\n");
		}
	}
	
	/**
	 * Processes the what_is_in command. 
	 * For every GIS record in the db file whose
	 * coordinates fall within the closed rectangle with
	 * the specified height and width, centered at 
	 * the specified geographic coordinate, the offset is logged
	 * at which the record was found and feature name, state name, and 
	 * primary latitude and primary longitude.
	 * 
	 * Note: Half-height and half-width are specified as seconds.
	 */
	public void processWhatIsIn() {	
		try {
			//RandomAccessFile db = new RandomAccessFile(gisDB, "r");
			
			// 
			ArrayList<String> instruct = command.getInstruct();
			ArrayList<Point> loc;
			String lat= instruct.get(0);
			String lon = instruct.get(1);
			int height = Integer.parseInt(instruct.get(2));
			int width = Integer.parseInt(instruct.get(3));
			
			long latDMS = getLatSeconds(lat);
			long lonDMS = getLongSeconds(lon);
			
			loc = locationIndex.findBox(lonDMS-width, lonDMS+width, latDMS-height, latDMS+height);
						
			String latClean = lat(lat);
			String lonClean = lon(lon);
			
			if (loc.size() == 0) {
				log.write("\tNothing was found in (" + 
						lonClean + " +/- " + width + ", " + latClean + " +/- " + height + ")\n");
				return;
			}
			
			log.write("\tThe following " + loc.size() + " features were found in (" + 
					lonClean + " +/- " + width + ", " + latClean + " +/- " + height + ")\n");
			
			for (int i=0; i<loc.size(); i++) {
				Point temp = loc.get(i);
				for (int j=0; j<temp.getOffset().size(); j++) {
					long off = temp.getOffset().get(j);
					db.seek(off);
					String record = db.readLine();
					String[] recs = record.split("\\|");
					String featName = recs[1];
					String stateName = recs[3];
					String recsLat = recs[7];
					String recsLon = recs[8];
					recsLat = lat(recsLat);
					recsLon = lon(recsLon);
					
					String offset = String.format("%6o%c ", off, ':');
					String out = offset + featName + "\t" + stateName + "\t" +
							"(" + recsLon + ", " + recsLat + ")\n";
					
						
					
					log.write("\t" + out);
					
				}
			}
			
			//db.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find GIS database file when processing what_is_in.\n");
		} catch (IOException e) {
			System.err.println("Error closing GIS database file when processing what_is_in.\n");
		}
	}
	
	/**
	 * Private hash function for feature name and state abbreviation
	 * to locate a record in the nameIndex HashTable.
	 * 
	 * @param name The feature name of the GIS record.
	 * @param stateAbbrev The state abbreviation of the GIs record.
	 * 
	 * @return An index of the nameIndex HashTable for where the
	 * 		   GIS record is located.
	 */
	private int Hash(String key) {


		int hashValue = key.length();
		for (int i = 0; i < key.length(); i++) {
			hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ key.charAt(i);
		}
		return ( hashValue & 0x0FFFFFFF );
	}
	
	/**
	 * Gets the total seconds for the latitude of the record.
	 * 
	 * @return Total seconds of latitude of record.
	 */
	private long getLatSeconds(String latDMS) {
		if (latDMS == null) return -1;
		
		String degrees = latDMS.substring(0,2);
		String minutes = latDMS.substring(2,4);
		String seconds = latDMS.substring(4,6);
		String direction = latDMS.substring(6);

		long totalSeconds;
		long deg;
		long min;
		long sec;
		
		if (Long.parseLong(minutes) < 10) {
			minutes = latDMS.substring(3, 4);
		}
		
		if (Long.parseLong(seconds) < 10) {
			seconds = latDMS.substring(5, 6);
		}
		
		deg = Long.parseLong(degrees) * 3600;
		min = Long.parseLong(minutes) * 60;
		sec = Long.parseLong(seconds);
		totalSeconds = deg + min + sec;
		
		if (direction.equals("S"))
			totalSeconds *= -1;
		
		return totalSeconds;
	}
	

	/**
	 * Gets the total seconds for the longitude of the record.
	 * 
	 * @return Total seconds of longitude of record.
	 */
	private long getLongSeconds(String longDMS) {
		if (longDMS == null) return -1;
		
		String degrees = longDMS.substring(0,3);
		String minutes = longDMS.substring(3,5);
		String seconds = longDMS.substring(5,7);
		String direction = longDMS.substring(7);
		
		long totalSeconds; 
		long deg;
		long min;
		long sec;
		
		if (Long.parseLong(degrees) < 100) {
			degrees = longDMS.substring(1, 3);
		}
		
		if (Long.parseLong(minutes) < 10) {
			minutes = longDMS.substring(4, 5);
		}
		
		if (Long.parseLong(seconds) < 10) {
			seconds = longDMS.substring(6, 7);
		}
		
		deg = Long.parseLong(degrees) * 3600;
		min = Long.parseLong(minutes) * 60;
		sec = Long.parseLong(seconds);
		totalSeconds = deg + min + sec;
		
		if (direction.equals("W"))
			totalSeconds *= -1;
		
		return totalSeconds;
	}
	
	/**
	 * Private helper method to convert latitude to
	 * readable format.
	 * 
	 * @return Latitude in a formatted manner.
	 */
	private String lat(String latDMS) {
		String degrees = latDMS.substring(0,2);
		String minutes = latDMS.substring(2,4);
		if (Integer.parseInt(minutes) < 10) {
			minutes = latDMS.substring(3, 4);
		}
		String seconds = latDMS.substring(4,6);
		if (Integer.parseInt(seconds) < 10) {
			seconds = latDMS.substring(5, 6);
		}
		String direction = latDMS.substring(6);
		String dir = "";
		if (direction.equals("N")) dir = "North";
		
		if (direction.equals("S")) dir = "South";
		StringBuilder sb = new StringBuilder();
		sb.append(degrees + "d ");
		sb.append(minutes + "m ");
		sb.append(seconds + "s ");
		sb.append(dir );
		return sb.toString();
		
		
	}
	
	/**
	 * Private helper method to convert longitude to 
	 * readable format.
	 * 
	 * @return Longitude in a formatted manner.
	 */
	private String lon(String longDMS) {
		String degrees = longDMS.substring(0,3);
		if (Integer.parseInt(degrees) < 100) {
			degrees = longDMS.substring(1, 3);
		}
		
		String minutes = longDMS.substring(3,5);
		if (Integer.parseInt(minutes) < 10) {
			minutes = longDMS.substring(4, 5);
		}
		
		String seconds = longDMS.substring(5,7);
		if (Integer.parseInt(seconds) < 10) {
			seconds = longDMS.substring(6, 7);
		}
		String direction = longDMS.substring(7);
		String dir = "";
		if (direction.equals("W")) dir = "West";
		
		if (direction.equals("E")) dir = "East";
		
		StringBuilder sb = new StringBuilder();
		sb.append(degrees + "d ");
		sb.append(minutes + "m ");
		sb.append(seconds + "s ");
		sb.append(dir );
		return sb.toString();

	}
}

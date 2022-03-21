package BE.DS;


/**
 * @author Jack Jiang
 * @version 4/16/2021
 * 
 * Data type for GIS records that has
 * the ability to return different features
 * of the record and check if it is within
 * the world boundaries.
 */
public class GISRecord {
	private String record;
	private String[] rec;
	private long offset;
	
	/**
	 * Constructor that creates a GISRecord
	 * object and assigns a GIS record
	 * as a string passed in from a GIS
	 * data file to its field and splits
	 * it by the pipe symbol | as a delimiter.
	 * 
	 * @param gis GIS record.
	 */
	public GISRecord(String gis) {
		this.record = gis;
		this.rec = record.split("\\|");
	}
	
	/**
	 * Setter method for offset.
	 * 
	 * @param offset Offset of the GIS record.
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	/**
	 * Getter method for offset.
	 * 
	 * @return The offset of the GIS record.
	 */
	public long getOffset() {
		return offset;
	}
	
	/**
	 * Getter method for GIS record.
	 * 
	 * @return The whole GIS record as a String.
	 */
	public String getRecord() {
		return this.record;
	}
	
	/**
	 * Processes the GIS record String and
	 * gets the feature name of the record.
	 * 
	 * @return The feature name of the GIS record.
	 */
	public String getFeatureName() {
		return rec[1];
		
	}
	
	/**
	 * Gets the state abbreviation of the GIS record.
	 * 
	 * @return State abbreviation of GIs record.
	 */
	public String getStateAbbrev() {
		return rec[3];
	}
		
	// get locations
	
	/**
	 * Gets the latitude of the GIS record.
	 * 
	 * @return Latitude of GIS record.
	 */
	public String getLat() {
		if (rec[7].equals("") || rec[7] == null || rec[7].equals("Unknown")) {
			return null;
		}
		
		return rec[7];
	}
	
	/**
	 * Gets the longitude of the GIS record.
	 * 
	 * @return Longitude of GIS record.
	 */
	public String getLon() {
		if (rec[8].equals("") || rec[8] == null || rec[8].equals("Unknown")) {
			return null;
		}
		
		return rec[8];
	}
	
	
	/**
	 * Gets the total seconds for the latitude of the record.
	 * 
	 * @return Total seconds of latitude of record.
	 */
	public float getLatSeconds() {
		String latDMS = getLat();
		if (latDMS == null) return -1;
		
		String degrees = latDMS.substring(0,2);
		String minutes = latDMS.substring(2,4);
		String seconds = latDMS.substring(4,6);
		String direction = latDMS.substring(6);

		float totalSeconds;
		float deg;
		float min;
		float sec;
		
		if (Float.parseFloat(minutes) < 10) {
			minutes = latDMS.substring(3, 4);
		}
		
		if (Float.parseFloat(seconds) < 10) {
			seconds = latDMS.substring(5, 6);
		}
		
		deg = Float.parseFloat(degrees) * 3600;
		min = Float.parseFloat(minutes) * 60;
		sec = Float.parseFloat(seconds);
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
	public float getLongSeconds() {
		String floatDMS = getLon();
		if (floatDMS == null) return -1;
		
		String degrees = floatDMS.substring(0,3);
		String minutes = floatDMS.substring(3,5);
		String seconds = floatDMS.substring(5,7);
		String direction = floatDMS.substring(7);
		
		float totalSeconds; 
		float deg;
		float min;
		float sec;
		
		if (Float.parseFloat(degrees) < 100) {
			degrees = floatDMS.substring(1, 3);
		}
		
		if (Float.parseFloat(minutes) < 10) {
			minutes = floatDMS.substring(4, 5);
		}
		
		if (Float.parseFloat(seconds) < 10) {
			seconds = floatDMS.substring(6, 7);
		}
		
		deg = Float.parseFloat(degrees) * 3600;
		min = Float.parseFloat(minutes) * 60;
		sec = Float.parseFloat(seconds);
		totalSeconds = deg + min + sec;
		
		if (direction.equals("W"))
			totalSeconds *= -1;
		
		return totalSeconds;
	}
	
	/**
	 * Checks if record is in world boundaries.
	 * 
	 * @param xMinWorld West longitude boundary.
	 * @param xMaxWorld East longitude boundary.
	 * @param yMinWorld South latitude boundary.
	 * @param yMaxWorld North latitude boundary.
	 * 
	 * @return True if record is in world.
	 * 		   False otherwise.
	 */
	public boolean inWorld(float xMinWorld, 
						float xMaxWorld, float yMinWorld, float yMaxWorld) {

		float longSec = this.getLongSeconds(); 	// x value
		float latSec = this.getLatSeconds();		// y value
		
		if (longSec >= xMinWorld && longSec <= xMaxWorld 
				&& latSec >= yMinWorld && latSec <= yMaxWorld) {
			return true;
		}
		
		return false;
	}
}

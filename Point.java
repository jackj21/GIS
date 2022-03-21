package BE.DS.Index.Coordinate;

import java.util.ArrayList;

public class Point implements Compare2D<Point> {

	private long xcoord;
	private long ycoord;
	private ArrayList<Long> offsets;
	
	public Point() {
		xcoord = 0;
		ycoord = 0;
	}
	public Point(long x, long y) {
		xcoord = x;
		ycoord = y;
		offsets = new ArrayList<Long>();
	}
	
	// For the following methods, let P designate the Point object on which
	// the method is invoked (e.g., P.getX()).

    // Reporter methods for the coordinates of P.
	
	/**
	 * Getter method for x coordinate.
	 * 
	 * @return X coordinate.
	 */
	public long getX() {
		return xcoord;
	}
	
	/**
	 * Getter method for y coordinate.
	 * 
	 * @return Y coordinate.
	 */
	public long getY() {
		return ycoord;
	}
	
	/**
	 * Getter method for offsets.
	 * 
	 * @return Offsets of point.
	 */
	public ArrayList<Long> getOffset() {
		return offsets;
	}
	
	/**
	 * Adds an offset to Point object.
	 * 
	 * @param offset Offset to add to ArrayList of Point object.
	 */
	public void addOffset(long offset) {
		offsets.add(offset);
	}
	
	// Determines which quadrant of the region centered at P the point (X, Y),
	// consistent with the relevent diagram in the project specification;
	// returns NODQUADRANT if P and (X, Y) are the same point.
	public Direction directionFrom(long X, long Y) {
		long x = this.getX();
		long y = this.getY();
		long xDiff = x - X;
		long yDiff = y - Y;
		if (xDiff == 0 && yDiff == 0)
			return Direction.NOQUADRANT;
		
		long yAvg = Y;
		long xAvg = X;
		
		
		if ((this.getY() >= yAvg && this.getX() > xAvg) || 
				this.getX() == 0 && this.getY() == 0)
				return Direction.NE;
			else if (this.getY() < yAvg && this.getX() >= xAvg)
				return Direction.SE;
			else if (this.getY() > yAvg && this.getX() <= xAvg)
				return Direction.NW;
			else
				return Direction.SW;
		
		
	}
	
	// Determines which quadrant of the specified region P lies in,
	// consistent with the relevent diagram in the project specification;
	// returns NOQUADRANT if P does not lie in the region. 
	public Direction inQuadrant(double xLo, double xHi, double yLo, double yHi) {
		double xAvg = (xLo + xHi) / 2;
		double yAvg = (yLo + yHi) / 2;
		if (this.inBox(xLo, xHi, yLo, yHi)) {
			if ((this.getY() >= yAvg && this.getX() > xAvg) || 
				this.getX() == 0 && this.getY() == 0)
				return Direction.NE;
			else if (this.getY() < yAvg && this.getX() >= xAvg)
				return Direction.SE;
			else if (this.getY() > yAvg && this.getX() <= xAvg)
				return Direction.NW;
			else
				return Direction.SW;
				
		}
        return Direction.NOQUADRANT;
	}
	
	// Returns true iff P lies in the specified region.
	public boolean inBox(double xLo, double xHi, double yLo, double yHi) {
        if (this.getX() >= xLo && this.getX() <= xHi && this.getY() >= yLo && this.getY() <= yHi)
        	return true;
		return false;
	}
	
    // Returns a String representation of P.
	public String toString() {
		
		return new String("[(" + xcoord + ", " + ycoord + "), " + offsets.toString().replace("[", "").replace("]", "") + "]");
	}
	
	// Returns true iff P and o specify the same point.
	public boolean equals(Object o) {
		Point temp = (Point) o;
		if (this.directionFrom(temp.getX(), temp.getY()) != Direction.NOQUADRANT) 
			return false;
		if (o == null)
			return false;
		if (!this.getClass().equals(o.getClass()) )
			return false;
		Point handle = (Point) o;
		if (this != handle)
			return false;
		return true;
	}
}

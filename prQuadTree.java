// On my honor:
//
// - I have not discussed the Java language code in my program with
// anyone other than my instructor or the teaching assistants
// assigned to this course.
//
// - I have not used Java language code obtained from another student,
// or any other unauthorized source, including the Internet, either
// modified or unmodified.
//
// - If any Java language code or documentation used in my program
// was obtained from another source, such as a text book or course
// notes, that has been clearly noted with a proper citation in
// the comments of my program.
//
// - I have not designed this program in such a way as to defeat or
// interfere with the normal operation of the grading code.
//
// Jack Jiang
// jackj21

package BE.DS.Index.Coordinate;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;


public class prQuadTree< T extends Compare2D<? super T> > {
	
	abstract class prQuadNode {
	}
	class prQuadLeaf extends prQuadNode {
		public ArrayList<T> Elements;
		
	}
   class prQuadInternal extends prQuadNode {
    	public prQuadNode NW, SW, SE, NE;
   }
    
   prQuadNode root;
   float xMin, xMax, yMin, yMax;
   int key;
   private final static int buckets = 4;
   // Initialize quadtree to empty state.
   public prQuadTree(float xMin, float xMax, float yMin, float yMax) {
	   this.xMin = xMin;
	   this.xMax = xMax;
	   this.yMin = yMin;
	   this.yMax = yMax;
	
   }
    
   // Pre:   elem != null
   // Post:  If elem lies within the tree's region, and elem is not already 
   //        present in the tree, elem has been inserted into the tree.
   // Return true iff elem is inserted into the tree. 
   public boolean insert(T elem) {
	  if (elem.getX() < this.xMin || elem.getX() > this.xMax || 
		  elem.getY() < this.yMin || elem.getY() > this.yMax)
		  return false;
	  key = 0;
	  root = insertHelper(root, elem, xMin, xMax, yMin, yMax);
	  if (key == 1) 
		  return false;

	  return true;
	   
	   // return false for duplicates
   }
    

   /**
    * Private helper method for insert for cases 4-6
    * @param sRoot		where the inserting begins
    * @param elem		element to be inserted
    * @param xMin		minimum X value of insertion
    * @param xMax		maximum X value of insertion
    * @param yMin		minimum Y value of insertion
    * @param yMax		maximum Y value of insertion
    * @return		returns where root is pointing to
    */
   @SuppressWarnings("unchecked")	// suppress warnings for casting
   private prQuadNode insertHelper(prQuadNode sRoot, T elem, double xMin, double xMax,
		   double yMin, double yMax) {
		   // Base case for recursion (Case 3)


		   // Base case for recursion (Case 3)
		   if (sRoot == null) {
			   prQuadLeaf leaf = new prQuadLeaf();
			   leaf.Elements = new ArrayList<T>();
			   leaf.Elements.add(elem);
			   sRoot = leaf;
			   return sRoot;
			   
		   }
		   double xAvg = (xMin + xMax) / 2;
		   double yAvg = (yMin + yMax) / 2;
		   prQuadInternal ret = null;
		   // Case 4: Current node during descent is a leaf node 
		   // and bucket is not full.
		   // 4a: Coordinate is duplicate
		   if (sRoot.getClass().equals(prQuadLeaf.class)) {
			   prQuadLeaf temp = (prQuadLeaf) sRoot;
			   // For bucket size of 4...if bucket is full
			   for (int i=0; i < temp.Elements.size(); i++) {
				   if (temp.Elements.get(i).getX() == elem.getX() && temp.Elements.get(i).getY() == elem.getY()) {
					   temp.Elements.get(i).getOffset().add(elem.getOffset().get(0));
					   return sRoot;
				   }
			   }
//			   if (temp.Elements.contains(elem)) {// elem in ArrayList?
//				   key = 1;
//				   return sRoot;
//			   }
			   if (temp.Elements.size() < buckets) {
				   temp.Elements.add(elem);
				   return temp;
			   }
			   
			   prQuadInternal in = new prQuadInternal();
			   for (int i=0; i <temp.Elements.size(); i++) {
				   prQuadLeaf guide;
				   Direction dir = temp.Elements.get(i).inQuadrant(xMin, xMax, yMin, yMax);
				   switch (dir) {
				   case NE:
					   if (in.NE == null) {
						   prQuadLeaf inNE = new prQuadLeaf();
						   inNE.Elements = new ArrayList<T>();
						   inNE.Elements.add(elem);
						   break;
					   }
					   guide = (prQuadLeaf) in.NE;
					   if (guide.Elements.size() < buckets) {
						   guide.Elements.add(elem);
					   }
					   break;
				   case NW:
					   if (in.NW == null) {
						   prQuadLeaf inNW = new prQuadLeaf();
						   inNW.Elements = new ArrayList<T>();
						   inNW.Elements.add(elem);
						   break;
					   }
					   guide = (prQuadLeaf) in.NW;
					   if (guide.Elements.size() < buckets) {
						   guide.Elements.add(elem);
					   }
					   break; 
				   
			   case SW:
				   if (in.SW == null) {
					   prQuadLeaf inSW = new prQuadLeaf();
					   inSW.Elements = new ArrayList<T>();
					   inSW.Elements.add(elem);
					   break;
				   }
				   guide = (prQuadLeaf) in.SW;
				   if (guide.Elements.size() < buckets) {
					   guide.Elements.add(elem);
				   }
				   break; 
			   case SE:
				   if (in.SE == null) {
					   prQuadLeaf inSE = new prQuadLeaf();
					   inSE.Elements = new ArrayList<T>();
					   inSE.Elements.add(elem);
					   break;
				   }
				   guide = (prQuadLeaf) in.SE;
				   if (guide.Elements.size() < buckets) {
					   guide.Elements.add(elem);
				   }
				   break; 
			   case NOQUADRANT:
				   System.out.println("NOQUADRANT returned when partitioning.\n");
				   System.exit(-1);
				   break;
				   }   
			   }
			   ret = in;
			   sRoot = ret;
			   Direction sq = elem.inQuadrant(xMin, xMax, yMin, yMax);

			   
			   switch(sq) {
			   case NE:
				   if (in.NE == null) {
					   prQuadLeaf neLeaf = new prQuadLeaf();
					   neLeaf.Elements = new ArrayList<T>();
					   neLeaf.Elements.add(elem);
					   in.NE = neLeaf;
					   break;
				   }
				   else {
					   in.NE = insertHelper(in.NE, elem, xAvg, xMax, yAvg, yMax);
					   break;
				   }
			   case NW:
				   if (in.NW == null) {
					   prQuadLeaf nwLeaf = new prQuadLeaf();
					   nwLeaf.Elements = new ArrayList<T>();
					   nwLeaf.Elements.add(elem);
					   in.NW = nwLeaf;
					   break;
				   }
				   else {
					   in.NW = insertHelper(in.NW, elem, xMin, xAvg, yAvg, yMax);
					   break;
				   }
			   case SW:
				   if (in.SW == null) {
					   prQuadLeaf swLeaf = new prQuadLeaf();
					   swLeaf.Elements = new ArrayList<T>();
					   swLeaf.Elements.add(elem);
					   in.SW = swLeaf;
					   break;
				   }
				   else {
					   in.SW = insertHelper(in.SW, elem, xMin, xAvg, yMin, yAvg);
					   break;
				   }
			   case SE:
				   if (in.SE == null) {
					   prQuadLeaf seLeaf = new prQuadLeaf();
					   seLeaf.Elements = new ArrayList<T>();
					   seLeaf.Elements.add(elem);
					   in.SE = seLeaf;
					   break;
				   }
				   else {
					   in.SE = insertHelper(in.SE, elem, xAvg, xMax, yMin, yAvg);
					   break;
				   }
			   case NOQUADRANT:
				   System.out.println("NOQUADRANT in partitioning returned.\n");
				   System.exit(-2);
				   break;
			   }
			   return ret;
		   }
		   

		   // Case 6: Current node is an internal node
		   if (sRoot.getClass().equals(prQuadInternal.class)) {
			   Direction sq = elem.inQuadrant(xMin, xMax, yMin, yMax);
			   prQuadInternal rootInt = (prQuadInternal) sRoot;
			   
			   switch(sq) {
			   case NE:
					   rootInt.NE = insertHelper(rootInt.NE, elem, xAvg, xMax, yAvg, yMax);
					   break;
			   case NW:
					   rootInt.NW = insertHelper(rootInt.NW, elem, xMin, xAvg, yAvg, yMax);
					   break;
	
			   case SW:
					   rootInt.SW = insertHelper(rootInt.SW, elem, xMin, xAvg, yMin, yAvg);
					   break;
			   case SE:
					   rootInt.SE = insertHelper(rootInt.SE, elem, xAvg, xMax, yMin, yAvg);
					   break;
			   case NOQUADRANT:
				   System.out.println("NOQUADRANT in prQuadInternal class returned.\n");
				   System.exit(-3);
				   break;
			   }
			   return sRoot;
		   }
		   return ret;
   } 
	   


   // Pre:  elem != null
   // Returns reference to an element x within the tree such that elem.equals(x)
   // is true, provided such a matching element occurs within the tree; returns 
   // null otherwise.
   public T find(T Elem) {
	   return findHelper(root, Elem);   
   }
   
   /**
    * Recursive helper function for find. 
    * Traverses through the prQuadTree to look
    * for element.
    * 
    * @param 	sRoot root node
    * @param 	elem  element to search for
    * @return	returns reference to element being searched for
    * 		  	and null if not found
    */
   private T findHelper(prQuadNode sRoot, T elem) {
	   float centerX = (xMin + xMax) / 2;
	   float centerY = (yMin + yMax) /2;
	   if (sRoot == null)
		   return null;
	   //long elemX = elem.getX();
	   //long elemY = elem.getY();
	   Direction quad = elem.directionFrom((long)centerX, (long)centerY);
	   if (sRoot.getClass().equals(prQuadLeaf.class)) {
		   @SuppressWarnings("unchecked")
		   prQuadLeaf temp = (prQuadLeaf) sRoot;
		   ArrayList<T> collect = temp.Elements;
		   for (int i=0; i<collect.size(); i++) {
			   T local = collect.get(i);
			   if (elem.getX() == local.getX() && elem.getY() == local.getY()) {
				   elem = collect.get(i);
			   }

		   }
		   return elem;
		   
	   }
	   else {
		   @SuppressWarnings("unchecked")
		   prQuadInternal temp = (prQuadInternal) sRoot;
		   switch (quad) {
		   case NE:
			   return findHelper(temp.NE, elem);  
		   case NW:
			   return findHelper(temp.NW, elem);
		   case SW:
			   return findHelper(temp.SW, elem);
			   
		   case SE:
			   return findHelper(temp.SE, elem);
		   case NOQUADRANT:
			   return null; 
		   }
		   return null;
	   }
	   
	   
   }
   public void display(FileWriter Log) throws IOException {
       
       if ( root == null ) {
          Log.write("Tree is empty.\n");
          return;
       }
       displayHelper(root, "", Log);
    }
    
    @SuppressWarnings("unchecked")
    public void displayHelper(prQuadNode sRoot, String Padding, FileWriter Out) throws IOException {

          // Check for empty leaf
          if ( sRoot == null ) {
             Out.write(Padding + "*\n");
             return;
          }
          // Check for and process SW and SE subtrees
          if ( sRoot.getClass().equals(prQuadInternal.class) ) {
             prQuadInternal p = (prQuadInternal) sRoot;
             displayHelper(p.SW, Padding + "   ", Out);
             displayHelper(p.SE, Padding + "   ", Out);
          }
          // Display indentation padding for current node
          Out.write(Padding);

          // Determine if at leaf or internal and display accordingly
          if ( sRoot.getClass().equals(prQuadLeaf.class) ) {
             prQuadLeaf p = (prQuadLeaf) sRoot;
             for (int pos = 0; pos < p.Elements.size(); pos++) {
                Out.write( p.Elements.get(pos) + " " );
             }
             Out.write("\n");
          }
          else
             Out.write( "@\n" );
          
          // Check for and process NE and NW subtrees
          if ( sRoot.getClass().equals(prQuadInternal.class)) {
             prQuadInternal p = (prQuadInternal) sRoot;
             displayHelper(p.NE, Padding + "   ", Out);
             displayHelper(p.NW, Padding + "   ", Out);
          }
    }
   
   
//   public void printTree(FileWriter Out) {
//	   try {
//		   if (root == null )
//			   Out.write("Empty tree.\n" );
//		   else {
//			   printTreeHelper(Out, root, "");
//		   }
//	   }
//	   catch ( IOException e ) {
//		   return;
//	   }
//   }
//
//	@SuppressWarnings({ "unchecked" })
//	public void printTreeHelper(FileWriter Out, prQuadNode sRoot, String Padding) {
//		String pad = "   ";
//		try {
//			// Check for empty leaf
//			if ( sRoot == null ) {
//				Out.write(Padding + "*\n");
//				return;
//			}
//			// Check for and process SW and SE subtrees
//			if ( sRoot.getClass().equals(prQuadInternal.class) ) {
//				prQuadInternal p = (prQuadInternal) sRoot;
//				printTreeHelper(Out, p.SW, Padding + pad);
//				printTreeHelper(Out, p.SE, Padding + pad);
//			}
//			// Display indentation padding for current node
//			//Out.write(Padding);
//
//			// Determine if at leaf or internal and display accordingly
//			if ( sRoot.getClass().equals(prQuadLeaf.class) ) {
//				prQuadLeaf p = (prQuadLeaf) sRoot;
//				for (int pos = 0; pos < p.Elements.size(); pos++) {
//					Out.write(Padding + p.Elements.get(pos) + "\n" );
//				}
//			}
//			else if ( sRoot.getClass().equals(prQuadInternal.class) )
//				Out.write(Padding + "@\n" );
//			else
//				Out.write(sRoot.getClass().getName() + "#\n");
//
//			// Check for and process NE and NW subtrees
//			if ( sRoot.getClass().equals(prQuadInternal.class) ) {
//				prQuadInternal p = (prQuadInternal) sRoot;
//				printTreeHelper(Out, p.NE, Padding + pad);
//				printTreeHelper(Out, p.NW, Padding + pad);
//			}
//		}
//		catch ( IOException e ) {
//			return;
//		}
//	}
   

   // Pre:  xLo, xHi, yLo and yHi define a rectangular region
   // Returns a collection of (references to) all elements x such that x is in
   // the tree and x lies at coordinates within the defined rectangular region,
   // including the boundary of the region.
   public ArrayList<T> findBox(float xLo, float xHi, float yLo, float yHi) {
	   
	   ArrayList<T> collection = new ArrayList<T>();
	   
	   findBoxHelper(root, collection, xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi);
	   

	   return collection;
   }
   
   /**
    * Helper method for region search to traverse
    * QuadTree in specific region of tree.
    * @param sRoot		node to start traversal at
    * @param xLo		minimum value of x for search
    * @param xHi		maximum value of x for search
    * @param yLo		minimum value of y for search
    * @param yHi		maximum value of y for search
    * @return		returns pointer to node for traversal
    * 				of QuadTree
    */
   @SuppressWarnings("unchecked")
   private void findBoxHelper(prQuadNode sRoot, ArrayList<T> list, float xMin, float xMax, 
		   				float yMin, float yMax, float xLo, float xHi, float yLo, float yHi) {
	   // need region search coordinates
	   // in box as base case
	   // make some functions to do calculations...return true/false and call it 4 times
	   if (sRoot.getClass().equals(prQuadLeaf.class)) {
		   prQuadLeaf guide = (prQuadLeaf) sRoot;
		   
		   ArrayList<T> collect = guide.Elements;
		   for (int i=0; i<collect.size(); i++) {
			   T local = collect.get(i);
			   if (local.inBox(xLo, xHi, yLo, yHi)) {
				   list.add(local);
			   }

		   }
		   
//		   T elem = guide.Elements.get(0);
//		   if (elem.inBox(xLo, xHi, yLo, yHi)) {
//			   list.add(elem);
//			   
//		   }
		   
	   } 
	   
	   else {
		   prQuadInternal intern = (prQuadInternal) sRoot;
		   if (inBox(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
			   
			   float xAvg = (xMin + xMax) / 2;
			   float yAvg = (yMin + yMax) / 2;
			   if (inNERegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.NE != null) {
					   findBoxHelper(intern.NE, list, xAvg, xMax, yAvg, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inNWRegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.NW != null) {
					   findBoxHelper(intern.NW, list, xMin, xAvg, yAvg, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inSWRegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.SW != null) {
					   findBoxHelper(intern.SW, list, xMin, xAvg, yMin, yAvg, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inSERegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.SE != null) {
					   findBoxHelper(intern.SE, list, xAvg, xMax, yMin, yAvg, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   return;
		   }
		   
			
		   else {
			   // search region bigger than quadtree
			 
			   if (inNERegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.NE != null) {
					   findBoxHelper(intern.NE, list, xMin, xMax, yMin, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inNWRegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.NW != null) {
					   findBoxHelper(intern.NW, list, xMin, xMax, yMin, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inSWRegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.SW != null) {
					   findBoxHelper(intern.SW, list, xMin, xMax, yMin, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
			   if (inSERegion(xMin, xMax, yMin, yMax, xLo, xHi, yLo, yHi)) {
				   if (intern.SE != null) {
					   findBoxHelper(intern.SE, list, xMin, xMax, yMin, yMax, xLo,
							   		xHi, yLo, yHi);							   
				   }
			   }
			   
		   }
		   return;
	   }
   }
   
   // Private helper to check if region search is in box
   private boolean inBox(float xMin,float xMax, float yMin,
		   			float yMax, float xLo, float xHi, float yLo, float yHi) {
       if (xLo >= xMin && xLo <= xMax && yHi >= yMin && yHi <= yMax)
    	   return true;
       if (xHi >= xMin && xHi <= xMax && yHi >= yMin && yHi <= yMax)
    	   return true;
       if (xHi >= xMin && xHi <= xMax && yLo >= yMin && yLo <= yMax)
    	   return true;
       if (xLo >= xMin && xLo <= xMax && yLo >= yMin && yLo <= yMax)
    	   return true;
       return false;
	}
   
   // Private helper to check if search region contains NE quadrant
   private boolean inNERegion(float xMin, float xMax, float yMin, float yMax, float xLo, 
		   float xHi, float yLo, float yHi) {
	   float xAvg = (xMin + xMax) / 2;
	   float yAvg = (yMin + yMax) / 2;
	   if ((xHi - xAvg > 0) && (yHi - yAvg >= 0) || (xLo - xAvg == 0 && yLo - yAvg == 0)) {
		   return true;
	   }
	   return false;
   }
   
   // Private helper to check if search region contains NW quadrant
   private boolean inNWRegion(float xMin, float xMax, float yMin, float yMax, float xLo, 
		   float xHi, float yLo, float yHi) {
	   float xAvg = (xMin + xMax) / 2;
	   float yAvg = (yMin + yMax) / 2;
	   if ((xLo - xAvg <= 0) && (yHi - yAvg > 0)) {
		   return true;
	   }
	   return false;
   }
   
   // Private helper to check if search region contains SW quadrant
   private boolean inSWRegion(float xMin, float xMax, float yMin, float yMax, float xLo, 
		   float xHi, float yLo, float yHi) {
	   float xAvg = (xMin + xMax) / 2;
	   float yAvg = (yMin + yMax) / 2;
	   if ((xLo - xAvg < 0) && (yLo - yAvg <= 0)) {
		   return true;
	   }
	   return false;
   }
   
   // Private helper to check if search region contains SE quadrant
   private boolean inSERegion(float xMin, float xMax, float yMin, float yMax, float xLo, 
		   float xHi, float yLo, float yHi) {
	   float xAvg = (xMin + xMax) / 2;
	   float yAvg = (yMin + yMax) / 2;
	   if ((xHi - xAvg >= 0) && (yLo - yAvg < 0)) {
		   return true;
	   }
	   return false;
   }
}


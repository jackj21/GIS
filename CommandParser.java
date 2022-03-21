package FE.CMD;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

/**
 * @author Jack Jiang
 * @version 4/17/2021
 * 
 * Parses Commands received as Command
 * objects from the script file.
 */
public class CommandParser {
	private RandomAccessFile script; 	// Script file
	private long offset = 0;
	
	/**
	 * Constructor that creates a new CommandParser object
	 * and contains a script file to be read and 
	 * its starting offset.
	 * 
	 * @param script Script file containing commands.
	 * @param offset Starting offset for database file.
	 */
	public CommandParser(RandomAccessFile raf, long offset) {
		this.script = raf;
		this.offset = offset;		
	}
	
	/**
	 * Moves file pointer for script file.
	 * @param findOffset Offset to move file pointer to.
	 */
	public void setOffset(long findOffset) {
		this.offset = findOffset;
	
	}
	
	/**
	 * Parses commands from script file
	 * and returns the parsed command as 
	 * a newly created Command object.
	 * 
	 * @return Parsed command from script file as Command object.
	 */
	public Command parseCommand() {
		Command cmd = null;
		String line = "null";
		
		try {
			script.seek(this.offset);
			line = script.readLine();
			
			while (line.contains(";") || line.equals("")) {
				line = script.readLine();
			}
			
			offset = script.getFilePointer();
			
			Scanner scanner = new Scanner(line);
			String com = scanner.next();
			cmd = new Command(com);
			
			while (true) {
			if (!scanner.hasNext()) {
				break;
			}
			
			String name = scanner.next();
			cmd.addInstruct(name);
			
			
			}
			
			scanner.close();
			
		} catch (IOException e) {
			System.err.println("Error parsing command.\n");
		}
		
		return cmd;
	}
	
}

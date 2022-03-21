package FE.CMD;

import java.util.ArrayList;

/**
 * @author Jack Jiang
 * @version 4/16/21
 * 
 * Data type for commands from script file
 * that contains the command keyword and
 * the specified instruction.
 */
public class Command {
	private String command;		// command keyword
	private ArrayList<String> instruction;		// instruction
	
	/**
	 * Constructor that creates a Command object
	 * and takes the command key word
	 * and a specified instruction as arguments
	 * and stores them as fields upon creation.
	 * 
	 * @param cmd Command keyword.
	 * @param instruc Instruction.
	 */
	public Command(String cmd) {	
		this.command = cmd;
		this.instruction = new ArrayList<String>();
	}
	
	/*
	 * Returns the whole command formatted
	 * to be echoed in an output file.
	 * 
	 * @return A formatted String for a Command
	 */
	public String getCommand() {
		if (instruction.size() <= 2)
			return this.command + "\t" + this.instruction.toString().replace(",","").replace("[", "").replace("]", "");
		return formatInstruction();
	}
	
	private String formatInstruction() {
		String inst = this.command + " ";
		for (int i=0; i<this.instruction.size()-1; i++) {
			inst += this.instruction.get(i);
			inst += " ";
		}
		
		inst += this.instruction.get(this.instruction.size()-1);
		
		return inst;
	}
	
	/**
	 * Returns the command keyword field.
	 * 
	 * @return The private command field of the Command object.
	 */
	public String getCom() {
		return this.command;
	}
	
	/**
	 * Adds instruction to the ArrayList of instructions.
	 * @param i Extra instructions to be added.
	 */
	public void addInstruct(String i) {
		this.instruction.add(i);
	}
	
	/*
	 * Returns the action/instruction field.
	 * 
	 * @return The private name field of the Command object.
	 */
	public ArrayList<String> getInstruct() {
		return this.instruction;
	}
	
	
	
	
}

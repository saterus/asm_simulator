package edu.osu.cse.mmxi.machine.interpreter.instructions;

import edu.osu.cse.mmxi.machine.Machine;

public interface Instruction {

	/**
	 * Executes an instruction on the Machine.
	 * 
	 * @param m
	 *            The machine upon which to execute the instruction.
	 * @return true if the executed instruction modified a general purpose
	 *         register.
	 */
	public boolean execute(Machine m);

}

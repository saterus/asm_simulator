package edu.osu.cse.mmxi.sim.machine.interpreter;

import java.util.Map;

import edu.osu.cse.mmxi.sim.machine.Machine;

public interface ALU {

    public void executeNextInstruction(short s);

    public String readInstruction(short inst);

    public String readInstructionAt(short mem);

    public String readInstruction(short inst, Machine context, Map<String, Short> symb);

}

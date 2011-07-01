package edu.osu.cse.mmxi.machine.interpreter;

public interface ALU {

    public void executeNextInstruction(short s);

    public String readInstruction(short inst);

    public String readInstructionAt(short mem);

}

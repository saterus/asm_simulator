package edu.osu.cse.mmxi.machine.interpreter;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction;

public class Interpreter implements ALU {
    public Machine m;

    public Interpreter(final Machine _m) {
        m = _m;
    }

    @Override
    public String executeNextInstruction(final short s) {
        final Instruction i = InstructionParser.parseInstruction(m.getMemory(s));
        i.execute(m);
        return i.toString();
    }
}

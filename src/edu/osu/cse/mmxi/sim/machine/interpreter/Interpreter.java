package edu.osu.cse.mmxi.sim.machine.interpreter;

import java.util.Map;

import edu.osu.cse.mmxi.sim.machine.Machine;
import edu.osu.cse.mmxi.sim.machine.interpreter.instructions.Instruction;

public class Interpreter implements ALU {
    public Machine m;

    public Interpreter(final Machine _m) {
        m = _m;
    }

    @Override
    public void executeNextInstruction(final short s) {
        final Instruction i = InstructionParser.parseInstruction(m.getMemory(s));
        i.execute(m);
    }

    @Override
    public String readInstruction(final short inst) {
        final Instruction i = InstructionParser.parseInstruction(inst);
        return i.toString();
    }

    @Override
    public String readInstructionAt(final short mem) {
        return readInstruction(m.getMemory(mem));
    }

    @Override
    public String readInstruction(final short inst, final Machine context,
        final Map<String, Short> symb) {
        final Instruction i = InstructionParser.parseInstruction(inst);
        return i.toString(context, symb);
    }
}

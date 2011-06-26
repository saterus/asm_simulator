package edu.osu.cse.mmxi.machine.interpreter;

import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction;

public class InstructionParser {
    public static final byte ADD = 1, AND = 5, BRx = 0, DBUG = 8, JSR = 4, JSRR = 12,
        LD = 2, LDI = 10, LDR = 6, LEA = 14, NOT = 9, RET = 13, ST = 3, STI = 11,
        STR = 7, TRAP = 15;

    public static Instruction parseInstruction(final short inst) {
        switch (inst >> 12 & 0xf) {
        case BRx:
            return new Instruction.BRx(inst >> 9 & 0x7, inst & 0x1ff);
        case ADD:
            if ((inst & 0x20) == 0)
                return new Instruction.ADD(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
            else
                return new Instruction.ADDimm(inst >> 9 & 0x7, inst >> 6 & 0x7,
                    inst & 0x1f);
        case LD:
            return new Instruction.LD(inst >> 9 & 0x7, inst & 0x1ff);
        case ST:
            return new Instruction.ST(inst >> 9 & 0x7, inst & 0x1ff);
        case JSR:
            return new Instruction.JSR((inst & 0x800) != 0, inst & 0x1ff);
        case AND:
            if ((inst & 0x20) == 0)
                return new Instruction.AND(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
            else
                return new Instruction.ANDimm(inst >> 9 & 0x7, inst >> 6 & 0x7,
                    inst & 0x1f);
        case LDR:
            return new Instruction.LDR(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f);
        case STR:
            return new Instruction.STR(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f);
        case DBUG:
            return new Instruction.DBUG();
        case NOT:
            return new Instruction.NOT(inst >> 9 & 0x7, inst >> 6 & 0x7);
        case LDI:
            return new Instruction.LDI(inst >> 9 & 0x7, inst & 0x1ff);
        case STI:
            return new Instruction.STI(inst >> 9 & 0x7, inst & 0x1ff);
        case JSRR:
            return new Instruction.JSRR((inst & 0x800) != 0, inst >> 6 & 0x7, inst & 0x3f);
        case RET:
            return new Instruction.RET();
        case LEA:
            return new Instruction.LEA(inst >> 9 & 0x7, inst & 0x1ff);
        case TRAP:
            return new Instruction.TRAP(inst & 0xff);
        default:
            return null; // impossible; here for eclipse's benefit
        }
    }
}

package edu.osu.cse.mmxi.sim.machine.interpreter;

import edu.osu.cse.mmxi.sim.machine.interpreter.instructions.Instruction;

/**
 * <p>
 * InstructionParser turns a 16-bit word into an Instruction that the Interpreter can
 * execute on the Machine.
 * </p>
 * 
 * <p>
 * Instructions are based on the most significant 4 bits in the raw instruction. Since we
 * have exactly 16 instructions, there is no combination of 4 bits which leads to an
 * invalid Instruction. This greatly simplifies the InstructionParser since there is no
 * possibility that the Parser fails to produce an Instruction (though it may not be the
 * instruction intended by the user).
 * </p>
 * 
 * <table width='100%'>
 * <tr style='text-align:left;border-bottom:1px solid black;background-color:#CCCCFF;'>
 * <th>Binary</th>
 * <th>Hex</th>
 * <th>Instruction</th>
 * <th>Name</th>
 * </tr>
 * <tr>
 * <td>0000</td>
 * <td>0x0</td>
 * <td>BRX</td>
 * <td>Branch NZP</td>
 * </tr>
 * <tr>
 * <td>0001</td>
 * <td>0x1</td>
 * <td>ADD</td>
 * <td>Addition</td>
 * </tr>
 * <tr>
 * <td>0010</td>
 * <td>0x2</td>
 * <td>LD</td>
 * <td>Load (Direct Addressing)</td>
 * </tr>
 * <tr>
 * <td>0011</td>
 * <td>0x3</td>
 * <td>ST</td>
 * <td>Store (Direct Addressing)</td>
 * </tr>
 * <tr>
 * <td>0100</td>
 * <td>0x4</td>
 * <td>JSR</td>
 * <td>Jump (Page + 9-bit offset)</td>
 * </tr>
 * <tr>
 * <td>0101</td>
 * <td>0x5</td>
 * <td>AND</td>
 * <td>Bitwise AND</td>
 * </tr>
 * <tr>
 * <td>0110</td>
 * <td>0x6</td>
 * <td>LDR</td>
 * <td>Load Register</td>
 * </tr>
 * <tr>
 * <td>0111</td>
 * <td>0x7</td>
 * <td>STR</td>
 * <td>Store Register</td>
 * </tr>
 * <tr>
 * <td>1000</td>
 * <td>0x8</td>
 * <td>DBUG</td>
 * <td>Debug</td>
 * </tr>
 * <tr>
 * <td>1001</td>
 * <td>0x9</td>
 * <td>NOT</td>
 * <td>Bitwise Negation</td>
 * </tr>
 * <tr>
 * <td>1010</td>
 * <td>0xA</td>
 * <td>LDI</td>
 * <td>Load (Indirect Addressing)</td>
 * </tr>
 * <tr>
 * <td>1011</td>
 * <td>0xB</td>
 * <td>STI</td>
 * <td>Store (Indirect Addressing)</td>
 * </tr>
 * <tr>
 * <td>1100</td>
 * <td>0xC</td>
 * <td>JSRR</td>
 * <td>Jump (Base Register + 6-bit offset)</td>
 * </tr>
 * <tr>
 * <td>1101</td>
 * <td>0xD</td>
 * <td>RET</td>
 * <td>Return from Subroutine</td>
 * </tr>
 * <tr>
 * <td>1110</td>
 * <td>0xE</td>
 * <td>LEA</td>
 * <td>Load Effective Address</td>
 * </tr>
 * <tr>
 * <td>1111</td>
 * <td>0xF</td>
 * <td>TRAP</td>
 * <td>Trap Vector Call</td>
 * </tr>
 * </table>
 */
public class InstructionParser {

    public static final byte ADD = 1, AND = 5, BRx = 0, DBUG = 8, JSR = 4, JSRR = 12,
        LD = 2, LDI = 10, LDR = 6, LEA = 14, NOT = 9, RET = 13, ST = 3, STI = 11,
        STR = 7, TRAP = 15;

    public static Instruction parseInstruction(final short inst) {
        switch (getBits(inst, 12, 4)) {
        case BRx:
            return new Instruction.BRx(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case ADD:
            if (getBits(inst, 5, 1) == 0)
                return new Instruction.ADD(getBits(inst, 9, 3), getBits(inst, 6, 3),
                    getBits(inst, 0, 3));
            else
                return new Instruction.ADDimm(getBits(inst, 9, 3), getBits(inst, 6, 3),
                    getBits(inst, 0, 5));
        case LD:
            return new Instruction.LD(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case ST:
            return new Instruction.ST(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case JSR:
            return new Instruction.JSR(getBits(inst, 11, 1) != 0, getBits(inst, 0, 9));
        case AND:
            if (getBits(inst, 5, 1) == 0)
                return new Instruction.AND(getBits(inst, 9, 3), getBits(inst, 6, 3),
                    getBits(inst, 0, 3));
            else
                return new Instruction.ANDimm(getBits(inst, 9, 3), getBits(inst, 6, 3),
                    getBits(inst, 0, 5));
        case LDR:
            return new Instruction.LDR(getBits(inst, 9, 3), getBits(inst, 6, 3), getBits(
                inst, 0, 6));
        case STR:
            return new Instruction.STR(getBits(inst, 9, 3), getBits(inst, 6, 3), getBits(
                inst, 0, 6));
        case DBUG:
            return new Instruction.DBUG();
        case NOT:
            return new Instruction.NOT(getBits(inst, 9, 3), getBits(inst, 6, 3));
        case LDI:
            return new Instruction.LDI(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case STI:
            return new Instruction.STI(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case JSRR:
            return new Instruction.JSRR(getBits(inst, 11, 1) != 0, getBits(inst, 6, 3),
                getBits(inst, 0, 6));
        case RET:
            return new Instruction.RET();
        case LEA:
            return new Instruction.LEA(getBits(inst, 9, 3), getBits(inst, 0, 9));
        case TRAP:
            return new Instruction.TRAP(getBits(inst, 0, 8));
        default:
            return null; // impossible; here for eclipse's benefit
        }
    }

    private static int getBits(final int dat, final int begin, final int len) {
        return dat >> begin & (1 << len) - 1;
    }
}

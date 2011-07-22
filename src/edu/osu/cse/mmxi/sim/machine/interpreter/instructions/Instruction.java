package edu.osu.cse.mmxi.sim.machine.interpreter.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.sim.Simulator;
import edu.osu.cse.mmxi.sim.machine.Machine;
import edu.osu.cse.mmxi.sim.ui.Error;
import edu.osu.cse.mmxi.sim.ui.ErrorCodes;

public abstract class Instruction {

    /**
     * Executes an instruction on the Machine.
     * 
     * @param m
     *            The machine upon which to execute the instruction.
     * @return true if the executed instruction modified a general purpose register.
     */
    public abstract boolean execute(Machine m);

    @Override
    public String toString() {
        return toString(null, null);
    }

    public abstract String toString(Machine context, Map<String, Short> symb);

    /**
     * Adds two registers and writes to a third.
     * 
     * The ADD instruction has two modes: register-based and immediate (see {@link ADDimm}
     * ). In register mode, the contents of two registers, {@code sr1} and {@code sr2},
     * are added and written to a destination register ({@code dr}). The syntax of the
     * command is {@code 0001dddsss0xxttt}, where {@code sss}, {@code ttt}, and
     * {@code ddd} represent the three-bit register indexes {@code sr1}, {@code sr2}, and
     * {@code dr}, respectively, and the {@code x}s represent "don't care" bits.
     */
    public static class ADD extends Instruction {
        private final byte dr, sr1, sr2;

        public ADD(final int _dr, final int _sr1, final int _sr2) {
            dr = (byte) _dr;
            sr1 = (byte) _sr1;
            sr2 = (byte) _sr2;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                (short) (m.getRegister(sr1).getValue() + m.getRegister(sr2).getValue()));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (sr1 == sr2) {
                if (dr == sr1)
                    return "DBL R" + dr;
                else
                    return "DBL R" + dr + ", R" + sr1;
            } else if (dr == sr1)
                return "INC R" + dr + ", R" + sr2;
            else if (dr == sr2)
                return "INC R" + dr + ", R" + sr1;
            else
                return "ADD R" + dr + ", R" + sr1 + ", R" + sr2;
        }
    }

    /**
     * Adds a register to an immediate operand and writes to another register.
     * 
     * The ADD instruction has two modes: register-based (see {@link ADD}) and immediate.
     * In immediate mode, the contents of a register, {@code sr}, and a 5-bit signed 2's
     * complement integer, {@code imm}, are added and written to a destination register (
     * {@code dr}). The syntax of the command is {@code 0001dddsss1iiiii}, where
     * {@code sss} and {@code ddd} represent the three-bit register indexes {@code sr} and
     * {@code dr}, respectively, and {@code iiiii} represents the immediate operand.
     */
    public static class ADDimm extends Instruction {
        private final byte dr, sr;
        private final int  imm;

        public ADDimm(final int _dr, final int _sr, final int _imm) {
            dr = (byte) _dr;
            sr = (byte) _sr;
            imm = _imm << 27 >> 27; // sign extend imm
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue((short) (m.getRegister(sr).getValue() + imm));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (dr == sr) {
                if (imm < 0) {
                    if (imm == -1)
                        return "DEC R" + dr;
                    else
                        return "DEC R" + dr + ", #" + -imm;
                } else if (imm == 1)
                    return "INC R" + dr;
                else
                    return "INC R" + dr + ", #" + imm;
            } else
                return "ADD R" + dr + ", R" + sr + ", #" + imm;
        }
    }

    /**
     * Takes the bitwise AND of two registers and writes to a third.
     * 
     * The AND instruction has two modes: register-based and immediate (see {@link ANDimm}
     * ). In register mode, the contents of two registers, {@code sr1} and {@code sr2},
     * are bitwise ANDed and written to a destination register ({@code dr}). The syntax of
     * the command is {@code 0001dddsss0xxttt}, where {@code sss}, {@code ttt}, and
     * {@code ddd} represent the three-bit register indexes {@code sr1}, {@code sr2}, and
     * {@code dr}, respectively, and the {@code x}s represent "don't care" bits.
     */
    public static class AND extends Instruction {
        private final byte dr, sr1, sr2;

        public AND(final int _dr, final int _sr1, final int _sr2) {
            dr = (byte) _dr;
            sr1 = (byte) _sr1;
            sr2 = (byte) _sr2;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                (short) (m.getRegister(sr1).getValue() & m.getRegister(sr2).getValue()));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (sr1 == dr)
                return "AND R" + dr + ", #" + sr2;
            else if (sr2 == dr)
                return "AND R" + dr + ", #" + sr1;
            else
                return "AND R" + dr + ", R" + sr1 + ", R" + sr2;
        }
    }

    /**
     * Takes the bitwise AND of a register with an immediate operand and writes to another
     * register.
     * 
     * The AND instruction has two modes: register-based (see {@link AND}) and immediate.
     * In immediate mode, the contents of a register, {@code sr}, and a 5-bit signed 2's
     * complement integer, {@code imm}, are bitwise ANDed and written to a destination
     * register ({@code dr}). The syntax of the command is {@code 0001dddsss1iiiii}, where
     * {@code sss} and {@code ddd} represent the three-bit register indexes {@code sr} and
     * {@code dr}, respectively, and {@code iiiii} represents the immediate operand.
     */
    public static class ANDimm extends Instruction {
        private final byte dr, sr;
        private final int  imm;

        public ANDimm(final int _dr, final int _sr, final int _imm) {
            dr = (byte) _dr;
            sr = (byte) _sr;
            imm = _imm << 27 >> 27; // sign extend imm
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue((short) (m.getRegister(sr).getValue() & imm));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (imm == 0)
                return "CLR R" + dr;
            else if (imm == -1) {
                if (sr == dr)
                    return "TST R" + dr;
                else
                    return "MOV R" + dr + ", R" + sr;
            } else if (sr == dr)
                return "AND R" + dr + ", #" + imm;
            else
                return "AND R" + dr + ", R" + sr + ", #" + imm;
        }
    }

    /**
     * Conditionally branches to a given immediate address on the current page, depending
     * on the condition codes.
     * 
     * The BRx branch instruction sets the page offset of the PC to the given 9-bit
     * immediate value if the bit corresponding to the current condition code is set. The
     * branch command comes in eight different varieties determining if the program will
     * branch under each condition (N, Z, and P). All combinations of branch on N, branch
     * on Z, and branch on P are supported, so the {@code BRx} command is actually a
     * collective term for the 8 different commands {@code JMP}, {@code BRnz},
     * {@code BRnp}, {@code BRzp}, {@code BRn}, {@code BRz}, {@code BRp}, and {@code NOP},
     * where the last command (otherwise known as {@code BR}) never branches, and so is
     * effectively a no-op, and the first command (otherwise known as {@code BRnzp})
     * always branches, so is equivalent to {@link JSR JMP}. Note that a NOP command with
     * a non-zero page offset field (which is still a no-op) is displayed as
     * {@code DATA 00xx}, since ASCII data and small numbers would be interpreted this
     * way.
     * 
     * The syntax of the command is {@code 0000nzpggggggggg}, where the {@code n} bit
     * indicates a branch if the flags register's {@code n} bit is set, with analogous
     * definitions for the {@code z} and {@code p} bits. The 9-bit {@code g} field is the
     * page offset.
     */
    public static class BRx extends Instruction {
        private final byte  nzp;
        private final short pgoff;

        public BRx(final int _nzp, final int _pgoff) {
            nzp = (byte) _nzp;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            if ((nzp & 4) != 0 && m.getFlags().getN() || (nzp & 2) != 0
                && m.getFlags().getZ() || (nzp & 1) != 0 && m.getFlags().getP())
                m.getPCRegister().setValue(
                    (short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff));
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            String addr;
            if (true)
                addr = "x" + MemoryUtilities.uShortToHex(pgoff);
            if (nzp == 0)
                return pgoff == 0 ? "NOP" : "DATA " + addr;
            else if (nzp == 7)
                return "JMP " + addr;
            else
                return "BR" + ((nzp & 4) != 0 ? "n" : "") + ((nzp & 2) != 0 ? "z" : "")
                    + ((nzp & 1) != 0 ? "p" : "") + " " + addr;
        }
    }

    /**
     * The debug command takes no arguments (its instruction format is
     * {@code 1000xxxxxxxxxxxx}, with {@code x} = don't care), and it just prints out the
     * contents of the 8 registers, the PC, and the FLAGS register. It will print
     * something like the sample below:
     * 
     * <pre>
     * R0 0123   R1 4567   R2 89AB   R3 CDEF   FLAGS --p
     * R4 FEDC   R5 BA98   R6 7654   R7 3210   PC BEEF
     * </pre>
     * 
     * This output indicates that register 2 is holding the value {@code 0x89AB}, the PC
     * is currently pointing at {@code 0xBEEF}, and the flags register has a cleared
     * {@code n} and {@code z} bit and a set {@code p} bit (which means that the last
     * register which changed was set to a positive number).
     */
    public static class DBUG extends Instruction {
        @Override
        public boolean execute(final Machine m) {
            for (int i = 0; i < 8; i++) {
                m.ui.print("R" + i + " "
                    + MemoryUtilities.uShortToHex(m.getRegister(i).getValue()) + "   ");
                if (i == 3)
                    m.ui.print(m.getFlags() + "\n");
                else if (i == 7)
                    m.ui.print("PC "
                        + MemoryUtilities.uShortToHex(m.getPCRegister().getValue())
                        + "\n\n");
            }
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "DBUG";
        }
    }

    /**
     * The {@code JMP}/{@code JSR} command performs an unconditional branch to the given
     * page offset in the current page. The {@code JSR} command additionally stores the
     * current PC value to R7 before jumping. The syntax of the {@code JMP} command is
     * {@code 01000xxggggggggg}, and the {@code JSR} command is {@code 01001xxggggggggg},
     * where {@code x} means "don't care" and the 9-bit {@code g} field is the page
     * offset.
     */
    public static class JSR extends Instruction {
        private final boolean l;
        private final short   pgoff;

        public JSR(final boolean _l, final int _pgoff) {
            l = _l;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            if (l)
                m.getRegister(7).setValue(m.getPCRegister().getValue());
            m.getPCRegister().setValue(
                (short) (m.getPCRegister().getValue() & 0xfe00 | pgoff));
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return (l ? "JSR" : "JMP") + " x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * The {@code JMPR}/{@code JSRR} command performs an unconditional branch to an
     * address, given as the value of a given register plus an unsigned 6-bit index. The
     * {@code JSRR} command additionally stores the current PC value to R7 before jumping.
     * The syntax of the {@code JMPR} command is {@code 11000xxggggggggg}, and the
     * {@code JSR} command is {@code 11001xxggggggggg}, where {@code x} means "don't care"
     * and the 9-bit {@code g} field is the page offset.
     */
    public static class JSRR extends Instruction {
        private final boolean l;
        private final byte    br, index;

        public JSRR(final boolean _l, final int _br, final int _index) {
            l = _l;
            br = (byte) _br;
            index = (byte) _index;
        }

        @Override
        public boolean execute(final Machine m) {
            if (l)
                m.getRegister(7).setValue(m.getPCRegister().getValue());
            m.getPCRegister().setValue((short) (m.getRegister(br).getValue() + index));
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return (l ? "JSR" : "JMP") + "R R" + br + ", x"
                + MemoryUtilities.sShortToHex(index);
        }
    }

    /**
     * The {@code LD} command loads the value of memory at a given page offset into a
     * register. The syntax of the command is {@code 0010dddggggggggg}, where {@code ddd}
     * is the number of the destination register and the 9-bit {@code g} field is the page
     * offset.
     */
    public static class LD extends Instruction {
        private final byte  dr;
        private final short pgoff;

        public LD(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                m.getMemory((short) (m.getPCRegister().getValue() & 0xfe00 | pgoff)));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "LD R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * The {@code LDI} command retrieves the value of memory at a given page offset,
     * interpreted as an address, and loads the value at this address into a register. The
     * syntax of the command is {@code 1010dddggggggggg}, where {@code ddd} is the number
     * of the destination register and the 9-bit {@code g} field is the page offset.
     */
    public static class LDI extends Instruction {
        private final byte  dr;
        private final short pgoff;

        public LDI(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                m.getMemory(m
                    .getMemory((short) (m.getPCRegister().getValue() & 0xfe00 | pgoff))));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "LDI R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * The {@code LDR} command loads the value of memory at an address calculated as a
     * base register value plus a 6-bit unsigned immediate index into a destination
     * register. The syntax of the command is {@code 0110dddbbbnnnnnn}, where {@code ddd}
     * is the destination register, {@code bbb} is the base register, and the 6-bit
     * {@code n} field is the index.
     */
    public static class LDR extends Instruction {
        private final byte dr, br, index;

        public LDR(final int _dr, final int _br, final int _index) {
            dr = (byte) _dr;
            br = (byte) _br;
            index = (byte) _index;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                m.getMemory((short) (m.getRegister(br).getValue() + index)));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (index == 0)
                return "LDR R" + dr + ", R" + br;
            else
                return "LDR R" + dr + ", R" + br + ", x"
                    + MemoryUtilities.sShortToHex(index);
        }
    }

    /**
     * The {@code LEA} command calculates an address given a page offset and the page
     * number of the PC, and stores it in a register. The syntax of the command is
     * {@code 1110dddggggggggg}, where {@code ddd} is the destination register and the
     * 9-bit {@code g} field is the page offset.
     */
    public static class LEA extends Instruction {
        private final byte  dr;
        private final short pgoff;

        public LEA(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                (short) (m.getPCRegister().getValue() & 0xfe00 | pgoff));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "LEA R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * Sets the destination register to the bitwise-NOT of the source register. The syntax
     * of the command is {@code 1001dddsssxxxxxx}, where {@code sss} and {@code ddd}
     * represent the three-bit register indexes {@code sr} and {@code dr}, respectively,
     * and the {@code x}s represent "don't care" bits.
     */
    public static class NOT extends Instruction {
        private final byte dr, sr;

        public NOT(final int _dr, final int _sr) {
            dr = (byte) _dr;
            sr = (byte) _sr;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue((short) ~m.getRegister(sr).getValue());
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            if (dr == sr)
                return "NOT R" + dr;
            else
                return "NOT R" + dr + ", R" + sr;
        }
    }

    /**
     * The {@code RET} instruction, designed for use with {@code JSR}/{@code JSRR} (see
     * {@link JSR}), sets the PC to the contents of register 7. The syntax of the command
     * is {@code 1101xxxxxxxxxxxx}, where {@code x} means "don't care", and it is a
     * special case of the {@code JMPR} command, as it is equivalent in effect to
     * {@code JMPR R7, #0} ( = {@code C1C0}).
     */
    public static class RET extends Instruction {
        @Override
        public boolean execute(final Machine m) {
            m.getPCRegister().setValue(m.getRegister(7).getValue());
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "RET";
        }
    }

    /**
     * The {@code ST} command stores a register to memory at a given page offset. The
     * syntax of the command is {@code 0011sssggggggggg}, where {@code sss} is the number
     * of the source register and the 9-bit {@code g} field is the page offset.
     */
    public static class ST extends Instruction {
        private final byte  sr;
        private final short pgoff;

        public ST(final int _sr, final int _pgoff) {
            sr = (byte) _sr;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.setMemory((short) (m.getPCRegister().getValue() & 0xfe00 | pgoff), m
                .getRegister(sr).getValue());
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "ST R" + sr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * The {@code STI} command retrieves the value of memory at a given page offset,
     * interpreted as an address, and stores the source register to this address. The
     * syntax of the command is {@code 1010sssggggggggg}, where {@code sss} is the number
     * of the source register and the 9-bit {@code g} field is the page offset.
     */
    public static class STI extends Instruction {
        private final byte  sr;
        private final short pgoff;

        public STI(final int _sr, final int _pgoff) {
            sr = (byte) _sr;
            pgoff = (short) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.setMemory(m
                .getMemory((short) (m.getPCRegister().getValue() & 0xfe00 | pgoff)), m
                .getRegister(sr).getValue());
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "STI R" + sr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    /**
     * The {@code STR} command stores a source register to memory at an address calculated
     * as a base register value plus a 6-bit unsigned immediate index. The syntax of the
     * command is {@code 0110sssbbbnnnnnn}, where {@code sss} is the source register,
     * {@code bbb} is the base register, and the 6-bit {@code n} field is the index.
     */
    public static class STR extends Instruction {
        private final byte sr, br, index;

        public STR(final int _sr, final int _br, final int _index) {
            sr = (byte) _sr;
            br = (byte) _br;
            index = (byte) _index;
        }

        @Override
        public boolean execute(final Machine m) {
            m.setMemory((short) (m.getRegister(br).getValue() + index), m.getRegister(sr)
                .getValue());
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            return "STR R" + sr + ", R" + br + ", x" + MemoryUtilities.sShortToHex(index);
        }
    }

    /**
     * The {@code TRAP} instruction allows the programmer to call the OS for access to I/O
     * functionality. Its syntax is {@code 1111xxxxvvvvvvvv}, where {@code x} means
     * "don't care" and {@code v} is the 8-bit trap vector. There are 7 supported trap
     * vectors:
     * <ul>
     * <li>{@code 0x21}: {@code OUT} - Prints the value of register 0 as an ASCII
     * character. If the high 9 bits are not all 0, a warning is posted, but they are
     * ignored in any case.</li>
     * <li>{@code 0x22}: {@code PUTS} - Prints a null-terminated string starting at the
     * address in register 0. If any of the high 9 bits of any of the words are nonzero,
     * they are ignored, but no warning is printed.</li>
     * <li>{@code 0x23}: {@code IN} - Prints the prompt {@code "Enter a character: "}, and
     * waits for a character to be entered. The result is stored in register 0.</li>
     * <li>{@code 0x25}: {@code HALT} - Ends instruction interpretation, and prints a
     * message recording how many instructions were interpreted.</li>
     * <li>{@code 0x31}: {@code OUTN} - Prints the value of register 0 as a decimal
     * number.</li>
     * <li>{@code 0x33}: {@code INN} - Prints the prompt {@code "Enter a number: "}, and
     * waits for a number to be entered. Program execution will not continue until a
     * complete number is entered. The result is stored in register 0.</li>
     * <li>{@code 0x43}: {@code RND} - Sets register 0 to a random number with the full
     * range of a 16-bit value.</li>
     * </ul>
     * If a trap vector other than these 7 is encountered, an error (Warning 400) is
     * printed.
     */
    public static class TRAP extends Instruction {
        public static final int OUT = 0x21, PUTS = 0x22, IN = 0x23, HALT = 0x25,
            OUTN = 0x31, INN = 0x33, RND = 0x43;
        private final byte      vector;

        public TRAP(final int _vector) {
            vector = (byte) _vector;
        }

        @Override
        public boolean execute(final Machine m) {
            switch (vector) {
            case OUT: // write the char in R0 to the console
                if ((m.getRegister(0).getValue() & 0xff80) != 0) {
                    final String msg = "at 0x"
                        + MemoryUtilities.uShortToHex((short) (m.getPCRegister()
                            .getValue() - 1)) + ": value is R0 = 0x"
                        + MemoryUtilities.uShortToHex(m.getRegister(0).getValue());
                    Simulator.printErrors(m.ui, new Error(msg, ErrorCodes.EXEC_TRAP_OUT));
                }
                m.ui.print("" + (char) (m.getRegister(0).getValue() & 0x7f));
                break;
            case PUTS: // write the null-terminated string pointed to by R0 to the
                // console
                short i = m.getRegister(0).getValue();
                short s = m.getMemory(i++);
                while (s != 0) {
                    m.ui.print("" + (char) (s & 0x7f));
                    s = m.getMemory(i++);
                }
                break;
            case IN: // print a prompt on screen and read a single character from
                // the prompt
                m.ui.print("Enter a character: ");
                m.getRegister(0).setValue(m.ui.getChar());
                break;
            case HALT: // halt execution
                m.halt();
                break;
            case OUTN: // write the value of R0 to the console as a decimal integer
                m.ui.print(m.getRegister(0).getValue() + "\n");
                break;
            case INN: // print a prompt on screen and read a decimal from the
                m.getRegister(0).setValue(m.ui.getShort());
                break; // prompt
            case RND: // store a random number in R0
                m.getRegister(0).setValue(MemoryUtilities.randomShort());
                break;
            default:
                final List<Error> errors = new ArrayList<Error>();
                errors.add(new Error("at 0x" + MemoryUtilities.sShortToHex(vector),
                    ErrorCodes.EXEC_TRAP_UNKN));
                Simulator.printErrors(m.ui, errors);
            }
            return false;
        }

        @Override
        public String toString(final Machine context, final Map<String, Short> symb) {
            switch (vector) {
            case 0x21:
                return "TRAP OUT";
            case 0x22:
                return "TRAP PUTS";
            case 0x23:
                return "TRAP IN";
            case 0x25:
                return "TRAP HALT";
            case 0x31:
                return "TRAP OUTN";
            case 0x33:
                return "TRAP HALT";
            case 0x43:
                return "TRAP RND";
            default:
                return "TRAP x" + MemoryUtilities.sShortToHex(vector);
            }
        }
    }
}

package edu.osu.cse.mmxi.machine.interpreter.instructions;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public interface Instruction {

    /**
     * Executes an instruction on the Machine.
     * 
     * @param m
     *            The machine upon which to execute the instruction.
     * @return true if the executed instruction modified a general purpose register.
     */
    public boolean execute(Machine m);

    public static class ADD implements Instruction {
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
        public String toString() {
            return "ADD R" + dr + ", R" + sr1 + ", R" + sr2;
        }
    }

    public static class ADDimm implements Instruction {
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
        public String toString() {
            return "ADD R" + dr + ", R" + sr + ", #"
                + MemoryUtilities.sShortToHex((short) imm);
        }
    }

    public static class AND implements Instruction {
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
        public String toString() {
            return "AND R" + dr + ", R" + sr1 + ", R" + sr2;
        }
    }

    public static class ANDimm implements Instruction {
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
        public String toString() {
            return "AND R" + dr + ", R" + sr + ", #"
                + MemoryUtilities.sShortToHex((short) imm);
        }
    }

    public static class BRx implements Instruction {
        private final byte nzp, pgoff;

        public BRx(final int _nzp, final int _pgoff) {
            nzp = (byte) _nzp;
            pgoff = (byte) _pgoff;
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
        public String toString() {
            return "BR" + ((nzp & 4) != 0 ? "n" : "") + ((nzp & 2) != 0 ? "z" : "")
                + ((nzp & 1) != 0 ? "p" : "") + " x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class DBUG implements Instruction {
        @Override
        public boolean execute(final Machine m) {
            m.print("PC " + MemoryUtilities.uShortToHex(m.getPCRegister().getValue())
                + "\n");
            for (int i = 0; i < 8; i++)
                m.print("R" + i + " "
                    + MemoryUtilities.uShortToHex(m.getRegister(i).getValue()) + "\n");
            m.print(m.getFlags().toString());
            return false;
        }

        @Override
        public String toString() {
            return "DBUG";
        }
    }

    public static class JSR implements Instruction {
        private final boolean l;
        private final byte    pgoff;

        public JSR(final boolean _l, final int _pgoff) {
            l = _l;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            if (l)
                m.getRegister(7).setValue(m.getPCRegister().getValue());
            m.getPCRegister().setValue(
                (short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff));
            return false;
        }

        @Override
        public String toString() {
            return (l ? "JSR" : "JMP") + " x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class JSRR implements Instruction {
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
        public String toString() {
            return (l ? "JSR" : "JMP") + "R R" + br + ", x"
                + MemoryUtilities.sShortToHex(index);
        }
    }

    public static class LD implements Instruction {
        private final byte dr, pgoff;

        public LD(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                m.getMemory((short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff)));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString() {
            return "LD R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class LDI implements Instruction {
        private final byte dr, pgoff;

        public LDI(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr)
                .setValue(
                    m.getMemory(m
                        .getMemory((short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff))));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString() {
            return "LDI R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class LDR implements Instruction {
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
        public String toString() {
            return "LDR R" + dr + ", R" + br + ", x" + MemoryUtilities.sShortToHex(index);
        }
    }

    public static class LEA implements Instruction {
        private final byte dr, pgoff;

        public LEA(final int _dr, final int _pgoff) {
            dr = (byte) _dr;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.getRegister(dr).setValue(
                (short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff));
            m.getFlags().setFlags(m.getRegister(dr));
            return true;
        }

        @Override
        public String toString() {
            return "LEA R" + dr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class NOT implements Instruction {
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
        public String toString() {
            return "NOT R" + dr + ", R" + sr;
        }
    }

    public static class RET implements Instruction {
        @Override
        public boolean execute(final Machine m) {
            m.getPCRegister().setValue(m.getRegister(7).getValue());
            return false;
        }

        @Override
        public String toString() {
            return "RET";
        }
    }

    public static class ST implements Instruction {
        private final byte sr, pgoff;

        public ST(final int _sr, final int _pgoff) {
            sr = (byte) _sr;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.setMemory((short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff), m
                .getRegister(sr).getValue());
            return false;
        }

        @Override
        public String toString() {
            return "ST R" + sr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class STI implements Instruction {
        private final byte sr, pgoff;

        public STI(final int _sr, final int _pgoff) {
            sr = (byte) _sr;
            pgoff = (byte) _pgoff;
        }

        @Override
        public boolean execute(final Machine m) {
            m.setMemory(m
                .getMemory((short) ((m.getPCRegister().getValue() & 0xfe00) + pgoff)), m
                .getRegister(sr).getValue());
            return false;
        }

        @Override
        public String toString() {
            return "STI R" + sr + ", x" + MemoryUtilities.sShortToHex(pgoff);
        }
    }

    public static class STR implements Instruction {
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
        public String toString() {
            return "AND R" + sr + ", R" + br + ", x" + MemoryUtilities.sShortToHex(index);
        }
    }

    public static class TRAP implements Instruction {
        private final byte vector;

        public TRAP(final int _vector) {
            vector = (byte) _vector;
        }

        @Override
        public boolean execute(final Machine m) {
            switch (vector) {
            case 0x21: // OUT: write the char in R0 to the console
                if ((m.getRegister(0).getValue() & 0xff80) != 0)
                    m.error("Warning: R0 does not contain a character");
                m.print("" + (char) (m.getRegister(0).getValue() & 0x7f));
                break;
            case 0x22: // TODO: PUTS: write the null-terminated string pointed to by R0 to
                break; // the console
            case 0x23: // TODO: IN: print a prompt on screen and read a single character
                break; // from the prompt
            case 0x25: // HALT: halt execution
                m.halt();
                break;
            case 0x31: // OUTN: write the value of R0 to the console as a decimal integer
                m.print(m.getRegister(0).getValue() + "\n");
                break;
            case 0x33: // TODO: INN: print a prompt on screen and read a decimal from the
                break; // prompt
            case 0x43: // RND: store a random number in R0
                m.getRegister(0).setValue(MemoryUtilities.randomShort());
                break;
            default:
                m.warn("Warning: unknown trap vector 0x"
                    + MemoryUtilities.sShortToHex(vector));
            }
            return false;
        }

        @Override
        public String toString() {
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
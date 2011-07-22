package edu.osu.cse.mmxi.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.sim.loader.parser.ParseException;

public class InstructionFormat {

    // @formatter:off
    public static final String[][] INST = {
        {"ADD",   "RRR",  "0001A--B--0xxC--"}, // ADD  Rd, Rs1, Rs2
        {"ADD",   "RR5",  "0001A--B--1C----"}, // ADD  Rd, Rs, #imm
        {"AND",   "RRR",  "0101A--B--0xxC--"}, // AND  Rd, Rs1, Rs2
        {"AND",   "RR5",  "0101A--B--1C----"}, // AND  Rd, Rs, #imm
        {"BRn",   "9",    "0000100A--------"}, // BRn  off
        {"BRz",   "9",    "0000010A--------"}, // BRz  off
        {"BRp",   "9",    "0000001A--------"}, // BRp  off
        {"BRnz",  "9",    "0000110A--------"}, // BRnz off
        {"BRnp",  "9",    "0000101A--------"}, // BRnp off
        {"BRzp",  "9",    "0000011A--------"}, // BRzp off
        {"BRnzp", "9",    "0000111A--------"}, // BRnzp off
        {"DBUG",  "",     "1000xxxxxxxxxxxx"}, // DBUG
        {"JMP",   "9",    "01000xxA--------"}, // JMP  off
        {"JSR",   "9",    "01001xxA--------"}, // JSR  off
        {"JMPR",  "R6",   "11000xxA--B-----"}, // JMPR off
        {"JSRR",  "R6",   "11001xxA--B-----"}, // JSRR off
        {"LD",    "R9",   "0010A--B--------"}, // LD   Rd, off
        {"LDI",   "R9",   "1010A--B--------"}, // LDI  Rd, off
        {"LDR",   "RR6",  "0110A--B--C-----"}, // LDR  Rd, Rb, ind
        {"LEA",   "R9",   "1110A--B--------"}, // LEA  Rd, off
        {"NOT",   "RR",   "1001A--B--xxxxxx"}, // NOT  Rd, Rs
        {"RET",   "",     "1101xxxxxxxxxxxx"}, // RET
        {"ST",    "R9",   "0011A--B--------"}, // ST   Rs, off
        {"STI",   "R9",   "1011A--B--------"}, // STI  Rs, off
        {"STR",   "RR6",  "0111A--B--C-----"}, // STR  Rs, Rb, ind
        {"TRAP",  "8",    "1111xxxxA-------"}, // TRAP vect

        {"AND",   "RR",   "0101A--A--0xxB--"}, // AND  Rd, Rs       = AND Rd, Rd, Rs
        {"AND",   "R5",   "0101A--A--1B----"}, // AND  Rd, #imm     = AND Rd, Rd, #imm
        {"CLR",   "R",    "0101A--A--100000"}, // CLR  Rd           = AND Rd, #0
        {"CLR",   "9R",   "0101B--B--100000",  // CLR  off, Rj*     = CLR Rj
                          "0101B--B--100000"}, //                     ST Rj, off
        {"DBL",   "RR",   "0001A--B--0xxB--"}, // DBL  Rd, Rs       = ADD Rd, Rs, Rs
        {"DBL",   "R",    "0001A--A--0xxA--"}, // DBL  Rd           = DBL Rd, Rd
        {"DEC*",  "RR",   "1001A--A--xxxxxx",  // DEC  Rd, Rs       = NOT Rd
                          "0001A--A--0xxB--",  //      [s != d]       INC Rd, Rs
                          "1001A--A--xxxxxx"}, //                     NOT Rd
        {"DEC*",  "RR",   "0101A--A--100000"}, // DEC  Rd, Rs [s == d] = CLR Rd
        {"DEC*",  "R5",   "0001A--A--1B----"}, // DEC  Rd, #imm     = ADD Rd, Rd, #-imm
        {"DEC",   "R",    "0001A--A--111111"}, // DEC  Rd           = DEC Rd, #1
        {"INC",   "RR",   "0001A--A--0xxB--"}, // INC  Rd, Rs       = ADD Rd, Rd, Rs
        {"INC",   "R5",   "0001A--A--1B----"}, // INC  Rd, #imm     = ADD Rd, Rd, #imm
        {"INC",   "R",    "0001A--A--100001"}, // INC  Rd           = INC Rd, #1
        {"LDR",   "RR",   "0110A--B--000000"}, // LDR  Rd, Rb       = LDR Rd, Rb, #0
        {"MOV",   "RR",   "0101A--B--111111"}, // MOV  Rd, Rs       = AND Rd, Rs, #-1
        {"NEG",   "RR",   "1001A--B--xxxxxx",  // NEG  Rd, Rs       = NOT Rd, Rs
                          "0001A--A--100001"}, //                     INC Rd
        {"NEG",   "R",    "1001A--A--xxxxxx",  // NEG  Rd           = NOT Rd, Rd
                          "0001A--A--100001"}, //                     INC Rd
        {"NOT",   "R",    "1001A--A--xxxxxx"}, // NOT  Rd           = NOT Rd, Rd
        {"NOP",   "",     "0000000xxxxxxxxx"}, // NOP               = BR x0
        {"OR",    "RRR",  "1001C--C--xxxxxx",  // OR   Rd, Ra, Rb*  = NOT Rb
                          "1001A--B--xxxxxx",  //                     NOT Rd, Ra
                          "0101A--A--0xxC--",  //                     AND Rd, Rb
                          "1001A--A--xxxxxx"}, //                     NOT Rd
        {"POP",   "RR",   "0001B--B--111111",  // POP  Rd, Rstk     = DEC Rstk
                          "0110A--B--000000"}, //                     LDR Rd, Rstk
        {"PUSH",  "RR",   "0111A--B--000000",  // PUSH Rs, Rstk     = STR Rs, Rstk
                          "0001B--B--100001"}, //                     INC Rstk
        {"SHL*",  "R4",   "0001A--A--0xxA--"}, // SHL  Rd, 0<=imm<16 = (DBL Rd) [imm times]
        {"STR",   "RR",   "0111A--B--000000"}, // 
        {"SUB*",  "RRR",  "1001A--C--xxxxxx",  // SUB  Rd,Rs1,Rs2   = NOT Rd, Rs2
                          "0001A--A--100001",  //      [d != s1]      INC Rd
                          "0001A--A--0xxB--"}, //                     INC Rd, Rs1
        {"SUB*",  "RRR",  "1001A--A--xxxxxx",  // SUB  Rd,Rs1,Rs2   = NOT Rd
                          "0001A--A--0xxC--",  //      [d == s1]      INC Rd, Rs2
                          "1001A--A--xxxxxx"}, //                     NOT Rd
        {"TST",   "R",    "0101A--A--111111"}, // TST  Rd           = MOV Rd, Rd
        {"XCHG",  "RRR",  "0101C--A--111111",  // XCHG Ra, Rb, Rj*  = MOV Rj, Ra
                          "0101A--B--111111",  //      [j != a, b]    MOV Ra, Rb
                          "0101B--C--111111"}, //                     MOV Rb, Rj
        {"XNOR*", "RRRR", "1001A--B--xxxxxx",  // XNOR Rd,Ra,Rb,Rj* = NOT Rd, Ra
                          "1001D--C--xxxxxx",  //      [j != a != b]  NOT Rj, Rb
                          "0101D--D--0xxB--",  //                     AND Rj, Ra
                          "0101A--A--0xxC--",  //                     AND Rd, Rb
                          "1001D--D--xxxxxx",  //                     NOT Rj
                          "1001A--A--xxxxxx",  //                     NOT Rd
                          "0101A--A--0xxD--"}, //                     AND Rd, Rj
        {"XNOR*", "RRRR", "1001A--B--xxxxxx",  // XNOR Rd,Ra*,Rb*,Rj= NOT Rd, Ra
                          "0101A--A--0xxC--",  //      [a != b == j]  AND Rd, Rb
                          "1001C--C--xxxxxx",  //                     NOT Rb
                          "0101C--C--0xxB--",  //                     AND Rb, Ra
                          "1001C--C--xxxxxx",  //                     NOT Rb
                          "1001A--A--xxxxxx",  //                     NOT Rd
                          "0101A--A--0xxC--"}, //                     AND Rd, Rb
        {"XNOR*", "RRRR", "0101A--B--111111"}, // XNOR Rd,Ra,Rb,Rj [a == b] = MOV Rd, Ra
        {"XOR*",  "RRRR", "1001A--B--xxxxxx",  // XOR  Rd,Ra,Rb,Rj* = NOT Rd, Ra
                          "1001D--C--xxxxxx",  //      [j != a != b]  NOT Rj, Rb
                          "0101D--D--0xxA--",  //                     AND Rj, Rd
                          "0101A--B--0xxC--",  //                     AND Rd, Ra, Rb
                          "1001D--D--xxxxxx",  //                     NOT Rj
                          "1001A--A--xxxxxx",  //                     NOT Rd
                          "0101A--A--0xxD--"}, //                     AND Rd, Rj
        {"XOR*",  "RRRR", "0101A--B--0xxC--",  // XOR  Rd,Ra*,Rb*,Rj= AND Rd, Ra, Rb
                          "1001B--B--xxxxxx",  //      [a != b == j]  NOT Ra
                          "1001C--C--xxxxxx",  //                     NOT Rb
                          "0101C--C--0xxB--",  //                     AND Rb, Ra
                          "1001C--C--xxxxxx",  //                     NOT Rb
                          "1001A--A--xxxxxx",  //                     NOT Rd
                          "0101A--A--0xxC--"}, //                     AND Rd, Rb
        {"XOR*",  "RRRR", "0101A--A--100000"}};// XOR  Rd,Ra,Rb,Rj [a == b] = CLR Rd
    // @formatter:on
    public static final Map<String, List<IFRecord>> instructions = new HashMap<String, List<IFRecord>>();
    static {
        for (final String[] inst : INST) {
            final IFRecord r = new IFRecord();
            if (inst[0].contains("*"))
                r.special = true;
            r.name = inst[0].replace("*", "");
            r.signature = inst[1];
            r.template = new short[inst.length - 2];
            final List<int[]> l = new ArrayList<int[]>();
            for (int i = 2; i < inst.length; i++) {
                r.template[i - 2] = (short) Integer.parseInt(
                    inst[i].replaceAll("[^1]", "0"), 2);
                final Matcher m = Pattern.compile("[A-Z]-*").matcher(inst[i]);
                while (m.find())
                    l.add(new int[] { inst[i].charAt(m.start()) - 'A', i - 2,
                    /**/16 - m.end(), m.end() - m.start() });
            }
            r.replacements = l.toArray(new int[0][]);
            final String key = r.name.toUpperCase() + ":" + r.signature.length();
            if (!instructions.containsKey(key))
                instructions.put(key, new ArrayList<IFRecord>());
            instructions.get(key).add(r);
        }
    }

    private static List<IFRecord> getInstruction(final String name, final int[] isReg)
        throws ParseException {
        final String key = name.toUpperCase() + ":" + isReg.length;
        if (!instructions.containsKey(key))
            throw new ParseException("Unknown opcode or signature");
        final List<IFRecord> candidates = new ArrayList<IFRecord>(instructions.get(key));
        loop: for (Iterator<IFRecord> i = candidates.iterator(); i.hasNext();) {
            final IFRecord r = i.next();
            for (int j = 0; j < isReg.length; j++) {
                final boolean sigIsReg = r.signature.charAt(j) == 'R';
                if (!sigIsReg && isReg[j] == 2) {
                    isReg[j] = 0;
                    i = candidates.iterator();
                    continue loop;
                }
                if (isReg[j] == (sigIsReg ? 0 : 1)) {
                    i.remove();
                    continue loop;
                }
            }
        }
        return candidates;
    }

    /**
     * Finds an instruction based on a name and signature. The {@code isReg} parameter
     * contains a list indicating if each parameter had a register (1) or not (0), or if
     * it was a symbol or symbol expression (2), so that it could be either. The
     * {@code values} parameter gives the actual short value of each argument. The return
     * value is a list of words that encode the instruction.
     * 
     * The {@code isReg} and {@code values} arrays must have the same length.
     * 
     * @param name
     *            the opcode
     * @param isReg
     *            whether each argument is a register: 0 = no, 1 = yes, 2 = unknown
     * @param values
     *            the value of each parameter
     * @return the list of words that encode the instruction
     */
    public static short[] getInstruction(final String name, final int[] isReg,
        final short[] values) throws ParseException {
        final List<IFRecord> candidates = getInstruction(name, isReg);
        final String key = name.toUpperCase() + ":" + isReg.length;
        if (candidates.size() == 0)
            throw new ParseException("Immediate used in place of register or vice-versa");
        IFRecord inst = candidates.get(0);
        if (inst.special)
            inst = doSpecial(key, isReg, values, candidates);
        for (int i = 0; i < values.length; i++)
            if (!isValid(inst.signature.charAt(i), values[i]))
                throw new ParseException("parameter " + (i + 1) + " out of range");
        final short[] ret = new short[inst.template.length];
        System.arraycopy(inst.template, 0, ret, 0, ret.length);
        for (final int[] rep : inst.replacements)
            ret[rep[1]] |= (values[rep[0]] & (1 << rep[3]) - 1) << rep[2];
        return ret;
    }

    private static IFRecord doSpecial(final String key, final int[] isReg,
        final short[] values, final List<IFRecord> candidates) {
        IFRecord inst = candidates.get(0);
        if (key.equals("DEC:2")) {
            if (isReg[1] == 0)
                values[1] = (short) -values[1];
            else if (values[0] == values[1])
                inst = candidates.get(1);
        } else if (key.equals("SHL:2")) {
            final IFRecord shl = inst;
            inst = new IFRecord();
            inst.template = new short[values[1]];
            inst.replacements = new int[shl.replacements.length * values[1]][];
            for (int i = 0; i < values[1]; i++) {
                inst.template[i] = shl.template[0];
                for (int j = 0; j < shl.replacements.length; j++) {
                    final int[] arr = new int[4];
                    arr[0] = shl.replacements[j][0];
                    arr[1] = i;
                    arr[2] = shl.replacements[j][2];
                    arr[3] = shl.replacements[j][3];
                    inst.replacements[i * shl.replacements.length + j] = arr;
                }
            }
        } else if (key.equals("SUB:3")) {
            if (values[0] == values[1])
                inst = candidates.get(1);
        } else if (key.equals("XNOR:4") || key.equals("XOR:4"))
            if (values[1] == values[2])
                inst = candidates.get(2);
            else if (values[2] == values[3])
                inst = candidates.get(1);
        return inst;
    }

    private static boolean isValid(final char sig, final short val) {
        switch (sig) {
        case 'R':
            return val >= 0 && val < 8;
        case '4':
            return val >= 0 && val < 16;
        case '5':
            return val >= -16 && val < 16;
        case '6':
            return val >= 0 && val < 64;
        case '8':
            return val >= 0 && val < 256;
        case '9':
            return true;
        }
        return false;
    }

    public static class IFRecord {
        public String  name;
        public String  signature;
        public boolean special;
        public short[] template;
        public int[][] replacements; // set of tuples (arg, word, start, length)
    }
}

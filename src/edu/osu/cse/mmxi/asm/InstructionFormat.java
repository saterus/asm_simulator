package edu.osu.cse.mmxi.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.inst.InstructionListLoader;
import edu.osu.cse.mmxi.asm.line.InstructionLine;
import edu.osu.cse.mmxi.asm.line.InstructionLine.Argument;
import edu.osu.cse.mmxi.asm.line.InstructionLine.ExpressionArg;
import edu.osu.cse.mmxi.asm.line.InstructionLine.RegisterArg;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.common.Location;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * This contains the format used for all instructions and psuedo operations. This is used
 * to check each line for proper format and parse into Symbols and command.
 * 
 */
public class InstructionFormat {
    /**
     * Representation of a Map of instrucitons
     */
    public static final Map<String, List<IFRecord>> instructions = new HashMap<String, List<IFRecord>>();
    static {
        InstructionListLoader.load();
    }

    public static void addIFRecord(final String[] inst) {
        final IFRecord r = interpretTextIFRecord(inst);
        final String key = r.name.toUpperCase() + ":" + r.signature.length();
        if (!instructions.containsKey(key))
            instructions.put(key, new ArrayList<IFRecord>());
        instructions.get(key).add(r);
    }

    /**
     * creates an IFRecord from the instruction object
     * 
     * Result contains a list of tuples (arg, index, start, len), where arg is the index
     * of the argument in the argument list, index is the choice of which word (of a
     * multi-word or synthetic instruction) to replace, start is the index of the least
     * significant bit in the word, and len is the number of bits to replace
     * 
     * @param inst
     *            An array of strings created by the instruction parser.
     * @return IFRecord The IFRecord representation.
     */
    private static IFRecord interpretTextIFRecord(final String[] inst) {
        final IFRecord r = new IFRecord();
        if (inst[0].contains("*"))
            r.special = true;
        r.name = inst[0].replace("*", ""); // name contains the opcode name
        r.signature = inst[1]; // signature contains a string with the types of each
                               // argument in the characters
        // template contains the list of shorts before putting values in the fields
        // (all bits are 0 except required 1's)
        r.template = new short[inst.length - 2];
        final List<int[]> l = new ArrayList<int[]>();
        for (int i = 2; i < inst.length; i++) {
            r.template[i - 2] = (short) Integer.parseInt(inst[i].replaceAll("[^1]", "0"),
                2);
            final Matcher m = Pattern.compile("[A-Z]-*").matcher(inst[i]);
            while (m.find())
                l.add(new int[] { inst[i].charAt(m.start()) - 'A', i - 2,
                /**/16 - m.end(), m.end() - m.start() });
        }
        // replacements contains a list of tuples (arg, index, start, len), where arg
        // is the index of the argument in the argument list, index is the choice of
        // which word (of a multi-word or synthetic instruction) to replace, start is
        // the index of the least significant bit in the word, and len is the number
        // of bits to replace
        r.replacements = l.toArray(new int[0][]);
        return r;
    }

    private static List<IFRecord> getInstruction(final String name, final int[] isReg)
        throws ParseException {
        final String key = name.toUpperCase() + ":" + isReg.length;
        if (!instructions.containsKey(key))
            throw new ParseException(AsmCodes.IF_BAD_ARG_NUM);
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
     * Get the length.
     * 
     * @param inst
     *            The instruction object
     * @return
     * @throws ParseException
     */
    public static SymbolExpression getLength(final InstructionLine inst)
        throws ParseException {
        final int[] isReg = new int[inst.args.length];
        for (int i = 0; i < isReg.length; i++)
            isReg[i] = inst.args[i].isReg();
        final List<IFRecord> candidates = getInstruction(inst.opcode, isReg);
        if (candidates.size() == 0)
            throw new ParseException(AsmCodes.IF_SIG_INVALID,
                "Immediate used in place of register or vice-versa");
        final SymbolExpression len = getSpecialLength(inst, candidates);
        if (len == null)
            return new NumExp((short) candidates.get(0).template.length);
        return len;
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
    public static Object[] getInstruction(final Location lc, final InstructionLine inst)
        throws ParseException {
        final int[] isReg = new int[inst.args.length];
        final Location[] values = new Location[inst.args.length];
        boolean hasNull = false;
        final Set<Symbol> undef = new HashSet<Symbol>();
        for (int i = 0; i < isReg.length; i++) {
            isReg[i] = inst.args[i].isReg();
            if (inst.args[i] instanceof RegisterArg)
                values[i] = new Location(false, ((RegisterArg) inst.args[i]).reg);
            else {
                final SymbolExpression se = ArithmeticParser
                    .simplify(((ExpressionArg) inst.args[i]).val);
                ((ExpressionArg) inst.args[i]).val = se;
                values[i] = Location.convertToRelative(se);
                if (values[i] == null
                    && !(se instanceof Symbol && ((Symbol) se).global == Symbol.EXT)) {
                    CommonParser.undefinedSymbols(undef, se, false);
                    hasNull = true;
                }
            }
        }
        if (hasNull) {
            CommonParser.errorOnUndefinedSymbols(undef);
            String s = "";
            for (final Argument arg : inst.args)
                s += ", " + arg;
            throw new ParseException(AsmCodes.IF_ARG_CMX, "Attempted to encode "
                + inst.opcode + " " + s.substring(2));
        }
        final List<IFRecord> candidates = getInstruction(inst.opcode, isReg);
        final String key = inst.opcode + ":" + isReg.length;
        if (candidates.size() == 0)
            throw new ParseException(AsmCodes.IF_SIG_INVALID,
                "Immediate used in place of register or vice-versa");
        IFRecord rec = candidates.get(0);
        if (rec.special)
            rec = getSpecialInstruction(key, isReg, values, candidates);
        for (int i = 0; i < values.length; i++)
            if (values[i] != null) {
                checkRange(i + 1, rec.signature.charAt(i), (short) values[i].address,
                    inst);
                if (rec.signature.charAt(i) == '9') {
                    if (lc.isRelative ^ values[i].isRelative)
                        throw new ParseException(AsmCodes.IF_ABS_ADDR);
                    if (((lc.address + 1 ^ values[i].address) & 0xFE00) != 0)
                        throw new ParseException(AsmCodes.IF_OFF_PAGE);
                } else if (values[i].isRelative)
                    throw new ParseException(AsmCodes.IF_ARG_CMX,
                        "relative parameter used in field which does not support it");
            }
        final short[] data = Arrays.copyOf(rec.template, rec.template.length);
        final int[] m = new int[data.length];
        final String[] ext = new String[data.length];
        Arrays.fill(m, -1);
        for (final int[] rep : rec.replacements) {
            if (values[rep[0]] == null)
                ext[rep[1]] = ((Symbol) ((ExpressionArg) inst.args[rep[1]]).val).name;
            else
                data[rep[1]] |= (values[rep[0]].address & (1 << rep[3]) - 1) << rep[2];
            if (rep[3] == 9 && rep[2] == 0)
                m[rep[1]] = 0;
        }
        return new Object[] { data, m, ext };
    }

    private static IFRecord getSpecialInstruction(final String key, final int[] isReg,
        final Location[] values, final List<IFRecord> candidates) {
        IFRecord inst = candidates.get(0);
        if (key.equals("DEC:2")) {
            if (isReg[1] == 0)
                values[1].address = (short) -values[1].address;
            else if (values[0] == values[1])
                inst = candidates.get(1);
        } else if (key.equals("SHL:2")) {
            final IFRecord shl = inst;
            inst = new IFRecord();
            inst.template = new short[values[1].address];
            inst.replacements = new int[shl.replacements.length * values[1].address][];
            for (int i = 0; i < values[1].address; i++) {
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
            if (values[0].address == values[1].address)
                inst = candidates.get(1);
        } else
            assert false : "special instruction " + key
                + " marked, but no special code exists in "
                + "InstructionFormat.getSpecialInstruction().";
        return inst;
    }

    private static SymbolExpression getSpecialLength(final InstructionLine inst,
        final List<IFRecord> candidates) {
        final String key = inst.opcode + ":" + inst.args.length;
        if (key.equals("DEC:2")) {
            if (inst.args[1] instanceof RegisterArg) {
                final Short reg0, reg1 = ((RegisterArg) inst.args[1]).reg;
                if (inst.args[0] instanceof RegisterArg)
                    reg0 = ((RegisterArg) inst.args[0]).reg;
                else
                    reg0 = ((ExpressionArg) inst.args[0]).val.evaluate();
                if (reg0 != null)
                    return new NumExp((short) (reg0 == reg1 ? 1 : 3));
                else
                    try {
                        return ArithmeticParser.parseF("(:0 - :1 ? 2) + 1",
                            ((ExpressionArg) inst.args[0]).val, reg1);
                    } catch (final ParseException e) {
                        assert false;
                    }
            }
        } else if (key.equals("SHL:2")) {
            final Short val = ((ExpressionArg) inst.args[1]).val.evaluate();
            if (val != null)
                return new NumExp(val);
            else
                return ((ExpressionArg) inst.args[1]).val;
        } else if (candidates.get(0).special && !key.equals("SUB:3"))
            assert false : "special instruction " + key
                + " marked, but no special code exists in "
                + "InstructionFormat.getSpecialLength().";

        return null;
    }

    private static void checkRange(final int index, final char sig, final short val,
        final InstructionLine inst) throws ParseException {
        final String arg = "at argument " + index + ": ";
        switch (sig) {
        case 'R':
            if (val < 0 || val >= 8)
                throw new ParseException(AsmCodes.IF_ARG_RANGE, arg
                    + "register parameter R" + val + "; instruction: " + inst.toString());
            return;
        case '4':
            if (val < 0 || val >= 16)
                throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "shift left by "
                    + val + "; instruction: " + inst.toString());
            return;
        case '5':
            if (val < -16 || val >= 16)
                throw new ParseException(AsmCodes.IF_ARG_RANGE, arg
                    + "immediate parameter " + val + "; instruction: " + inst.toString());
            return;
        case '6':
            if (val < 0 || val >= 64)
                throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "index6 parameter "
                    + (val & 0xFFFF) + "; instruction: " + inst.toString());
            return;
        case '8':
            if (val < 0 || val >= 256)
                throw new ParseException(AsmCodes.IF_ARG_RANGE, arg + "trap vector "
                    + Utilities.sShortToHex(val) + "; instruction: " + inst.toString());
            return;
        case '9':
            return;
        }
        assert false : "bad signature character";
    }

    public static class IFRecord {
        public String  name;
        public String  signature;
        public boolean special;
        public short[] template;
        public int[][] replacements; // set of tuples (arg, word, start, length)

        public String[] toArray() {
            final String[] arr = new String[template.length + 2];
            arr[0] = name + (special ? "*" : "");
            arr[1] = signature;
            for (int i = 0; i < template.length; i++) {
                String s = Integer.toBinaryString(template[i]);
                if (s.length() > 16)
                    s = s.substring(s.length() - 16);
                arr[i + 2] = Utilities.padLeft(s, 16, '0');
            }
            for (final int[] r : replacements)
                arr[r[1] + 2] = arr[r[1] + 2].substring(0, 16 - r[2] - r[3])
                    + Utilities.padRight("" + (char) ('A' + r[0]), r[3], '-')
                    + arr[r[1] + 2].substring(16 - r[2]);
            return arr;
        }

        @Override
        public String toString() {
            return Arrays.toString(toArray());
        }
    }
}

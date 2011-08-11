package edu.osu.cse.mmxi.asm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.osu.cse.mmxi.asm.InstructionFormat.IFRecord;
import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * This is the common parser used by pass 1 and pass 2 for parsing of instruction line.
 * 
 */
public class CommonParser {
    /**
     * THis handles the psuedo op parsing.
     */
    private static final String OPS,
        PSEUDO_OPS = "[.](ORIG|ENT|EXT|END|EQU|FILL|STRZ|BLKW)";
    static {
        final SortedSet<String> ao = new TreeSet<String>();
        for (final List<IFRecord> i : InstructionFormat.instructions.values())
            for (final IFRecord j : i)
                ao.add(j.name.toUpperCase());
        String aoS = "";
        for (final String op : ao)
            aoS += "|" + op;
        OPS = aoS.substring(1);
    }

    /**
     * Breaks lines into its parts, labels, instruction, and arguments for line parsing.
     * 
     * @param line
     *            The line to parse.
     * @return Returns a parseLine object string.
     * @throws ParseException
     */
    public static String[] parseLine(String line) throws ParseException {
        if (line.contains(";"))
            line = line.substring(0, line.indexOf(';'));
        if (line.trim().length() == 0)
            return new String[] { null, null };
        // instructions start with a white space, labels and programs name do not
        return parseLine(line, line.substring(0, 1).matches("\\S"));
    }

    /**
     * Parses a line. Really no checking occurs before this. It is presumed to be an
     * instruction.
     * 
     * @param line
     * @param hasLabel
     *            True if this is a label, False if this is an instruciton
     * @return
     * @throws ParseException
     */
    private static String[] parseLine(final String line, final boolean hasLabel)
        throws ParseException {
        final int l = hasLabel ? 1 : 0;
        final String[] tokens = line.trim().split("\\s+", l + 2);
        final String label = hasLabel ? tokens[0] : null;

        if (tokens.length <= l + 1)
            return new String[] { label,
                    tokens.length == l ? null : tokens[l].toUpperCase() };
        final String[] args;
        if (tokens[l + 1].matches("\\s*\".*(?<!\\\\)\"\\s*")) {
            String str = tokens[l + 1].trim();
            str = str.substring(1, str.length() - 1);
            args = new String[] { "\"" + Utilities.parseString(str) + "\"" };
        } else
            args = tokens[l + 1].split(",");
        final String[] ret = new String[args.length + 2];
        ret[0] = label;
        ret[1] = tokens[l].toUpperCase();
        for (int i = 0; i < args.length; i++)
            ret[i + 2] = args[i].trim();
        return ret;
    }

    /**
     * Checks the line for invalid symbol names and instructions.
     * 
     * @param line
     *            The line string to check.
     * @return Returns the parsed line object
     * @throws ParseException
     */
    public static String[] checkLine(final String[] line) throws ParseException {
        if (line[0] != null) {
            if (Utilities.parseShort(line[0]) != null)
                throw new ParseException(AsmCodes.P1_INVALID_SYMB,
                    "Numbers can not be labels.");
            if (line[0].matches("[rR][0-7]"))
                throw new ParseException(AsmCodes.P1_INVALID_SYMB,
                    "Registers cannot be labels.");
        }
        if (line[1] != null)
            if (line[1].matches(OPS)) {
                if (!InstructionFormat.instructions.containsKey(line[1] + ":"
                    + (line.length - 2)))
                    throw new ParseException(AsmCodes.IF_BAD_ARG_NUM);
            } else if (line[1].matches(PSEUDO_OPS)) {
                if (!(line[1].matches("[.]ENT|[.]EXT") || line.length == 3 || line.length == 2
                    && line[1].matches("[.]ORIG|[.]END")))
                    throw new ParseException(AsmCodes.IF_BAD_ARG_NUM);
            } else if (line[0] != null
                && (line[0].matches(OPS) || line[0].matches(PSEUDO_OPS)))
                throw new ParseException(AsmCodes.P1_INST_NO_SPACE);
            else
                throw new ParseException(AsmCodes.P1_INST_BAD_OP_CODE, line[1]);
        return line;
    }

    /**
     * Throw a specific error for undefined symbol.
     * 
     * @param se
     *            The symbol that could not be resolved
     * @param allowExt
     *            whether to allow external symbols
     * @throws ParseException
     */
    public static void errorOnUndefinedSymbols(final SymbolExpression se,
        final boolean allowExt) throws ParseException {
        errorOnUndefinedSymbols(undefinedSymbols(se, allowExt));
    }

    public static void errorOnUndefinedSymbols(final Set<Symbol> undef)
        throws ParseException {
        if (undef.size() != 0) {
            String str = "undefined symbols: ";
            for (final Symbol s : undef)
                str += s.name + ", ";

            str = str.substring(0, str.length() - 2);
            throw new ParseException(AsmCodes.P2_INST_BAD_SYMBOL, str);
        }
    }

    /**
     * Create a set for undefined symbol with the given symbol in the set.
     * 
     * @param se
     *            The symbol
     * @param allowExt
     *            whether to allow external symbols
     * @return A set containing the given symbol
     */
    private static Set<Symbol> undefinedSymbols(final SymbolExpression se,
        final boolean allowExt) {
        final Set<Symbol> undef = new HashSet<Symbol>();
        undefinedSymbols(undef, se, allowExt);
        return undef;
    }

    /**
     * Add undefined symbols to the symbol set.
     * 
     * @param undef
     *            The undefined symbol set
     * @param se
     *            The symbol to be added.
     * @param allowExt
     *            whether to allow external symbols
     */
    public static void undefinedSymbols(final Set<Symbol> undef,
        final SymbolExpression se, final boolean allowExt) {
        if (se == null)
            return;
        if (se instanceof OpExp)
            for (final SymbolExpression operand : ((OpExp) se).operands)
                undefinedSymbols(undef, operand, allowExt);
        else if (se instanceof Symbol)
            if (((Symbol) se).value != null)
                undefinedSymbols(undef, ((Symbol) se).value, allowExt);
            else if (!(se == Symbol.getSymb(":START") || allowExt
                && ((Symbol) se).global == Symbol.EXT))
                undef.add((Symbol) se);
    }
}

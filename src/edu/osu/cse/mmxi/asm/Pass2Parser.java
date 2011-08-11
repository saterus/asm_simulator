package edu.osu.cse.mmxi.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
import static edu.osu.cse.mmxi.asm.CommonParser.errorOnUndefinedSymbols;
import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;
import static edu.osu.cse.mmxi.common.Utilities.padLeft;
import static edu.osu.cse.mmxi.common.Utilities.padRight;
import static edu.osu.cse.mmxi.common.Utilities.uShortToHex;

import java.util.List;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.line.InstructionLine;
import edu.osu.cse.mmxi.asm.line.InstructionLine.Argument;
import edu.osu.cse.mmxi.asm.line.InstructionLine.ExpressionArg;
import edu.osu.cse.mmxi.asm.line.InstructionLine.StringArg;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * Handles the actual parsing of the input file and finalizing the literals and symbols.
 */
public class Pass2Parser {
    /**
     * Reference to the assembler
     */
    private final Assembler   a;

    /**
     * The location counter
     */
    private Location          lc;

    /**
     * The current line number being read from file.
     */
    private int               lineNumber;

    /**
     * The current line being read.
     */
    private String            line;

    /**
     * The instruction for the current line.
     */
    private InstructionLine   inst;

    /**
     * The error list for holding parseExceptions and errors
     */
    private final List<Error> errors;

    /**
     * Constructor for Pass2Parser. Makes references for assebler and error list.
     * 
     * @param a
     *            The reference to the assembler object
     * @param errors
     *            The reference to the errors
     */
    public Pass2Parser(final Assembler a, final List<Error> errors) {
        this.a = a;
        this.errors = errors;
    }

    /**
     * Performs the actual parsing for pass 2.
     */
    public void parse() {
        lc = Location.convertToRelative(Symbol.getSymb(":START"));
        lineNumber = 1;
        try {
            encodeHeader();
            line = a.io.getLine();
        } catch (final ParseException e) {
            errors.add(e.getError());
        }
        while (line != null) {
            try {
                final String[] parsed = checkLine(parseLine(line));
                inst = parsed[1] == null ? null : new InstructionLine(parsed);
                if (inst != null)
                    if (inst.opcode.charAt(0) == '.') {
                        if (inst.opcode.equals(".STRZ"))
                            parseSTRZ();
                        else if (inst.opcode.equals(".ENT"))
                            checkENT();
                        else if (inst.opcode.equals(".FILL"))
                            parseFILL();
                        else if (inst.opcode.equals(".BLKW"))
                            parseBLKW();
                        else
                            write(new short[0], new int[0], null);
                    } else
                        parseInstruction();
            } catch (final ParseException e) {
                errors.add(e.getError());
            }
            lineNumber++;
            try {
                line = a.io.getLine();
            } catch (final ParseException e) {
                errors.add(e.getError());
            }
        }
        try {
            encodeLiterals();
        } catch (final ParseException e) {
            errors.add(e.getError());
        }
        try {
            encodeExec();
        } catch (final ParseException e) {
            errors.add(e.getError());
        }
    }

    /**
     * Encode the machine language header record.
     * 
     * @throws ParseException
     */
    private void encodeHeader() throws ParseException {
        final SymbolExpression se = ArithmeticParser.simplify(ArithmeticParser.parseF(
            ":0 + :1 - :2", ":END", Literal.table.size(), ":START"));
        final Short len = se.evaluate();
        if (len == null) {
            errorOnUndefinedSymbols(se, false);
            throw new ParseException(AsmCodes.P2_LEN_CMX, "length = " + se);
        }
        if (a.segName == null)
            throw new ParseException(AsmCodes.P2_NO_ORIG);
        errorOnUndefinedSymbols(Symbol.getSymb(":START"), false);
        a.io.writeOLine("H" + padRight(a.segName, 6, ' ')
            + uShortToHex((short) lc.address) + uShortToHex(len));
    }

    /**
     * Encode the literals into machine language.
     * 
     * @throws ParseException
     */
    private void encodeLiterals() throws ParseException {
        lineNumber = 0;
        line = "";
        for (final Literal l : Literal.table.values())
            write(new short[] { l.contents }, new int[] { -1 }, null);
    }

    /**
     * Encode the the exec record into machine language.
     * 
     * @throws ParseException
     */
    private void encodeExec() throws ParseException {
        final Location exec = Location.convertToRelative(Symbol.getSymb(":EXEC"));
        if (exec == null || lc.isRelative ^ exec.isRelative) {
            if (Symbol.getSymb(":EXEC").value == null)
                throw new ParseException(AsmCodes.P2_NO_EXEC);
            final SymbolExpression se = ArithmeticParser
                .simplify(Symbol.getSymb(":EXEC"));
            errorOnUndefinedSymbols(se, false);
            throw new ParseException(AsmCodes.P2_EXEC_CMX, "exec = " + se);
        }
        a.io.writeOLine("E" + uShortToHex((short) exec.address));
    }

    private void checkENT() throws ParseException {
        for (final Argument a : inst.args) {
            final Symbol s = (Symbol) ((ExpressionArg) a).val;
            if (s.value == null)
                throw new ParseException(AsmCodes.P2_UNDEF_ENT, "on symbol " + s.name);
        }

    }

    /**
     * Take a .STRZ pseudo op record and add each character including a null byte
     * character for machine code.
     * 
     * @throws ParseException
     */
    private void parseSTRZ() throws ParseException {
        final String str = ((StringArg) inst.args[0]).arg;
        final short[] dat = new short[str.length() + 1];
        final int[] m = new int[str.length() + 1];
        m[0] = -1;
        for (int i = 0; i < str.length(); i++) {
            dat[i] = (short) str.charAt(i);
            m[i + 1] = -1;
        }
        write(dat, m, null);
    }

    /*
     * Take a .FILL pseudo op record and put into machine language.
     */
    private void parseFILL() throws ParseException {
        final SymbolExpression arg = ArithmeticParser.simplify(
            ((ExpressionArg) inst.args[0]).val, true);
        final Location l = Location.convertToRelative(arg);
        if (l == null) {
            if (arg instanceof Symbol && ((Symbol) arg).global == Symbol.EXT)
                write(new short[] { (short) 0 }, new int[] { 1 },
                    new String[] { "" + arg });
            errorOnUndefinedSymbols(arg, true);
            throw new ParseException(AsmCodes.P2_FILL_CMX, "fill value = " + arg);
        }
        write(new short[] { (short) l.address }, new int[] { l.isRelative ? 1 : -1 },
            null);
    }

    /**
     * Parse the .BLKW pseudo op record and put into machine language. Increments the
     * location counter to skip appropriate record count.
     * 
     * @throws ParseException
     */
    private void parseBLKW() throws ParseException {
        final SymbolExpression se = ArithmeticParser
            .simplify(((ExpressionArg) inst.args[0]).val);
        final Short len = se.evaluate();
        if (len == null) {
            errorOnUndefinedSymbols(se, false);
            throw new ParseException(AsmCodes.P2_BLK_CMX, "length = " + se);
        }
        write(new short[0], new int[0], null);
        lc.address += len;
    }

    /**
     * Parse the instruction and convert into machine language.
     * 
     * @throws ParseException
     */
    private void parseInstruction() throws ParseException {
        final Object[] ret = InstructionFormat.getInstruction(lc, inst);
        write((short[]) ret[0], (int[]) ret[1], (String[]) ret[2]);
    }

    /**
     * Used by the parsing instructions to perform the actual file writing.
     * 
     * @param data
     *            The list of 16 bit address values to write
     * @param m
     *            The M records to indicate if the data is a 9 bit or full 16 bit record
     *            for movable programs, or -1 to indicate an absolute.
     * @throws ParseException
     */
    private void write(final short[] data, final int[] m, final String[] ext)
        throws ParseException {
        if (data.length == 0)
            a.io.writeLLine((lc == null ? "      " : "("
                + uShortToHex((short) lc.address) + ")")
                + padLeft("", 26, ' ')
                + "("
                + padLeft("" + lineNumber, 4, ' ')
                + ")\t"
                + line);
        else
            for (int i = 0; i < data.length; i++) {
                final String s = ext == null ? null : ext[i];
                final char mx = s == null ? 'M' : 'X';
                a.io.writeOLine("T" + uShortToHex((short) lc.address)
                    + uShortToHex(data[i])
                    + (m[i] < 0 ? "" : "" + mx + m[i] + (s == null ? "" : s)));
                a.io.writeLLine("(" + uShortToHex((short) lc.address) + ") "
                    + uShortToHex(data[i]) + (m[i] < 0 ? "   " : " " + mx + m[i]) + " "
                    + padLeft(Integer.toBinaryString(data[i] & 0xFFFF), 16, '0') + " ("
                    + padLeft(lineNumber == 0 ? "lit" : "" + lineNumber, 4, ' ') + ")\t"
                    + (i == 0 ? line : ""));
                lc.address++;
            }
    }
}

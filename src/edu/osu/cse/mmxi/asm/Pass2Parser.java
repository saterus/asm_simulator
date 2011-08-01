package edu.osu.cse.mmxi.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
import static edu.osu.cse.mmxi.asm.CommonParser.errorOnUndefinedSymbols;
import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;

import java.io.IOException;

import edu.osu.cse.mmxi.asm.line.AssemblyLine.ExpressionArg;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.InstructionLine;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.StringArg;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public class Pass2Parser {
    private final Assembler a;
    private Location        lc;
    private int             lineNumber;
    private String          line;
    private InstructionLine inst;

    public Pass2Parser(final Assembler a) {
        this.a = a;
    }

    public void parse() throws IOException {
        try {
            lc = Location.convertToRelative(Symbol.getSymb(":START"));
            encodeHeader();
            lineNumber = 1;
            while ((line = a.io.getLine()) != null) {
                try {
                    final String[] parsed = checkLine(parseLine(line));
                    inst = parsed[1] == null ? null : new InstructionLine(parsed);
                    if (inst != null)
                        if (inst.opcode.charAt(0) == '.') {
                            if (inst.opcode.equals(".STRZ"))
                                parseSTRZ();
                            else if (inst.opcode.equals(".FILL"))
                                parseFILL();
                            else if (inst.opcode.equals(".BLKW"))
                                parseBLKW();
                            else
                                write(new short[0], new int[0]);
                        } else
                            parseInstruction();
                } catch (final ParseException e) {
                    System.err.println("At line " + lineNumber + ": " + e.getMessage());
                }
                lineNumber++;
            }
            encodeLiterals();
            encodeExec();
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
        }
    }

    private void encodeHeader() throws IOException, ParseException {
        final SymbolExpression se = ArithmeticParser.simplify(ArithmeticParser.parseF(
            ":0 + :1 - :2", ":END", Literal.table.size(), ":START"));
        final Short len = se.evaluate();
        if (len == null) {
            errorOnUndefinedSymbols(se);
            throw new ParseException("Program length " + se + " too complex to encode");
        }
        if (a.segName == null)
            a.ui.error("No .ORIG line found!");
        a.io.writeOLine("H" + padRight(a.segName, 6, ' ')
            + MemoryUtilities.uShortToHex((short) lc.address)
            + MemoryUtilities.uShortToHex(len));
    }

    private void encodeLiterals() throws IOException {
        lineNumber = 0;
        line = "";
        for (final Literal l : Literal.table.values())
            write(new short[] { l.contents }, new int[] { -1 });
    }

    private void encodeExec() throws IOException, ParseException {
        final Location exec = Location.convertToRelative(Symbol.getSymb(":EXEC"));
        if (exec == null || lc.isRelative ^ exec.isRelative) {
            final SymbolExpression se = ArithmeticParser
                .simplify(Symbol.getSymb(":EXEC"));
            errorOnUndefinedSymbols(se);
            throw new ParseException("Execution address " + se + " too complex to encode");
        }
        a.io.writeOLine("E" + MemoryUtilities.uShortToHex((short) exec.address));
    }

    private void parseSTRZ() throws IOException {
        final String str = ((StringArg) inst.args[0]).arg;
        final short[] dat = new short[str.length() + 1];
        final int[] m = new int[str.length() + 1];
        m[0] = -1;
        for (int i = 0; i < str.length(); i++) {
            dat[i] = (short) str.charAt(i);
            m[i + 1] = -1;
        }
        write(dat, m);
    }

    private void parseFILL() throws IOException, ParseException {
        final SymbolExpression arg = ArithmeticParser.simplify(
            ((ExpressionArg) inst.args[0]).val, true);
        final Location l = Location.convertToRelative(arg);
        if (l == null) {
            errorOnUndefinedSymbols(arg);
            throw new ParseException("relation " + arg + " too complex to encode");
        }
        write(new short[] { (short) l.address }, new int[] { l.isRelative ? 1 : -1 });
    }

    private void parseBLKW() throws IOException, ParseException {
        final SymbolExpression se = ArithmeticParser
            .simplify(((ExpressionArg) inst.args[0]).val);
        final Short len = se.evaluate();
        if (len == null) {
            errorOnUndefinedSymbols(se);
            throw new ParseException("block length " + se + " too complex to encode");
        }
        write(new short[0], new int[0]);
        lc.address += len;
    }

    private void parseInstruction() throws IOException, ParseException {
        final short[][] words = InstructionFormat.getInstruction(lc, inst);
        final short[] dat = new short[words.length];
        final int[] m = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            dat[i] = words[i][0];
            m[i] = words[i][1];
        }
        write(dat, m);
    }

    private void write(final short[] data, final int[] m) throws IOException {
        if (data.length == 0)
            a.io.writeLLine((lc == null ? "      " : "("
                + MemoryUtilities.uShortToHex((short) lc.address) + ")")
                + padLeft("", 26, ' ')
                + "("
                + padLeft("" + lineNumber, 4, ' ')
                + ")\t"
                + line);
        else
            for (int i = 0; i < data.length; i++) {
                a.io.writeOLine("T" + MemoryUtilities.uShortToHex((short) lc.address)
                    + MemoryUtilities.uShortToHex(data[i]) + (m[i] < 0 ? "" : "M" + m[i]));
                a.io.writeLLine("(" + MemoryUtilities.uShortToHex((short) lc.address)
                    + ") " + MemoryUtilities.uShortToHex(data[i])
                    + (m[i] < 0 ? "   " : " M" + m[i]) + " "
                    + padLeft(Integer.toBinaryString(data[i] & 0xFFFF), 16, '0') + " ("
                    + padLeft(lineNumber == 0 ? "lit" : "" + lineNumber, 4, ' ') + ")\t"
                    + (i == 0 ? line : ""));
                lc.address++;
            }
    }

    private static String padLeft(final String s, int len, final char pad) {
        len -= s.length();
        if (len < 0)
            len = 0;
        return new String(new char[len]).replace('\0', pad) + s;
    }

    private static String padRight(final String s, int len, final char pad) {
        len -= s.length();
        if (len < 0)
            len = 0;
        return s + new String(new char[len]).replace('\0', pad);
    }
}

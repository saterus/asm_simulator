package edu.osu.cse.mmxi.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;

import java.io.IOException;

import edu.osu.cse.mmxi.asm.line.AssemblyLine.ExpressionArg;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.InstructionLine;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.StringArg;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.Operator;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public class Pass2Parser {

    public static void parse(final Assembler a) throws IOException {
        for (final Symbol s : Symbol.symbs.values())
            if (s.value == null)
                if (s.name.equals(":EXEC"))
                    a.ui.error("No end record found");
                else if (!s.name.equals(":START"))
                    a.ui.error("Symbol '" + s.name + "' not defined");
        final Location lc = Location.convertToRelative(Symbol.getSymb(":START"));
        {
            // evaluate :END + [# literals] - :START to get program length
            final SymbolExpression se = ArithmeticParser.simplify(new OpExp(
                Operator.MINUS, new OpExp(Operator.PLUS, Symbol.getSymb(":END"),
                    new NumExp((short) Literal.table.size())), Symbol.getSymb(":START")));
            final Short len = se.evaluate();
            if (len == null)
                a.ui.error("Program length " + se + " too complex to encode");
            if (a.segName == null)
                a.ui.error("No .ORIG line found!");
            a.io.writeOLine("H" + padRight(a.segName, 6, ' ')
                + MemoryUtilities.uShortToHex((short) lc.address)
                + MemoryUtilities.uShortToHex(len));
        }
        int lineNumber = 1;
        String line;
        while ((line = a.io.getLine()) != null) {
            try {
                final String[] parsed = checkLine(parseLine(line));
                final InstructionLine inst = parsed[1] == null ? null
                    : new InstructionLine(parsed);
                if (inst != null && inst.opcode.charAt(0) == '.') {
                    if (inst.opcode.equals(".STRZ")) {
                        final String str = ((StringArg) inst.args[0]).arg;
                        final short[] dat = new short[str.length() + 1];
                        final int[] m = new int[str.length() + 1];
                        m[0] = -1;
                        for (int i = 0; i < str.length(); i++) {
                            dat[i] = (short) str.charAt(i);
                            m[i + 1] = -1;
                        }
                        write(a, lc, dat, m, lineNumber, line);
                    } else if (inst.opcode.equals(".FILL")) {
                        final SymbolExpression arg = ArithmeticParser.simplify(
                            ((ExpressionArg) inst.args[0]).val, true);
                        final Location l = Location.convertToRelative(arg);
                        if (l == null)
                            throw new ParseException("relation " + arg
                                + " too complex to encode");
                        write(a, lc, new short[] { (short) l.address },
                            new int[] { l.isRelative ? 1 : -1 }, lineNumber, line);
                    } else if (inst.opcode.equals(".BLKW")) {
                        final SymbolExpression se = ArithmeticParser
                            .simplify(((ExpressionArg) inst.args[0]).val);
                        final Short len = se.evaluate();
                        if (len == null)
                            throw new ParseException("block length " + se
                                + " too complex to encode");
                        write(a, lc, new short[0], new int[0], lineNumber, line);
                        lc.address += len;
                    } else
                        write(a, lc, new short[0], new int[0], lineNumber, line);
                } else if (inst != null) {
                    final short[][] words = InstructionFormat.getInstruction(lc, inst);
                    final short[] dat = new short[words.length];
                    final int[] m = new int[words.length];
                    for (int i = 0; i < words.length; i++) {
                        dat[i] = words[i][0];
                        m[i] = words[i][1];
                    }
                    write(a, lc, dat, m, lineNumber, line);
                }
            } catch (final ParseException e) {
                System.err.println("At line " + lineNumber + ": " + e.getMessage());
            }
            lineNumber++;
        }
        for (final Literal l : Literal.table.values())
            write(a, lc, new short[] { l.contents }, new int[] { -1 }, 0, "");
        final Location exec = Location.convertToRelative(Symbol.getSymb(":EXEC"));
        if (exec == null || lc.isRelative ^ exec.isRelative)
            a.ui.error("Execution address "
                + ArithmeticParser.simplify(Symbol.getSymb(":EXEC"))
                + " too complex to encode");
        a.io.writeOLine("E" + MemoryUtilities.uShortToHex((short) exec.address));
    }

    private static void write(final Assembler a, final Location loc, final short[] data,
        final int[] m, final int lineNumber, String line) throws IOException {
        if (data.length == 0)
            a.io.writeLLine((loc == null ? "      " : "("
                + MemoryUtilities.uShortToHex((short) loc.address) + ")")
                + padLeft("", 24, ' ')
                + "("
                + padLeft("" + lineNumber, 4, ' ')
                + ") "
                + line);
        else
            for (int i = 0; i < data.length; i++) {
                a.io.writeOLine("T" + MemoryUtilities.uShortToHex((short) loc.address)
                    + MemoryUtilities.uShortToHex(data[i]) + (m[i] < 0 ? "" : "M" + m[i]));
                a.io.writeLLine("(" + MemoryUtilities.uShortToHex((short) loc.address)
                    + ") " + MemoryUtilities.uShortToHex(data[i]) + "  "
                    + padLeft(Integer.toBinaryString(data[i] & 0xFFFF), 16, '0') + " ("
                    + padLeft(lineNumber == 0 ? "lit" : "" + lineNumber, 4, ' ') + ") "
                    + line);
                loc.address++;
                line = "";
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

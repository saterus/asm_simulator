package edu.osu.cse.mmxi.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;

import java.io.IOException;

import edu.osu.cse.mmxi.asm.line.AssemblyLine.ExpressionArg;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.InstructionLine;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.Label;
import edu.osu.cse.mmxi.asm.line.AssemblyLine.StringArg;
import edu.osu.cse.mmxi.asm.symb.Operator;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.ParseException;

public class Pass1Parser {
    public static void parse(final Assembler a) throws IOException {
        Symbol lcBase = Symbol.getSymb(":START");
        short lc = 0;
        int lineNumber = 1;
        int tempNumber = 0;
        String line;
        while ((line = a.io.getLine()) != null) {
            try {
                final String[] parsed = checkLine(parseLine(line));
                final Label label = parsed[0] == null ? null : new Label(parsed[0]);
                final InstructionLine inst = parsed[1] == null ? null
                    : new InstructionLine(parsed);
                if ((inst == null || !inst.opcode.matches("[.]ORIG|[.]EQU"))
                    && label != null)
                    label.symb.set(new OpExp(Operator.PLUS, lcBase, new NumExp(lc)));
                if (inst != null && inst.opcode.charAt(0) == '.') {
                    if (inst.opcode.equals(".ORIG")) {
                        if (inst.args.length > 1)
                            throw new ParseException("too many args for .ORIG");
                        if (label == null)
                            throw new ParseException("no segment name given");
                        Symbol.removeSymb(a.segName = label.symb.name);
                        if (inst.args.length > 0) {
                            if (!(inst.args[0] instanceof ExpressionArg))
                                throw new ParseException(
                                    "argument must be an immediate or expression");
                            Symbol.getSymb(":START").set(
                                ((ExpressionArg) inst.args[0]).val);
                        }
                    } else if (inst.args.length != 1)
                        throw new ParseException(inst.opcode
                            + " takes exactly one argument");
                    if (inst.opcode.equals(".EQU")) {
                        if (label == null)
                            throw new ParseException(".EQU requires a label");
                        if (!(inst.args[0] instanceof ExpressionArg))
                            throw new ParseException(
                                "argument must be an immediate or expression");
                        label.symb.set(((ExpressionArg) inst.args[0]).val);
                    } else if (inst.opcode.equals(".END")) {
                        if (!(inst.args[0] instanceof ExpressionArg))
                            throw new ParseException(
                                "argument must be an immediate or expression");
                        Symbol.getSymb(":EXEC").set(((ExpressionArg) inst.args[0]).val);
                    } else if (inst.opcode.equals(".STRZ")) {
                        if (!(inst.args[0] instanceof StringArg))
                            throw new ParseException("argument must be a string");
                        lc += ((StringArg) inst.args[0]).arg.length() + 1;
                    } else if (inst.opcode.equals(".FILL")) {
                        if (!(inst.args[0] instanceof ExpressionArg))
                            throw new ParseException(
                                "argument must be a immediate or expression");
                        lc++;
                    } else if (inst.opcode.equals(".BLKW")) {
                        if (!(inst.args[0] instanceof ExpressionArg))
                            throw new ParseException(
                                "argument must be an immediate or expression");
                        final SymbolExpression len = ((ExpressionArg) inst.args[0]).val;
                        final Short val = len.evaluate();
                        if (val != null)
                            lc += val;
                        else {
                            lcBase = Symbol.getSymb(":T" + ++tempNumber).set(
                                new OpExp(Operator.PLUS, new OpExp(Operator.PLUS, lcBase,
                                    new NumExp(lc)), len));
                            lc = 0;
                        }
                    }
                } else if (inst != null) {
                    final SymbolExpression len = InstructionFormat.getLength(inst);
                    final Short val = len.evaluate();
                    if (val != null)
                        lc += val;
                    else {
                        lcBase = Symbol.getSymb(":T" + ++tempNumber).set(
                            new OpExp(Operator.PLUS, new OpExp(Operator.PLUS, lcBase,
                                new NumExp(lc)), len));
                        lc = 0;
                    }
                }
            } catch (final ParseException e) {
                System.err.println("At line " + lineNumber + ": " + e.getMessage());
                System.out.println(Symbol.printSymbs());
            }
            lineNumber++;
        }
        try {
            Symbol.getSymb(":END").set(new OpExp(Operator.PLUS, lcBase, new NumExp(lc)));
            Literal.complete = true;
        } catch (final ParseException e) {
            System.err.println(e.getMessage());
        }
        for (final Symbol s : Symbol.symbs.values())
            s.expand();
        for (final Symbol s : Literal.table.values())
            s.expand();
        System.out.println(Symbol.printSymbs());
    }
}

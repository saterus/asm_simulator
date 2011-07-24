package edu.osu.cse.mmxi.asm.line;

import edu.osu.cse.mmxi.asm.Literal;
import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public interface AssemblyLine {
    public static class Label implements AssemblyLine {
        Symbol symb;

        public Label(final String name) {
            symb = Symbol.getSymb(name);
        }
    }

    public static class InstructionLine implements AssemblyLine {
        String     opcode;
        Argument[] args;

        public InstructionLine(final String[] line) throws ParseException {
            opcode = line[1];
            args = new Argument[line.length - 2];
            for (int i = 0; i < args.length; i++) {
                final String arg = line[i + 2];
                if (arg.matches("[rR][0-7]"))
                    args[i] = new RegisterArg((short) (arg.charAt(1) - '0'));
                else {
                    String num = arg;
                    final boolean isLiteral = num.charAt(0) == '=';
                    if (isLiteral)
                        num = num.substring(1);
                    if (num.charAt(0) == '#')
                        num = num.substring(1);
                    final int v = MemoryUtilities.parseShort(num);
                    if (v == -1)
                        try {
                            args[i] = new ExpressionArg(arg);
                        } catch (final ParseException e) {
                            throw new ParseException("on argument " + (i + 1) + ": "
                                + e.getMessage());
                        }
                    else if (isLiteral)
                        args[i] = new LiteralArg((short) v);
                    else
                        args[i] = new ImmediateArg((short) v);
                }
            }
        }
    }

    public static interface Argument {
        public int isReg();
    }

    public static class RegisterArg implements Argument {
        public short reg;

        public RegisterArg(final short r) {
            reg = r;
        }

        @Override
        public int isReg() {
            return 1;
        }
    }

    public static class ImmediateArg implements Argument {
        public short imm;

        public ImmediateArg(final short v) {
            imm = v;
        }

        @Override
        public int isReg() {
            return 0;
        }
    }

    public static class LiteralArg implements Argument {
        public Literal lit;

        public LiteralArg(final short v) {
            lit = Literal.getLiteral(v);
        }

        @Override
        public int isReg() {
            return 0;
        }
    }

    public static class ExpressionArg implements Argument {
        public SymbolExpression val;

        public ExpressionArg(final String exp) throws ParseException {
            val = ArithmeticParser.parse(exp);
        }

        @Override
        public int isReg() {
            return 2;
        }
    }
}

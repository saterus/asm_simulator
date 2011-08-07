package edu.osu.cse.mmxi.asm.line;

import edu.osu.cse.mmxi.asm.Literal;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.ParseException;

public class InstructionLine {
    public String     opcode;
    public Argument[] args;

    public InstructionLine(final String[] line) throws ParseException {
        opcode = line[1];
        args = new Argument[line.length - 2];
        for (int i = 0; i < args.length; i++) {
            final String arg = line[i + 2];
            if (arg.matches("[rR][0-7]"))
                args[i] = new RegisterArg((short) (arg.charAt(1) - '0'));
            else if (arg.matches("\".*\""))
                args[i] = new StringArg(arg.substring(1, arg.length() - 1));
            else {
                String num = arg;
                final boolean isLiteral = num.charAt(0) == '=';
                if (isLiteral)
                    num = num.substring(1);
                if (num.charAt(0) == '#')
                    num = num.substring(1);
                final Short v = Utilities.parseShort(num);
                if (v == null)
                    try {
                        args[i] = new ExpressionArg(arg);
                    } catch (final ParseException e) {
                        e.getError().setMessage(
                            "on argument " + (i + 1) + ": " + e.getMessage());
                        throw e;
                    }
                else if (isLiteral)
                    args[i] = new LiteralArg(v);
                else
                    args[i] = new ImmediateArg(v);
            }
        }
    }

    public static interface Argument {
        public int isReg();
    }

    public static class StringArg implements Argument {
        public String arg;

        public StringArg(final String s) {
            arg = s;
        }

        @Override
        public int isReg() {
            return 0;
        }

        @Override
        public String toString() {
            return "\"" + arg + "\"";
        }
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

        @Override
        public String toString() {
            return "R" + reg;
        }
    }

    public static class ExpressionArg implements Argument {
        public SymbolExpression val;

        public ExpressionArg(final String exp) throws ParseException {
            this(ArithmeticParser.parse(exp));
        }

        public ExpressionArg(final SymbolExpression exp) {
            val = exp;
        }

        @Override
        public int isReg() {
            return 2;
        }

        @Override
        public String toString() {
            return val.toString();
        }
    }

    public static class LiteralArg extends ExpressionArg {
        public LiteralArg(final short v) {
            super(Literal.getLiteral(v));
        }

        @Override
        public int isReg() {
            return 0;
        }
    }

    public static class ImmediateArg extends ExpressionArg {
        public ImmediateArg(final short v) {
            super(new NumExp(v));
        }

        @Override
        public int isReg() {
            return 0;
        }
    }
}

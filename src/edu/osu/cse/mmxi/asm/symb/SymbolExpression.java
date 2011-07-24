package edu.osu.cse.mmxi.asm.symb;

import java.util.HashSet;
import java.util.Set;

import edu.osu.cse.mmxi.asm.Symbol;

public abstract class SymbolExpression {
    public Short evaluate() {
        return evaluate(new HashSet<Symbol>());
    }

    public abstract Short evaluate(Set<Symbol> used);

    public static class OpExp extends SymbolExpression {
        public Operator           op;
        public SymbolExpression[] operands;

        public OpExp(final Operator o, final SymbolExpression... exp) {
            op = o;
            operands = exp;
        }

        @Override
        public String toString() {
            String s = "";
            for (int i = 0; i < operands.length; i++) {
                if (operands.length == 1)
                    s += op.value;
                else if (i != 0)
                    s += " " + op.value + " ";
                if (operands[i] instanceof OpExp) {
                    final OpExp oe = (OpExp) operands[i];
                    if (oe.operands.length == 2
                        && (oe.op.prec < op.prec || oe.op.prec == op.prec && op.lAssoc
                            ^ i == 0))
                        s += "(" + operands[i].toString() + ")";
                    else
                        s += operands[i].toString();
                } else
                    s += operands[i].toString();
            }
            return s;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof OpExp))
                return false;
            if (operands[1] == null && ((OpExp) o).operands[1] != null)
                return false;
            return op == ((OpExp) o).op && operands[0].equals(((OpExp) o).operands[0])
                && operands[1].equals(((OpExp) o).operands[1]);
        }

        @Override
        public Short evaluate(final Set<Symbol> used) {
            final SymbolExpression sea = operands[0];
            SymbolExpression seb = null;
            final Short va = sea.evaluate(used);
            Short vb = null;
            if (operands.length == 2)
                vb = (seb = operands[1]).evaluate(used);
            if (va == null || seb != null && vb == null)
                return null;
            switch (op) {
            case GROUP:
                return va;
            case OR:
                return (short) (va | vb);
            case XOR:
                return (short) (va ^ vb);
            case AND:
                return (short) (va & vb);
            case USHR:
                return (short) ((va & 0xFFFF) >>> vb);
            case SHR:
                return (short) (va >> vb);
            case SHL:
                return (short) (va << vb);
            case PLUS:
                if (seb == null)
                    return va;
                else
                    return (short) (va + vb);
            case MINUS:
                if (seb == null)
                    return (short) -va;
                else
                    return (short) (va - vb);
            case TIMES:
                return (short) (va * vb);
            case DIV:
                return (short) (va / vb);
            case MOD:
                return (short) (va % vb);
            case POWER:
                return (short) ((long) Math.pow(va, vb) & 0xFFFF);
            case NOT:
                return (short) ~va;
            }
            return null;
        }
    }

    public static class IfExp extends SymbolExpression {
        public SymbolExpression cond, ifExp, elseExp;

        public IfExp(final SymbolExpression c, final SymbolExpression i,
            final SymbolExpression e) {
            cond = c;
            ifExp = i;
            elseExp = e;
        }

        @Override
        public String toString() {
            if (cond instanceof OpExp && ((OpExp) cond).op == Operator.MINUS)
                return "(" + ((OpExp) cond).operands[0] + " == "
                    + ((OpExp) cond).operands[1] + " ? " + ifExp + " : " + elseExp + ")";
            return "(" + cond + " == 0 ? " + ifExp + " : " + elseExp + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof IfExp))
                return false;
            return cond.equals(((IfExp) o).cond) && ifExp.equals(((IfExp) o).ifExp)
                && elseExp.equals(((IfExp) o).elseExp);
        }

        @Override
        public Short evaluate(final Set<Symbol> used) {
            final Short cVal = cond.evaluate(used);
            return cVal == null ? null : cVal == 0 ? ifExp.evaluate(used) : elseExp
                .evaluate(used);
        }
    }

    public static class NumExp extends SymbolExpression {
        public short value;

        public NumExp(final short v) {
            value = v;
        }

        @Override
        public String toString() {
            return "" + value;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof SymbolExpression))
                return false;
            return value == ((SymbolExpression) o).evaluate();
        }

        @Override
        public Short evaluate(final Set<Symbol> used) {
            return value;
        }
    }
}

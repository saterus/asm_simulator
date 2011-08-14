package edu.osu.cse.mmxi.asm.symb;

import java.util.ArrayDeque;
import java.util.Deque;

import edu.osu.cse.mmxi.asm.Symbol;

/**
 * Symbols are stored in A SortedMap of Symbol objects keyed by their String name. The
 * constructor of the Symbol type is not public; rather, a static factory method
 * Symbol.getSymb makes sure that all symbols are registered in the table
 * 
 */
public abstract class SymbolExpression {
    public Short evaluate() {
        return evaluate(new ArrayDeque<Symbol>());
    }

    /**
     * Get the value of the Symbol.
     * 
     * @param used
     *            The set of symbols.
     * @return
     */
    public abstract Short evaluate(Deque<Symbol> used);

    /**
     * Static class for handling an expression with a symbol.
     * 
     */
    public static class OpExp extends SymbolExpression {
        public Operator           op;
        public SymbolExpression[] operands;

        public OpExp(final Operator o, final SymbolExpression... exp) {
            op = o;
            operands = exp;
        }

        /**
         * Get the string representation of the symbol.
         */
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

        /**
         * check if the object equals the current op value. Return false otherwise.
         * 
         * @return check if the object equals the current op value. Return false
         *         otherwise.
         */
        @Override
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof OpExp))
                return false;
            if (operands[1] == null && ((OpExp) o).operands[1] != null)
                return false;
            return op == ((OpExp) o).op && operands[0].equals(((OpExp) o).operands[0])
                && operands[1].equals(((OpExp) o).operands[1]);
        }

        /**
         * Evaluate the expression based on its operand.
         */
        @Override
        public Short evaluate(final Deque<Symbol> used) {
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
            case IF:
                return (short) (va != 0 ? vb : 0);
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

    /**
     * Static class used for storing immediate numeric values.
     * 
     */
    public static class NumExp extends SymbolExpression {
        public short value;

        /**
         * Constructor. Just initialize the value.
         * 
         * @param v
         *            The numeric value.
         */
        public NumExp(final short v) {
            value = v;
        }

        /**
         * Convert numberic value to string showing sign. Return hex value.
         */
        @Override
        public String toString() {
            return (value < 0 ? "-x" : "x")
                + Integer.toHexString(value < 0 ? -value : value).toUpperCase();
        }

        /**
         * Check if the value is equal to a symbolExpression value.
         * 
         * @return Return true if they are equal, false otherwise.
         */
        @Override
        public boolean equals(final Object o) {
            if (o == null || !(o instanceof SymbolExpression))
                return false;
            return value == ((SymbolExpression) o).evaluate();
        }

        /**
         * Return the numeric value.
         * 
         * @return Return the numeric value.
         */
        @Override
        public Short evaluate(final Deque<Symbol> used) {
            return value;
        }
    }
}

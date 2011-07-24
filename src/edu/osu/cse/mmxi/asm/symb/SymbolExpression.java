package edu.osu.cse.mmxi.asm.symb;


public interface SymbolExpression {
    public Short evaluate();

    public static class OpExp implements SymbolExpression {
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
        public Short evaluate() {
            final SymbolExpression sea = operands[0];
            SymbolExpression seb = null;
            final Short va = sea.evaluate();
            Short vb = null;
            if (operands.length == 2)
                vb = (seb = operands[1]).evaluate();
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

    public static class NumExp implements SymbolExpression {
        public short value;

        public NumExp(final short v) {
            value = v;
        }

        @Override
        public String toString() {
            return "" + value;
        }

        @Override
        public Short evaluate() {
            return value;
        }
    }
}

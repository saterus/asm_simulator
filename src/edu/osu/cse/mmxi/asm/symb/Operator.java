package edu.osu.cse.mmxi.asm.symb;

public enum Operator {
    GROUP(0, "(", 5),

    OR(1, "|", 3),

    XOR(2, "^", 3),

    AND(3, "&", 3),

    USHR(4, ">>>", 3), SHR(4, ">>", 3), SHL(4, "<<", 3),

    PLUS(5, "+", 7), MINUS(5, "-", 7),

    // this one is out of prec. order because TIMES has an operator that is a prefix of
    // this one, so POWER must come first (same with USHR and SHR)
    POWER(7, "**", 2),

    TIMES(6, "*", 3), DIV(6, "/", 3), MOD(6, "%", 3),

    // unary operators automatically get top precedence
    NOT(8, "~", 4), HASH(8, "#", 4), LIT(8, "=", 4);

    public int    prec;
    public String value;
    public boolean unary, binary, lAssoc;

    Operator(final int p, final String v, final int ubl) {
        prec = p;
        value = v;
        unary = (ubl & 4) != 0;
        binary = (ubl & 2) != 0;
        lAssoc = (ubl & 1) != 0;
    }

    public static Operator get(final String v) {
        for (final Operator o : values())
            if (v.equals(o.value))
                return o;
        return null;
    }
}

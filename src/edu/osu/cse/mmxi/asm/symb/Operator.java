package edu.osu.cse.mmxi.asm.symb;

/**
 * This is an enumeration for handling the expression operators.
 * 
 */
public enum Operator {
    GROUP(0, "(", 5),

    IF(1, "?", 3),

    OR(2, "|", 3),

    XOR(3, "^", 3),

    AND(4, "&", 3),

    USHR(5, ">>>", 3), SHR(5, ">>", 3), SHL(5, "<<", 3),

    PLUS(6, "+", 7), MINUS(6, "-", 7),

    // this one is out of prec. order because TIMES has an operator that is a prefix of
    // this one, so POWER must come first (same with USHR and SHR)
    POWER(8, "**", 2),

    TIMES(7, "*", 3), DIV(7, "/", 3), MOD(7, "%", 3),

    // unary operators automatically get top precedence
    NOT(9, "~", 4), HASH(9, "#", 4), LIT(9, "=", 4);

    public int    prec;
    public String value;
    public boolean unary, binary, lAssoc;

    /**
     * Constructor.
     * 
     * @param p
     *            The preceding value
     * @param v
     *            The value of the expression.
     * @param ubl
     *            Determines what type of value this is.
     */
    Operator(final int p, final String v, final int ubl) {
        prec = p;
        value = v;
        unary = (ubl & 4) != 0;
        binary = (ubl & 2) != 0;
        lAssoc = (ubl & 1) != 0;
    }

    /**
     * Get the operator value.
     * 
     * @param v
     *            The string rep of the operator
     * @return
     */
    public static Operator get(final String v) {
        for (final Operator o : values())
            if (v.equals(o.value))
                return o;
        return null;
    }
}

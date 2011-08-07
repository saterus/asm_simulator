package edu.osu.cse.mmxi.asm;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.error.ParseException;

public class Symbol extends SymbolExpression {
    public static SortedMap<String, Symbol> symbs = new TreeMap<String, Symbol>();
    public String                           name;
    public SymbolExpression                 value;

    public static Symbol getSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        if (!symbs.containsKey(sName))
            symbs.put(sName, new Symbol(name));
        return symbs.get(sName);
    }

    public static void removeSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        symbs.remove(sName);
    }

    public Symbol set(final SymbolExpression se) throws ParseException {
        if (value != null)
            throw new ParseException(AsmCodes.P1_SYMB_RESET, "symbol " + name
                + " already set to " + value);
        value = ArithmeticParser.simplify(se, false);
        evaluate();
        return this;
    }

    public void expand() {
        evaluate(); // will catch any recursion
        value = ArithmeticParser.simplify(ArithmeticParser.expand(value, this));
    }

    protected Symbol(final String n) {
        name = n;
        value = null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Short evaluate(final Set<Symbol> used) {
        if (value == null || used == null)
            return null;
        if (used.contains(this)) {
            String msg = "recursion detected: \n";
            for (final Symbol s : used)
                msg += s.name + " = "
                    + ArithmeticParser.simplify(s.value, false).toString() + "\n";
            throw new RuntimeException(msg);
        }
        used.add(this);
        final Short eval = value.evaluate(used);
        used.remove(this);
        if (ArithmeticParser.collapseIfEvaluable && eval != null
            && !(value instanceof NumExp))
            value = new NumExp(eval);
        return eval;
    }

    public static String printSymbs() {
        String ret = "";
        for (final Symbol s : symbs.values())
            ret += s.value == null ? s.name + " =.\n" : s.name + " = " + s.value + "\n";
        if (Literal.complete)
            for (final Symbol s : Literal.table.values())
                ret += s.name + " = " + s.value + "\n";
        return ret;
    }
}

package edu.osu.cse.mmxi.asm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.ParseException;

public class Symbol extends SymbolExpression {
    public static Map<String, Symbol> symbs = new HashMap<String, Symbol>();
    public String                     name;
    public SymbolExpression           value;

    public static Symbol getSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        if (!symbs.containsKey(sName))
            symbs.put(sName, new Symbol(name));
        return symbs.get(sName);
    }

    public Symbol set(final SymbolExpression se) throws ParseException {
        if (value != null)
            throw new ParseException("symbol " + name + " already set to " + value);
        value = se;
        evaluate();
        return this;
    }

    private Symbol(final String n) {
        name = n;
        value = null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Short evaluate(final Set<Symbol> used) {
        if (value == null)
            return null;
        if (used.contains(this)) {
            String msg = "recursion detected: \n";
            for (final Symbol s : used)
                msg += s.name + " = " + s.value.toString() + "\n";
            throw new RuntimeException(msg);
        }
        used.add(this);
        final Short eval = value.evaluate(used);
        if (ArithmeticParser.collapseIfEvaluable && eval != null
            && !(value instanceof NumExp))
            value = new NumExp(eval);
        return eval;
    }

    public static String printSymbs() {
        String ret = "";
        for (final Symbol s : symbs.values())
            ret += s.value == null ? s.name + " =.\n" : s.name + " = " + s.value + "\n";
        return ret;
    }
}

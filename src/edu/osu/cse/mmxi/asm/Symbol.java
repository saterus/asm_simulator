package edu.osu.cse.mmxi.asm;

import java.util.HashMap;
import java.util.Map;

import edu.osu.cse.mmxi.asm.SymbolExpression.Symbol;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;

public class Symbol implements SymbolExpression {
    public static Map<String, Symbol> symbs = new HashMap<String, Symbol>();
    public String                     name;
    public SymbolExpression           value;

    public static Symbol getSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        if (!symbs.containsKey(sName))
            symbs.put(sName, new Symbol(name));
        return symbs.get(sName);
    }

    public static Symbol setSymb(final String name, final SymbolExpression se) {
        final Symbol s = getSymb(name);
        s.value = se;
        return s;
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
    public Short evaluate() {
        if (value == null)
            return null;
        return value.evaluate();
    }
}
package edu.osu.cse.mmxi.asm;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * This is the Symbol table row representation
 * 
 * Represented by a tuple of (String<name>, SymbolExpression<value>). Representation also
 * holds a table (SortedMap) of <name,value> pairs of all Symbols.
 * 
 */
public class Symbol extends SymbolExpression {
    /**
     * Holds a representation of all symbols
     */
    public static SortedMap<String, Symbol> symbs = new TreeMap<String, Symbol>();

    /**
     * The specific Symbol's string representation
     */
    public String                           name;

    /**
     * The specific Symbol's value
     */
    public SymbolExpression                 value;

    /**
     * The global type: one of LOCAL, EXT, ENT
     */
    public int                              global;
    public static final int                 LOCAL = 0, EXT = 1, ENT = 2;

    /**
     * Retrieve a symbol from the SortedMap
     * 
     * @param name
     *            The symbol name to retrieve
     * @return Symbol
     */
    public static Symbol getSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        if (!symbs.containsKey(sName))
            symbs.put(sName, new Symbol(name));
        return symbs.get(sName);
    }

    /**
     * Remove a symbol from the SortedMap
     * 
     * @param name
     *            The symbol name to be removed
     */
    public static void removeSymb(final String name) {
        final String sName = name.length() > 6 ? name.substring(0, 6) : name;
        symbs.remove(sName);
    }

    /**
     * Set the value of a symbol.
     * 
     * @param se
     *            The value of the symbol to set
     * @return This representation of the symbol
     * @throws ParseException
     */
    public Symbol set(final SymbolExpression se) throws ParseException {
        if (value != null)
            throw new ParseException(AsmCodes.P1_SYMB_RESET, "symbol " + name
                + " already set to " + value);
        else if (global == EXT)
            throw new ParseException(AsmCodes.P1_DEF_EXT, "external symbol " + name
                + " being defined to " + value);
        value = ArithmeticParser.simplify(se, false);
        evaluate();
        return this;
    }

    /**
     * Wrapper for the evaluate method and update the value of the symbol based on the
     * Sorted map symbol list.
     */
    public void expand() {
        evaluate(); // will catch any recursion
        value = ArithmeticParser.simplify(ArithmeticParser.expand(value, this));
    }

    /**
     * Initialize the symbol
     * 
     * @param n
     */
    protected Symbol(final String n) {
        name = n;
        value = null;
        global = LOCAL;
    }

    /**
     * Return the string representation of the symbol (name)
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Evaluate the SortedMap of symbols and looks for recursion. If the symbol is already
     * in the map, then it is detected if another matching symbol value exists.
     * 
     * @return Will return the value of the symbol or null.
     */
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

    /**
     * Return a string representing the contents of the Symbol SortedMap.
     * 
     * @return String representing the contents of the Symbol SortedMap.
     */
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

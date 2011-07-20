package edu.osu.cse.mmxi.table;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // has map that holds the value of discovered symbol and their location values
    private static Map<String, Integer> symbolMap = new HashMap<String, Integer>();

    // This map holds the thing an equated symbol points to.
    private static Map<String, String>  equateMap = new HashMap<String, String>();

    public void defineSymbol(final String label, final int location) {
        symbolMap.put(label, location);
    }

    public void defineEqu(final String label, final String equated) {
        equateMap.put(label, equated);
    }

    public String getEqu(final String label) {
        return equateMap.get(label);
    }

    public static boolean isSymbolDefined(final String label) {
        final boolean checkSymbol = symbolMap.containsKey(label);

        return checkSymbol;

    }

    public void updateLocation(final String label, final int location) {
        symbolMap.put(label, location);
    }

    // only gets you the line location, not the actual address.
    public int getLocation(final String label) {
        final int location = symbolMap.get(label);
        return location;

    }

}

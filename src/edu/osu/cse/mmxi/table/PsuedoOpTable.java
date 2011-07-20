package edu.osu.cse.mmxi.table;

import java.util.HashMap;
import java.util.Map;

public class PsuedoOpTable {

    // a directive map that contains Directive Words and Directive_Number
    private final Map<String, Integer> table;

    // empty Constructor
    // Changed this to just create the table, no reason not to here -DMB 4/10/11
    public PsuedoOpTable() {
        table = addPseudoMap();
    }

    private Map<String, Integer> addPseudoMap() {

        final Map<String, Integer> temp = new HashMap<String, Integer>();

        temp.put(".ORIG", 1);
        temp.put(".END", 2);
        temp.put(".EQU", 3);
        temp.put(".FILL", 4);
        temp.put(".STRZ", 5);
        temp.put(".BLKW", 6);

        return temp;

    }

}

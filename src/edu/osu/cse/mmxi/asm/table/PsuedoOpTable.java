package edu.osu.cse.mmxi.asm.table;

import java.util.HashMap;
import java.util.Map;

public class PsuedoOpTable {

    // a directive map that contains Directive Words and Directive_Number
    public static final Map<String, Integer> table = new HashMap<String, Integer>();
    static {
        table.put(".ORIG", 1);
        table.put(".END", 2);
        table.put(".EQU", 3);
        table.put(".FILL", 4);
        table.put(".STRZ", 5);
        table.put(".BLKW", 6);
    }
}

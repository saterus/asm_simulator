package edu.osu.cse.mmxi.junit.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.common.ParseException;

public class CommonParserTest {

    @Test
    public void testParseLine() throws ParseException {
        assertArrayEquals(new String[] { "bla", null }, parseLine("bla "));
        assertArrayEquals(new String[] { null, "NOP" }, parseLine(" NOP"));
        assertArrayEquals(new String[] { null, "NOP" }, parseLine("NOP ; comment"));
        assertArrayEquals(new String[] { "BRz", null }, parseLine("BRz"));
        assertArrayEquals(new String[] { "bla", "NOP" }, parseLine("bla NOP"));
        assertArrayEquals(new String[] { "bla", "BRz", "x0" }, parseLine("bla BRz x0"));
        assertArrayEquals(new String[] { "NOP", "NOP" }, parseLine("NOP NOP"));
        assertArrayEquals(new String[] { "NOP", "BRz", "x0" }, parseLine("NOP BRz x0"));
        assertArrayEquals(new String[] { null, "BRz", "x0" }, parseLine("BRz x0"));
        assertArrayEquals(new String[] { null, "LDR", "R0", "x0" },
            parseLine("LDR R0, x0"));
        assertArrayEquals(new String[] { "BRz", "NOP" }, parseLine("BRz NOP"));
        assertArrayEquals(new String[] { null, "LDR", "NOP", "x0" },
            parseLine("LDR NOP, x0"));
        assertArrayEquals(new String[] { null, "BRz", "BRz" }, parseLine("BRz BRz"));
        assertArrayEquals(new String[] { "BRz", "BRz", "x0" }, parseLine("BRz BRz x0"));
    }

}

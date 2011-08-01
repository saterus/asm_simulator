package edu.osu.cse.mmxi.junit.asm;

import static edu.osu.cse.mmxi.asm.CommonParser.checkLine;
import static edu.osu.cse.mmxi.asm.CommonParser.parseLine;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import edu.osu.cse.mmxi.asm.error.ParseException;

public class CommonParserTest {

    @Test
    public void testParseLine() throws ParseException {
        assertArrayEquals(new String[] { null, null }, parseLine("; comment"));
        assertArrayEquals(new String[] { "bla", null }, parseLine("bla "));
        assertArrayEquals(new String[] { null, "NOP" }, parseLine(" nop"));
        assertArrayEquals(new String[] { null, "NOP" }, parseLine(" NOP ; comment"));
        assertArrayEquals(new String[] { "BRz", null }, parseLine("BRz"));
        assertArrayEquals(new String[] { "bla", "NOP" }, parseLine("bla NOP"));
        assertArrayEquals(new String[] { "bla", "BRZ", "x0" }, parseLine("bla BRz x0"));
        assertArrayEquals(new String[] { "NOP", "NOP" }, parseLine("NOP NOP"));
        assertArrayEquals(new String[] { "NOP", "BRZ", "x0" }, parseLine("NOP BRz x0"));
        assertArrayEquals(new String[] { null, "BRZ", "x0" }, parseLine(" BRz x0"));
        assertArrayEquals(new String[] { null, "LDR", "R0", "x0" },
            parseLine(" LDR R0, x0"));
        assertArrayEquals(new String[] { "BRz", "NOP" }, parseLine("BRz NOP"));
        assertArrayEquals(new String[] { null, "LDR", "NOP", "x0" },
            parseLine(" LDR NOP, x0"));
        assertArrayEquals(new String[] { null, "BRZ", "BRz" }, parseLine(" BRz BRz"));
        assertArrayEquals(new String[] { "BRz", "BRZ", "x0" }, parseLine("BRz BRz x0"));
    }

    @Test
    public void testCheckLine() {
        try {
            checkLine(new String[] { "0xBEEF", "NOP" });
            fail("no exception thrown");
        } catch (final ParseException e) {
            assertEquals("numbers can not be labels", e.getMessage());
        }
        try {
            checkLine(new String[] { "R7", "NOP" });
            fail("no exception thrown");
        } catch (final ParseException e) {
            assertEquals("registers can not be labels", e.getMessage());
        }
        try {
            checkLine(new String[] { null, "BLA" });
            fail("no exception thrown");
        } catch (final ParseException e) {
            assertEquals("unknown opcode", e.getMessage());
        }
        try {
            checkLine(new String[] { null, "BRZ" });
            fail("no exception thrown");
        } catch (final ParseException e) {
            assertEquals("incorrect number of arguments", e.getMessage());
        }
        try {
            checkLine(new String[] { null, null });
            checkLine(new String[] { "bla", null });
            checkLine(new String[] { null, "NOP" });
            checkLine(new String[] { "BRz", null });
            checkLine(new String[] { "bla", "NOP" });
            checkLine(new String[] { "bla", "BRZ", "x0" });
            checkLine(new String[] { "NOP", "NOP" });
            checkLine(new String[] { "NOP", "BRZ", "x0" });
            checkLine(new String[] { null, "BRZ", "x0" });
            checkLine(new String[] { null, "LDR", "R0", "x0" });
            checkLine(new String[] { "BRz", "NOP" });
            checkLine(new String[] { null, "LDR", "NOP", "x0" });
            checkLine(new String[] { null, "BRZ", "BRz" });
            checkLine(new String[] { "BRz", "BRZ", "x0" });
        } catch (final ParseException e) {
            fail("exception thrown: " + e.getMessage());
        }
    }

}

package edu.osu.cse.mmxi.junit.sim;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.common.MemoryUtilities;

public class MemoryUtilitiesTest {

    @Test
    public void pageAddressTest() {
        final short test = (short) 0xFFFF;
        final byte res = MemoryUtilities.pageAddress(test);

        assertEquals(res, 0x7F);
    }

    @Test
    public void addressOffsetTest() {
        final short test = (short) 0xFFFF;
        final short res = MemoryUtilities.addressOffset(test);

        assertEquals(511, res);
    }

    @Test
    public void uShortToHexTest() {
        final short test = (short) 0xFECD;

        assertEquals(MemoryUtilities.uShortToHex(test), "FECD");
    }

    @Test
    public void sShortToHexTest() {
        final short test = (short) 0x001 * -1;
        assertEquals(MemoryUtilities.sShortToHex(test), "-1");
    }

    @Test
    public void sShortToHexUppderLimitTest() {
        final short test = Short.MAX_VALUE;

        assertEquals(MemoryUtilities.sShortToHex(test), "7FFF");
    }

    @Test
    public void sShortToHexLowerLimitTest() {
        final short test = Short.MIN_VALUE;// -32768;

        assertEquals(MemoryUtilities.sShortToHex(test), "-8000");
    }

    // cannot really test this
    @Test
    public void rrandomShortTest() {
        // assertEquals((MemoryUtilities.randomShort() != 0), true);
    }
}

package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

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

        assertEquals(MemoryUtilities.sShortToHex(test), String.valueOf(Short.MIN_VALUE));
    }

}

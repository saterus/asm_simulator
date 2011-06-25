package edu.osu.cse.mmxi.machine.memory;

public class MemoryUtilities {

    public static byte pageAddress(final short s) {
        return (byte) (s >> 9 & 0x7f);
    }

    public static short addressOffset(final short s) {
        return (byte) (s & 0x1ff);
    }

    public static String shortToHex(final short s) {
        return String.format("%x", s);
    }
}

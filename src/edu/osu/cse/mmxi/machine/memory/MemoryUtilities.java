package edu.osu.cse.mmxi.machine.memory;

public class MemoryUtilities {
    public static long seed = System.nanoTime(), rand = 0;

    public static byte pageAddress(final short s) {
        return (byte) (s >> 9 & 0x7f);
    }

    public static short addressOffset(final short s) {
        return (short) (s & 0x1ff);
    }

    public static String uShortToHex(final short s) {
        return String.format("%X", s + 0x20000).substring(1);
    }

    public static String sShortToHex(final short s) {
        if (s < 0)
            return "-" + String.format("%X", -s);
        else
            return String.format("%X", s);
    }

    public static short randomShort() {
        if (rand == 0) {
            seed ^= seed << 21;
            seed ^= seed >>> 35;
            seed ^= seed << 4;
            rand = seed;
        }
        final short s = (short) (rand & 0xffffL);
        rand >>>= 16;
        return s;
    }
}

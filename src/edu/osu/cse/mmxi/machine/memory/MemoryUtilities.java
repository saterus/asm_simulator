package edu.osu.cse.mmxi.machine.memory;

/**
 * The <code>MemoryUtilities</code> class contains several methods of general-purpose use
 * which are used throughout the program.
 * 
 */
public class MemoryUtilities {
    private static long seed = System.nanoTime(), rand = 0;

    /**
     * Gets the page number of a 16-bit memory address by extracting the high 7 bits.
     * 
     * @param addr
     *            the address in memory
     * @return the page number
     */
    public static byte pageAddress(final short addr) {
        return (byte) (addr >> 9 & 0x7f);
    }

    /**
     * Gets the page offset of a 16-bit memory address by extracting the low 9 bits.
     * 
     * @param addr
     *            the address in memory
     * @return the page offset
     */
    public static short addressOffset(final short addr) {
        return (short) (addr & 0x1ff);
    }

    /**
     * Converts the incoming word argument to a 4-digit padded capital hexadecimal string.
     * 
     * @param s
     *            the word to convert
     * @return the word as a string
     */
    public static String uShortToHex(final short s) {
        return String.format("%X", s + 0x20000).substring(1);
    }

    /**
     * Converts the incoming word argument to a signed capital hexadecimal string with a
     * negative sign (not padded).
     * 
     * @param s
     *            the word to convert
     * @return the word as a string
     */
    public static String sShortToHex(final short s) {
        if (s < 0)
            return "-" + String.format("%X", -s);
        else
            return String.format("%X", s);
    }

    /**
     * Generates a random word, manipulating an internal seed which is initially set to
     * the system time.
     * 
     * @return a random word
     */
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

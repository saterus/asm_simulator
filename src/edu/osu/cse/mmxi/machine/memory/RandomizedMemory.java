package edu.osu.cse.mmxi.machine.memory;

/**
 * An implementation of physical memory where all words in all pages are initialized to
 * random bytes instead of zeros.
 */
public class RandomizedMemory implements Memory {

    // TODO: Remove the random bit-shift and bitwise-ANDs and put them into methods.
    // Possibly into some sort of MemoryUtilities class.

    public short[][]        memory;
    public long             seed;

    public final static int DEFAULT_NUM_PAGES = 0x80;

    public RandomizedMemory() {
        this(DEFAULT_NUM_PAGES);
    }

    public RandomizedMemory(final int numPages) {
        this.memory = new short[numPages][];
        this.seed = System.nanoTime();
    }

    @Override
    public short getMemory(final short relativeOffset) {
        return this.getMemory((byte) (relativeOffset >> 9 & 0x7f),
                (short) (relativeOffset & 0x1ff));
    }

    @Override
    public short getMemory(final byte page, final short pageOffset) {
        return this.getPage(page)[pageOffset];
    }

    @Override
    public void setMemory(final short relativeOffset, final short value) {
        this.setMemory((byte) (relativeOffset >> 9 & 0x7f),
                (short) (relativeOffset & 0x1ff), value);
    }

    @Override
    public void setMemory(final byte page, final short pageOffset, final short value) {
        this.getPage(page)[pageOffset] = value;
    }

    /**
     * Lazily initializes a page of memory to a random 16-bit value.
     * 
     * @param page
     *            the ith page in memory
     * @return the words of memory that make up the ith page.
     */
    private short[] getPage(final byte page) {
        if (this.memory[page] == null) {
            this.memory[page] = new short[0x200];
            long rand = 0;
            for (int i = 0; i < 0x200; i++) {
                if (rand == 0) {
                    this.seed ^= this.seed << 21;
                    this.seed ^= this.seed >>> 35;
                    this.seed ^= this.seed << 4;
                    rand = this.seed;
                }
                this.memory[page][i] = (short) (rand & 0xffffL);
                rand >>= 32;
            }
        }
        return this.memory[page];
    }
}

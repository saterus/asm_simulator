package edu.osu.cse.mmxi.machine.memory;

/**
 * An implementation of physical memory where all words in all pages are initialized to
 * random bytes instead of zeros. RandomizedMemory is stored internally as a short[][] for
 * efficient random access to pages and words.
 */
public class RandomizedMemory implements Memory {

    public short[][]        memory;

    public final static int DEFAULT_NUM_PAGES = 0x80;

    public RandomizedMemory() {
        this(DEFAULT_NUM_PAGES);
    }

    public RandomizedMemory(final int numPages) {
        memory = new short[numPages][];
    }

    @Override
    public short getMemory(final short absoluteAddress) {
        return this.getMemory(MemoryUtilities.pageAddress(absoluteAddress),
            MemoryUtilities.addressOffset(absoluteAddress));
    }

    @Override
    public short getMemory(final byte page, final short pageOffset) {
        return getPage(page)[pageOffset];
    }

    @Override
    public void setMemory(final short absoluteAddress, final short value) {
        this.setMemory(MemoryUtilities.pageAddress(absoluteAddress),
            MemoryUtilities.addressOffset(absoluteAddress), value);
    }

    @Override
    public void setMemory(final byte page, final short pageOffset, final short value) {
        getPage(page)[pageOffset] = value;
    }

    /**
     * Lazily initializes a page of memory to a random 16-bit value.
     * 
     * @param page
     *            the ith page in memory
     * @return the words of memory that make up the ith page.
     */
    private short[] getPage(final byte page) {
        if (memory[page] == null) {
            memory[page] = new short[0x200];
            for (int i = 0; i < 0x200; i++)
                memory[page][i] = MemoryUtilities.randomShort();
        }
        return memory[page];
    }
}

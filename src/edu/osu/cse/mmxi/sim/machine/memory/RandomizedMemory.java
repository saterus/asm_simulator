package edu.osu.cse.mmxi.sim.machine.memory;

import edu.osu.cse.mmxi.common.MemoryUtilities;

/**
 * An implementation of physical memory where all words in all pages are initialized to
 * random bytes instead of zeros.
 */
public class RandomizedMemory extends PagedMemory {

    /**
     * Creates a {@code RandomizedMemory} object with 128 pages. The memory itself is not
     * initialized at this step, being instead initialized when the page is first used.
     */
    public RandomizedMemory() {
        super();
    }

    /**
     * Creates a {@code RandomizedMemory} object with a custom number of pages. The memory
     * itself is not initialized at this step, being instead initialized when the page is
     * first used.
     * 
     * @param numPages
     *            the number of pages in memory
     */
    public RandomizedMemory(final int numPages) {
        super(numPages);
    }

    /**
     * Lazily initializes a page of memory to a random 16-bit value.
     * 
     * @param page
     *            the <i>i</i>th page in memory
     * @return the words of memory that make up the <i>i</i>th page.
     */
    @Override
    protected short[] getPage(final byte page) {
        if (memory[page] == null) {
            memory[page] = new short[0x200];
            for (int i = 0; i < 0x200; i++)
                memory[page][i] = MemoryUtilities.randomShort();
        }
        return memory[page];
    }
}

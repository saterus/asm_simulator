package edu.osu.cse.mmxi.machine.memory;

/**
 * An implementation of physical memory where all words in all pages are initialized to
 * random bytes instead of zeros.
 */
public class RandomizedMemory extends PagedMemory {

    public RandomizedMemory() {
        super();
    }

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

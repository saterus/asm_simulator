package edu.osu.cse.mmxi.machine.memory;

/**
 * An implementation of physical memory where all words in all pages are initialized to a
 * particular <i>fill</i> value.
 */
public class FillMemory extends PagedMemory {

    private final short fill;

    /**
     * Creates a new {@link PagedMemory} object with a known fill value (although the
     * memory is not initialized immediately).
     * 
     * @param _fill
     */
    public FillMemory(final short _fill) {
        super();
        fill = _fill;
    }

    public FillMemory(final int numPages, final short _fill) {
        super(numPages);
        fill = _fill;
    }

    /**
     * Retrieves a page of memory, lazily initializing it to the fill value.
     * 
     * @param page
     *            the <i>i</i>th page in memory
     * @return the words of memory that make up the <i>i</i>th page.
     */
    @Override
    protected short[] getPage(final byte page) {
        if (memory[page] == null) {
            memory[page] = new short[0x200];
            if (fill != 0)
                for (int i = 0; i < 0x200; i++)
                    memory[page][i] = fill;
        }
        return memory[page];
    }
}

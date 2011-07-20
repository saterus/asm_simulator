package edu.osu.cse.mmxi.sim.machine.memory;

/**
 * <p>
 * A representation of random access memory within a hardware computer system. Memory is
 * divided into pages and then further into words. Words are 16-bit Java {@code short}s.
 * {@code PagedMemory} is stored internally as a {@code short[][]} for efficient random
 * access to pages and words.
 * </p>
 * 
 * <p>
 * A memory address is given by a 16-bit quantity where the upper 7 bits denote the page
 * number and the lower 9 bits denote the offset within that page.
 * </p>
 */
public abstract class PagedMemory implements Memory {

    /**
     * The words of memory, stored as a double array with pages at the top level and page
     * offsets at the second level.
     */
    protected short[][]     memory;

    /**
     * The default number of pages. The value of this variable, {@code 0x80}, cannot
     * easily be changed because the number of bits of the page encoding (7) is hard-coded
     * into the instruction format.
     */
    public final static int DEFAULT_NUM_PAGES = 0x80;

    /**
     * Creates a {@code PagedMemory} object with 128 pages. The memory itself is not
     * initialized at this step, being instead initialized when the page is first used.
     */
    public PagedMemory() {
        this(DEFAULT_NUM_PAGES);
    }

    /**
     * Creates a {@code PagedMemory} object with a custom number of pages. The memory
     * itself is not initialized at this step, being instead initialized when the page is
     * first used.
     * 
     * @param numPages
     *            the number of pages in memory
     */
    public PagedMemory(final int numPages) {
        memory = new short[numPages][];
    }

    /**
     * Retrieves the 16-bit word stored at the absolute memory address.
     * 
     * A memory address is given by a 16-bit quantity where the upper 7 bits denote the
     * page number and the lower 9 bits denote the offset within that page.
     * 
     * @param absoluteAddress
     *            16-bit representation of the absolute memory address.
     * @return the contents of the address.
     */
    @Override
    public short getMemory(final short absoluteAddress) {
        return this.getMemory(MemoryUtilities.pageAddress(absoluteAddress),
            MemoryUtilities.addressOffset(absoluteAddress));
    }

    /**
     * Retrieves the 16-bit word stored at the relative memory offset of the indicated
     * page.
     * 
     * @param page
     *            the jth page in all of memory
     * @param pageOffset
     *            the ith word of the jth page.
     * @return the contents of the ith word of the jth page.
     */
    @Override
    public short getMemory(final byte page, final short pageOffset) {
        return getPage(page)[pageOffset];
    }

    /**
     * Sets the contents of the 16-bit word stored at the absolute memory address of the
     * current page to a 16-bit value.
     * 
     * A memory address is given by a 16-bit quantity where the upper 7 bits denote the
     * page number and the lower 9 bits denote the offset within that page.
     * 
     * @param absoluteAddress
     *            16-bit representation of the absolute memory address.
     * @param value
     *            a 16-bit value to be stored in memory.
     */
    @Override
    public void setMemory(final short absoluteAddress, final short value) {
        this.setMemory(MemoryUtilities.pageAddress(absoluteAddress),
            MemoryUtilities.addressOffset(absoluteAddress), value);
    }

    /**
     * Sets the contents of the 16-bit word stored at the relative memory offset of the
     * current page to a 16-bit value.
     * 
     * @param page
     *            the jth page in all of memory
     * @param pageOffset
     *            the ith word of the jth page.
     * @param value
     *            a 16-bit value to be stored in memory.
     */
    @Override
    public void setMemory(final byte page, final short pageOffset, final short value) {
        getPage(page)[pageOffset] = value;
    }

    /**
     * Retrieves a page of memory, accessed through a function to allow for lazy
     * initialization.
     * 
     * @param page
     *            the <i>i</i>th page in memory
     * @return the words of memory that make up the <i>i</i>th page.
     */
    protected abstract short[] getPage(final byte page);
}

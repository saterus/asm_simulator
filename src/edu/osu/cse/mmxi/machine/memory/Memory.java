package edu.osu.cse.mmxi.machine.memory;

/**
 * A representation of random access memory within a hardware computer system. Memory is
 * divided into pages and then further into words. Words are 16-bit Java `shorts`.
 */
public interface Memory {

    /**
     * Retrieves the 16-bit word stored at the relative memory offset of the current page.
     * 
     * @param relativeOffset
     *            the ith word of the current page.
     * @return the contents of the ith word of the current page.
     */
    public abstract short getMemory(short relativeOffset);

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
    public abstract short getMemory(byte page, short pageOffset);

    /**
     * Sets the contents of the 16-bit word stored at the relative memory offset of the
     * current page to a 16-bit value.
     * 
     * @param relativeOffset
     *            the ith word of the current page.
     * @param value
     *            a 16-bit value to be stored in memory.
     */
    public abstract void setMemory(short relativeOffset, short value);

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
    public abstract void setMemory(byte page, short pageOffset, short value);

}

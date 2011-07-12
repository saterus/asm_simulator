package edu.osu.cse.mmxi.machine;

import edu.osu.cse.mmxi.machine.interpreter.ALU;
import edu.osu.cse.mmxi.machine.interpreter.Interpreter;
import edu.osu.cse.mmxi.machine.memory.FillMemory;
import edu.osu.cse.mmxi.machine.memory.Memory;
import edu.osu.cse.mmxi.machine.memory.RandomizedMemory;
import edu.osu.cse.mmxi.ui.UI;

public class Machine {

    protected final Register[] registers; // must be protected for testing purposes
    protected Register         pc;
    protected FlagsRegister    nzp;
    protected Memory           memory;
    protected final ALU        alu;

    private int                clockCount;
    private boolean            halted;

    public UI                  ui;

    public Machine() {
        ui = new UI();
        registers = new Register[8];
        alu = new Interpreter(this);
        reset(-1);
    }

    public void reset(final int fill) {
        clockCount = 1;
        halted = false;

        for (int i = 0; i < 8; i++)
            registers[i] = new Register(fill);
        pc = new Register(fill);
        nzp = new FlagsRegister(fill);
        if (fill == -1)
            memory = new RandomizedMemory();
        else
            memory = new FillMemory((short) fill);
    }

    /**
     * Retrieves the 16-bit word stored at the absolute memory address in the Machine.
     * 
     * A memory address is given by a 16-bit quantity where the upper 7 bits denote the
     * page number and the lower 9 bits denote the offset within that page.
     * 
     * @param absoluteAddress
     *            16-bit representation of the absolute memory address.
     * @return the contents of the address.
     */
    public short getMemory(final short absoluteAddress) {
        return memory.getMemory(absoluteAddress);
    }

    /**
     * Retrieves the 16-bit word stored at the relative memory offset of the indicated
     * page of the Machine's memory.
     * 
     * @param page
     *            the jth page in all of memory
     * @param pageOffset
     *            the ith word of the jth page.
     * @return the contents of the ith word of the jth page.
     */
    public short getMemory(final byte page, final short pageOffset) {
        return memory.getMemory(page, pageOffset);
    }

    /**
     * Sets the contents of the 16-bit word stored at the absolute memory address of the
     * current page to a 16-bit value in the Machine.
     * 
     * A memory address is given by a 16-bit quantity where the upper 7 bits denote the
     * page number and the lower 9 bits denote the offset within that page.
     * 
     * @param absoluteAddress
     *            16-bit representation of the absolute memory address.
     * @param value
     *            a 16-bit value to be stored in memory.
     */
    public void setMemory(final short absoluteAddress, final short value) {
        memory.setMemory(absoluteAddress, value);
    }

    /**
     * Sets the contents of the 16-bit word stored at the relative memory offset of the
     * current page to a 16-bit value of the Machine's memory.
     * 
     * @param page
     *            the jth page in all of memory
     * @param pageOffset
     *            the ith word of the jth page.
     * @param value
     *            a 16-bit value to be stored in memory.
     */
    public void setMemory(final byte page, final short pageOffset, final short value) {
        memory.setMemory(page, pageOffset, value);
    }

    /**
     * A TRAP HALT will cause the Machine to stop running.
     * 
     * @return whether the Machine no longer intends to continue processing instructions.
     */
    public boolean hasHalted() {
        return halted;
    }

    /**
     * Called by TRAP HALT to cause the Machine to stop running.
     */
    public void halt() {
        halted = true;
    }

    /**
     * The number of instructions that have been executed since the Machine began.
     * 
     * The clock count is incremented with each clock cycle.
     */
    public int clockCount() {
        return clockCount;
    }

    /**
     * Executes the next clock cycle.
     */
    public void stepClock() {

        clockCount++;
        alu.executeNextInstruction(nextInstruction());
    }

    public Register getRegister(final int index) {
        return registers[index];
    }

    public Register getPCRegister() {
        return pc;
    }

    public FlagsRegister getFlags() {
        return nzp;
    }

    /**
     * Retrieves the value from the PC Register, then increments it as an atomic
     * operation.
     * 
     * @return the relative memory offset from the current page of the next instruction to
     *         be executed.
     */
    public short nextInstruction() {
        return pc.increment();
    }
}

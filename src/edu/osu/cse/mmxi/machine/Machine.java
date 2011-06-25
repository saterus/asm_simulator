package edu.osu.cse.mmxi.machine;

import java.util.HashMap;
import java.util.Map;

import edu.osu.cse.mmxi.machine.Register.RegisterType;
import edu.osu.cse.mmxi.machine.interpreter.ALU;
import edu.osu.cse.mmxi.machine.interpreter.Interpreter;
import edu.osu.cse.mmxi.machine.memory.Memory;
import edu.osu.cse.mmxi.machine.memory.RandomizedMemory;

public class Machine {

    private final Map<RegisterType, Register> registers;
    private final Memory                      memory;
    private final ALU                         alu;

    private int                               clockCount;
    private boolean                           halted;

    public Machine() {
        this.clockCount = 0;
        this.halted = false;

        this.registers = new HashMap<RegisterType, Register>();
        this.memory = new RandomizedMemory();
        this.alu = new Interpreter(this);
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
        return this.memory.getMemory(absoluteAddress);
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
        return this.memory.getMemory(page, pageOffset);
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
        this.memory.setMemory(absoluteAddress, value);
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
        this.memory.setMemory(page, pageOffset, value);
    }

    /**
     * A Trap[HALT] will cause the Machine to stop running.
     * 
     * @return the halted-ness of the Machine.
     */
    public boolean hasHalted() {
        return this.halted;
    }

    /**
     * The number of instructions that have been
     * executethis.registers.get(RegisterType.PC).d since the Machine began.
     * 
     * The clock count is incremented with each clock cycle.
     */
    public int clockCount() {
        return this.clockCount();
    }

    /**
     * Executes the next clock cycle.
     * 
     * @return the Instruction details of the executed Instruction.
     */
    public String stepClock() {

        this.clockCount++;
        this.halted = false;
        return this.alu.executeNextInstruction(((PCRegister) this.registers
                .get(RegisterType.PC)).nextInstruction());
    }

    public Register getRegister(final RegisterType r) {
        return this.registers.get(r);
    }
}

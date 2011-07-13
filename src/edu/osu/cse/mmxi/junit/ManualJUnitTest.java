package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.TRAP;

public class ManualJUnitTest {
    private final byte    r0 = 0;
    private final byte    r1 = 1;
    private final byte    r2 = 2;
    private final byte    r3 = 3;
    private final byte    r4 = 4;
    private final byte    r5 = 5;
    private final byte    r6 = 6;
    private final byte    r7 = 7;

    // private final InstructionParser parser = new InstructionParser();
    private final Machine m  = new Machine();

    // have to look at the output
    @Test
    public final void trapx31() {
        m.getRegister(r0).setValue((short) 99);
        System.out.println("");
        System.out.println("");
        System.out.println("Should see 99: ");

        final TRAP trap = new TRAP(0x31);
        trap.execute(m);
    }

    // x21
    @Test
    public final void trapx21() {
        System.out.println("");
        System.out.println("");
        System.out.println("Should get an error:");

        m.getRegister(r0).setValue((short) 0x9973);// s

        final TRAP trap = new TRAP(0x21);
        trap.execute(m);
    }

    @Test
    public final void trapx22Hello() {
        System.out.println("");
        System.out.println("");
        System.out.println("Should see a 'Hello':");

        m.getPCRegister().setValue((short) 0);
        m.getRegister(r0).setValue((short) 100);
        m.setMemory((short) 100, (short) 'H');
        m.setMemory((short) 101, (short) 'e');
        m.setMemory((short) 102, (short) 'l');
        m.setMemory((short) 103, (short) 'l');
        m.setMemory((short) 104, (short) 'o');
        m.setMemory((short) 105, (short) '\0');

        final TRAP trap = new TRAP(0x22);
        trap.execute(m);
    }

    // cannot test automatically
    @Test
    public final void trapx23inputS() {
        System.out.println("");
        System.out.println("");

        m.getRegister(r0).setValue((short) 99); // make sure r0 no 's'
        System.out.println("enter 's' at the promt");

        final TRAP trap = new TRAP(0x23);
        trap.execute(m);

        assertEquals(m.getRegister(r0).getValue(), (short) 's');
    }

    @Test
    public final void trapx33goodValEnter9() {
        m.getRegister(r0).setValue((short) 99);

        System.out.println("");
        System.out.println("");

        System.out.println("Should enter a 9:");

        // get a decimal value in
        // final TRAP trap = new TRAP(0x33);
        // trap.execute(m);

        // assertEquals((short) 9, m.getRegister(r0).getValue());

    }

    @Test
    public final void trapx33badValEnterCharacter() {
        System.out.println("");
        System.out.println("");

        System.out.println("Should enter Character, no number:");

        // final TRAP trap = new TRAP(0x33);
        // trap.execute(m);

        // assertEquals((short) 9, m.getRegister(r0).getValue());

    }

    /* Cannot really test random */
    public final void trapx43rand() {
        m.getRegister(r0).setValue((short) 0);
        final TRAP trap = new TRAP(0x43);
        trap.execute(m);

        // assertEquals((m.getRegister(r0).getValue() != 0), true);
    }
}

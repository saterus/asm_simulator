package edu.osu.cse.mmxi.sim.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.sim.machine.Machine;

public class MemoryTest {

    private final Machine m = new Machine();

    @Test
    public void insertRetreiveTestOffPage() {
        m.getPCRegister().setValue((short) 0);
        m.setMemory((short) 120, (short) 99); // off the page

        assertEquals(m.getMemory((short) 120), 99);
    }

    @Test
    public void insertRetreiveTestonPage() {
        m.getPCRegister().setValue((short) 0);
        m.setMemory((short) 513, (short) 99); // off the page

        assertEquals(m.getMemory((short) 513), 99);
    }

    @Test
    public void insertRetreiveTestDeepOffPage() {
        m.getPCRegister().setValue((short) 0);
        m.setMemory((short) 10513, (short) 99); // off the page

        assertEquals(m.getMemory((short) 10513), 99);
    }

    /**
     * Test Page Offsets
     */
    @Test
    public void insertOffsetRetreiveTestOffPage() {
        // 1024 = 2 * 512 or page 2
        // 20 is the offset used

        System.out.println(m.getPCRegister().getValue());
        m.getPCRegister().setValue((short) (1024 / 512));
        System.out.println(m.getPCRegister().getValue());
        m.setMemory((byte) (1024 / 512), (short) 20, (short) 99);

        assertEquals(m.getMemory((short) 1044), 99);
    }

    @Test
    public void insertOffsetRetreiveOffsetTestOffPage() {
        // 1024 = 2 * 512 or page 2
        // 20 is the offset used

        System.out.println(m.getPCRegister().getValue());
        m.getPCRegister().setValue((short) (1024 / 512));
        System.out.println(m.getPCRegister().getValue());
        m.setMemory((byte) (1024 / 512), (short) 20, (short) 99);

        assertEquals(m.getMemory((byte) 2, (short) 20), 99);
    }
}

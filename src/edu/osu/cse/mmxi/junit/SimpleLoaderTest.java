package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.machine.Machine;

public class SimpleLoaderTest {
    Machine m = null;

    @Before
    public void init() {
        m = new Machine();
    }

    @Test(expected = IOException.class)
    public void testDirectoryRead() throws IOException {
        SimpleLoader.load("src", m);
    }

    @Test(expected = IOException.class)
    public void testNonexistentRead() throws IOException {
        SimpleLoader.load("garbage", m);
    }

    @Test
    public void testRead() throws IOException {
        SimpleLoader.load("sample2.txt", m);
        final int[] dat = { 0x0004, 0x22B0, 0xE0B7, 0xF022, 0x127F, 0x02B3, 0xF025,
                0x0068, 0x0069, 0x0020, 0x0000 };
        for (int i = 0; i < dat.length; i++)
            assertEquals(m.getMemory((short) (0x30B0 + i)), (short) dat[i]);
        assertEquals(m.getPCRegister().getValue(), (short) 0x30B1);
    }
}

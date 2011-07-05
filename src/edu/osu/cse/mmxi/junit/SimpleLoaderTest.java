package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.SimpleLoaderFatalException;
import edu.osu.cse.mmxi.machine.Machine;

public class SimpleLoaderTest {
    String  dir = "src/edu/osu/cse/mmxi/junit";
    Machine m   = null;

    @Before
    public void init() {
        m = new Machine();
    }

    // test file exists, but not readable, using a directory
    @Test(expected = IOException.class)
    public void testDirectoryRead() throws IOException, SimpleLoaderFatalException {
        SimpleLoader.load("src", m);
    }

    // testing that a file doesnt exist error
    @Test(expected = IOException.class)
    public void testNonexistentRead() throws IOException, SimpleLoaderFatalException {
        SimpleLoader.load("garbage", m);
    }

    @Test
    public void testRead() throws IOException, SimpleLoaderFatalException {
        SimpleLoader.load(dir + "/jSample1.txt", m);

        // array of memory locations to verify against
        final int[] dat = { 0x0004, 0x22B0, 0xE0B7, 0xF022, 0x127F, 0x02B3, 0xF025,
                0x0068, 0x0069, 0x0020, 0x0000 };

        // test memory was loaded properly
        for (int i = 0; i < dat.length; i++)
            assertEquals(m.getMemory((short) (0x30B0 + i)), (short) dat[i]);

        assertEquals(m.getPCRegister().getValue(), (short) 0x30B1);
    }

    // test memory out of bounds error

    // test exec out of bounds error

    // test error messages

    // test parser indirectly
    // bad header
    @Test
    public void testBadHex() throws IOException {
        Boolean result = false;

        try {
            SimpleLoader.load(dir + "/jBadHeader.txt", m);
        } catch (final SimpleLoaderFatalException e) {
            result = true;
        }

        assertEquals(true, result);
    }
    // bad main
    // bad exec
    // does error tracking work?
    // good header, h vs. H
    // good main, good in = good out
    // good exec
}

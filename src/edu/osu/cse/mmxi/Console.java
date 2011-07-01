package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.Machine.FillMode;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Console {
    private final Machine m;
    private int           maxClock;
    private final int     memTrack;

    public Console(final Machine _m, final String file) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        memTrack = -1;
        m.ui.print("McMoxel MMXI Emulator\n");
        m.ui.print("Version 2\n\n");
        if (file != null)
            load("load", file);
        while (true)
            promptForCommand();
    }

    private void promptForCommand() {
        final String cmd = m.ui.prompt("\n\nMcMoxel> ");
        final String[] words = cmd.split(" ");
        if (words.length == 0)
            return;
        if ("help".startsWith(words[0]))
            help(words);
        else if ("load".startsWith(words[0]))
            load(words);
        else if ("quit".startsWith(words[0]))
            System.exit(0);
        else if ("run".startsWith(words[0]))
            run(words);
        else if ("step".startsWith(words[0]))
            step(false, words);
        else if ("trace".startsWith(words[0]))
            step(true, words);
        else if (words[0].length() > 1) {
            if ("reset".startsWith(words[0]))
                reset(words);
            else if ("run".startsWith(words[0]))
                run(words);
            else
                m.ui.warn("Unknown command");
        } else
            m.ui.warn("Unknown command");
    }

    private void help(final String... words) {

    }

    private void load(final String... words) {
        if (words.length < 2) {
            help("help", "load");
            return;
        }
        m.ui.print("Loading file: " + words[1] + "\n");
        try {
            SimpleLoader.load(words[1], m);
        } catch (final ParseException e) {
            m.ui.error(e.getMessage());
        } catch (final IOException e) {
            m.ui.error("I/O Error: " + e.getMessage());
        }
        printInstruction();
    }

    private void reset(final String[] words) {
        int load = 0;
        final FillMode fill = FillMode.RAND;
        if (words.length > 1 && words[1].equals("-l"))
            load = 1;
        if (words.length > load + 1)
            if (words[load + 1].equals("zero"))
                ; // TODO
    }

    private void run(final String... words) {
        while (true) {
            m.stepClock();
            if (m.clockCount() > maxClock) {
                m.ui.print("Clock limit " + maxClock + " reached.");
                maxClock *= 2;
                break;
            }
            if (m.hasHalted()) {
                m.ui.print("Program exited normally after " + (m.clockCount() - 1)
                    + " steps.");
                break;
            }
        }
    }

    private void step(final boolean trace, final String... words) {
        int steps = 1;
        if (words.length > 1)
            try {
                steps = Integer.parseInt(words[1]);
                if (steps < 0) {
                    m.ui.print("'steps' cannot be negative.");
                    help("help", "trace");
                    return;
                }
            } catch (final NumberFormatException e) {
                m.ui.print("'steps' must be a number.");
                help("help", "trace");
                return;
            }
        runMachine(steps);
        if (trace)
            printRegistersAndShortMemory();
        printInstruction();
    }

    private int runMachine(final int steps) {
        for (int i = 0; i < steps; i++) {
            m.stepClock();
            if (m.clockCount() > maxClock) {
                m.ui.print("Clock limit " + maxClock + " reached.");
                maxClock *= 2;
                return 2;
            }
            if (m.hasHalted()) {
                m.ui.print("Program exited normally after " + (m.clockCount() - 1)
                    + " steps.");
                return 1;
            }
        }
        return 0;
    }

    private void printRegistersAndShortMemory() {
        short mem;
        if (memTrack == -1)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = (short) memTrack;
        m.ui.print("\n                           ");
        for (int j = 0; j < 8; j++)
            m.ui.print(" --" + (mem + j & 7) + "-");
        m.ui.print("\n");
        for (int i = 0; i < 4; i++) {
            m.ui.print("R" + 2 * i + ": ");
            m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i).getValue())
                + "  ");
            m.ui.print("R" + (2 * i + 1) + ": ");
            m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i + 1).getValue())
                + "   ");
            m.ui.print(MemoryUtilities.uShortToHex((short) (mem + 8 * i)) + " | ");
            for (int j = 0; j < 8; j++)
                m.ui.print(MemoryUtilities.uShortToHex(m
                    .getMemory((short) (mem + 8 * i + j))) + " ");
            m.ui.print("\n");
        }
    }

    private void printInstruction() {
        m.ui.print("\n  PC: " + MemoryUtilities.uShortToHex(m.getPCRegister().getValue())
            + "  ");
        m.ui.print((m.getFlags().getN() ? "n" : "-") + (m.getFlags().getZ() ? "z" : "-")
            + (m.getFlags().getP() ? "p" : "-") + "  ");
        m.ui.print(MemoryUtilities.uShortToHex(m.getMemory(m.getPCRegister().getValue()))
            + ": ");
        m.ui.print(m.alu.readInstructionAt(m.getPCRegister().getValue()));
    }

}

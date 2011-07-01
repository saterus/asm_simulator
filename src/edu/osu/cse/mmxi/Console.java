package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Console {
    private final Machine m;
    private int           maxClock;
    private int           memTrack;
    private String        file;

    public Console(final Machine _m, final String _file) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        memTrack = -1;
        file = null;
        m.ui.print("McMoxel MMXI Emulator\n");
        m.ui.print("Version 2");
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
        if ("clock".startsWith(words[0]))
            clock(words);
        else if ("disasm".startsWith(words[0]))
            disasm(words);
        else if ("help".startsWith(words[0]))
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
        else if (words[0].equals("track"))
            track(words);
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

    private void clock(final String[] words) {
        int max;
        if (words.length > 1) {
            try {
                max = Integer.parseInt(words[1]);
            } catch (final NumberFormatException e) {
                m.ui.print("Malformed clock maximum '" + words[1] + "'\n");
                help("help", "disasm");
                return;
            }
            if (max < 0) {
                max = Integer.MAX_VALUE;
                m.ui.print("Setting maximum clock ticks to unlimited.");
            } else
                m.ui.print("Setting maximum clock ticks to " + max + ".");
            maxClock = Simulator.MAX_CLOCK_COUNT = max;
        } else
            m.ui.print("Clock currently at " + (m.clockCount() - 1) + " instructions"
                + (maxClock == 0 ? "." : " out of " + maxClock + "."));
    }

    private void disasm(final String[] words) {
        int base = memTrack, len = 16;
        if (words.length > 1) {
            try {
                base = Integer.parseInt(words[1], 16) & 0xffff;
            } catch (final NumberFormatException e) {
                if (words[1].equalsIgnoreCase("pc"))
                    base = -1;
                else {
                    m.ui.print("Malformed address '" + words[1] + "'\n");
                    help("help", "disasm");
                    return;
                }
            }
            if (words.length > 2)
                try {
                    len = Integer.parseInt(words[2]);
                    if (len < 0) {
                        m.ui.print("Length must be greater than 0.\n");
                        help("help", "disasm");
                        return;
                    }
                } catch (final NumberFormatException e) {
                    m.ui.print("Malformed length parameter '" + words[2] + "'\n");
                    help("help", "disasm");
                    return;
                }
        }
        short mem;
        if (base == -1)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = (short) base;
        for (int i = 0; i < len; i++)
            m.ui.print("\n    " + MemoryUtilities.uShortToHex((short) (mem + i))
                + ":   [" + MemoryUtilities.uShortToHex(m.getMemory((short) (mem + i)))
                + "] " + m.alu.readInstructionAt((short) (mem + i)));
    }

    private void help(final String... words) {

    }

    private void load(final String... words) {
        if (words.length < 2) {
            help("help", "load");
            return;
        }
        file = words[1];
        m.ui.print("Loading file: " + file + "\n");
        try {
            SimpleLoader.load(file, m);
        } catch (final ParseException e) {
            m.ui.error(e.getMessage());
        } catch (final IOException e) {
            m.ui.error("I/O Error: " + e.getMessage());
        }
        printInstruction();
    }

    private void reset(final String[] words) {
        int load = 0;
        if (words.length > 1 && words[1].equals("-l"))
            load++;
        int fill = -1;
        if (words.length > load + 1)
            try {
                fill = Integer.parseInt(words[load + 1], 16);
            } catch (final NumberFormatException e) {
                if (!words[load + 1].equals("rand")) {
                    m.ui.print("Malformed fill mode parameter '" + words[load + 1]
                        + "'\n");
                    help("help", "reset");
                    return;
                }
            }
        m.reset(fill);
        if (load != 0)
            load("load", file);
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

    private void track(final String[] words) {
        if (words.length < 1) {
            help("help", "load");
            return;
        }
        if (words[0].equalsIgnoreCase("pc")) {
            m.ui.print("Tracking Program Counter.");
            memTrack = -1;
        } else {
            try {
                memTrack = Short.parseShort(words[1], 16);
            } catch (final NumberFormatException e) {
                m.ui.print("Invalid tracking address.");
                help("help", "trace");
                return;
            }
            m.ui.print("Tracking address "
                + MemoryUtilities.uShortToHex((short) memTrack) + ".");
        }
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

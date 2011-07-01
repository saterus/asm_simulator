package edu.osu.cse.mmxi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Console {
    private final Machine            m;
    private int                      maxClock;
    private int                      memTrack;
    private String                   file;
    private final Map<Short, Object> breakpoints;

    public Console(final Machine _m, final String _file) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        memTrack = -1;
        file = null;
        breakpoints = new TreeMap<Short, Object>();
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
        if ("break".startsWith(words[0]))
            _break(words);
        else if ("clock".startsWith(words[0]))
            clock(words);
        else if ("edit".startsWith(words[0]))
            edit(words);
        else if ("help".startsWith(words[0]))
            help(words);
        else if ("load".startsWith(words[0]))
            load(words);
        else if ("quit".startsWith(words[0]))
            System.exit(0);
        else if ("step".startsWith(words[0]))
            step(false, words);
        else if ("trace".startsWith(words[0]))
            step(true, words);
        else if (words[0].equals("track"))
            track(words);
        else if (words[0].length() > 1) {
            if ("disasm".startsWith(words[0]))
                disasm(words);
            else if ("dump".startsWith(words[0]))
                dump(words);
            else if ("reg".startsWith(words[0]))
                reg(words);
            else if ("reset".startsWith(words[0]))
                reset(words);
            else if ("run".startsWith(words[0]))
                run(words);
            else
                m.ui.warn("Unknown command");
        } else
            m.ui.warn("Unknown command");
    }

    private void _break(final String... words) {
        final boolean del = words.length > 1 && words[1].equals("-d");
        final boolean delAll = words.length > 1 && words[1].equals("-D");
        if (delAll) {
            breakpoints.clear();
            m.ui.print("All breakpoints cleared.");
            return;
        }
        if (words.length <= (del ? 2 : 1)) {
            if (breakpoints.size() == 0)
                m.ui.print("No breakpoints are set. Use 'break [addr]' to set a breakpoint.");
            else {
                m.ui.print("Breakpoints are set at:\n");
                for (final Map.Entry<Short, Object> e : breakpoints.entrySet()) {
                    final short b = e.getKey(), inst = m.getMemory(b);
                    m.ui.print("\n    " + MemoryUtilities.uShortToHex(b) + ":   ["
                        + MemoryUtilities.uShortToHex(inst) + "] "
                        + m.alu.readInstruction(inst));
                }
            }
            return;
        }
        int addr;
        if ((addr = readAddr(words[del ? 2 : 1])) == -1) {
            m.ui.print("Malformed address '" + words[del ? 2 : 1] + "'\n");
            help("help", "break");
            return;
        }
        if (breakpoints.containsKey((short) addr)) {
            if (del)
                breakpoints.remove((short) addr);
            else
                m.ui.print("A breakpoint is already set at "
                    + MemoryUtilities.uShortToHex((short) addr) + "\n");
        } else if (del)
            m.ui.print("No breakpoints set at "
                + MemoryUtilities.uShortToHex((short) addr) + "\n");
        else
            breakpoints.put((short) addr, null);
    }

    private void clock(final String... words) {
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

    private void disasm(final String... words) {
        int base = memTrack, len = 16;
        if (words.length > 1) {
            if ((base = readAddr(words[1])) == -1 && !words[1].equalsIgnoreCase("pc")) {
                m.ui.print("Malformed address '" + words[1] + "'\n");
                help("help", "disasm");
                return;
            }
            if (words.length > 2)
                if (words[2].equalsIgnoreCase("halt"))
                    len = -1;
                else if (words[2].equalsIgnoreCase("ret"))
                    len = -2;
                else
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
        for (int i = 0;; i++) {
            final short inst = m.getMemory((short) (mem + i));
            m.ui.print("\n    " + MemoryUtilities.uShortToHex((short) (mem + i))
                + ":   [" + MemoryUtilities.uShortToHex(inst) + "] "
                + m.alu.readInstruction(inst));
            if (len == -1 && inst == (short) 0xF025 || len == -2
                && (inst & 0xF000) == 0xD000 || len >= 0 && i + 2 > len)
                break;
        }
    }

    private void dump(final String... words) {
        int base = memTrack, len = 8;
        if (words.length > 1) {
            if ((base = readAddr(words[1])) == -1 && !words[1].equalsIgnoreCase("pc")) {
                m.ui.print("Malformed address '" + words[1] + "'\n");
                help("help", "dump");
                return;
            }
            if (words.length > 2)
                try {
                    len = Integer.parseInt(words[2]);
                    if (len < 0) {
                        m.ui.print("Length must be greater than 0.\n");
                        help("help", "dump");
                        return;
                    }
                } catch (final NumberFormatException e) {
                    m.ui.print("Malformed length parameter '" + words[2] + "'\n");
                    help("help", "dump");
                    return;
                }
        }
        short mem;
        if (base == -1)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = (short) base;
        m.ui.print("\n      ");
        for (int j = 0; j < 16; j++)
            m.ui.print(" --" + Integer.toHexString(mem + j & 15).toUpperCase() + "-");
        m.ui.print("  ");
        for (int j = 0; j < 16; j++)
            m.ui.print(Integer.toHexString(mem + j & 15).toUpperCase());
        for (int i = 0; i < len; i++) {
            m.ui.print("\n");
            m.ui.print(MemoryUtilities.uShortToHex((short) (mem + 16 * i)) + " | ");
            for (int j = 0; j < 16; j++)
                m.ui.print(MemoryUtilities.uShortToHex(m
                    .getMemory((short) (mem + 16 * i + j))) + " ");
            m.ui.print(" ");
            for (int j = 0; j < 16; j++) {
                final short s = m.getMemory((short) (mem + 16 * i + j));
                m.ui.print(s >= 32 && s < 127 ? "" + (char) s : ".");
            }
        }
    }

    private void edit(final String... words) {
        if (words.length < 3) {
            help("help", "edit");
            return;
        }
        int addr, value;
        if ((addr = readAddr(words[1])) == -1) {
            m.ui.print("Malformed address '" + words[1] + "'\n");
            help("help", "edit");
            return;
        }
        if ((value = readAddr(words[2])) == -1) {
            m.ui.print("Malformed value '" + words[2] + "'\n");
            help("help", "edit");
            return;
        }
        m.setMemory((short) addr, (short) value);
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

    private void reg(final String... words) {
        if (words.length <= 1) {
            for (int i = 0; i < 8; i++)
                m.ui.print("    R" + i);
            m.ui.print("    PC   FLAGS\n ");
            for (int i = 0; i < 8; i++)
                m.ui.print("  "
                    + MemoryUtilities.uShortToHex(m.getRegister(i).getValue()));
            m.ui.print("  " + MemoryUtilities.uShortToHex(m.getPCRegister().getValue()));
            m.ui.print("  " + MemoryUtilities.uShortToHex(m.getFlags().getValue()));
            return;
        }
        int value = -1;
        if (words.length > 2)
            if ((value = readAddr(words[2])) == -1) {
                m.ui.print("Malformed value '" + words[2] + "'\n");
                help("help", "reg");
                return;
            }
        if (words[1].equalsIgnoreCase("pc")) {
            if (value != -1)
                m.getPCRegister().setValue((short) value);
            m.ui.print("PC: " + MemoryUtilities.uShortToHex(m.getPCRegister().getValue()));
        } else if (words[1].equalsIgnoreCase("flags")) {
            if (value != -1) {
                if (value != 1 && value != 2 && value != 4) {
                    m.ui.print("The FLAGS register may only be set to the values 1, 2, or 4.");
                    return;
                }
                m.getFlags().setValue((short) value);
            }
            m.ui.print("FLAGS: " + MemoryUtilities.uShortToHex(m.getFlags().getValue())
                + " [");
            m.ui.print((m.getFlags().getN() ? "n" : "-")
                + (m.getFlags().getZ() ? "z" : "-") + (m.getFlags().getP() ? "p" : "-")
                + "]");
        } else if (words[1].matches("[rR]\\d")) {
            final int rnum = words[1].charAt(1) - '0';
            if (value != -1)
                m.getRegister(rnum).setValue((short) value);
            m.ui.print("R" + rnum + ": "
                + MemoryUtilities.uShortToHex(m.getRegister(rnum).getValue()));
        } else {
            m.ui.print("Malformed register '" + words[1] + "'\n");
            help("help", "reg");
            return;
        }
    }

    private void reset(final String... words) {
        int load = 0;
        if (words.length > 1 && words[1].equals("-l"))
            load++;
        int fill = -1;
        if (words.length > load + 1)
            if ((fill = readAddr(words[load + 1])) == -1
                && !words[load + 1].equals("rand")) {
                m.ui.print("Malformed fill mode parameter '" + words[load + 1] + "'\n");
                help("help", "reset");
                return;
            }
        m.reset(fill);
        if (load != 0)
            load("load", file);
    }

    private void run(final String... words) {
        runMachine(Integer.MAX_VALUE);
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

    private void track(final String... words) {
        if (words.length < 1) {
            help("help", "load");
            return;
        }
        if (words[0].equalsIgnoreCase("pc")) {
            m.ui.print("Tracking Program Counter.");
            memTrack = -1;
        } else {
            if (readAddr(words[1]) == -1) {
                m.ui.print("Invalid tracking address.");
                help("help", "trace");
                return;
            } else
                memTrack = readAddr(words[1]);
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
            final short pc = m.getPCRegister().getValue();
            for (final Map.Entry<Short, Object> e : breakpoints.entrySet())
                if (pc == e.getKey()) {
                    m.ui.print("Breakpoint encountered at "
                        + MemoryUtilities.uShortToHex(pc));
                    return 3;
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

    private int readAddr(String s) {
        if (s.equalsIgnoreCase("pc"))
            return m.getPCRegister().getValue();
        else if (s.matches("[rR]\\d"))
            return m.getRegister(s.charAt(1) - '0').getValue();
        else {
            int radix = 10;
            if (s.startsWith("0x") || s.startsWith("0X")) {
                radix = 16;
                s = s.substring(2);
            } else if (s.substring(0, 1).equalsIgnoreCase("x")) {
                radix = 16;
                s = s.substring(1);
            } else if (s.startsWith("0b") || s.startsWith("0B")) {
                radix = 2;
                s = s.substring(2);
            } else if (s.length() > 1 && s.startsWith("0"))
                radix = 8;
            try {
                return (short) Integer.parseInt(s, radix);
            } catch (final NumberFormatException e) {
                return -1;
            }
        }
    }
}

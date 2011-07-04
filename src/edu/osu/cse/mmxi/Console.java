package edu.osu.cse.mmxi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Console {
    private final Machine             m;
    private int                       maxClock;
    private int                       memTrack;
    private String                    file;
    private final Map<Short, Object>  breakpoints;
    private final Map<Integer, Short> watchpoints;

    public Console(final Machine _m, final String _file) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        memTrack = -1;
        file = null;
        breakpoints = new TreeMap<Short, Object>();
        watchpoints = new TreeMap<Integer, Short>();
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
        else if ("disasm".startsWith(words[0]))
            disasm(words);
        else if ("dump".startsWith(words[0]))
            dump(words);
        else if ("edit".startsWith(words[0]))
            edit(words);
        else if ("exit".startsWith(words[0]) || "quit".startsWith(words[0]))
            System.exit(0);
        else if ("help".startsWith(words[0]) || words[0].equals("?"))
            help(words);
        else if ("load".startsWith(words[0]))
            load(words);
        else if ("run".startsWith(words[0]))
            run(words);
        else if ("reg".startsWith(words[0]))
            reg(words);
        else if ("reset".startsWith(words[0]))
            reset(words);
        else if ("step".startsWith(words[0]))
            step(false, words);
        else if (words[0].length() > 3 && "track".startsWith(words[0]))
            track(words);
        else if ("trace".startsWith(words[0]))
            step(true, words);
        else if ("watch".startsWith(words[0]))
            watch(words);
        else {
            m.ui.print("Unknown command\n");
            help("help");
        }
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
                + (maxClock == Integer.MAX_VALUE ? "." : " out of " + maxClock + "."));
    }

    private void disasm(final String... words) {
        int base = memTrack, len = 16;
        if (words.length > 1) {
            if ((base = readAddr(words[1])) == -1) {
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
            if ((base = readAddr(words[1])) == -1) {
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
        final String help = " Syntax: help [keyword]\n"
            + " Mnemonics: ? h he hel help\n\n"
            + " The help command provides information on specific commands and features.\n"
            + " If used without any arguments, help will display a short list of commands\n"
            + " available in the simulator.\n\n"
            + " You can get additional help on specific commands by typing help followed\n"
            + " by the command or keyword you want more information on.\n\n COMMANDS:\n"
            + "    break           clock           disasm          dump\n"
            + "    edit            help            load            quit\n"
            + "    reg             reset           run             step\n"
            + "    trace           track           watch";
        if (words.length < 2 || words[1].length() == 0)
            m.ui.print(help);
        else if ("break".startsWith(words[1]))
            m.ui.print(" Syntax: break [-d] <address>\n         break [-D]\n"
                + " Mnemonics: b br bre brea break\n\n"
                + " Breakpoints allow you to stop execution of the program when certain\n"
                + " addresses are reached. The breakpoint facility functions during trace,\n"
                + " step, and execution modes.\n\n"
                + " Breakpoints can be listed, added, or removed.\n\n"
                + "   > break           Lists the currently set breakpoints\n"
                + "   > break -D        Deletes all breakpoints\n"
                + "   > break x3021     Sets breakpoint at address 0x3021\n"
                + "   > break -d x3021  Removes the breakpoint\n\n"
                + " A message will be displayed to notify you if you try to set a breakpoint\n"
                + " that already exists, or try to delete a breakpoint that does not exist.");
        else if ("clock".startsWith(words[1]))
            m.ui.print(" Syntax: clock [<max>]\n" + " Mnemonics: c cl clo cloc clock\n\n");
        else if ("disasm".startsWith(words[1]))
            m.ui.print(" Syntax: disasm [<address> [halt|ret|<length>]]\n"
                + " Mnemonics: d di dis disa disas disasm\n\n"
                + " The disasm command provides a disassembled interpretation of the words\n"
                + " located at the specified address. The optional second argument allows\n"
                + " the choice of having a set number of instructions disassembled, or to\n"
                + " continue disassembling until a TRAP HALT or RET command is encountered.\n\n"
                + " The command will provide assembly mnemonic listings for 16 consecutive\n"
                + " words (by default). The first column of the listing specifies the\n"
                + " address of the word being disassembled, the second column contains the\n"
                + " hexadecimal data at that word, while the remaining columns provide the\n"
                + " disassembled instructions.\n\n"
                + "   > disasm           Disassembles starting at the current memory track\n"
                + "                      (see 'help track' for details)\n"
                + "   > disasm pc halt   Disassembles starting at PC, and continuing until\n"
                + "                      TRAP HALT is found\n"
                + "   > disasm x3000     Disassembles 16 instructions starting at 0x3000\n\n"
                + "   > disasm x1000 20  Disassembles 20 instructions starting at 0x1000\n"
                + "   > disasm r0 ret    Disassembles starting at the address in register\n"
                + "                      0, and continuing until RET is found");
        else if ("dump".startsWith(words[1]))
            m.ui.print(" Syntax: dump [<address> [<length>]]\n"
                + " Mnemonics: du dum dump\n\n"
                + " The dump Command allows the user to inspect memory.\n\n"
                + " Without arguments, the dump command will display 128 words of memory\n"
                + " starting from the memory track (see 'help track' for details).\n\n"
                + " Optionally, an address can be specified to the dump command which will\n"
                + " result in the dump starting from the specified address. An optional\n"
                + " length parameter allows the number of lines of data to be specified,\n"
                + " defaulting to 8 lines.\n\n"
                + "   > dump        Display memory starting at the memory track\n"
                + "   > dump x1000  Display memory starting at address 0x1000\n"
                + "   > dump pc 20  Display 20 lines (320 words) of memory\n"
                + "                 starting at the PC");
        else if ("edit".startsWith(words[1]))
            m.ui.print(" Syntax: edit <address> <value>\n"
                + " Mnemonics: e ed edi edit\n\n"
                + " The edit command allows users to modify memory locations. Either of the\n"
                + " fields 'address' or 'value' may be register names in order to change\n"
                + " memory locations pointed to by those registers.\n\n"
                + "   > edit x1000 x001C  Replace the word at 0x1000 with the value 0x001C\n"
                + "   > edit r0 pc        Set the memory loaction pointed to by register 0\n"
                + "                       to the current value of the PC");
        else if ("exit".startsWith(words[1]) || "quit".startsWith(words[1]))
            m.ui.print(" Syntax: quit\n" + " Mnemonics: ex exi exit q qu qui quit\n\n"
                + " Exit from the simulator program.");
        else if ("load".startsWith(words[1]))
            m.ui.print(" Syntax: load <file>\n"
                + " Mnemonics: l lo loa load\n\n"
                + " The load command loads a compiled assembly language program from the\n"
                + " disk. The program will be in its initial state after loading and will\n"
                + " need to be started via the run command afterwards.\n\n"
                + "   > load test.out       loads 'test.out' from current directory\n"
                + "   > load /usr/bin/test  loads 'test' from /usr/bin/\n\n"
                + " The file must be in MMXI file format. Otherwise, any parsing errors\n"
                + " will be displayed on-screen, with the machine in an indeterminate\n"
                + " state afterwards (another load command will reset the machine).");
        else if ("run".startsWith(words[1]))
            m.ui.print(" Syntax: run\n" + " Mnemonics: r ru run\n\n" + " TODO: Text here");
        else if ("reg".startsWith(words[1]))
            m.ui.print(" Syntax: reg [pc|flags|r<num> [<value>]]\n"
                + " Mnemonics: re reg\n\n" + " TODO: Text here");
        else if ("reset".startsWith(words[1]))
            m.ui.print(" Syntax: reset [-l] [<value>|rand]\n"
                + " Mnemonics: res rese reset\n\n" + " TODO: Text here");
        else if ("step".startsWith(words[1]))
            m.ui.print(" Syntax: step [<steps>]\n" + " Mnemonics: s st ste step\n\n"
                + " TODO: Text here");
        else if (words[1].length() > 3 && "track".startsWith(words[1]))
            m.ui.print(" Syntax: track pc|<address>\n" + " Mnemonics: trac track\n\n"
                + " TODO: Text here");
        else if ("trace".startsWith(words[1]))
            m.ui.print(" Syntax: trace [<steps>]\n" + " Mnemonics: t tr tra trace\n\n"
                + " TODO: Text here");
        else if ("watch".startsWith(words[1]))
            m.ui.print(" Syntax: watch [-d] <address>\n         watch [-D]\n"
                + " Mnemonics: w wa wat watc watch\n\n" + " TODO: Text here");
        else
            m.ui.print(help);

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
            printInstruction();
        } catch (final IOException e) {
            m.ui.print("I/O Error: " + e.getMessage());
        }
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
        } else if (words[1].matches("[rR][0-7]")) {
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
                    help("help", trace ? "trace" : "step");
                    return;
                }
            } catch (final NumberFormatException e) {
                m.ui.print("'steps' must be a number.");
                help("help", trace ? "trace" : "step");
                return;
            }
        runMachine(steps);
        if (trace)
            printRegistersAndShortMemory();
        printInstruction();
    }

    private void track(final String... words) {
        if (words.length < 2) {
            help("help", "track");
            return;
        }
        if (words[1].equalsIgnoreCase("pc")) {
            m.ui.print("Tracking Program Counter.");
            memTrack = -1;
        } else {
            if (readAddr(words[1]) == -1) {
                m.ui.print("Invalid tracking address.");
                help("help", "track");
                return;
            } else
                memTrack = readAddr(words[1]);
            m.ui.print("Tracking address "
                + MemoryUtilities.uShortToHex((short) memTrack) + ".");
        }
    }

    private void watch(final String... words) {
        final boolean del = words.length > 1 && words[1].equals("-d");
        final boolean delAll = words.length > 1 && words[1].equals("-D");
        if (delAll) {
            watchpoints.clear();
            m.ui.print("All watchpoints cleared.");
            return;
        }
        if (words.length <= (del ? 2 : 1)) {
            if (watchpoints.size() == 0)
                m.ui.print("No watchpoints are set. Use 'watch [addr]' to set a watchpoints.");
            else {
                m.ui.print("Watchpoints are set on:\n");
                for (final Map.Entry<Integer, Short> e : watchpoints.entrySet()) {
                    final int b = e.getKey();
                    if (b < 0)
                        m.ui.print("\n      R"
                            + (b + 8)
                            + ":    "
                            + MemoryUtilities
                                .uShortToHex(m.getRegister(b + 8).getValue()));
                    else {
                        final short inst = m.getMemory((short) b);
                        m.ui.print("\n    " + MemoryUtilities.uShortToHex((short) b)
                            + ":   [" + MemoryUtilities.uShortToHex(inst) + "] "
                            + m.alu.readInstruction(inst));
                    }
                }
            }
            return;
        }
        int pt;
        String addr = words[del ? 2 : 1];
        if (addr.matches("[rR][0-7]"))
            pt = words[del ? 2 : 1].charAt(1) - '8';
        else
            try {
                if (addr.startsWith("0x") || addr.startsWith("0X"))
                    addr = addr.substring(2);
                else if (addr.startsWith("x") || addr.startsWith("X"))
                    addr = addr.substring(1);
                pt = Integer.parseInt(addr, 16);
                if (pt < 0)
                    throw new NumberFormatException();
            } catch (final NumberFormatException e) {
                m.ui.print("'target' must be a hex address of memory or a register reference.");
                help("help", "watch");
                return;
            }

        if (watchpoints.containsKey(pt)) {
            if (del)
                watchpoints.remove(pt);
            else if (pt < 0)
                m.ui.print("A watchpoint is already set on register " + (pt + 8) + ".\n");
            else
                m.ui.print("A watchpoint is already set on address "
                    + MemoryUtilities.uShortToHex((short) pt) + "\n");
        } else if (!del)
            watchpoints.put(pt,
                pt < 0 ? m.getRegister(pt + 8).getValue() : m.getMemory((short) pt));
        else if (pt < 0)
            m.ui.print("No watchpoints set on register " + (pt + 8) + ".\n");
        else
            m.ui.print("No watchpoints set on address "
                + MemoryUtilities.uShortToHex((short) pt) + ".\n");
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
            for (final Map.Entry<Integer, Short> e : watchpoints.entrySet()) {
                final int k = e.getKey();
                final short v = k < 0 ? m.getRegister(k + 8).getValue() : m
                    .getMemory((short) k);
                if (v != e.getValue()) {
                    if (k < 0)
                        m.ui.print("Watchpoint triggered on register " + (k + 8));
                    else
                        m.ui.print("Watchpoint triggered on memory location "
                            + MemoryUtilities.uShortToHex((short) k));
                    m.ui.print(": value changed from "
                        + MemoryUtilities.uShortToHex(e.getValue()) + " to "
                        + MemoryUtilities.uShortToHex(v));
                    e.setValue(v);
                    return 4;
                }
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
        else if (s.matches("[rR][0-7]"))
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

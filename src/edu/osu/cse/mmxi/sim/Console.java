package edu.osu.cse.mmxi.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.sim.loader.LinkingLoader;
import edu.osu.cse.mmxi.sim.loader.SimpleLoader;
import edu.osu.cse.mmxi.sim.machine.Machine;

public class Console {

    private final Machine             m;
    private int                       maxClock;
    private Short                     memTrack;
    private final Set<String>         file;
    private LinkingLoader             loader;
    private final SortedSet<Short>    breakpoints;
    private final Map<Integer, Short> watchpoints;
    private final Map<String, Short>  symbols;
    private int                       symbLength = 0;
    private final Map<Short, Integer> lines;
    private final String              mcmoxel    = ""
                                                     + "    _/      _/            _/      _/                                _/ \n"
                                                     + "   _/_/  _/_/    _/_/_/  _/_/  _/_/    _/_/    _/    _/    _/_/    _/  \n"
                                                     + "  _/  _/  _/  _/        _/  _/  _/  _/    _/    _/_/    _/_/_/_/  _/   \n"
                                                     + " _/      _/  _/        _/      _/  _/    _/  _/    _/  _/        _/    \n"
                                                     + "_/      _/    _/_/_/  _/      _/    _/_/    _/    _/    _/_/_/  _/     \n";

    public Console(final Machine _m, final String _file) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        memTrack = null;
        file = null;
        loader = null;
        breakpoints = new TreeSet<Short>();
        watchpoints = new TreeMap<Integer, Short>();
        symbols = new TreeMap<String, Short>();
        lines = new TreeMap<Short, Integer>();
        m.ui.print(mcmoxel);
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
        else if ("symb".startsWith(words[0]))
            symb(words);
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
                for (final short b : breakpoints) {
                    final short inst = m.getMemory(b);
                    m.ui.print("\n    " + Utilities.uShortToHex(b) + ":   ["
                        + Utilities.uShortToHex(inst) + "] "
                        + m.alu.readInstruction(inst));
                }
            }
            return;
        }
        Short addr;
        if ((addr = readSymbAddr(words[del ? 2 : 1])) == null) {
            m.ui.print("Malformed address '" + words[del ? 2 : 1] + "'\n");
            help("help", "break");
            return;
        }
        if (breakpoints.contains(addr)) {
            if (del)
                breakpoints.remove(addr);
            else
                m.ui.print("A breakpoint is already set at "
                    + Utilities.uShortToHex(addr) + "\n");
        } else if (del)
            m.ui.print("No breakpoints set at " + Utilities.uShortToHex(addr) + "\n");
        else
            breakpoints.add(addr);
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
        Short base = memTrack;
        int len = 16;
        if (words.length > 1) {
            if ((base = readSymbAddr(words[1])) == null) {
                m.ui.print("Malformed address '" + words[1] + "'\n");
                help("help", "disasm");
                return;
            }
            if (words.length > 2)
                if (words[2].equalsIgnoreCase("halt"))
                    len = -1;
                else if (words[2].equalsIgnoreCase("ret"))
                    len = -2;
                else {
                    final Short v = readSymbAddr(words[2]);
                    if (v == null) {
                        m.ui.print("Malformed length parameter '" + words[2] + "'\n");
                        help("help", "disasm");
                        return;
                    }
                    len = v & 0xFFFF;
                }
        }
        short mem;
        if (base == null)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = base;
        for (int i = 0;; i++) {
            final short inst = m.getMemory((short) (mem + i));
            m.ui.print("\n  " + padLeft(toSymb((short) (mem + i)), symbLength + 4, ' ')
                + ": ");
            if (lines.size() == 0)
                m.ui.print(" ");
            else if (lines.containsKey((short) (mem + i)))
                m.ui.print(padRight("L" + lines.get((short) (mem + i)), 4, ' '));
            else
                m.ui.print("    ");
            m.ui.print(" [" + Utilities.uShortToHex(inst) + "] "
                + m.alu.readInstruction(inst));
            if (len == -1 && inst == (short) 0xF025 || len == -2
                && (inst & 0xF000) == 0xD000 || len >= 0 && i + 2 > len)
                break;
        }
    }

    private void dump(final String... words) {
        Short base = memTrack;
        int len = 8;
        if (words.length > 1) {
            if ((base = readSymbAddr(words[1])) == null) {
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
        if (base == null)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = base;
        m.ui.print("\n  " + padLeft("", symbLength + 4, ' '));
        for (int j = 0; j < 16; j++)
            m.ui.print(" --" + Integer.toHexString(mem + j & 15).toUpperCase() + "-");
        m.ui.print("  ");
        for (int j = 0; j < 16; j++)
            m.ui.print(Integer.toHexString(mem + j & 15).toUpperCase());
        for (int i = 0; i < len; i++) {
            m.ui.print("\n");
            m.ui.print(padLeft(toSymb((short) (mem + 16 * i)), symbLength + 4, ' ')
                + " | ");
            for (int j = 0; j < 16; j++)
                m.ui.print(Utilities.uShortToHex(m.getMemory((short) (mem + 16 * i + j)))
                    + " ");
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
        Short addr, value;
        if ((addr = readSymbAddr(words[1])) == null) {
            m.ui.print("Malformed address '" + words[1] + "'\n");
            help("help", "edit");
            return;
        }
        if ((value = readSymbAddr(words[2])) == null) {
            m.ui.print("Malformed value '" + words[2] + "'\n");
            help("help", "edit");
            return;
        }
        m.setMemory(addr, value);
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
            + "    symb            trace           track           watch";
        if (words.length < 2 || words[1].length() == 0)
            m.ui.print(help);
        else if ("break".startsWith(words[1]))
            m.ui.print(" Syntax: break [-d] <address>\n         break [-D]\n"
                + " Mnemonics: b br bre brea break\n\n"
                + " Breakpoints allow the user to stop execution of the program when certain\n"
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
            m.ui.print(" Syntax: clock [<max>]\n Mnemonics: c cl clo cloc clock\n\n"
                + "Used without arguments, the clock command will print how many\n"
                + " instructions have been executed since the last reset (see 'help reset'),\n"
                + " along with the current maximum. The maximum exists to stop the machine\n"
                + " if an infinite loop is entered. If the maximum is reached (by running\n"
                + " the machine in 'run', 'step', or 'trace' mode), it will automatically\n"
                + " be doubled, after stopping the machine, so that execution will continue\n"
                + " if the machine is run again immediately.\n\n"
                + " The optional 'max' parameter allows the user to set a new maximum.\n"
                + " Setting a negative value removes the limit entirely.");
        else if ("disasm".startsWith(words[1]))
            m.ui.print(" Syntax: disasm [<address> [halt|ret|<length>]]\n"
                + " Mnemonics: d di dis disa disas disasm\n\n"
                + " The disasm command provides a disassembled interpretation of the words\n"
                + " located at the specified address. The optional second argument allows\n"
                + " the choice of having a set number of instructions disassembled, or to\n"
                + " continue disassembling until a TRAP HALT or RET command is encountered.\n\n"
                + " The command will provide assembly mnemonic listings for 16 consecutive\n"
                + " words (by default). The first column of the listing specifies the\n"
                + " address of the word being disassembled, the second column (if present)\n"
                + " specifies the line number in the original file, the third column\n"
                + " contains the hexadecimal data at that word, while the remaining columns\n"
                + " provide the disassembled instructions.\n\n"
                + "   > disasm           Disassembles starting at the current memory track\n"
                + "                      (see 'help track' for details)\n"
                + "   > disasm pc halt   Disassembles starting at PC, and continuing until\n"
                + "                      TRAP HALT is found\n"
                + "   > disasm x3000     Disassembles 16 instructions starting at 0x3000\n\n"
                + "   > disasm x1000 20  Disassembles 20 instructions starting at 0x1000\n"
                + "   > disasm r0 ret    Disassembles starting at the address in register\n"
                + "                      0, and continuing until RET is found\n\n"
                + "Anatomy of a line of disasm output:\n"
                + "  1020: L18  [1021] ADD R0, R0, #1\n"
                + "  +---  +--   +---  +-------------\n"
                + "  |     |     |     +- The instruction in MMXI assembly format\n"
                + "  |     |     +------- The hexadecimal value of the instruction\n"
                + "  |     +------------- The line number (line 18)\n"
                + "  +------------------- The hexadecimal address of the instruction");
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
            m.ui.print(" Syntax: load [<file>]\n"
                + " Mnemonics: l lo loa load\n\n"
                + " The load command loads a compiled assembly language program from the\n"
                + " disk. The program will be in its initial state after loading and will\n"
                + " need to be started via the run command afterwards. If the 'file'\n"
                + " parameter is given, it reloads the last loaded program.\n\n"
                + "   > load test.out       loads 'test.out' from current directory\n"
                + "   > load                reloads 'test.out'\n"
                + "   > load /usr/bin/test  loads 'test' from /usr/bin/\n\n"
                + " The file must be in MMXI file format. Otherwise, any parsing errors\n"
                + " will be displayed on-screen, with the machine in an indeterminate\n"
                + " state afterwards (another load command will reset the machine).");
        else if ("run".startsWith(words[1]))
            m.ui.print(" Syntax: run\n"
                + " Mnemonics: r ru run\n\n"
                + " The run command executes the program starting from the current location\n"
                + " of the PC. Program execution stops if:\n\n"
                + "   1: The program hits a TRAP HALT command and exits normally.\n"
                + "   2: The program reaches its clock maximum after a certain number of\n"
                + "      instructions. If this happens, the clock maximum is automatically\n"
                + "      doubled, and execution will continue if another run command is\n"
                + "      issued. (See 'help clock' for details on the clock maximum.)\n"
                + "   3: A breakpoint (see 'help break') is reached.\n"
                + "   4: A watched portion of memory (see 'help watch') is modified.\n\n"
                + " Under all conditions, another run command will pick up execution where\n"
                + " the previous one stopped. If the program exited normally, this means\n"
                + " execution will continue after a TRAP HALT command, which is usually\n"
                + " invalid memory, so undesired operation will occur.");
        else if ("reg".startsWith(words[1]))
            m.ui.print(" Syntax: reg [pc|flags|r<num> [<value>]]\n"
                + " Mnemonics: re reg\n\n"
                + " The reg command can be used to display and modify registers.\n\n"
                + " Without arguments, the reg command will display all 8 integer unit\n"
                + " registers, the PC, and the n/z/p condition code flags.\n\n"
                + " To display a specific register, use the reg command followed by the\n"
                + " specific register designation.  You can refer to the registers by\n"
                + " name (R0 thru R7). In addition to the integer unit data registers, you\n"
                + " can also access the pc and flags control registers.\n\n"
                + " The contents of a register may be set by adding the desired value after\n"
                + " the register name.\n\n"
                + "   > reg r0 x001C  Set register 0 to the value 0x001C\n"
                + "   > reg flags 4   Set the flags to n=1, z=0, p=0.\n"
                + "   > reg pc r7     Set the PC to the contents of register 7\n"
                + "                   (equivalent to a RET command)");
        else if ("reset".startsWith(words[1]))
            m.ui.print(" Syntax: reset [-l] [<fill>|rand]\n Mnemonics: res rese reset\n\n"
                + " The reset command clears all registers and memory and returns the\n"
                + " machine to its initial condition. If the -l option is set, it will load\n"
                + " the last loaded program after clearing. The optional fill argument\n"
                + " allows the user to fill memory with a single repeating word, or\n"
                + " randomize memory (which is the default behavior).\n\n"
                + "   > reset        Randomize all memory\n"
                + "   > reset xFEED  Set all registers and words of memory to 0xFEED\n"
                + "   > reset -l 0   Clear everything, then load the last loaded program");
        else if ("step".startsWith(words[1]))
            m.ui.print(" Syntax: step [<steps>]\n Mnemonics: s st ste step\n\n"
                + " Execute 'steps' instructions (default 1), then print the PC and\n"
                + " disassemble the next instruction to be evaluated. Execution may stop\n"
                + " after fewer than 'steps' instructions if breakpoints, watchpoints, or a\n"
                + " TRAP HALT command comes first (see 'help run').");
        else if ("symb".startsWith(words[1]))
            m.ui.print(" Syntax: symb <address> <value>\n"
                + "         symb -d <address>\n        symb [<prefix>]\n"
                + " Mnemonics: sy sym symb\n\n"
                + " Symbols are artifacts used during the assembly process for the benefit\n"
                + " of the user: mappings from words to addresses in memory or other word\n"
                + " values. This command allows the user to view, edit, and remove existing\n"
                + " symbols.\n\n"
                + "   > symb              Lists the currently defined symbols\n"
                + "   > symb s            Lists all symbols that start with 's'\n"
                + "   > symb start x3021  Defines or redefines 'start' to be x3021\n"
                + "   > symb -d start     Undefines symbol 'start'\n\n"
                + " A message will be displayed to notify you if you try to undefine a symbol\n"
                + " that is not defined. Note that 'pc', 'flags', and 'r0', ... 'r7' are not\n"
                + " legal symbol names, and symbols can not start with a numerical character.");
        else if (words[1].length() > 3 && "track".startsWith(words[1]))
            m.ui.print(" Syntax: track pc|<address>\n Mnemonics: trac track\n\n"
                + " Sets the memory track, which is used by the disasm, dump, and trace\n"
                + " commands as defaults for displaying blocks of memory. If 'pc' is chosen,"
                + " the printed portion of memory will start about 16 words before the PC (and\n"
                + " usually aligned to 8-word chunks, depending on the command), and follow\n"
                + " the PC as it changes. Any other choice will keep the track stationary on\n"
                + " the chosen address. The default setting is 'track pc'.\n\n"
                + "   > track pc     Keep the memory track on the PC\n"
                + "   > track x1000  Track memory location 0x1000\n"
                + "   > track r0     Track the memory location pointed to by R0 (unlike"
                + "                  'track pc', the track will NOT change if R0 changes)");
        else if ("trace".startsWith(words[1]))
            m.ui.print(" Syntax: trace [<steps>]\n Mnemonics: t tr tra trace\n\n"
                + " Execute 'steps' instructions (default 1), then print the contents of the\n"
                + " registers and a small amount of memory starting from the memory track\n"
                + " (see 'help track'), and disassemble the next instruction to be\n"
                + " evaluated. Execution may stop after fewer than 'steps' instructions if\n"
                + " breakpoints, watchpoints, or a TRAP HALT command comes first\n"
                + " (see 'help run').");
        else if ("watch".startsWith(words[1]))
            m.ui.print(" Syntax: watch [-d] <address>\n         watch [-D]\n"
                + " Mnemonics: w wa wat watc watch\n\n"
                + " Watchpoints allow the user to stop execution of the program when certain\n"
                + " registers or memory locations are altered. The watchpoint facility\n"
                + " functions during trace, step, and execution modes.\n\n"
                + " Watchpoints can be listed, added, or removed.\n\n"
                + "   > watch        Lists the currently set watchpoints\n"
                + "   > watch -D     Deletes all watchpoints\n"
                + "   > watch x3021  Sets watchpoint on memory address 0x3021\n"
                + "   > watch -d r3  Removes the watchpoint on register 3\n\n"
                + " A message will be displayed to notify you if you try to set a watchpoint\n"
                + " that already exists, or try to delete a watchpoint that does not exist.");
        else
            m.ui.print(help);

    }

    private void load(final String... words) {
        if (words.length < 2)
            if (file == null) {
                m.ui.print("No file loaded yet!\n");
                help("help", "load");
                return;
            }
        final List<Error> errors = new ArrayList<Error>();
        for (int i = 1; i < words.length; i++)
            if (loader == null) {
                m.ui.print("Loading main: " + words[i] + "\n");
                loader = new LinkingLoader(words[i], m, errors);
            } else {
                m.ui.print("Loading file: " + file + "\n");
                loader.addFile(words[i], errors);
            } // TODO
        lines.clear();
        symbols.clear();
        m.ui.printErrors(SimpleLoader.load(file, m, null, symbols)); // TODO
        symbLength = 0;
        for (final String k : symbols.keySet())
            if (k.length() > symbLength)
                symbLength = k.length();
        printInstruction();
    }

    private void reg(final String... words) {
        if (words.length <= 1) {
            for (int i = 0; i < 8; i++)
                m.ui.print("    R" + i);
            m.ui.print("    PC   FLAGS\n ");
            for (int i = 0; i < 8; i++)
                m.ui.print("  " + Utilities.uShortToHex(m.getRegister(i).getValue()));
            m.ui.print("  " + Utilities.uShortToHex(m.getPCRegister().getValue()));
            m.ui.print("  " + Utilities.uShortToHex(m.getFlags().getValue()));
            return;
        }
        Short value = null;
        if (words.length > 2)
            if ((value = readSymbAddr(words[2])) == null) {
                m.ui.print("Malformed value '" + words[2] + "'\n");
                help("help", "reg");
                return;
            }
        if (words[1].equalsIgnoreCase("pc")) {
            if (value != null)
                m.getPCRegister().setValue(value);
            m.ui.print("PC: " + Utilities.uShortToHex(m.getPCRegister().getValue()));
        } else if (words[1].equalsIgnoreCase("flags")) {
            if (value != null) {
                if (value != 1 && value != 2 && value != 4) {
                    m.ui.print("The FLAGS register may only be set to the values 1, 2, or 4.");
                    return;
                }
                m.getFlags().setValue(value);
            }
            m.ui.print("FLAGS: " + Utilities.uShortToHex(m.getFlags().getValue()) + " [");
            m.ui.print((m.getFlags().getN() ? "n" : "-")
                + (m.getFlags().getZ() ? "z" : "-") + (m.getFlags().getP() ? "p" : "-")
                + "]");
        } else if (words[1].matches("[rR][0-7]")) {
            final int rnum = words[1].charAt(1) - '0';
            if (value != null)
                m.getRegister(rnum).setValue(value);
            m.ui.print("R" + rnum + ": "
                + Utilities.uShortToHex(m.getRegister(rnum).getValue()));
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
        Short fill = null;
        if (words.length > load + 1)
            if ((fill = readSymbAddr(words[load + 1])) == null
                && !words[load + 1].equals("rand")) {
                m.ui.print("Malformed fill mode parameter '" + words[load + 1] + "'\n");
                help("help", "reset");
                return;
            }
        m.reset(fill);
        if (load != 0)
            load("load", file);
        else {
            lines.clear();
            symbols.clear();
        }
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

    private void symb(final String... words) {
        if (words.length <= 2) {
            if (symbols.size() == 0) {
                m.ui.print("No symbols are defined.");
                return;
            }
            final String start = words.length > 1 ? words[1] : "";
            for (final String k : symbols.keySet())
                if (k.startsWith(start))
                    m.ui.print(padLeft(k, symbLength + 3, ' ') + " = 0x"
                        + Utilities.uShortToHex(symbols.get(k)) + "\n");
        } else if (words[1].equalsIgnoreCase("-d"))
            if (symbols.containsKey(words[2])) {
                symbols.remove(words[2]);
                symbLength = 0;
                for (final String k : symbols.keySet())
                    if (k.length() > symbLength)
                        symbLength = k.length();
            } else
                m.ui.print("Symbol '" + words[2] + "' is not defined.");
        else {
            final Short v = readSymbAddr(words[2]);
            if (words[1].contains("+") || words[1].contains("-")
                || words[1].contains(":") || readAddr(words[1]) != null)
                m.ui.print("'" + words[1]
                    + "' is not a valid symbol name. See 'help symb' for syntax.");
            else if (v == null)
                m.ui.print("Malformed value '" + words[2]
                    + "' encountered. See 'help symb' for syntax.");
            else {
                symbols.put(words[1], v);
                if (words[1].length() > symbLength)
                    symbLength = words[1].length();
            }
        }

    }

    private void track(final String... words) {
        if (words.length < 2) {
            help("help", "track");
            return;
        }
        if (words[1].equalsIgnoreCase("pc")) {
            m.ui.print("Tracking Program Counter.");
            memTrack = null;
        } else {
            final Short addr = readSymbAddr(words[1]);
            if (addr == null) {
                m.ui.print("Invalid tracking address.");
                help("help", "track");
                return;
            } else
                memTrack = addr;
            m.ui.print("Tracking address " + Utilities.uShortToHex(memTrack) + ".");
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
                        m.ui.print("\n      R" + (b + 8) + ":    "
                            + Utilities.uShortToHex(m.getRegister(b + 8).getValue()));
                    else {
                        final short inst = m.getMemory((short) b);
                        m.ui.print("\n    " + Utilities.uShortToHex((short) b) + ":   ["
                            + Utilities.uShortToHex(inst) + "] "
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
                    + Utilities.uShortToHex((short) pt) + "\n");
        } else if (!del)
            watchpoints.put(pt,
                pt < 0 ? m.getRegister(pt + 8).getValue() : m.getMemory((short) pt));
        else if (pt < 0)
            m.ui.print("No watchpoints set on register " + (pt + 8) + ".\n");
        else
            m.ui.print("No watchpoints set on address "
                + Utilities.uShortToHex((short) pt) + ".\n");
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
            if (breakpoints.contains(pc)) {
                m.ui.print("Breakpoint encountered at " + Utilities.uShortToHex(pc));
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
                            + Utilities.uShortToHex((short) k));
                    m.ui.print(": value changed from "
                        + Utilities.uShortToHex(e.getValue()) + " to "
                        + Utilities.uShortToHex(v));
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
        if (memTrack == null)
            mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
        else
            mem = memTrack;
        m.ui.print("\n" + padLeft("", symbLength + 26, ' '));
        for (int j = 0; j < 8; j++)
            m.ui.print(" --" + (mem + j & 7) + "-");
        m.ui.print("\n");
        for (int i = 0; i < 4; i++) {
            m.ui.print("R" + 2 * i + ": ");
            m.ui.print(Utilities.uShortToHex(m.getRegister(2 * i).getValue()) + "  ");
            m.ui.print("R" + (2 * i + 1) + ": ");
            m.ui.print(Utilities.uShortToHex(m.getRegister(2 * i + 1).getValue()));
            m.ui.print("  "
                + padLeft(toSymb((short) (mem + 16 * i)), symbLength + 4, ' ') + " | ");
            for (int j = 0; j < 8; j++)
                m.ui.print(Utilities.uShortToHex(m.getMemory((short) (mem + 8 * i + j)))
                    + " ");
            m.ui.print("\n");
        }
    }

    private void printInstruction() {
        final short pc = m.getPCRegister().getValue();
        m.ui.print("\n  PC: " + Utilities.uShortToHex(pc) + "  ");
        m.ui.print((m.getFlags().getN() ? "n" : "-") + (m.getFlags().getZ() ? "z" : "-")
            + (m.getFlags().getP() ? "p" : "-") + "  ");
        if (lines.containsKey(pc))
            m.ui.print("Line " + lines.get(pc) + ": ");
        m.ui.print(Utilities.uShortToHex(m.getMemory(pc)) + ": ");
        m.ui.print(m.alu.readInstructionAt(pc));
    }

    private Short readSymbAddr(final String s) {
        int d = s.lastIndexOf('+');
        int sgn = 1;
        if (d < s.lastIndexOf('-')) {
            sgn = -1;
            d = s.lastIndexOf('-');
        }
        if (d == -1) {
            Short a = readAddr(s);
            if (a == null && symbols.containsKey(s)) {
                final Short v = symbols.get(s);
                if (v != null)
                    a = v;
            }
            return a;
        } else {
            final Short l = readSymbAddr(s.substring(0, d));
            final Short r = readSymbAddr(s.substring(d + 1));
            if (l == null || r == null)
                return null;
            else
                return (short) (l + sgn * r);
        }
    }

    private Short readAddr(final String s) {
        if (s.equalsIgnoreCase("pc"))
            return m.getPCRegister().getValue();
        else if (s.matches("[rR][0-7]"))
            return m.getRegister(s.charAt(1) - '0').getValue();
        else
            return Utilities.parseShort(s);
    }

    private String toSymb(final short addr) {
        return toSymb(addr, symbols);
    }

    public static String toSymb(final short addr, final Map<String, Short> symb) {
        int closest = -1;
        final int uAddr = addr & 0xFFFF;
        String cSymb = null;
        for (final Entry<String, Short> i : symb.entrySet()) {
            final int uVal = i.getValue() & 0xFFFF;
            if (uVal > closest && uVal <= uAddr) {
                cSymb = i.getKey();
                closest = uVal;
            }
        }
        if (cSymb == null || uAddr - closest >= 512)
            return Utilities.uShortToHex(addr);
        else
            return cSymb + (uAddr == closest ? "" : "+" + (uAddr - closest));
    }

    private String padLeft(final String s, int len, final char pad) {
        len -= s.length();
        if (len < 0)
            len = 0;
        return new String(new char[len]).replace('\0', pad) + s;
    }

    private String padRight(final String s, int len, final char pad) {
        len -= s.length();
        if (len < 0)
            len = 0;
        return s + new String(new char[len]).replace('\0', pad);
    }
}

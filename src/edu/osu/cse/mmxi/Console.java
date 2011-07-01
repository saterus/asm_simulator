package edu.osu.cse.mmxi;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Console {
    private final Machine m;
    private int           maxClock;

    public Console(final Machine _m) {
        m = _m;
        maxClock = Simulator.MAX_CLOCK_COUNT;
        m.ui.print("McMoxel MMXI Emulator\n");
        m.ui.print("Version 2\n\n");
        while (true)
            promptForCommand();
    }

    private void promptForCommand() {
        final String cmd = m.ui.prompt("\n\nMcMoxel> ");
        final String[] words = cmd.split(" ");
        if (words.length == 0)
            return;
        if (words[0].equals("help"))
            help(words);
        else if (words[0].equals("run"))
            run(words);
        else if (words[0].equals("step"))
            step(words);
        else if (words[0].equals("trace"))
            trace(words);
        else
            m.ui.warn("Unknown command");
    }

    private void trace(final String[] words) {
        // TODO Auto-generated method stub

    }

    private void step(final String[] words) {

    }

    private void printInstruction() {
        m.ui.print("\n          PC: "
            + MemoryUtilities.uShortToHex(m.getPCRegister().getValue()) + "  ");
        m.ui.print((m.getFlags().getN() ? "n" : "-") + (m.getFlags().getZ() ? "z" : "-")
            + (m.getFlags().getP() ? "p" : "-") + "  ");
        m.ui.print(MemoryUtilities.uShortToHex(m.getMemory(m.getPCRegister().getValue()))
            + ": ");
        m.ui.print(instructionDetails + "\n\n");
    }

    private void run(final String[] words) {
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

    private void help(final String[] words) {

    }

}

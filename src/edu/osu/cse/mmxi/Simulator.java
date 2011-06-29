package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
import edu.osu.cse.mmxi.ui.UI;

/** TODO: Write decent high level description of the Simulator as the main driver. */
public final class Simulator {

    private final static int     MAX_CLOCK_COUNT = 100000;
    private final static boolean printTrace      = false;

    /**
     * <p>
     * The Simulator begins stepping the Machine's clock.
     * </p>
     * 
     * <p>
     * The Simulator controls the Machine's clock by telling it when it is ready to
     * advance to the next instruction and execute it.
     * </p>
     * 
     * <p>
     * If the Machine has halted or run beyond the maximum number of instructions, the
     * clock stops.
     * </p>
     * 
     * <p>
     * During Step or Trace mode, we pass information from the Machine to the UI during
     * each clock step.
     * </p>
     */
    public static void startClockLoop(final Machine machine) {

        while (machine.clockCount() < MAX_CLOCK_COUNT && !machine.hasHalted()) {

            if (printTrace) {
                if (machine.clockCount() % 20 == 0) {
                    machine.print(" PC");
                    for (int i = 0; i < 8; i++)
                        machine.print("   R" + i);
                    machine.print("  nzp inst\n");
                }

                machine.print(MemoryUtilities.uShortToHex(machine.getPCRegister()
                    .getValue()) + " ");
                for (int i = 0; i < 8; i++)
                    machine.print(MemoryUtilities.uShortToHex(machine.getRegister(i)
                        .getValue()) + " ");
                machine.print((machine.getFlags().getN() ? "n" : "-")
                    + (machine.getFlags().getZ() ? "z" : "-")
                    + (machine.getFlags().getP() ? "p" : "-") + " ");

                machine.print(MemoryUtilities.uShortToHex(machine.getMemory(machine
                    .getPCRegister().getValue())) + " ");
            }

            final String instructionDetails = machine.stepClock();

            // TODO: if tracing/stepping, print instruction details and machine stats
            // if stepping, pause for user.
            if (printTrace)
                machine.print(instructionDetails + "\n");
        }
    }

    /**
     * <p>
     * This function reads the command line arguments passed in, and parses them to
     * extract the file to be processed and any ther optional arguments.
     * </p>
     * 
     * <p>
     * The format of the instruction is:
     * </p>
     * 
     * <pre>
     *    java Simulator [-c<i>num</i>|--max-clock-ticks <i>num</i>]
     *                   [-s|-t|-q|--step|--trace|--quiet]
     *                   <i>file.txt</i>
     * </pre>
     * 
     * <p>
     * The <code>--max-clock-ticks</code> argument (short name <code>-c</code>) sets the
     * maximum number of instructions to be executed before quitting (assuming a
     * <code>TRAP HALT</code> command hasn't already been executed). If this limit is
     * reached, a prompt is given to optionally allow continued operation of the program.
     * The default value for <i>num</i> is 10000. If a negative value is given for
     * <i>num</i>, the machine is never halted.
     * </p>
     * 
     * <p>
     * The <code>--step</code>, <code>--trace</code>, and <code>--quiet</code> modes
     * (short names <code>-s</code>, <code>-t</code>, and <code>-q</code>) are mutually
     * exclusive and control the operation of the machine. In quiet mode, all instructions
     * are executed normally and the only output is that driven by the program. In trace
     * mode, every instruction, along with the current register state, is printed as it is
     * executed, for debugging purposes. In step mode, a detailed view of the register
     * states and the current page of memory is printed after every instruction, and the
     * machine stops and waits for a command to continue each time.
     * </p>
     * 
     * @param args
     */
    public static void processArgs(final String[] args) {

        if (args.length != 1) {
            System.err
                .println("program requires exactly one argument: the file to be processed");
            System.exit(1);
            // TODO: exiting here might be harsher than we want.
        }
    }

    public static void main(final String[] args) {

        processArgs(args);

        final UI cli = new UI();
        final Machine machine = new Machine();

        try {

            SimpleLoader.load(args[0], machine);

        } catch (final ParseException e) {
            cli.error(e.getMessage());
        } catch (final IOException e) {
            cli.error("I/O Error: " + e.getMessage());
        }

        startClockLoop(machine);
    }
}

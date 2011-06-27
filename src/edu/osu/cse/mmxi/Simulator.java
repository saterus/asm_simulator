package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
import edu.osu.cse.mmxi.ui.UI;

/** TODO: Write decent high level description of the Simulator as the main driver. */
public final class Simulator {

    private final static int MAX_CLOCK_COUNT = 100000;

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

            if (machine.clockCount() % 20 == 0) {
                machine.print(" PC");
                for (int i = 0; i < 8; i++)
                    machine.print("   R" + i);
                machine.print("  inst\n");
            }

            machine.print(MemoryUtilities.uShortToHex(machine.getPCRegister().getValue())
                + " ");
            for (int i = 0; i < 8; i++)
                machine.print(MemoryUtilities.uShortToHex(machine.getRegister(i)
                    .getValue()) + " ");

            machine.print(MemoryUtilities.uShortToHex(machine.getMemory(machine
                .getPCRegister().getValue())) + " ");

            final String instructionDetails = machine.stepClock();

            // TODO: if tracing/stepping, print instruction details and machine stats
            // if stepping, pause for user.
            machine.print(instructionDetails + "\n");
            machine.print("\n");
        }
    }

    // TODO Handle optional flag for [--step/--trace/--quiet] mode
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

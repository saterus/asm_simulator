package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryException;
import edu.osu.cse.mmxi.ui.UI;

public final class Simulator {

    private final static int MAX_CLOCK_COUNT = 10000;

    /**
     * The Simulator begins stepping the Machine's clock.
     * 
     * The Simulator controls the Machine's clock by telling it when it is ready
     * to advance to the next instruction and execute it.
     * 
     * If the Machine has halted or run beyond the maximum number of
     * instructions, the clock stops.
     * 
     * During Step or Trace mode, we pass information from the Machine to the UI
     * during each clock step.
     */
    public static void startClockLoop(Machine machine) {

        while (machine.clockCount() < MAX_CLOCK_COUNT && !machine.hasHalted()) {

            String instructionDetails = machine.stepClock();
            // TODO: if tracing/stepping, print instruction details and machine stats
            // if stepping, pause for user.
        }
    }

    // TODO Handle optional flag for [--step/--trace/--quiet] mode
    public static void processArgs(String[] args) {
        if (args.length != 1) {
            System.err
                    .println("program requires exactly one argument: the file to be processed");
        }
    }

    public static void main(String[] args) {

        processArgs(args);

        UI cli = new UI();
        Machine machine = new Machine();

        try {

            SimpleLoader.load(args[0], machine);

        } catch (MemoryException e) {
            cli.error("error: attempted to write out of allowed memory");
        } catch (ParseException e) {
            cli.error(e.getMessage());
        } catch (IOException e) {
            cli.error("I/O Error: " + e.getMessage());
        }

        startClockLoop(machine);
    }
}

package edu.osu.cse.mmxi;

import java.io.IOException;

import edu.osu.cse.mmxi.loader.SimpleLoader;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
import edu.osu.cse.mmxi.ui.UI;

/** TODO: Write decent high level description of the Simulator as the main driver. */
public final class Simulator {

    private static final int     QUIET           = 1;
    private static final int     TRACE           = 2;
    private static final int     STEP            = 3;

    private static int           MAX_CLOCK_COUNT = 9999; // counts inclusively, so this
                                                          // is max 10000
    private static int           MODE            = 0;
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

        while (machine.clockCount() <= MAX_CLOCK_COUNT && !machine.hasHalted()) {

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
     * <p>
     * Sample valid command line strings:
     * </p>
     * 
     * <pre>
     *    java Simulator prog.txt
     *    java Simulator -c100000 prog.txt
     *    java Simulator -s prog.txt -c 100000
     *    java Simulator --max-clock-ticks 100000 prog.txt --step
     * </pre>
     * 
     * @param args
     *            the arguments in the command line
     */
    public static String processArgs(final String[] args) {
        boolean clockMode = false, error = false;
        String file = null;
        words: for (int i = 0; i < args.length; i++) {
            String word = args[i];
            if (clockMode) {
                clockMode = false;
                try {
                    if (word.length() > 2
                        && word.substring(0, 2).toLowerCase().equals("0x"))
                        MAX_CLOCK_COUNT = Integer.parseInt(word.substring(2), 16) - 1;
                    else
                        MAX_CLOCK_COUNT = Integer.parseInt(word) - 1;
                } catch (final NumberFormatException e) {
                    error = true;
                    System.err.println("--max-clock-ticks argument "
                        + "in invalid format; ignoring...");
                }
            } else if (word.length() > 1 && word.charAt(0) == '-')
                if (word.length() > 2 && word.charAt(1) == '-') {
                    word = word.substring(2);
                    if (word.equals("max-clock-ticks"))
                        clockMode = true;
                    else if (word.equals("quiet"))
                        error |= setMode(QUIET);
                    else if (word.equals("trace"))
                        error |= setMode(TRACE);
                    else if (word.equals("step"))
                        error |= setMode(STEP);
                    else {
                        error = true;
                        System.err.println("Unknown command --" + word + "; ignoring...");
                    }
                } else
                    for (int j = 1; j < word.length(); j++)
                        switch (word.charAt(j)) {
                        case 'c':
                            clockMode = true;
                            if (j == word.length() - 1)
                                break;
                            else {
                                args[i--] = word.substring(j + 1);
                                continue words;
                            }
                        case 'q':
                            error |= setMode(QUIET);
                            break;
                        case 't':
                            error |= setMode(TRACE);
                            break;
                        case 's':
                            error |= setMode(STEP);
                            break;
                        }
            else if (file == null)
                file = word;
            else {
                error = true;
                System.err.println("More than one file given; ignoring \"" + word
                    + "\"...");
            }
        }
        if (file == null) {
            error = true;
            System.err.println("No files given!");
        }
        if (error) {
            System.err.println("Proper syntax:");
            System.err.println("java Simulator [-c num|--max-clock-ticks num]");
            System.err.println("               [-s|-t|-q|--step|--trace|--quiet]");
            System.err.println("               file.txt");
        }
        if (file == null)
            System.exit(1);
        if (MODE == 0)
            MODE = 1;
        if (MAX_CLOCK_COUNT < -1) // // // // // // Using this value means that the
            MAX_CLOCK_COUNT = Integer.MAX_VALUE; // clockCount() <= MAX comparison above
                                                 // will always be true due to overflow
        return file;
    }

    private static boolean setMode(final int mode) {
        final String[] modes = { "quiet", "trace", "step" };
        boolean error = false;
        if (MODE != 0) {
            error = true;
            System.err.println("More than one mode setting found. Setting "
                + modes[mode - 1] + " mode...");
        }
        MODE = mode;
        return error;
    }

    public static void main(final String[] args) {

        final String file = processArgs(args);

        final UI cli = new UI();
        final Machine machine = new Machine();

        try {

            SimpleLoader.load(file, machine);

        } catch (final ParseException e) {
            cli.error(e.getMessage());
        } catch (final IOException e) {
            cli.error("I/O Error: " + e.getMessage());
        }

        startClockLoop(machine);
    }
}

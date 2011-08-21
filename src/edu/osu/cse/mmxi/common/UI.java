package edu.osu.cse.mmxi.common;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.osu.cse.mmxi.common.error.Error;

public class UI {
    protected InputStream in = System.in;
    protected PrintStream out = System.out, err = System.err;

    public void error(final String msg) {
        warn(msg);
        exit();
    }

    /**
     * Wrapper for System.exit(1): exit with error
     */
    public void exit() {
        System.exit(1);
    }

    public void warn(final String msg) {
        err.println(msg);
    }

    public void print(final String msg) {
        out.print(msg);
    }

    public void println(final String msg) {
        out.println(msg);
    }

    public String prompt(final String msg) {
        print(msg);
        try {
            return new Scanner(in).nextLine();
        } catch (final NoSuchElementException e) {
            return "";
        }
    }

    /**
     * Wrapper for printing errors returned from SimpleLoader
     * 
     * @param ui
     *            Reference to the User Interface (UI)
     * @param errors
     *            List of errors
     * @see edu.osu.cse.mmxi.sim.loader.SimpleLoader
     */
    public void printErrors(final Error... errors) {
        printErrors(Arrays.asList(errors));
    }

    /**
     * Wrapper for printing errors returned from SimpleLoader
     * 
     * @param ui
     *            Reference to the User Interface (UI)
     * @param errors
     *            List of errors
     * @see edu.osu.cse.mmxi.sim.loader.SimpleLoader
     */
    public void printErrors(final List<Error> errors) {
        // flag for warn
        boolean warn = false;
        boolean fatal = false;

        for (final Error e : errors)
            switch (e.getLevel()) {
            case FATAL:
                fatal = true;
                warn(e.toString());
                break;
            default:
            case WARN:
                warn = true;
                warn(e.toString());
                break;
            case MSG:
                print(e.toString());
                break;
            }

        if (fatal)
            error("Fatal Errors Detected.  Exiting Program.");
        else if (warn) {
            String input = null;
            input = prompt("\nWarnings Detected.  Continue or Quit (q)?");
            if (input.equalsIgnoreCase("q")) {
                print("Exiting...");
                exit();
            }
        } else if (errors.size() != 0)
            prompt("Messages Detected. Press any key to continue.");
        errors.clear();
    }
}

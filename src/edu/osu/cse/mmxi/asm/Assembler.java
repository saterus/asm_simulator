package edu.osu.cse.mmxi.asm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.osu.cse.mmxi.asm.error.Error;
import edu.osu.cse.mmxi.asm.error.ErrorCodes;
import edu.osu.cse.mmxi.asm.error.ParseException;
import edu.osu.cse.mmxi.asm.io.InputOutput;
import edu.osu.cse.mmxi.asm.ui.UI;

public class Assembler {
    public InputOutput io;
    public String      segName = null;
    final List<Error>  errors  = new ArrayList<Error>();
    final UI           ui;

    public Assembler(final UI ui, final String in, final String out,
        final String intermediate) throws IOException {
        this.ui = ui;
        io = new InputOutput();
        try {
            io.openReader(in);
        } catch (final FileNotFoundException e) {
            errors.add(new Error(ErrorCodes.IO_BAD_FILE));
            printErrors(ui, errors);
        }

        String interDat = null;

        try {
            interDat = new Pass1Parser(this, errors).parse();
        } catch (final ParseException e) {
            final Error error = new Error(e.getLineNumber(), e.getMessage(),
                e.getErrorCode());
            errors.add(error);
        } catch (final IOException e) {
            errors.add(new Error(ErrorCodes.IO_BAD_READ));
        }

        if (intermediate == null)
            // System.out.print(interDat);
            Assembler.printErrors(ui, errors);
        else
            InputOutput.writeFile(intermediate, interDat);
        if (out != null) {
            io.resetReader();
            try {
                io.openWriters(out, null);
            } catch (final FileNotFoundException e) {
                errors.add(new Error(ErrorCodes.IO_BAD_READ));
                printErrors(ui, errors);
            }
            new Pass2Parser(this).parse();
            io.closeWriters();
        }
        io.closeReader();
    }

    public static void main(final String[] args) throws IOException {
        final UI ui = new UI();
        String file = null;
        boolean intermediate = false;
        final List<Error> errors = new ArrayList<Error>();
        for (final String s : args)
            if (s.equals("-i"))
                intermediate = true;
            else if (file == null)
                file = s;
            else
                errors.add(new Error(s, ErrorCodes.IO_MANY_INPUT));
        if (file == null)
            errors.add(new Error(ErrorCodes.IO_BAD_INPUT));
        printErrors(ui, errors);
        String stem = file;
        if (stem.indexOf('.') != 0)
            stem = stem.substring(0, stem.lastIndexOf('.'));
        new Assembler(ui, args[0], stem + ".o", intermediate ? stem + ".i" : null);
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
    public static void printErrors(final UI ui, final Error... errors) {
        printErrors(ui, Arrays.asList(errors));
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
    public static void printErrors(final UI ui, final List<Error> errors) {
        // flag for warn
        boolean warn = false;
        boolean fatal = false;

        for (final Error e : errors)
            switch (e.getLevel()) {
            case FATAL:
                fatal = true;
                ui.warn(e.toString());
                break;
            default:
            case WARN:
                warn = true;
                ui.warn(e.toString());
                break;
            case MSG:
                ui.print(e.toString());
                break;
            }

        if (fatal)
            ui.error("Fatal Errors Detected.  Exiting Program.");
        else if (warn) {
            String input = null;
            input = ui.prompt("\nWarnings Detected.  Continue or Quit (q)?");
            if (input.equalsIgnoreCase("q")) {
                ui.print("Exiting...");
                ui.exit();
            }
        } else if (errors.size() != 0)
            ui.prompt("Messages Detected. Press any key to continue.");
    }
}

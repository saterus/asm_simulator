package edu.osu.cse.mmxi.sim.ui;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import edu.osu.cse.mmxi.common.UI;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.sim.error.SimCodes;

public class SimUI extends UI {

    public enum UIMode {
        QUIET, TRACE, STEP
    };

    private UIMode mode;

    public SimUI() {
        this(null);
    }

    public SimUI(final UIMode _mode) {
        mode = _mode;
    }

    public boolean setMode(final UIMode _mode) {
        final boolean ret = mode == null;
        mode = _mode;
        return ret;
    }

    public UIMode getMode() {
        return mode;
    }

    public byte getChar() {
        byte b = 0;
        try {
            final int n = in.read();
            if (n != -1)
                b = (byte) n;
        } catch (final IOException e) {
            // Should we error?
        }
        return b;
    }

    public short getShort() {
        short s = 0;
        String prompt = "Enter a number: ";
        final Scanner sc = new Scanner(in);
        String num;
        while (true) {
            print(prompt);
            num = null;
            try {
                num = sc.next("-?(0[xX][0-9A-Fa-f]+|[0-9]+)");
            } catch (final NoSuchElementException e) {
                printErrors(new Error("while reading number", SimCodes.EXEC_END_OF_FILE));
            }
            if (num == null)
                prompt = "You can do better than that. Put your heart into it: ";
            else
                break;
        }
        boolean neg = false;
        if (num.charAt(0) == '-') {
            neg = true;
            num = num.substring(1);
        }
        if (num.length() >= 2 && num.substring(0, 2).toLowerCase().equals("0x"))
            s = Short.parseShort((neg ? "-" : "") + num.substring(2), 16);
        else
            s = Short.parseShort((neg ? "-" : "") + num);
        return s;
    }
}

package edu.osu.cse.mmxi.ui;

import java.io.IOException;
import java.util.Scanner;

public class UI {

    public enum UIMode {
        QUIET, TRACE, STEP
    };

    private UIMode mode;

    public UI() {
        this(null);
    }

    public UI(final UIMode _mode) {
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

    public void error(final String msg) {
        warn(msg);
        System.exit(1);
    }

    public void warn(final String msg) {
        System.err.println(msg);
    }

    public void print(final String msg) {
        System.out.print(msg);
    }

    public String prompt(final String msg) {
        print(msg);
        return new Scanner(System.in).nextLine();
    }

    public byte getChar() {
        byte b = 0;
        try {
            final int n = System.in.read();
            if (n != -1)
                b = (byte) n;
        } catch (final IOException e) {
            // Should we error?
        }
        return b;
    }

    public short getShort() {
        short s = 0;
        try {
            final Scanner sc = new Scanner(System.in);
            String num = sc.next("-?(0[xX][0-9A-Fa-f]+|[0-9]+)");
            boolean neg = false;
            if (num.charAt(0) == '-') {
                neg = true;
                num = num.substring(1);
            }
            if (num.substring(0, 2).toLowerCase().equals("0x"))
                s = Short.parseShort((neg ? "-" : "") + num.substring(2), 16);
            else
                s = Short.parseShort((neg ? "-" : "") + num);
        } catch (final NumberFormatException e) {
            // Should we error?
        }
        return s;
    }
}

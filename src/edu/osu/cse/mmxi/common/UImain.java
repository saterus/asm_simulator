package edu.osu.cse.mmxi.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UImain {
    protected InputStream in = System.in;
    protected PrintStream out = System.out, err = System.err;

    public void error(final String msg) {
        warn(msg);
        System.exit(1);
    }

    /**
     * Wrapper for system.exit(1)
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
        return new Scanner(in).nextLine();
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
                printErrorEndOfFile();
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

    /**
     * Must be overwritten
     */
    public void printErrorEndOfFile() {

    }
}

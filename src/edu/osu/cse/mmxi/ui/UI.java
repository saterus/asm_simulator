package edu.osu.cse.mmxi.ui;

public class UI {

    public enum UIMode {
        QUIET, TRACE, STEP
    };

    private final UIMode mode;

    public UI() {
        this(UIMode.QUIET);
    }

    public UI(final UIMode mode) {
        this.mode = mode;
    }

    public void error(final String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    public void warn(final String msg) {
        System.err.println(msg);
    }

    public void print(final String msg) {
        System.out.print(msg);
    }
}

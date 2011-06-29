package edu.osu.cse.mmxi.ui;

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

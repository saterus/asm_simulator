package edu.osu.cse.mmxi.sim.ui;

import edu.osu.cse.mmxi.common.UImain;
import edu.osu.cse.mmxi.sim.Simulator;
import edu.osu.cse.mmxi.sim.error.Error;
import edu.osu.cse.mmxi.sim.error.ErrorCodes;

public class UI extends UImain {

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

    @Override
    public void printErrorEndOfFile() {
        Simulator.printErrors(this, new Error("while reading number",
            ErrorCodes.EXEC_END_OF_FILE));
    }
}

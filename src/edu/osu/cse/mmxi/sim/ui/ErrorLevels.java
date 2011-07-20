package edu.osu.cse.mmxi.sim.ui;

public enum ErrorLevels {
    FATAL("Fatal Error"), WARN("Warning"), MSG("Note");

    private String title;

    ErrorLevels(final String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}

package edu.osu.cse.mmxi.common.error;

/**
 * McMoxell supports 3 error levels. FATAL - This will ultimately halt execution during
 * the printing of error messages. WARN - will display errors, but will prompt user for if
 * they want to continue. MSG - This will just diplay a message to the user when errors
 * are printed.
 * 
 */
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

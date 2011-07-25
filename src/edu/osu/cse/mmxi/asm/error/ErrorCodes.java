package edu.osu.cse.mmxi.asm.error;

import edu.osu.cse.mmxi.common.error.ErrorLevels;

public enum ErrorCodes {
    // simpleLoaderErrors IO
    IO_BAD_PATH(100, "File path does not refer to a file", ErrorLevels.FATAL),

    IO_BAD_READ(101, "Failed to read file", ErrorLevels.FATAL),

    IO_BAD_FILE(102, "File was empty", ErrorLevels.FATAL),

    EXEC_END_OF_FILE(402, "End of File reached prematurely", ErrorLevels.FATAL),

    // unknown error
    UNKNOWN(999, "Unknown Error", ErrorLevels.WARN);

    private int         code;
    private String      str;
    private ErrorLevels level;

    ErrorCodes(final int code, final String str, final ErrorLevels level) {
        this.str = str;
        this.code = code;
        this.level = level;
    }

    public String getMsg() {
        return str;
    }

    public int getCode() {
        return code;
    }

    public ErrorLevels getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return level + " " + code + (str == null ? "" : ": " + str);
    }
}

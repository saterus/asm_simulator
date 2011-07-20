package edu.osu.cse.mmxi.sim.ui;

public enum ErrorCodes {
    // simpleLoaderErrors IO
    IO_BAD_PATH(100, "File path does not refer to a file", ErrorLevels.FATAL),

    IO_BAD_READ(101, "Failed to read file", ErrorLevels.FATAL),

    IO_BAD_FILE(102, "File was empty", ErrorLevels.FATAL),

    // simpleLoader errors
    ADDR_OUT_BOUNDS(200, "Text address out of bounds", ErrorLevels.FATAL),

    ADDR_EXEC_OUT_BOUNDS(201, "Execution address out of bounds", ErrorLevels.FATAL),

    // parser errors
    PARSE_EXECPTION(300, "Parsing exception", ErrorLevels.WARN),

    PARSE_EMPTY(301, "Parsing complete, no tokens found.", ErrorLevels.FATAL),

    PARSE_NO_HEADER(302, "Object File did not contain a Header record", ErrorLevels.FATAL),

    PARSE_NO_RECORDS(303, "Object File did not contain any Text records",
        ErrorLevels.FATAL),

    PARSE_NO_EXEC(304, "Object File did not contain an Exec record", ErrorLevels.FATAL),

    PARSE_BAD_TEXT(399, "Malformed record", ErrorLevels.FATAL),

    // execution errors
    EXEC_TRAP_UNKN(400, "Unknown TRAP vector", ErrorLevels.WARN),

    EXEC_TRAP_OUT(401, "R0 does not contain a character", ErrorLevels.WARN),

    EXEC_END_OF_FILE(402, "End of File reached prematurely", ErrorLevels.FATAL),

    // UI errors
    UI_MAX_CLOCK(500, "--max-clock-ticks argument in invalid format", ErrorLevels.WARN),

    UI_MULTI_CLOCK(501, "duplicate --max-clock-ticks argument", ErrorLevels.WARN),

    UI_MULTI_FILE(502, "Multiple files given", ErrorLevels.WARN),

    UI_NO_FILE(503, "No files given", ErrorLevels.FATAL),

    UI_MULTI_SETTINGS(504, "Multiple settings given", ErrorLevels.WARN),

    UI_UNKN_CMD(599, "Unknown command", ErrorLevels.WARN),

    // messages
    MSG_SYNTAX(800, null, ErrorLevels.MSG),

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

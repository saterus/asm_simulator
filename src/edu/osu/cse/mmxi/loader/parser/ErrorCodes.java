package edu.osu.cse.mmxi.loader.parser;

public enum ErrorCodes {
    // unknow error
    UNKNOWN(0, "Unknow Error Occurred"),

    // simpleLoaderErrors IO
    IO_BAD_PATH(100, "File path does not refer to a file"), IO_BAD_READ(101,
        "Failed to read the file."), IO_BAD_FILE(102, "File was empty."),

    // simpleLoader errors
    ADDR_OUT_BOUNDS(200, "Text address out of bounds"), ADDR_EXEC_OUT_BOUNDS(201,
        "Execution address out of bounds");

    private int    code;
    private String str;

    ErrorCodes(final int code, final String str) {
        this.str = str;
        this.code = code;
    }

    public String getErrMsg() {
        return str;
    }

    public int getErrCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + " : " + str;
    }
}

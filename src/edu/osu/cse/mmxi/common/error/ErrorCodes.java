package edu.osu.cse.mmxi.common.error;

public interface ErrorCodes {
    public String getMsg();

    public int getCode();

    public ErrorLevels getLevel();

    public static class Unknown implements ErrorCodes {
        @Override
        public String getMsg() {
            return "Unknown Error";
        }

        @Override
        public int getCode() {
            return 999;
        }

        @Override
        public ErrorLevels getLevel() {
            return ErrorLevels.WARN;
        }

    }
}

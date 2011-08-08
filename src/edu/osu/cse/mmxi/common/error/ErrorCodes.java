package edu.osu.cse.mmxi.common.error;

/**
 * Interface for which ErrorCodes must follow. This ensures there is atleast and error
 * code of 999.
 * 
 */
public interface ErrorCodes {
    public String getMsg();

    /**
     * Get the error code. ErrorCodes will never be null. Generic error code of 999 given
     * at a minimum.
     * 
     * @return ErrorCodes enum value for this error.
     */
    public int getCode();

    /**
     * Get the level of the error code. Wrapper for the enum for ErrorCodes.getLevel()
     * 
     * @return The error code level.
     * @see ErrorLevels
     */
    public ErrorLevels getLevel();

    /**
     * Default error to be thrown.
     */
    public static class Unknown implements ErrorCodes {
        /**
         * Unknow error message.
         */
        @Override
        public String getMsg() {
            return "Unknown Error";
        }

        /**
         * Return the default error code level.
         */
        @Override
        public int getCode() {
            return 999;
        }

        /**
         * Return the default error code level of WARN.
         */
        @Override
        public ErrorLevels getLevel() {
            return ErrorLevels.WARN;
        }

    }
}

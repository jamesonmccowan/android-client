package com.pedalportland.routetracker;

/**
 * This class extends the <code>RuntimeException<code/> class, and implements
 * a new exception for RouteTracker specific errors
 *
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>RuntimeException<code/> class.
 * created 2/12/14
 */

public class RouteTrackerException extends RuntimeException {

    public enum Error {
        ERR_OUT_OF_MEM(1001),
        ERR_BAD_ARG_ACCURACY(1002),
        ERR_BAD_ARG_BEARING(1003),
        ERR_SECURITY(1004),
        ERR_NO_PROVIDER(1005),
        ERR_WAKELOCK(1006),
        ERR_BAD_ARG_REMOVE_UPDATES(1007),
        ERR_BAD_REQUEST_UPDATES(1008);

        private final int value;

        Error(int value) {
            this.value = value;
        }

        public int value() { return value; }
    };

    private Error error;

    /**
     * Constructs a new {@code RouteTrackerException} that includes the current
     * stack trace.
     */
    public RouteTrackerException(Error error) {
        this.error = error;
    }

    /**
     * Constructs a new {@code RouteTrackerException} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public RouteTrackerException(Error error, String detailMessage) {
        super(detailMessage);
        this.error = error;
    }

    /**
     * Returns specific error which caused exception
     * stack trace.
     */
    public Error getError() {
        return error;
    }
}

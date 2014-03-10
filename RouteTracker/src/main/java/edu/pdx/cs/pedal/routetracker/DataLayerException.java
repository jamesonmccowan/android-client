package edu.pdx.cs.pedal.routetracker;

/**
 * This class extends the <code>RuntimeException<code/> class, and implements
 * a new exception for RouteTracker specific errors
 *
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>RuntimeException<code/> class.
 * created 3/1/14
 */

public class DataLayerException extends RuntimeException {

    /**
     * Constructs a new {@code DataLayerException} with the current stack
     * trace and the specified detail message.
     */
    public DataLayerException() {
        super();
    }

    /**
     * Constructs a new {@code DataLayerException} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public DataLayerException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code DataLayerException} with the current stack trace
     * and the specified cause.
     *
     * @param cause
     *            the optional cause of this exception, may be {@code null}.
     * @since 1.5
     */
    public DataLayerException(Throwable cause) {
        super(cause);
    }
}

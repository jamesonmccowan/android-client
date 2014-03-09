package edu.pdx.cs.pedal.routetracker;

/**
 * This class extends the <code>RuntimeException<code/> class, and
 * happens when the RouteTracker cannot allocate memory
 *
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>RuntimeException<code/> class.
 * created 2/14/14
 */

public class RouteTrackerExceptionWakeLock extends RuntimeException  {

    /**
     * Constructs a new {@code RouteTrackerExceptionWakeLock} with the current stack
     * trace and the specified detail message.
     */
    public RouteTrackerExceptionWakeLock() {
        super();
    }

    /**
     * Constructs a new {@code RouteTrackerExceptionWakeLock} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public RouteTrackerExceptionWakeLock(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code RouteTrackerExceptionWakeLock} with the current stack trace
     * and the specified cause.
     *
     * @param cause
     *            the optional cause of this exception, may be {@code null}.
     * @since 1.5
     */
    public RouteTrackerExceptionWakeLock(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}

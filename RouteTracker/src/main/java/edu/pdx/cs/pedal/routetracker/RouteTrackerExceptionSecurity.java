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

public class RouteTrackerExceptionSecurity extends RuntimeException  {

    /**
     * Constructs a new {@code RouteTrackerExceptionSecurity} with the current stack
     * trace and the specified detail message.
     */
    public RouteTrackerExceptionSecurity() {
        super();
    }

    /**
     * Constructs a new {@code RouteTrackerExceptionSecurity} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public RouteTrackerExceptionSecurity(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code RouteTrackerExceptionSecurity} with the current stack trace
     * and the specified cause.
     *
     * @param cause
     *            the optional cause of this exception, may be {@code null}.
     * @since 1.5
     */
    public RouteTrackerExceptionSecurity(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}

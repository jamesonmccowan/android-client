package com.pedalportland.routetracker;

/**
 * This class extends the <code>RuntimeException<code/> class, and
 * happens when the RouteTracker cannot allocate memory
 *
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>RuntimeException<code/> class.
 * created 2/14/14
 */

public class RouteTrackerExceptionNoProvider extends RuntimeException  {

    /**
     * Constructs a new {@code RouteTrackerExceptionNoProvider} with the current stack
     * trace and the specified detail message.
     */
    public RouteTrackerExceptionNoProvider() {
        super();
    }

    /**
     * Constructs a new {@code RouteTrackerExceptionNoProvider} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public RouteTrackerExceptionNoProvider(String detailMessage) {
        super(detailMessage);
    }
}

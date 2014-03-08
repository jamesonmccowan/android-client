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

public class UploadServiceException extends RuntimeException {

    /**
     * Constructs a new {@code UploadServiceException} with the current stack
     * trace and the specified detail message.
     */
    public UploadServiceException() {
        super();
    }

    /**
     * Constructs a new {@code UploadServiceException} with the current stack
     * trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public UploadServiceException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code UploadServiceException} with the current stack trace
     * and the specified cause.
     *
     * @param cause
     *            the optional cause of this exception, may be {@code null}.
     * @since 1.5
     */
    public UploadServiceException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}

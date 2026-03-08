package edu.cs102.g04t06.game.exception;

/**
 * base checked exception for rule violations caused by invalid player moves.
 */
public class InvalidMoveException extends RuntimeException {

    /**
     * creates an exception with no detail message.
     */
    public InvalidMoveException() {
    }

    /**
     * creates an exception with a detail message.
     * @param message the detail message
     */
    public InvalidMoveException(String message) {
        super(message);
    }

    /**
     * creates an exception with a detail message and cause.
     * @param message the detail message
     * @param cause the underlying cause of this exception
     */
    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * creates an exception with full control over suppression and stack trace behavior.
     * @param message the detail message
     * @param cause the underlying cause of this exception
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public InvalidMoveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * creates an exception with only a cause.
     * @param cause the underlying cause of this exception
     */
    public InvalidMoveException(Throwable cause) {
        super(cause);
    }

}

package edu.cs102.g04t06.game.exception;

import edu.cs102.g04t06.game.rules.entities.Noble;

/**
 * thrown when a requested noble cannot be selected from the current game state.
 */
public class NobleNotAvailableException extends Exception {

    /**
     * creates an exception with a default message.
     */
    public NobleNotAvailableException() {
        super("Selected noble is not available in the current game state.");
    }

    /**
     * creates an exception with context about the unavailable noble.
     * @param n the noble that is unavailable
     */
    public NobleNotAvailableException(Noble n) {
        super(buildUnavailableNobleMessage(n));
    }

    /**
     * creates an exception with context about the unavailable noble and a cause.
     * @param n the noble that is unavailable
     * @param cause the underlying cause of this exception
     */
    public NobleNotAvailableException(Noble n, Throwable cause) {
        super(buildUnavailableNobleMessage(n), cause);
    }

    /**
     * creates an exception with full control over suppression and stack trace behavior.
     * @param n the noble that is unavailable
     * @param cause the underlying cause of this exception
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public NobleNotAvailableException(Noble n, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(buildUnavailableNobleMessage(n), cause, enableSuppression, writableStackTrace);
    }

    /**
     * creates an exception with only a cause.
     * @param cause the underlying cause of this exception
     */
    public NobleNotAvailableException(Throwable cause) {
        super(cause);
    }

    /**
     * builds a message including the unavailable noble.
     * @param n the noble that is unavailable
     * @return a message describing the unavailable noble
     */
    private static String buildUnavailableNobleMessage(Noble n) {
        return n.toString() + "is not available in the current game state.";
    }
}

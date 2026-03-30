package edu.cs102.g04t06.game.execution;

/**
 * A simple value object to communicate the result of a game action.
 */
public class ActionResult {
    private final boolean success;
    private final String message;

    /**
     * Creates a result describing whether an action succeeded.
     *
     * @param success whether the action succeeded
     * @param message human-readable result message
     */
    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Returns whether the action succeeded.
     *
     * @return true when the action succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the user-facing outcome message.
     *
     * @return the action result message
     */
    public String getMessage() {
        return message;
    }
}

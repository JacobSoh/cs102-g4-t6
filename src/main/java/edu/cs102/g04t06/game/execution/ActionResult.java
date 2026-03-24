package edu.cs102.g04t06.game.execution;

/**
 * A simple value object to communicate the result of a game action.
 */
public class ActionResult {
    private final boolean success;
    private final String message;

    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
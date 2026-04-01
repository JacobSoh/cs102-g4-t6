package edu.cs102.g04t06.game.exception;

import edu.cs102.g04t06.game.rules.entities.GemColor;

/**
 * Thrown when a player attempts an action requiring more gems than they hold.
 */
public class InsufficientGemsException extends InvalidMoveException {

    /**
     * Creates the exception with a descriptive message showing the shortfall.
     *
     * @param color     the gem color that is insufficient
     * @param required  the number of gems needed
     * @param available the number of gems the player actually has
     */
    public InsufficientGemsException(GemColor color, int required, int available) {
        super("Need " + required + " " + color + " gem(s) but only have " + available);
    }
}

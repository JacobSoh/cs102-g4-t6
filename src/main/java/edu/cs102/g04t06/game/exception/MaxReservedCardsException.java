package edu.cs102.g04t06.game.exception;

/**
 * Thrown when a player attempts to reserve a card but already holds the maximum of 3.
 */
public class MaxReservedCardsException extends InvalidMoveException {

    public MaxReservedCardsException() {
        super("Cannot reserve more than 3 cards");
    }
}

package edu.cs102.g04t06.game.rules.entities;

/**
 * Action categories that a player can take on a turn.
 */
public enum ActionType {
    /** Take three gems of different colors. */
    TAKE_THREE_DIFFERENT,
    /** Take two gems of the same color (when allowed). */
    TAKE_TWO_SAME,
    /** Purchase a development card. */
    PURCHASE_CARD,
    /** Reserve a development card. */
    RESERVE_CARD
}

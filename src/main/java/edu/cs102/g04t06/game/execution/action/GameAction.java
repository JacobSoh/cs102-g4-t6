package edu.cs102.g04t06.game.execution.action;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.rules.GameState;

/**
 * Command pattern interface for game actions.
 * Each concrete action encapsulates all parameters and logic for one player move.
 *
 * <p>Usage:
 * <pre>
 *     GameAction action = new PurchaseCardAction(card, fromReserved);
 *     ActionResult result = action.execute(state);
 * </pre>
 */
public interface GameAction {
    /**
     * Executes this action against the given game state.
     *
     * @param state the active game state to mutate
     * @return the outcome of the action
     */
    ActionResult execute(GameState state);
}

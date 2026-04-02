package edu.cs102.g04t06.game.execution;

import edu.cs102.g04t06.game.execution.action.ClaimNobleAction;
import edu.cs102.g04t06.game.execution.action.GameAction;
import edu.cs102.g04t06.game.execution.action.PurchaseCardAction;
import edu.cs102.g04t06.game.execution.action.ReserveCardAction;
import edu.cs102.g04t06.game.execution.action.ReserveTopCardAction;
import edu.cs102.g04t06.game.execution.action.ReturnGemsAction;
import edu.cs102.g04t06.game.execution.action.TakeThreeDifferentGemsAction;
import edu.cs102.g04t06.game.execution.action.TakeTwoSameGemsAction;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Facade that executes validated game actions by delegating to
 * {@link GameAction} command objects.
 *
 * <p>Static helper methods are preserved for backward compatibility with
 * existing callers. Each method now constructs the appropriate
 * {@link GameAction} and calls {@code execute(state)}.
 */
public class ActionExecutor {

    /**
     * Executes the action of taking two gems of the same color.
     *
     * @param state the active game state
     * @param color the color to take twice
     * @return the outcome of the action
     */
    public static ActionResult executeTakeTwoSameGems(GameState state, GemColor color) {
        return new TakeTwoSameGemsAction(color).execute(state);
    }

    /**
     * Executes the action of taking three different gems.
     *
     * @param state     the active game state
     * @param selection the gems being requested
     * @return the outcome of the action
     */
    public static ActionResult executeTakeThreeDifferentGems(GameState state, GemCollection selection) {
        return new TakeThreeDifferentGemsAction(selection).execute(state);
    }

    /**
     * Executes a gem return action after a player exceeds the hand limit.
     *
     * @param state    the active game state
     * @param toReturn the gems to return to the bank
     * @return the outcome of the action
     */
    public static ActionResult executeReturnGems(GameState state, GemCollection toReturn) {
        return new ReturnGemsAction(toReturn).execute(state);
    }

    /**
     * Executes the purchase of a visible or reserved card.
     *
     * @param state        the active game state
     * @param card         the card being purchased
     * @param fromReserved whether the card comes from the player's reserved hand
     * @return the outcome of the action
     */
    public static ActionResult executePurchaseCard(GameState state, Card card, boolean fromReserved) {
        return new PurchaseCardAction(card, fromReserved).execute(state);
    }

    /**
     * Reserves a visible market card for the current player.
     *
     * @param state the active game state
     * @param card  the card to reserve
     * @return the outcome of the action
     */
    public static ActionResult executeReserveCard(GameState state, Card card) {
        return new ReserveCardAction(card).execute(state);
    }

    /**
     * Reserves the top card from the requested deck tier.
     *
     * @param state the active game state
     * @param tier  the deck tier to reserve from
     * @return the outcome of the action
     */
    public static ActionResult executeReserveTopCard(GameState state, int tier) {
        return new ReserveTopCardAction(tier).execute(state);
    }

    /**
     * Claims a noble for the current player when its requirements are satisfied.
     *
     * @param state the active game state
     * @param noble the noble to claim
     * @return the outcome of the action
     */
    public static ActionResult executeClaimNoble(GameState state, Noble noble) {
        return new ClaimNobleAction(noble).execute(state);
    }
}

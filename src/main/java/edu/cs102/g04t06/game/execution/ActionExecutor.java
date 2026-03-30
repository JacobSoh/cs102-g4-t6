package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Executes validated game actions by mutating the shared {@link GameState}.
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
        // 1. Validate the rule (Bank must have >= 4)
        GameRules rules = new GameRules();
        if (!rules.canTakeTwoSameGems(color, state.getGemBank())) {
            return new ActionResult(false, "Illegal move: Not enough gems in bank to take two.");
        }

        // 2. Perform the execution
        Player currentPlayer = state.getCurrentPlayer();
        
        // Create a temporary collection of just the 2 gems being taken
        GemCollection toTake = new GemCollection().add(color, 2);

        // Remove from bank and add to player
        state.removeGemsFromBank(toTake);
        currentPlayer.addGems(toTake);

        // 3. Return results (Checking the 10-gem limit)
        if (currentPlayer.getGemCount() > 10) {
            return new ActionResult(true, "Gems taken, but you now exceed 10. Must return excess!");
        }

        return new ActionResult(true, "Successfully took 2 " + color.name() + " gems.");
    }

    /**
     * Executes the action of taking three different gems.
     *
     * @param state the active game state
     * @param selection the gems being requested
     * @return the outcome of the action
     */
    public static ActionResult executeTakeThreeDifferentGems(GameState state, GemCollection selection) {
        GameRules rules = new GameRules();
        if (!rules.canTakeThreeDifferentGems(selection, state.getGemBank())) {
            return new ActionResult(false, "Illegal move: The bank does not have the requested gems.");
        }

        Player currentPlayer = state.getCurrentPlayer();
        state.removeGemsFromBank(selection);
        currentPlayer.addGems(selection);

        if (currentPlayer.getGemCount() > 10) {
            return new ActionResult(true, "Gems taken, but you must now return excess to stay at 10!");
        }

        List<String> colorNames = new ArrayList<>();
        for (Map.Entry<GemColor, Integer> e : selection.asMap().entrySet()) {
            if (e.getValue() > 0) {
                colorNames.add(e.getKey().name());
            }
        }
        return new ActionResult(true, "Successfully took " + String.join(", ", colorNames) + ".");
    }

    /**
     * Executes a gem return action after a player exceeds the hand limit.
     *
     * @param state the active game state
     * @param toReturn the gems to return to the bank
     * @return the outcome of the action
     */
    public static ActionResult executeReturnGems(GameState state, GemCollection toReturn) {
        Player currentPlayer = state.getCurrentPlayer();

        if (!currentPlayer.getGems().contains(toReturn)) {
            return new ActionResult(false, "Error: You cannot return gems you do not own.");
        }

        currentPlayer.deductGems(toReturn);
        state.addGemsToBank(toReturn);

        if (currentPlayer.getGemCount() > 10) {
            return new ActionResult(false, "Invalid return: You still have " + currentPlayer.getGemCount() + " gems. Must be 10 or fewer.");
        }

        return new ActionResult(true, "Successfully returned excess gems.");
    }

    /**
     * Executes the purchase of a visible or reserved card.
     *
     * @param state the active game state
     * @param card the card being purchased
     * @param fromReserved whether the card comes from the player's reserved hand
     * @return the outcome of the action
     */
    public static ActionResult executePurchaseCard(GameState state, Card card, boolean fromReserved) {
        Player player = state.getCurrentPlayer();

        // 1. Validation via Rules
        GameRules rules = new GameRules();
        if (!rules.canAffordCard(player, card)) {
            return new ActionResult(false, "Illegal move: You cannot afford this card.");
        }

        // 2. Calculate actual cost (Now returning a Cost object)
        Map<GemColor, Integer> bonuses = player.calculateBonuses();
        Cost baseCost = card.getCost().afterBonuses(bonuses);
        
        // 3. Handle Gold Wildcards
        GemCollection finalPayment = new GemCollection();
        int goldNeeded = 0;

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            
            // USE THEIR NEW METHOD: getRequired() instead of getCount()
            int required = baseCost.getRequired(color); 
            int playerHas = player.getGems().getCount(color);
            
            if (playerHas >= required) {
                finalPayment = finalPayment.add(color, required);
            } else {
                // Player is short; they use Gold
                finalPayment = finalPayment.add(color, playerHas);
                goldNeeded += (required - playerHas);
            }
        }
        finalPayment = finalPayment.add(GemColor.GOLD, goldNeeded);

        // 4. Update Game State
        player.deductGems(finalPayment);
        state.addGemsToBank(finalPayment);
        
        if (fromReserved) {
            player.removeReservedCard(card);
        } else {
            state.getMarket().removeCard(card);
        }
        player.addCard(card);

        return new ActionResult(true, "Successfully purchased " + card.getBonus() + " card.");
    }

    /**
     * Reserves a visible market card for the current player.
     *
     * @param state the active game state
     * @param card the card to reserve
     * @return the outcome of the action
     */
    public static ActionResult executeReserveCard(GameState state, Card card) {
        Player player = state.getCurrentPlayer();

        // 1. Validation: Max 3 reserved cards
        if (player.getReservedCards().size() >= 3) {
            return new ActionResult(false, "You already have 3 reserved cards.");
        }

        // 2. Movement: Take from market, put in player's reserve

        state.getMarket().removeCard(card);
        player.addReservedCard(card);

        // 3. The Gold Reward
        if (state.getGemBank().getCount(GemColor.GOLD) > 0) {
            GemCollection oneGold = new GemCollection().add(GemColor.GOLD, 1);
            state.removeGemsFromBank(oneGold);
            player.addGems(oneGold);
            
            // Check 10-gem limit
            if (player.getGemCount() > 10) {
                return new ActionResult(true, "Card reserved. You got a Gold gem but must return some to stay at 10!");
            }
        }

        return new ActionResult(true, "Card reserved successfully.");
    }

    /**
     * Reserves the top card from the requested deck tier.
     *
     * @param state the active game state
     * @param tier the deck tier to reserve from
     * @return the outcome of the action
     */
    public static ActionResult executeReserveTopCard(GameState state, int tier) {
        Player player = state.getCurrentPlayer();

        if (player.getReservedCards().size() >= 3) {
            return new ActionResult(false, "You already have 3 reserved cards.");
        }

        if (state.getMarket().getDeckSize(tier) <= 0) {
            return new ActionResult(false, "That deck is empty.");
        }

        Card card = state.getMarket().drawCard(tier);
        player.addReservedCard(card);

        if (state.getGemBank().getCount(GemColor.GOLD) > 0) {
            GemCollection oneGold = new GemCollection().add(GemColor.GOLD, 1);
            state.removeGemsFromBank(oneGold);
            player.addGems(oneGold);

            if (player.getGemCount() > 10) {
                return new ActionResult(true, "Top card reserved. You got a Gold gem but must return some to stay at 10!");
            }
        }

        return new ActionResult(true, "Top card reserved successfully.");
    }

    /**
     * Claims a noble for the current player when its requirements are satisfied.
     *
     * @param state the active game state
     * @param noble the noble to claim
     * @return the outcome of the action
     */
    public static ActionResult executeClaimNoble(GameState state, Noble noble) {
        Player player = state.getCurrentPlayer();

        GameRules rules = new GameRules();
        List<Noble> claimable = rules.getClaimableNobles(player, state.getAvailableNobles());
        
        if (!claimable.contains(noble)) {
            return new ActionResult(false, "You do not meet the bonus requirements for this Noble.");
        }

        // 2. Movement
        state.removeNoble(noble);
        player.claimNoble(noble);

        return new ActionResult(true, "Noble " + noble.getName() + " has visited your court!");
    }
}

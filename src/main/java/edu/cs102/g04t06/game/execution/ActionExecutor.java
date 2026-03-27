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


public class ActionExecutor {

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

    public static ActionResult executeTakeThreeDifferentGems(GameState state, GemCollection selection) {
    // 1. Ask GameRules if the bank has these 3 gems
    GameRules rules = new GameRules();
    if (!rules.canTakeThreeDifferentGems(selection, state.getGemBank())) {
        return new ActionResult(false, "Illegal move: The bank does not have the requested gems.");
    }

    // 2. Perform the movement
    Player currentPlayer = state.getCurrentPlayer();
    state.removeGemsFromBank(selection);
    currentPlayer.addGems(selection);

    // 3. Check the 10-gem limit (Standard check for all gem actions)
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

public static ActionResult executeReturnGems(GameState state, GemCollection toReturn) {
    Player currentPlayer = state.getCurrentPlayer();

    // 1. Safety Check: Does the player actually have these gems?
    if (!currentPlayer.getGems().contains(toReturn)) {
        return new ActionResult(false, "Error: You cannot return gems you do not own.");
    }

    // 2. Move gems from Player back to Bank
    currentPlayer.deductGems(toReturn);
    state.addGemsToBank(toReturn);

    // 3. Final Verification: Did they return enough?
    if (currentPlayer.getGemCount() > 10) {
        return new ActionResult(false, "Invalid return: You still have " + currentPlayer.getGemCount() + " gems. Must be 10 or fewer.");
    }

    return new ActionResult(true, "Successfully returned excess gems.");
}

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
    public static ActionResult executeClaimNoble(GameState state, Noble noble) {
        Player player = state.getCurrentPlayer();

        // 1. Check if the player is actually eligible for this Noble
        // We use the GameRules stub we created earlier
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

package edu.cs102.g04t06.game.execution;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;


public class ActionExecutor {

    public static ActionResult executeTakeTwoSameGems(GameState state, GemColor color) {
        // 1. Validate the rule (Bank must have >= 4)
        if (!GameRules.canTakeTwoSameGems(color, state.getGemBank())) {
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

        return new ActionResult(true, "Successfully took 2 " + color + " gems.");
    }

    public static ActionResult executeTakeThreeDifferentGems(GameState state, GemCollection selection) {
    // 1. Ask GameRules if the bank has these 3 gems
    if (!GameRules.canTakeThreeDifferentGems(selection, state.getGemBank())) {
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

    return new ActionResult(true, "Successfully took three different gems.");
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
}
package edu.cs102.g04t06.game.execution.action;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.execution.GameAction;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Command: return excess gems to the bank after exceeding the 10-gem limit.
 */
public class ReturnGemsAction implements GameAction {

    private final GemCollection toReturn;

    public ReturnGemsAction(GemCollection toReturn) {
        this.toReturn = toReturn;
    }

    @Override
    public ActionResult execute(GameState state) {
        Player currentPlayer = state.getCurrentPlayer();

        if (!currentPlayer.getGems().contains(toReturn)) {
            return new ActionResult(false, "Error: You cannot return gems you do not own.");
        }

        currentPlayer.deductGems(toReturn);
        state.addGemsToBank(toReturn);

        if (currentPlayer.getGemCount() > 10) {
            return new ActionResult(false, "Invalid return: You still have "
                    + currentPlayer.getGemCount() + " gems. Must be 10 or fewer.");
        }
        return new ActionResult(true, "Successfully returned excess gems.");
    }
}

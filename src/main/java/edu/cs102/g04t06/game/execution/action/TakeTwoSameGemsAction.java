package edu.cs102.g04t06.game.execution.action;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.execution.GameAction;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Command: take two gems of the same color from the bank.
 * Bank must have at least 4 of that color.
 */
public class TakeTwoSameGemsAction implements GameAction {

    private final GemColor color;

    public TakeTwoSameGemsAction(GemColor color) {
        this.color = color;
    }

    @Override
    public ActionResult execute(GameState state) {
        GameRules rules = new GameRules();
        if (!rules.canTakeTwoSameGems(color, state.getGemBank())) {
            return new ActionResult(false, "Illegal move: Not enough gems in bank to take two.");
        }

        Player currentPlayer = state.getCurrentPlayer();
        GemCollection toTake = new GemCollection().add(color, 2);
        state.removeGemsFromBank(toTake);
        currentPlayer.addGems(toTake);

        if (currentPlayer.getGemCount() > 10) {
            return new ActionResult(true, "Gems taken, but you now exceed 10. Must return excess!");
        }
        return new ActionResult(true, "Successfully took 2 " + color.name() + " gems.");
    }
}

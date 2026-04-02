package edu.cs102.g04t06.game.execution.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Command: take one gem each of three different colors from the bank.
 */
public class TakeThreeDifferentGemsAction implements GameAction {

    private final GemCollection selection;

    public TakeThreeDifferentGemsAction(GemCollection selection) {
        this.selection = selection;
    }

    @Override
    public ActionResult execute(GameState state) {
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
}

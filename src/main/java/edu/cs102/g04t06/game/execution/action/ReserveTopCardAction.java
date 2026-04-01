package edu.cs102.g04t06.game.execution.action;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.execution.GameAction;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Command: reserve the top (hidden) card from a deck tier and receive one gold gem if available.
 */
public class ReserveTopCardAction implements GameAction {

    private final int tier;

    public ReserveTopCardAction(int tier) {
        this.tier = tier;
    }

    @Override
    public ActionResult execute(GameState state) {
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
}

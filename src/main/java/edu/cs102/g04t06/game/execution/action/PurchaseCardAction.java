package edu.cs102.g04t06.game.execution.action;

import java.util.Map;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Command: purchase a visible market card or a card from the player's reserve.
 */
public class PurchaseCardAction implements GameAction {

    private final Card card;
    private final boolean fromReserved;

    public PurchaseCardAction(Card card, boolean fromReserved) {
        this.card = card;
        this.fromReserved = fromReserved;
    }

    @Override
    public ActionResult execute(GameState state) {
        Player player = state.getCurrentPlayer();

        GameRules rules = new GameRules();
        if (!rules.canAffordCard(player, card)) {
            return new ActionResult(false, "Illegal move: You cannot afford this card.");
        }

        Map<GemColor, Integer> bonuses = player.calculateBonuses();
        Cost baseCost = card.getCost().afterBonuses(bonuses);

        GemCollection finalPayment = new GemCollection();
        int goldNeeded = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            int required = baseCost.getRequired(color);
            int playerHas = player.getGems().getCount(color);
            if (playerHas >= required) {
                finalPayment = finalPayment.add(color, required);
            } else {
                finalPayment = finalPayment.add(color, playerHas);
                goldNeeded += (required - playerHas);
            }
        }
        finalPayment = finalPayment.add(GemColor.GOLD, goldNeeded);

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
}

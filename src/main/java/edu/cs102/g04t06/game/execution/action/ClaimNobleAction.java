package edu.cs102.g04t06.game.execution.action;

import java.util.List;

import edu.cs102.g04t06.game.execution.ActionResult;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * Command: claim a noble whose bonus requirements the current player satisfies.
 */
public class ClaimNobleAction implements GameAction {

    private final Noble noble;

    public ClaimNobleAction(Noble noble) {
        this.noble = noble;
    }

    @Override
    public ActionResult execute(GameState state) {
        Player player = state.getCurrentPlayer();

        GameRules rules = new GameRules();
        List<Noble> claimable = rules.getClaimableNobles(player, state.getAvailableNobles());

        if (!claimable.contains(noble)) {
            return new ActionResult(false, "You do not meet the bonus requirements for this Noble.");
        }

        state.removeNoble(noble);
        player.claimNoble(noble);

        return new ActionResult(true, "Noble " + noble.getName() + " has visited your court!");
    }
}

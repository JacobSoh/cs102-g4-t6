package edu.cs102.g04t06.game.execution.ai;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

import java.util.List;

/**
 * Strategy interface for AI decision-making.
 * Implemented by EasyAIStrategy and HardAIStrategy.
 * AIPlayer delegates all decisions to the active strategy,
 * allowing difficulty to be swapped mid-game via AIPlayer.setStrategy().
 */
public interface AIStrategy {

    /**
     * Decides the main action for this turn.
     *
     * @param state the current game state
     * @param self  the AI's own player
     * @return an AIAction describing what the AI chose to do
     */
    AIAction decideAction(GameState state, Player self);

    /**
     * Chooses which noble to claim when multiple are available.
     * Called by GameBoardUI after executing the AI's main action,
     * when rules.getClaimableNobles() returns more than one result.
     *
     * @param claimable the nobles the AI is eligible to claim
     * @param state     the current game state
     * @param self      the AI's own player
     * @return the noble the AI chooses to claim
     */
    Noble chooseNoble(List<Noble> claimable, GameState state, Player self);

    /**
     * Chooses which gems to return when the AI exceeds 10 gems.
     * Called by GameBoardUI when GameRules.mustReturnGems(player) is true.
     *
     * @param self        the AI's own player
     * @param excessCount the number of gems that must be returned
     * @param state       the current game state
     * @return a GemCollection of exactly excessCount gems to return to the bank
     */
    GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state);
}

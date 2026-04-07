package edu.cs102.g04t06.game.execution.ai;

import java.util.List;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Strategy-backed wrapper around a {@link Player} for AI-controlled turns.
 */
public class AIPlayer{

    private final Player player;
    private AIStrategy strategy;

    /**
     * Wraps a Player entity and delegates all AI decisions to the chosen strategy.
     * GameBoardUI interacts with this class
     */
    public AIPlayer(Player player, AIStrategy strategy){
        this.player = player;
        this.strategy = strategy;
    }

    /**
     * decides the main turn action by delegating to the current strategy
     * 
     * @param state the current game state
     * @return AIAction containing the chosen action type, target card or gem selection
     */
    public AIAction decideAction(GameState state){
        return strategy.decideAction(state, player);
    }

    /**
     * Chooses which noble to claim when multiple nobles become claimable.
     * Called by GameBoardUI after executing the AI's main action.
     *
     * @param claimable list of nobles the player is eligible to claim
     * @param state current game state
     * @return the chosen Noble
     */
    public Noble chooseNoble(List<Noble> claimable, GameState state) {
        return strategy.chooseNoble(claimable, state, player);
    }

    /**
     * Chooses which gems to return when the player exceeds 10 gems.
     * Called by GameBoardUI when GameRules.mustReturnGems(player) is true.
     *
     * @param excessCount number of gems that must be returned
     * @param state current game state
     * @return GemCollection of exactly excessCount gems to return to the bank
     */
    public GemCollection chooseGemsToReturn(int excessCount, GameState state) {
        return strategy.chooseGemsToReturn(player, excessCount, state);
    }

    /**
     * Returns the underlying Player entity for identity checks.
     *
     * @return the wrapped Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Swaps the strategy mid-game.
     *
     * @param strategy the new strategy to use
     */
    @Deprecated
    public void setStrategy(AIStrategy strategy) {
        this.strategy = strategy;
    }
}

package edu.cs102.g04t06.game.execution;

// Edited by GPT-5 (Codex)

import edu.cs102.g04t06.game.rules.GameState;

/**
 * Compatibility wrapper that forwards turn orchestration to {@link GameEngine}.
 */
public class TurnProcessor {
    private final GameEngine gameEngine = new GameEngine();

    /**
     * Processes a turn command such as taking gems, buying a card, or reserving a card.
     *
     * @param state the active game state
     * @param input the raw player command
     * @return the result of processing the command
     */
    public TurnResult processCommand(GameState state, String input) {
        return gameEngine.processPlayerCommand(state, input);
    }

    /**
     * Processes a follow-up gem return command after a player exceeds the hand limit.
     *
     * @param state the active game state
     * @param input the raw gem return input
     * @return the result of processing the return
     */
    public TurnResult processReturnGems(GameState state, String input) {
        return gameEngine.processGemReturn(state, input);
    }

    /**
     * Resolves a disconnected player's turn by automatically passing.
     *
     * @param state the active game state
     * @return the result of the automatic pass
     */
    public TurnResult processAutomaticPass(GameState state) {
        return gameEngine.processAutomaticPass(state);
    }

    /**
     * Resolves an over-limit gem situation automatically for a disconnected player.
     *
     * @param state the active game state
     * @return the result of the automatic gem return
     */
    public TurnResult processAutomaticReturnGems(GameState state) {
        return gameEngine.processAutomaticReturnGems(state);
    }

    /**
     * Result of a processed turn step.
     */
    public static final class TurnResult {
        private final boolean success;
        private final boolean awaitingReturn;
        private final int excessCount;
        private final String message;

        private TurnResult(boolean success, boolean awaitingReturn, int excessCount, String message) {
            this.success = success;
            this.awaitingReturn = awaitingReturn;
            this.excessCount = excessCount;
            this.message = message;
        }

        /**
         * Creates a successful result that completes the turn.
         *
         * @param message the message to expose to the caller
         * @return a successful turn result
         */
        public static TurnResult success(String message) {
            return new TurnResult(true, false, 0, message);
        }

        /**
         * Creates a successful result that still requires gem returns.
         *
         * @param message the message to expose to the caller
         * @param excessCount the number of gems that must be returned
         * @return a turn result awaiting gem return
         */
        public static TurnResult awaitingReturn(String message, int excessCount) {
            return new TurnResult(true, true, excessCount, message);
        }

        /**
         * Creates a failed result.
         *
         * @param message the failure message
         * @return a failed turn result
         */
        public static TurnResult failure(String message) {
            return new TurnResult(false, false, 0, message);
        }

        /**
         * Returns whether the turn step succeeded.
         *
         * @return true when the operation succeeded
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns whether gem return input is still required.
         *
         * @return true when the caller must return gems
         */
        public boolean isAwaitingReturn() {
            return awaitingReturn;
        }

        /**
         * Returns how many gems must be returned.
         *
         * @return the excess gem count
         */
        public int getExcessCount() {
            return excessCount;
        }

        /**
         * Returns the user-facing outcome message.
         *
         * @return the turn result message
         */
        public String getMessage() {
            return message;
        }
    }
}

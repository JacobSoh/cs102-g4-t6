package edu.cs102.g04t06.game.execution;

import java.util.List;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Shared turn processor for local and LAN game flows.
 */
public class TurnProcessor {
    private final InputHandler inputHandler = new InputHandler();
    private final GameRules rules = new GameRules();

    public TurnResult processCommand(GameState state, String input) {
        if (state == null) {
            return TurnResult.failure("Game state is unavailable.");
        }
        if (input == null || input.isBlank()) {
            return TurnResult.failure("Command cannot be empty.");
        }

        String s = input.toLowerCase().trim();
        if (s.equals("q")) {
            return TurnResult.failure("Quitting is not supported during a turn.");
        }
        if (s.equals("?")) {
            return TurnResult.failure("Help is handled by the local UI. Use take, buy, reserve, or pass.");
        }

        ActionResult actionResult;
        if (s.startsWith("take")) {
            actionResult = handleTake(state, s);
        } else if (s.startsWith("buy")) {
            actionResult = handleBuy(state, s);
        } else if (s.startsWith("reserve")) {
            actionResult = handleReserve(state, s);
        } else if (s.equals("pass")) {
            actionResult = new ActionResult(true, "Turn passed.");
        } else {
            return TurnResult.failure("Unknown command. Try: take, buy, reserve, pass");
        }

        if (!actionResult.isSuccess()) {
            return TurnResult.failure(actionResult.getMessage());
        }

        Player player = state.getCurrentPlayer();
        int excess = Math.max(0, player.getGemCount() - 10);
        if (excess > 0) {
            return TurnResult.awaitingReturn(actionResult.getMessage(), excess);
        }

        return finalizeTurn(state, player, actionResult.getMessage());
    }

    public TurnResult processReturnGems(GameState state, String input) {
        if (state == null) {
            return TurnResult.failure("Game state is unavailable.");
        }

        Player player = state.getCurrentPlayer();
        int excess = Math.max(0, player.getGemCount() - 10);
        if (excess <= 0) {
            return TurnResult.failure("No gems need to be returned.");
        }

        try {
            List<GemColor> colors = inputHandler.parseGemSequence(input);
            GemCollection toReturn = inputHandler.promptGemsToReturn(player, excess, colors);
            ActionResult result = ActionExecutor.executeReturnGems(state, toReturn);
            if (!result.isSuccess()) {
                return TurnResult.failure(result.getMessage());
            }
            return finalizeTurn(state, player, result.getMessage());
        } catch (IllegalArgumentException e) {
            return TurnResult.failure(e.getMessage());
        }
    }

    private ActionResult handleTake(GameState state, String s) {
        String payloadRaw = s.replaceFirst("^take", "").trim();
        if (payloadRaw.isEmpty()) {
            return new ActionResult(false, "Usage: take <gem> [gem] [gem]");
        }

        List<GemColor> colors;
        try {
            colors = inputHandler.parseGemSequence(payloadRaw);
        } catch (IllegalArgumentException e) {
            return new ActionResult(false, e.getMessage());
        }

        if (colors.size() == 3) {
            try {
                GemCollection selection = inputHandler.promptGemSelection(3, colors);
                return ActionExecutor.executeTakeThreeDifferentGems(state, selection);
            } catch (IllegalArgumentException e) {
                return new ActionResult(false, e.getMessage());
            }
        }

        if (colors.size() == 2 && colors.get(0) == colors.get(1)) {
            return ActionExecutor.executeTakeTwoSameGems(state, colors.get(0));
        }

        return new ActionResult(false, "Invalid take: choose 3 different or 2 of the same color.");
    }

    private ActionResult handleBuy(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            return new ActionResult(false, "Usage: buy <tier> <slot> or buy reserve <slot>");
        }

        Player player = state.getCurrentPlayer();
        boolean fromReserved;
        Card card;

        try {
            if (isReservedBuyToken(p[1])) {
                int reservedSlotIndex = inputHandler.parseSlotToken(p[2]);
                if (reservedSlotIndex >= player.getReservedCards().size()) {
                    return new ActionResult(false, "Reserved card does not exist at that slot.");
                }
                card = player.getReservedCards().get(reservedSlotIndex);
                fromReserved = true;
            } else {
                int tier = inputHandler.parseTierToken(p[1]);
                int slotIndex = inputHandler.parseSlotToken(p[2]);
                card = state.getMarket().getVisibleCard(tier, slotIndex);
                fromReserved = false;
            }
        } catch (IllegalArgumentException e) {
            return new ActionResult(false, e.getMessage());
        }

        return ActionExecutor.executePurchaseCard(state, card, fromReserved);
    }

    private ActionResult handleReserve(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            return new ActionResult(false, "Usage: reserve <tier> <slot> or reserve deck <tier>");
        }

        try {
            if (isDeckReserveToken(p[1])) {
                int tier = inputHandler.parseTierToken(p[2]);
                return ActionExecutor.executeReserveTopCard(state, tier);
            }
            int tier = inputHandler.parseTierToken(p[1]);
            int slotIndex = inputHandler.parseSlotToken(p[2]);
            Card card = state.getMarket().getVisibleCard(tier, slotIndex);
            return ActionExecutor.executeReserveCard(state, card);
        } catch (IllegalArgumentException e) {
            return new ActionResult(false, e.getMessage());
        }
    }

    private boolean isReservedBuyToken(String token) {
        return "reserved".equalsIgnoreCase(token)
                || "reserve".equalsIgnoreCase(token)
                || "r".equalsIgnoreCase(token);
    }

    private boolean isDeckReserveToken(String token) {
        return "deck".equalsIgnoreCase(token)
                || "top".equalsIgnoreCase(token)
                || "hidden".equalsIgnoreCase(token);
    }

    private TurnResult finalizeTurn(GameState state, Player player, String baseMessage) {
        StringBuilder message = new StringBuilder(baseMessage);

        List<Noble> claimable = rules.getClaimableNobles(player, state.getAvailableNobles());
        if (!claimable.isEmpty()) {
            Noble noble = claimable.get(0);
            ActionResult nobleResult = ActionExecutor.executeClaimNoble(state, noble);
            if (nobleResult.isSuccess()) {
                message.append(" ").append(nobleResult.getMessage());
            }
        }

        if (!state.isFinalRoundTriggered() && rules.hasPlayerWon(player, state.getWinningThreshold())) {
            state.triggerFinalRound();
            message.append(" Final round triggered.");
        }

        state.advanceToNextPlayer();

        if (state.isGameOver()) {
            Player winner = rules.getWinner(state.getPlayers(), state.getWinningThreshold());
            if (winner != null) {
                message.append(" Game over. Winner: ")
                        .append(winner.getName())
                        .append(" with ")
                        .append(winner.getPoints())
                        .append(" points.");
            } else {
                message.append(" Game over. No winner: final scores remain tied after tiebreaks.");
            }
        }

        return TurnResult.success(message.toString());
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

        public static TurnResult success(String message) {
            return new TurnResult(true, false, 0, message);
        }

        public static TurnResult awaitingReturn(String message, int excessCount) {
            return new TurnResult(true, true, excessCount, message);
        }

        public static TurnResult failure(String message) {
            return new TurnResult(false, false, 0, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isAwaitingReturn() {
            return awaitingReturn;
        }

        public int getExcessCount() {
            return excessCount;
        }

        public String getMessage() {
            return message;
        }
    }
}

package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.execution.ai.AIAction;
import edu.cs102.g04t06.game.execution.ai.AIPlayer;
import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.ActionType;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Creates and advances core game state for a Splendor match.
 */
public class GameEngine {

    private final GameRules gameRules;
    private final InputHandler inputHandler;

    public GameEngine() {
        this.gameRules = new GameRules();
        this.inputHandler = new InputHandler();
    }

    /**
     * Initializes a new game state with shuffled decks, selected nobles,
     * and a gem bank sized for the number of players.
     *
     * @param playerCount  number of players (2-4)
     * @param playerNames  list of player names in turn order
     * @param config       config loader for gem counts
     * @param level1       full list of level 1 cards
     * @param level2       full list of level 2 cards
     * @param level3       full list of level 3 cards
     * @param allNobles    full list of all nobles
     * @return a fully initialized GameState ready to play
     */
    public GameState initializeGame(
        int playerCount,
        List<String> playerNames,
        ConfigLoader config,
        List<Card> level1,
        List<Card> level2,
        List<Card> level3,
        List<Noble> allNobles
    ) {
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player(playerNames.get(i), i + 1));
        }

        List<Card> shuffledLevel1 = new ArrayList<>(level1);
        List<Card> shuffledLevel2 = new ArrayList<>(level2);
        List<Card> shuffledLevel3 = new ArrayList<>(level3);
        Collections.shuffle(shuffledLevel1);
        Collections.shuffle(shuffledLevel2);
        Collections.shuffle(shuffledLevel3);

        List<Noble> selectedNobles = new ArrayList<>(allNobles);
        Collections.shuffle(selectedNobles);
        List<Noble> gameNobles = selectedNobles.subList(0, playerCount + 1);

        int regularGemCount = getRegularGemCount(playerCount);
        Map<GemColor, Integer> bankMap = new EnumMap<>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            bankMap.put(color, color == GemColor.GOLD ? 5 : regularGemCount);
        }
        GemCollection initialGems = new GemCollection(bankMap);

        CardMarket market = new CardMarket(shuffledLevel1, shuffledLevel2, shuffledLevel3);

        return new GameState(
            players,
            market,
            initialGems,
            new ArrayList<>(gameNobles),
            config.getWinningPoints()
        );
    }

    /**
     * Checks the win condition against the configured threshold.
     *
     * @param state current game state
     * @param winningPoints points needed to win
     * @return the winner, or null when none exists
     */
    public Player checkWinCondition(GameState state, int winningPoints) {
        return gameRules.getWinner(state.getPlayers(), winningPoints);
    }

    /**
     * Processes a player-entered command and resolves the resulting turn state.
     *
     * @param state the active game state
     * @param input the raw player command
     * @return the turn outcome
     */
    public TurnProcessor.TurnResult processPlayerCommand(GameState state, String input) {
        if (state == null) {
            return TurnProcessor.TurnResult.failure("Game state is unavailable.");
        }
        if (input == null || input.isBlank()) {
            return TurnProcessor.TurnResult.failure("Command cannot be empty.");
        }

        String s = input.toLowerCase().trim();
        if (s.equals("q")) {
            return TurnProcessor.TurnResult.failure("Quitting is not supported during a turn.");
        }
        if (s.equals("?")) {
            return TurnProcessor.TurnResult.failure("Help is handled by the local UI. Use take, buy, reserve, or pass.");
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
            return TurnProcessor.TurnResult.failure("Unknown command. Try: take, buy, reserve, pass");
        }

        if (!actionResult.isSuccess()) {
            return TurnProcessor.TurnResult.failure(actionResult.getMessage());
        }

        Player player = state.getCurrentPlayer();
        int excess = Math.max(0, player.getGemCount() - 10);
        if (excess > 0) {
            return TurnProcessor.TurnResult.awaitingReturn(actionResult.getMessage(), excess);
        }

        return finalizeTurn(state, player, actionResult.getMessage(), null);
    }

    /**
     * Processes a gem-return command after a player exceeds the hand limit.
     *
     * @param state the active game state
     * @param input the raw gem return input
     * @return the turn outcome
     */
    public TurnProcessor.TurnResult processGemReturn(GameState state, String input) {
        if (state == null) {
            return TurnProcessor.TurnResult.failure("Game state is unavailable.");
        }

        Player player = state.getCurrentPlayer();
        int excess = Math.max(0, player.getGemCount() - 10);
        if (excess <= 0) {
            return TurnProcessor.TurnResult.failure("No gems need to be returned.");
        }

        try {
            List<GemColor> colors = inputHandler.parseGemSequence(input);
            GemCollection toReturn = inputHandler.promptGemsToReturn(player, excess, colors);
            ActionResult result = ActionExecutor.executeReturnGems(state, toReturn);
            if (!result.isSuccess()) {
                return TurnProcessor.TurnResult.failure(result.getMessage());
            }
            return finalizeTurn(state, player, result.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return TurnProcessor.TurnResult.failure(e.getMessage());
        }
    }

    /**
     * Resolves a disconnected player's turn by automatically passing.
     *
     * @param state the active game state
     * @return the turn outcome
     */
    public TurnProcessor.TurnResult processAutomaticPass(GameState state) {
        if (state == null) {
            return TurnProcessor.TurnResult.failure("Game state is unavailable.");
        }
        return finalizeTurn(state, state.getCurrentPlayer(), "Turn auto-passed after disconnect.", null);
    }

    /**
     * Resolves excess gems automatically for a disconnected player.
     *
     * @param state the active game state
     * @return the turn outcome
     */
    public TurnProcessor.TurnResult processAutomaticReturnGems(GameState state) {
        if (state == null) {
            return TurnProcessor.TurnResult.failure("Game state is unavailable.");
        }

        Player player = state.getCurrentPlayer();
        int excess = Math.max(0, player.getGemCount() - 10);
        if (excess <= 0) {
            return finalizeTurn(state, player, "Disconnected turn resolved automatically.", null);
        }

        GemCollection toReturn = chooseAutomaticReturnGems(player, excess);
        ActionResult result = ActionExecutor.executeReturnGems(state, toReturn);
        if (!result.isSuccess()) {
            return TurnProcessor.TurnResult.failure(result.getMessage());
        }
        return finalizeTurn(state, player, "Excess gems were auto-returned after disconnect.", null);
    }

    /**
     * Executes a full AI-controlled turn through the shared engine pipeline.
     *
     * @param state the active game state
     * @param aiPlayer the AI player making the decision
     * @return the turn outcome
     */
    public TurnProcessor.TurnResult processAITurn(GameState state, AIPlayer aiPlayer) {
        if (state == null) {
            return TurnProcessor.TurnResult.failure("Game state is unavailable.");
        }
        if (aiPlayer == null) {
            return TurnProcessor.TurnResult.failure("AI player is unavailable.");
        }

        Player player = state.getCurrentPlayer();
        AIAction action = aiPlayer.decideAction(state);

        ActionResult actionResult = executeAIAction(state, action);
        if (!actionResult.isSuccess()) {
            state.advanceToNextPlayer();
            return TurnProcessor.TurnResult.success(player.getName() + " passed (AI could not act).");
        }

        StringBuilder message = new StringBuilder(actionResult.getMessage());
        int excess = player.getGemCount() - 10;
        if (excess > 0) {
            GemCollection toReturn = aiPlayer.chooseGemsToReturn(excess, state);
            ActionResult returnResult = ActionExecutor.executeReturnGems(state, toReturn);
            if (!returnResult.isSuccess()) {
                return TurnProcessor.TurnResult.failure(returnResult.getMessage());
            }
            message.append(" ").append(returnResult.getMessage());
        }

        Noble chosenNoble = null;
        List<Noble> claimable = gameRules.getClaimableNobles(player, state.getAvailableNobles());
        if (!claimable.isEmpty()) {
            chosenNoble = aiPlayer.chooseNoble(claimable, state);
        }

        return finalizeTurn(state, player, message.toString(), chosenNoble);
    }

    /**
     * Advances the game to the next player's turn and auto-claims a noble when one is available.
     *
     * @param state the active game state
     * @return the nobles that were claimable before the turn advanced
     */
    public List<Noble> advanceTurn(GameState state) {
        Player currentPlayer = state.getCurrentPlayer();

        List<Noble> claimableNobles = gameRules.getClaimableNobles(
            currentPlayer,
            state.getAvailableNobles()
        );

        if (claimableNobles.size() >= 1) {
            Noble noble = claimableNobles.get(0);
            currentPlayer.claimNoble(noble);
            state.removeNoble(noble);
            claimableNobles.clear();
        }

        boolean isLastPlayer = (state.getCurrentPlayerIndex() == state.getPlayers().size() - 1);
        if (isLastPlayer) {
            Player winner = checkWinCondition(state, state.getWinningThreshold());
            if (winner != null) {
                state.setGameOver(true);
                return claimableNobles;
            }
        }

        state.advanceToNextPlayer();
        return claimableNobles;
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

    private GemCollection chooseAutomaticReturnGems(Player player, int excessCount) {
        List<GemColor> priority = new ArrayList<>(List.of(GemColor.values()));
        priority.sort((left, right) -> {
            int countDiff = Integer.compare(
                    player.getGems().getCount(right),
                    player.getGems().getCount(left));
            if (countDiff != 0) {
                return countDiff;
            }
            return Integer.compare(left.ordinal(), right.ordinal());
        });

        GemCollection toReturn = new GemCollection();
        int remaining = excessCount;
        for (GemColor color : priority) {
            if (remaining <= 0) {
                break;
            }
            int available = player.getGems().getCount(color);
            if (available <= 0) {
                continue;
            }
            int amount = Math.min(available, remaining);
            toReturn = toReturn.add(color, amount);
            remaining -= amount;
        }

        if (remaining > 0) {
            throw new IllegalStateException("Unable to determine automatic gem return for disconnected player.");
        }

        return toReturn;
    }

    private ActionResult executeAIAction(GameState state, AIAction action) {
        if (action == null || action.getActionType() == null) {
            return new ActionResult(false, "AI error: no action selected.");
        }

        ActionType actionType = action.getActionType();
        return switch (actionType) {
            case TAKE_THREE_DIFFERENT ->
                ActionExecutor.executeTakeThreeDifferentGems(state, action.getGemSelection());
            case TAKE_TWO_SAME -> {
                GemColor color = null;
                if (action.getGemSelection() != null) {
                    for (Map.Entry<GemColor, Integer> e : action.getGemSelection().asMap().entrySet()) {
                        if (e.getValue() > 0 && e.getKey() != GemColor.GOLD) {
                            color = e.getKey();
                            break;
                        }
                    }
                }
                if (color == null) {
                    yield new ActionResult(false, "AI error: no color for TAKE_TWO_SAME");
                }
                yield ActionExecutor.executeTakeTwoSameGems(state, color);
            }
            case PURCHASE_CARD ->
                ActionExecutor.executePurchaseCard(state, action.getTargetCard(), action.isFromReserved());
            case RESERVE_CARD ->
                ActionExecutor.executeReserveCard(state, action.getTargetCard());
        };
    }

    private TurnProcessor.TurnResult finalizeTurn(
            GameState state,
            Player player,
            String baseMessage,
            Noble preferredNoble
    ) {
        StringBuilder message = new StringBuilder(baseMessage);

        List<Noble> claimable = gameRules.getClaimableNobles(player, state.getAvailableNobles());
        if (!claimable.isEmpty()) {
            Noble nobleToClaim = claimable.get(0);
            if (preferredNoble != null && claimable.contains(preferredNoble)) {
                nobleToClaim = preferredNoble;
            }
            ActionResult nobleResult = ActionExecutor.executeClaimNoble(state, nobleToClaim);
            if (nobleResult.isSuccess()) {
                message.append(" ").append(nobleResult.getMessage());
            }
        }

        if (!state.isFinalRoundTriggered() && gameRules.hasPlayerWon(player, state.getWinningThreshold())) {
            state.triggerFinalRound();
            message.append(" Final round triggered.");
        }

        state.advanceToNextPlayer();

        if (state.isGameOver()) {
            Player winner = gameRules.getWinner(state.getPlayers(), state.getWinningThreshold());
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

        return TurnProcessor.TurnResult.success(message.toString());
    }

    private int getRegularGemCount(int playerCount) {
        switch (playerCount) {
            case 2: return 4;
            case 3: return 5;
            case 4: return 7;
            default: throw new IllegalArgumentException("Invalid player count!");
        }
    }
}

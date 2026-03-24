package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.cs102.g04t06.game.execution.TurnProcessor;
import edu.cs102.g04t06.game.exception.NobleNotAvailableException;
import edu.cs102.g04t06.game.execution.ai.AIAction;
import edu.cs102.g04t06.game.execution.ai.AIPlayer;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * GameBoardUI
 *
 * Renders the full Splendor game board in the console.
 *
 */
public class GameBoardUI implements ThemeStyleSheet {
    private static final String ACTION_PROMPT = "ACTION > ";
    private static final String YOUR_TURN_PROMPT = "YOUR TURN > ";
    private static final String WAITING_PROMPT = "WAITING > ";
    private static final int MAIN_WIDTH = 88;
    private static final int SIDEBAR_WIDTH = 27;

    // Gem colour → ANSI
    private static final Map<GemColor, String> GEM_ANSI = new EnumMap<>(GemColor.class);
    static {
        GEM_ANSI.put(GemColor.WHITE, WHITE);
        GEM_ANSI.put(GemColor.BLUE,  BLUE);
        GEM_ANSI.put(GemColor.GREEN, GREEN);
        GEM_ANSI.put(GemColor.RED,   RED);
        GEM_ANSI.put(GemColor.BLACK, GREY);
        GEM_ANSI.put(GemColor.GOLD,  GOLD);
    }

    // Short label per gem colour  (W R U G K *)
    private static final Map<GemColor, String> GEM_LABEL = new EnumMap<>(GemColor.class);
    static {
        GEM_LABEL.put(GemColor.WHITE, "W");
        GEM_LABEL.put(GemColor.BLUE,  "U");
        GEM_LABEL.put(GemColor.GREEN, "G");
        GEM_LABEL.put(GemColor.RED,   "R");
        GEM_LABEL.put(GemColor.BLACK, "K");
        GEM_LABEL.put(GemColor.GOLD,  "*");
    }

    private static final List<GemColor> CARD_ORDER = Arrays.asList(
            GemColor.WHITE, GemColor.RED, GemColor.BLUE, GemColor.GREEN, GemColor.BLACK);

    private static final List<GemColor> BANK_ORDER = Arrays.asList(
            GemColor.WHITE, GemColor.RED, GemColor.BLUE,
            GemColor.GREEN, GemColor.BLACK, GemColor.GOLD);

    // -------------------------------------------------------------------------
    // Board dimensions
    //   border : "  ╔" + "═"×86 + "╗"  →  total 90 terminal columns
    //   content: "  ║ " + <84 visible> + " ║"
    // -------------------------------------------------------------------------
    private static final int INNER = MAIN_WIDTH + SIDEBAR_WIDTH + 1;
    private static final int FILL  = INNER + 2;

    // Tile inner widths (visible chars between ┌/┐)
    private static final int NOBLE_INNER = 16;   // ┌ + 16×─ + ┐  = 18 wide
    private static final int CARD_INNER  = 12;   // ┌ + 12×─ + ┐  = 14 wide

    // -------------------------------------------------------------------------
    // Scanner + UI-local session state
    // -------------------------------------------------------------------------
    private final Scanner scanner;
    private Map<Player, AIPlayer> aiPlayers = new HashMap<>();
    private final List<String> actionLog = new ArrayList<>();
    private final TurnProcessor turnProcessor = new TurnProcessor();
    private int actionLinesFromBottom = 1;
    private int statusLinesFromBottom = 1;
    private String actionStatus = "";
    private String actionPromptLabel = ACTION_PROMPT;
    private List<String> readOnlyLogEntries = null;

    private static final class MainAreaRender {
        private final List<String> lines;
        private final int actionLineIndex;
        private final int statusLineIndex;

        private MainAreaRender(List<String> lines, int actionLineIndex, int statusLineIndex) {
            this.lines = lines;
            this.actionLineIndex = actionLineIndex;
            this.statusLineIndex = statusLineIndex;
        }
    }

    /**
     * Creates a board UI that reads input from standard input.
     */
    public GameBoardUI() {
        this(new Scanner(System.in));
    }

    /**
     * Creates a board UI backed by the supplied scanner.
     *
     * @param scanner scanner used for interactive commands
     */
    GameBoardUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Registers AI players so the game loop can execute their turns automatically.
     *
     * @param aiPlayers map from Player to the AIPlayer controlling that player
     */
    public void registerAIPlayers(Map<Player, AIPlayer> aiPlayers) {
        this.aiPlayers = aiPlayers;
    }

    // -------------------------------------------------------------------------
    // Public entry points
    // -------------------------------------------------------------------------
    /**
     * Runs the interactive game loop until the match ends or the user exits.
     *
     * @param state game state to render and mutate
     */
    public void show(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }

        this.actionPromptLabel = ACTION_PROMPT;
        this.readOnlyLogEntries = null;
        while (true) {
            clearScreen();
            render(state);
            if (state.isGameOver()) {
                break;
            }
            if (aiPlayers.containsKey(state.getCurrentPlayer())) {
                executeAITurn(state);
                continue;
            }
            String input = promptAction();
            if (input.equals("?")) {
                showHelpOverlay();
                continue;
            }
            if (input.equalsIgnoreCase("q")) {
                break;
            }
            handleAction(state, input);
        }
    }

    /** Renders the board once without prompting for user input. */
    public void displayGameState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }
        this.actionPromptLabel = ACTION_PROMPT;
        this.actionStatus = "";
        this.readOnlyLogEntries = null;
        clearScreen();
        render(state);
    }

    /**
     * Renders the board in read-only mode for network flows.
     *
     * @param state game state to render
     * @param statusMessage message displayed below the read-only action hint
     */
    public void displayReadOnlyState(GameState state, String statusMessage, List<String> logEntries) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }
        this.actionPromptLabel = WAITING_PROMPT;
        this.actionStatus = statusMessage == null ? "" : CYAN + statusMessage + RESET;
        this.readOnlyLogEntries = logEntries == null ? null : new ArrayList<>(logEntries);
        clearScreen();
        render(state);
    }

    /**
     * Renders the board in LAN interactive mode and collects a command inside the panel.
     *
     * @param state game state to render
     * @param statusMessage inline status or error message
     * @param statusColor ANSI color for the status line
     * @param logEntries log entries to display
     * @return trimmed user input
     */
    public String promptNetworkTurn(GameState state, String statusMessage, String statusColor, List<String> logEntries) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }
        this.actionPromptLabel = YOUR_TURN_PROMPT;
        this.actionStatus = formatInlineStatus(statusMessage, statusColor);
        this.readOnlyLogEntries = logEntries == null ? null : new ArrayList<>(logEntries);
        clearScreen();
        render(state);
        return promptAction();
    }

    /**
     * Renders a passive LAN board state using a waiting prompt label.
     *
     * @param state game state to render
     * @param statusMessage message shown below the waiting prompt
     * @param statusColor ANSI color for the status line
     * @param logEntries log entries to display
     */
    public void displayNetworkState(GameState state, String statusMessage, String statusColor, List<String> logEntries) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }
        this.actionPromptLabel = WAITING_PROMPT;
        this.actionStatus = formatInlineStatus(statusMessage, statusColor);
        this.readOnlyLogEntries = logEntries == null ? null : new ArrayList<>(logEntries);
        clearScreen();
        render(state);
    }

    /**
     * Convenience overload for read-only rendering without a log override.
     *
     * @param state game state to render
     * @param statusMessage message displayed below the read-only action hint
     */
    public void displayReadOnlyState(GameState state, String statusMessage) {
        displayReadOnlyState(state, statusMessage, null);
    }

    // -------------------------------------------------------------------------
    // Master render
    // -------------------------------------------------------------------------
    /**
     * Renders the full board, sidebar, and action area for the current state.
     *
     * @param state state to render
     */
    private void render(GameState state) {
        System.out.print("\u001B[?25l");
        System.out.print("\u001B[2;1H");
        boardTop();
        printHeader(state);
        MainAreaRender mainArea = buildMainArea(state);
        List<String> sidebar = buildPlayerSidebar(state.getPlayers(), state.getCurrentPlayer());
        List<String> combined = combineColumns(mainArea.lines, sidebar, MAIN_WIDTH, SIDEBAR_WIDTH);
        actionLinesFromBottom = combined.size() - mainArea.actionLineIndex;
        statusLinesFromBottom = combined.size() - mainArea.statusLineIndex;
        for (String row : combined) {
            line(row);
        }
        boardBottom();
    }

    /**
     * Displays the full-screen help overlay until the user dismisses it.
     */
    private void showHelpOverlay() {
        clearScreen();
        System.out.print("\u001B[2;1H");
        boardTop();
        line(GOLD + BOLD + "SPLENDOR HELP" + RESET
                + sp(INNER - "SPLENDOR HELP".length() - "Press ? to return".length())
                + DIM + WHITE + "Press ? to return" + RESET);
        divider();
        line(BOLD + WHITE + "Gem Actions" + RESET
                + " - take 3 different or 2 same gems; bank needs 4+ for doubles.");
        line(DIM + WHITE + "  Gem limit: a player may hold at most 10 gems after their turn." + RESET);
        blank();
        line(BOLD + WHITE + "Card Actions" + RESET
                + " - buy a visible card or buy from your reserved slots.");
        line(BOLD + WHITE + "Reserve Action" + RESET
                + " - reserve a card, gain a gold (*) if available, max 3 reserved.");
        blank();
        line(BOLD + WHITE + "Noble Claiming" + RESET
                + " - automatic at end of turn if your card bonuses meet the noble cost.");
        line(BOLD + WHITE + "Winning Condition" + RESET
                + " - first to 15 points triggers the final round; most points wins.");
        blank();
        line(BOLD + WHITE + "Card Costs" + RESET
                + " - shorthand like " + DIM + "w2r3k1" + RESET + " means 2 white, 3 red, 1 black.");
        line(BOLD + WHITE + "Gold Wildcard" + RESET
                + " - gold (*) can substitute for any gem color when buying.");
        blank();
        line(DIM + WHITE + "Examples:" + RESET);
        line(DIM + WHITE + "  take w r u   |   take w w   |   buy t2 slot1   |   buy reserve 1" + RESET);
        line(DIM + WHITE + "  reserve t1 slot3   |   pass   |   q to leave the game board" + RESET);
        blank();
        line(GREEN + BOLD + "Press any key, then Enter, to return to the game board." + RESET);
        boardBottom();
        System.out.print("\u001B[1A\r\u001B[4C");
        scanner.nextLine();
    }

    // -------------------------------------------------------------------------
    // Board frame
    // -------------------------------------------------------------------------
    /**
     * Prints the top border of the outer board frame.
     */
    private void boardTop() {
        System.out.println(WHITE + "  ╔" + rep('═', FILL) + "╗" + RESET);
    }

    /**
     * Prints the bottom border of the outer board frame.
     */
    private void boardBottom() {
        System.out.print(WHITE + "  ╚" + rep('═', FILL) + "╝" + RESET);
    }

    /**
     * Prints one content row padded to the full board width.
     *
     * @param content row content excluding the outer border
     */
    private void line(String content) {
        int pad = INNER - vlen(content);
        if (pad < 0) {
            pad = 0;
        }
        System.out.println(WHITE + "  ║ " + RESET
                + content + sp(pad)
                + WHITE + " ║" + RESET);
    }

    /**
     * Prints an empty content row inside the outer frame.
     */
    private void blank() {
        line("");
    }

    /**
     * Prints a full-width divider inside the outer frame.
     */
    private void divider() {
        System.out.println(WHITE + "  ╠" + rep('═', FILL) + "╣" + RESET);
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------
    /**
     * Renders the board header with title, round counter, and help hints.
     *
     * @param state state whose round number is displayed
     */
    private void printHeader(GameState state) {
        String left  = GOLD + BOLD + "SPLENDOR" + RESET;
        String mid   = WHITE + "Round " + state.getRoundNumber() + RESET;
        String right = DIM + WHITE + "[?] Help    [Q] Quit" + RESET;

        int g1 = (INNER / 2) - vlen(left) - vlen(mid) / 2;
        int g2 = INNER - vlen(left) - vlen(mid) - vlen(right) - g1;
        if (g1 < 1) {
            g1 = 1;
        }
        if (g2 < 1) {
            g2 = 1;
        }
        line(left + sp(g1) + mid + sp(g2) + right);
        divider();
    }

    // -------------------------------------------------------------------------
    // Nobles
    // -------------------------------------------------------------------------
    /**
     * Renders the nobles section.
     *
     * @param nobles nobles currently available for claiming
     */
    private void printNobles(List<Noble> nobles) {
        line(PURPLE + BOLD + "NOBLES" + RESET);

        if (nobles == null || nobles.isEmpty()) {
            line(DIM + WHITE + "  No nobles available." + RESET);
            blank();
            divider();
            return;
        }

        // Build each row across all noble tiles
        String[] topRow = new String[nobles.size()];
        String[] ptRow  = new String[nobles.size()];
        String[] rqRow  = new String[nobles.size()];
        String[] botRow = new String[nobles.size()];

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);
            String border = WHITE;
            String dash   = rep('─', NOBLE_INNER);

            topRow[i] = border + "┌" + dash + "┐" + RESET;
            botRow[i] = border + "└" + dash + "┘" + RESET;

            String pContent = " " + GOLD + "★ " + noble.getPoints() + RESET;
            ptRow[i] = WHITE + "│" + RESET
                    + padTo(pContent, NOBLE_INNER)
                    + WHITE + "│" + RESET;

            String rContent = " " + formatNobleRequirements(noble.getRequirements());
            rqRow[i] = WHITE + "│" + RESET
                    + padTo(rContent, NOBLE_INNER)
                    + WHITE + "│" + RESET;
        }

        line(joinTiles(topRow, 2));
        line(joinTiles(ptRow,  2));
        line(joinTiles(rqRow,  2));
        line(joinTiles(botRow, 2));
        blank();
        divider();
    }

    // -------------------------------------------------------------------------
    // Tier sections
    // -------------------------------------------------------------------------
    /**
     * Renders one development card tier section.
     *
     * @param label title shown for the tier
     * @param cards visible cards in the tier
     * @param deckCount remaining deck size for the tier
     */
    private void printTier(String label, List<Card> cards, int deckCount) {
        String hL = BOLD + WHITE + label + RESET;
        String hR = DIM + WHITE + "DECK: " + RESET + BOLD + WHITE + deckCount + RESET;
        int gap = INNER - vlen(hL) - vlen(hR);
        if (gap < 1) {
            gap = 1;
        }
        line(hL + sp(gap) + hR);

        if (cards == null || cards.isEmpty()) {
            line(DIM + WHITE + "  No cards visible." + RESET);
            blank();
            divider();
            return;
        }

        String[] topRow   = new String[cards.size()];
        String[] labelRow = new String[cards.size()];
        String[] costRow  = new String[cards.size()];
        String[] botRow   = new String[cards.size()];

        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            String border = WHITE;
            String dash   = rep('─', CARD_INNER);
            String gemLabel = GEM_LABEL.getOrDefault(c.getBonus(), "?");
            String gemAnsi = GEM_ANSI.getOrDefault(c.getBonus(), WHITE);

            topRow[i] = border + "┌" + dash + "┐" + RESET;
            botRow[i] = border + "└" + dash + "┘" + RESET;

            String lContent = " " + gemAnsi + BOLD + gemLabel + RESET
                    + "  " + WHITE + c.getPoints() + RESET;
            labelRow[i] = WHITE + "│" + RESET
                    + padTo(lContent, CARD_INNER)
                    + WHITE + "│" + RESET;

            String cContent = " " + DIM + WHITE + formatCardCost(c) + RESET;
            costRow[i] = WHITE + "│" + RESET
                    + padTo(cContent, CARD_INNER)
                    + WHITE + "│" + RESET;
        }

        line("  " + joinTiles(topRow,   1));
        line("  " + joinTiles(labelRow, 1));
        line("  " + joinTiles(costRow,  1));
        line("  " + joinTiles(botRow,   1));
        blank();
        divider();
    }

    // -------------------------------------------------------------------------
    // Bank gems
    // -------------------------------------------------------------------------
    /**
     * Renders the shared gem bank counts.
     *
     * @param bank gem bank to display
     */
    private void printBank(GemCollection bank) {
        line(BOLD + WHITE + "BANK GEMS:" + RESET);

        StringBuilder sb = new StringBuilder("  ");
        for (GemColor color : BANK_ORDER) {
            String a = GEM_ANSI.getOrDefault(color, WHITE);
            String l = GEM_LABEL.getOrDefault(color, "?");
            int count = bank == null ? 0 : bank.getCount(color);
            sb.append(a).append(BOLD).append(l).append(":").append(count)
                    .append(RESET).append("   ");
        }
        line(sb.toString());
        divider();
    }

    // -------------------------------------------------------------------------
    // Player panels
    // -------------------------------------------------------------------------
    /**
     * Renders the legacy full-width player strip.
     *
     * @param players players to render
     * @param currentPlayer active player in the state
     */
    private void printPlayers(List<Player> players, Player currentPlayer) {
        if (players == null || players.isEmpty()) {
            line(DIM + WHITE + "No players in game state." + RESET);
            divider();
            return;
        }

        for (Player p : players) {
            if (p == currentPlayer) {
                printCurrentPlayer(p);
            } else {
                printOtherPlayer(p);
            }
        }
        divider();
    }

    /**
     * Renders the active player in the legacy player strip.
     *
     * @param p active player
     */
    private void printCurrentPlayer(Player p) {
        String ind   = GREEN + BOLD + "► " + RESET;
        String name  = CYAN  + BOLD + p.getName() + " (you)" + RESET;
        String pts   = GOLD  + BOLD + p.getPoints() + " pts" + RESET;
        int gap = INNER - vlen(ind) - vlen(name) - vlen(pts);
        if (gap < 2) {
            gap = 2;
        }
        line(ind + name + sp(gap) + pts);

        String cardStats = colorStats(formatStats(p.calculateBonuses(), false));
        String gemStats  = colorStats(formatStats(p.getGems().asMap(), true));

        line(WHITE + "  Cards :  " + RESET
                + cardStats
                + WHITE + "   Reserved: " + RESET
                + reservedSlots(p.getReservedCards().size()));

        line(WHITE + "  Gems  :  " + RESET
                + gemStats
                + DIM + WHITE + "   (Total: " + p.getGemCount() + ")" + RESET);
    }

    /**
     * Renders a non-active player in the legacy player strip.
     *
     * @param p player to render
     */
    private void printOtherPlayer(Player p) {
        String cardStats = formatStats(p.calculateBonuses(), false);
        line(RED + BOLD + p.getName() + RESET
                + "  " + BOLD + WHITE + p.getPoints() + " pts" + RESET
                + DIM + WHITE
                + "   Cards: " + cardStats
                + "   Gems: "  + p.getGemCount() + " total"
                + "   Reserved: " + p.getReservedCards().size()
                + RESET);
    }

    // -------------------------------------------------------------------------
    // Log
    // -------------------------------------------------------------------------
    /**
     * Renders the legacy single-line action log.
     */
    private void printLog() {
        StringBuilder sb = new StringBuilder(DIM + WHITE + "LOG:  " + RESET);
        if (actionLog.isEmpty()) {
            sb.append(DIM).append(WHITE).append("No actions yet").append(RESET);
            line(sb.toString());
            return;
        }
        sb.append(WHITE).append(actionLog.get(actionLog.size() - 1)).append(RESET);
        line(sb.toString());
    }

    // -------------------------------------------------------------------------
    // ACTION line + prompt
    // -------------------------------------------------------------------------
    /**
     * Renders the legacy action guide and prompt row.
     */
    private void printActionLine() {
        line(DIM + WHITE + "  ┌ Available Actions ────────────────────────────────────────────────┐" + RESET);
        line(DIM + WHITE + "  . take w r u  : take 3 diff gems    . buy t1 slot1 : buy visible card" + RESET);
        line(DIM + WHITE + "  . take w w    : take 2 same gems    . buy reserve  : buy reserve card" + RESET);
        line(DIM + WHITE + "  . reserve t1 slot1 : reserve + gold      . pass         : skip turn" + RESET);
        line(DIM + WHITE + "  └───────────────────────────────────────────────────────────────────┘" + RESET);
        line(GREEN + BOLD + ACTION_PROMPT + RESET);
    }

    /**
     * Places the cursor on the action prompt row and reads one command line.
     *
     * @return trimmed player command
     */
    private String promptAction() {
        // Move cursor into the ACTION row inside the board:
        int cursorOffset = 5 + vlen(actionPromptLabel);
        System.out.print("\u001B[" + actionLinesFromBottom + "A\r\u001B[" + cursorOffset + "C\u001B[?25h");
        return scanner.nextLine().trim();
    }

    // -------------------------------------------------------------------------
    // Action dispatch
    // -------------------------------------------------------------------------
    /**
     * Dispatches one command string to the matching action handler.
     *
     * @param state game state to mutate
     * @param input raw player input
     */
    private void handleAction(GameState state, String input) {
        TurnProcessor.TurnResult result = turnProcessor.processCommand(state, input);
        if (!result.isSuccess()) {
            err(result.getMessage());
            return;
        }

        if (result.isAwaitingReturn()) {
            ok(result.getMessage());
            handleReturnExcessGems(state);
        player.addReservedCard(card);
        state.getMarket().removeCard(card);

        // Gain a gold gem if available
        if (state.getGemBank().getCount(GemColor.GOLD) > 0) {
            GemCollection gold = new GemCollection().add(GemColor.GOLD, 1);
            state.removeGemsFromBank(gold);
            player.addGems(gold);
        }

        actionLog.add(player.getName() + " reserved t" + tier + " slot" + (slotIndex + 1));
        ok("Reserved t" + tier + " slot" + (slotIndex + 1));

        if (rules.mustReturnGems(player)) {
            handleReturnExcessGems(state, player);
        }

        handleEndOfTurnNobleClaim(state, player);
        advanceTurn(state);
        handleGameEnd(state);
        sleep(800);
    }

    /**
     * Ends the current turn without another game action.
     *
     * @param state game state to advance
     */
    private void handlePass(GameState state) {
        Player player = state.getCurrentPlayer();
        String current = player.getName();
        handleEndOfTurnNobleClaim(state, player);
        advanceTurn(state);
        handleGameEnd(state);
        actionLog.add(current + " passed. Current: " + state.getCurrentPlayer().getName());
        ok("Turn passed.");
        sleep(600);
    }

    // -------------------------------------------------------------------------
    // AI turn execution
    // -------------------------------------------------------------------------

    /**
     * Executes a full AI turn: decides an action, applies it, then handles
     * noble claiming, gem return, win check, and turn advancement.
     *
     * @param state game state to mutate
     */
    private void executeAITurn(GameState state) {
        Player player = state.getCurrentPlayer();
        AIPlayer aiPlayer = aiPlayers.get(player);

        // Show "thinking" indicator before deciding
        ok(player.getName() + " (AI) is thinking...");
        clearScreen();
        render(state);
        sleep(700);

        AIAction action = aiPlayer.decideAction(state);

        actionLog.add(player.getName() + " (AI): " + action.getDescription());
        ok(player.getName() + " (AI): " + action.getDescription());

        executeAIAction(state, player, action);

        // Noble claim
        List<Noble> claimable = rules.getClaimableNobles(player, state.getAvailableNobles());
        if (!claimable.isEmpty()) {
            Noble chosen = aiPlayer.chooseNoble(claimable, state);
            try {
                Noble claimed = state.removeNoble(chosen);
                player.claimNoble(claimed);
                actionLog.add(player.getName() + " (AI) claimed noble " + claimed.getName());
            } catch (NobleNotAvailableException e) {
                // Noble was already taken; nothing to do
            }
        }

        // Gem return
        if (rules.mustReturnGems(player)) {
            int excess = player.getGemCount() - 10;
            GemCollection toReturn = aiPlayer.chooseGemsToReturn(excess, state);
            player.deductGems(toReturn);
            state.addGemsToBank(toReturn);
            actionLog.add(player.getName() + " (AI) returned " + formatGemShort(toReturn));
        }

        handleWinCheck(state, player);
        advanceTurn(state);
        handleGameEnd(state);
        sleep(1000);
    }

    /**
     * Applies the core state mutation for an AI action (no noble/gem follow-up).
     *
     * @param state  game state to mutate
     * @param player acting player
     * @param action AI action to apply
     */
    private void executeAIAction(GameState state, Player player, AIAction action) {
        switch (action.getActionType()) {
            case PURCHASE_CARD -> {
                Card card = action.getTargetCard();
                GemCollection payment = buildPayment(player, rules.calculateActualCost(player, card));
                player.deductGems(payment);
                state.addGemsToBank(payment);
                player.addCard(card);
                if (!action.isFromReserved()) {
                    state.getMarket().removeCard(card);
                }
            }
            case RESERVE_CARD -> {
                Card card = action.getTargetCard();
                player.addReservedCard(card);
                state.getMarket().removeCard(card);
                if (state.getGemBank().getCount(GemColor.GOLD) > 0) {
                    GemCollection gold = new GemCollection().add(GemColor.GOLD, 1);
                    state.removeGemsFromBank(gold);
                    player.addGems(gold);
                }
            }
            case TAKE_THREE_DIFFERENT, TAKE_TWO_SAME -> {
                GemCollection requested = action.getGemSelection();
                state.removeGemsFromBank(requested);
                player.addGems(requested);
            }
        }
    }

    /**
     * Checks whether the supplied command token targets reserved-card buying.
     *
     * @param token command token to inspect
     * @return true if the token refers to reserved cards
     */
    private boolean isReservedBuyToken(String token) {
        return "reserved".equalsIgnoreCase(token)
                || "reserve".equalsIgnoreCase(token)
                || "r".equalsIgnoreCase(token);
    }

    /**
     * Applies a validated gem-taking action and advances turn flow.
     *
     * @param state game state to mutate
     * @param player acting player
     * @param requested gems to move from bank to player
     * @param verb status verb shown after success
     */
    private void applyTake(GameState state, Player player, GemCollection requested, String verb) {
        try {
            state.removeGemsFromBank(requested);
            player.addGems(requested);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

        actionLog.add(result.getMessage());
        ok(result.getMessage());
        sleep(800);
    }

    /**
     * Forces a player over the gem limit to return gems until legal again.
     *
     * @param state game state to mutate
     */
    private void handleReturnExcessGems(GameState state) {
        Player player = state.getCurrentPlayer();
        int excess = player.getGemCount() - 10;
        String promptMessage = "Return " + excess + " gem(s) [e.g. wr, uu]: ";
        while (excess > 0) {
            String input = promptActionStatus(state, promptMessage);

            TurnProcessor.TurnResult result = turnProcessor.processReturnGems(state, input);
            if (!result.isSuccess()) {
                promptMessage = "Return " + excess + " gem(s) [invalid input, try again]: ";
                continue;
            }

            actionLog.add(result.getMessage());
            ok(result.getMessage());
            excess = player.getGemCount() - 10;
            promptMessage = "Return " + excess + " gem(s) [e.g. wr, uu]: ";
        }
        actionStatus = "";
        sleep(800);
    }

    // -------------------------------------------------------------------------
    // Rendering utilities
    // -------------------------------------------------------------------------

    /**
     * Formats gem statistics into space-separated label/value tokens.
     *
     * @param stats gem counts to format
     * @param includeGold whether gold should be included
     * @return formatted stat string
     */
    private String formatStats(Map<GemColor, Integer> stats, boolean includeGold) {
        StringBuilder sb = new StringBuilder();
        List<GemColor> order = includeGold ? BANK_ORDER : CARD_ORDER;

        for (GemColor color : order) {
            if (!includeGold && color == GemColor.GOLD) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            int value = stats == null ? 0 : stats.getOrDefault(color, 0);
            sb.append(GEM_LABEL.getOrDefault(color, "?")).append(":").append(value);
        }

        return sb.toString();
    }

    /**
     * Formats a card cost into shorthand such as {@code w2r3k1}.
     *
     * @param card card whose cost should be formatted
     * @return shorthand cost string
     */
    private String formatCardCost(Card card) {
        if (card == null || card.getCost() == null) {
            return "-";
        }

        Map<GemColor, Integer> req = card.getCost().asMap();
        StringBuilder sb = new StringBuilder();
        for (GemColor color : CARD_ORDER) {
            int count = req.getOrDefault(color, 0);
            if (count <= 0) {
                continue;
            }
            sb.append(gemCodeLower(color)).append(count);
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    /**
     * Formats noble requirements using colored gem tokens.
     *
     * @param req noble requirements map
     * @return formatted requirement string
     */
    private String formatNobleRequirements(Map<GemColor, Integer> req) {
        if (req == null || req.isEmpty()) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();
        for (GemColor color : CARD_ORDER) {
            int count = req.getOrDefault(color, 0);
            if (count <= 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(GEM_ANSI.getOrDefault(color, WHITE))
                    .append(GEM_LABEL.getOrDefault(color, "?"))
                    .append(":")
                    .append(count)
                    .append(RESET);
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    /**
     * Colours a stats string like "W:2 R:1 U:3" token by token.
     * Each token is coloured by its first letter.
     */
    private String colorStats(String stats) {
        if (stats == null || stats.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String t : stats.split("\\s+")) {
            if (t.isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append("  ");
            }
            sb.append(gemAnsi(t.charAt(0))).append(BOLD).append(t).append(RESET);
            first = false;
        }
        return sb.toString();
    }

    /** Maps uppercase gem stat letter (W/R/U/G/K/*) to ANSI colour. */
    private String gemAnsi(char c) {
        return switch (c) {
            case 'W' -> WHITE;
            case 'R' -> RED;
            case 'U' -> BLUE;
            case 'G' -> GREEN;
            case 'K' -> GREY;
            case '*' -> GOLD;
            default  -> WHITE;
        };
    }

    /**
     * Maps a gem color to its lowercase shorthand character.
     *
     * @param color gem color to encode
     * @return lowercase shorthand character
     */
    private char gemCodeLower(GemColor color) {
        return switch (color) {
            case WHITE -> 'w';
            case RED -> 'r';
            case BLUE -> 'u';
            case GREEN -> 'g';
            case BLACK -> 'k';
            case GOLD -> '*';
        };
    }

    /**
     * Pads string s (may contain ANSI codes) so its visible width
     * equals exactly targetWidth. Strips ANSI for width measurement.
     */
    private String padTo(String s, int targetWidth) {
        int pad = targetWidth - vlen(s);
        return pad > 0 ? s + sp(pad) : s;
    }

    /**
     * Joins an array of pre-built tile strings with a gap of gapSize spaces
     * between each tile.
     */
    private String joinTiles(String[] tiles, int gapSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiles.length; i++) {
            if (i > 0) {
                sb.append(sp(gapSize));
            }
            sb.append(tiles[i]);
        }
        return sb.toString();
    }

    /** Renders 3 reserved card slots: [ ][ ][ ] */
    private String reservedSlots(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < count ? CYAN + "[■]" + RESET : DIM + "[ ]" + RESET);
        }
        return sb.toString();
    }

    /**
     * Visible length of s: strips ANSI escape sequences before measuring.
     */
    private int vlen(String s) {
        return s.replaceAll(ANSI_REGEX, "").length();
    }

    /** Returns n spaces. */
    private String sp(int n) {
        return n > 0 ? " ".repeat(n) : "";
    }

    /** Returns n repetitions of character c as a String. */
    private String rep(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Builds the left-hand market column of the board as renderable rows.
     *
     * @param state state to render
     * @return prepared render rows plus prompt row metadata
     */
    private MainAreaRender buildMainArea(GameState state) {
        List<String> lines = new ArrayList<>();
        appendMarketPanel(lines, "NOBLES", "", buildNoblePanelLines(state.getAvailableNobles()));
        appendMarketPanel(lines, "TIER 3", "DECK: " + state.getMarket().getDeckSize(3),
                buildTierPanelLines(state.getMarket().getVisibleCards(3)));
        appendMarketPanel(lines, "TIER 2", "DECK: " + state.getMarket().getDeckSize(2),
                buildTierPanelLines(state.getMarket().getVisibleCards(2)));
        appendMarketPanel(lines, "TIER 1", "DECK: " + state.getMarket().getDeckSize(1),
                buildTierPanelLines(state.getMarket().getVisibleCards(1)));
        appendMarketPanel(lines, "BANK GEMS", "", List.of(formatBankLine(state.getGemBank())));
        appendMarketPanel(lines, "LOG", "", formatLogLines());

        List<String> actionBody = List.of(
                DIM + WHITE + ". take w r u  : take 3 diff gems    . buy t1 slot1 : buy visible card" + RESET,
                DIM + WHITE + ". take w w    : take 2 same gems    . buy reserve 1: buy reserve card" + RESET,
                DIM + WHITE + ". reserve t1 slot1 : reserve + gold      . pass         : skip turn" + RESET,
                "",
                GREEN + BOLD + actionPromptLabel + RESET,
                formatActionStatus()
        );
        int actionLineIndex = lines.size() + 2 + 4;
        int statusLineIndex = lines.size() + 2 + 5;
        appendMarketPanel(lines, "ACTIONS", "", actionBody);
        return new MainAreaRender(lines, actionLineIndex, statusLineIndex);
    }

    /**
     * Builds the right-hand player sidebar as stacked cards.
     *
     * @param players players to display
     * @param currentPlayer active player for highlighting
     * @return sidebar rows
     */
    private List<String> buildPlayerSidebar(List<Player> players, Player currentPlayer) {
        List<String> lines = new ArrayList<>();
        lines.add(DIM + WHITE + "PLAYERS" + RESET);
        for (Player player : players) {
            if (!lines.isEmpty()) {
                lines.add("");
            }
            appendPlayerCard(lines, player, player == currentPlayer);
        }
        return lines;
    }

    /**
     * Combines left and right column render rows into a single board row list.
     *
     * @param left left column rows
     * @param right right column rows
     * @param leftWidth visible width of the left column
     * @param rightWidth visible width of the right column
     * @return merged rows
     */
    private List<String> combineColumns(List<String> left, List<String> right, int leftWidth, int rightWidth) {
        int total = Math.max(left.size(), right.size());
        List<String> rows = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            String leftPart = i < left.size() ? padTo(left.get(i), leftWidth) : sp(leftWidth);
            String rightPart = i < right.size() ? padTo(right.get(i), rightWidth) : sp(rightWidth);
            rows.add(leftPart + " " + rightPart);
        }
        return rows;
    }

    /**
     * Appends one boxed market panel to the left-column render buffer.
     *
     * @param lines target row buffer
     * @param title left-aligned panel title
     * @param rightTitle optional right-aligned panel title
     * @param body panel body rows
     */
    private void appendMarketPanel(List<String> lines, String title, String rightTitle, List<String> body) {
        lines.add(panelTop(MAIN_WIDTH));
        lines.add(panelHeader(MAIN_WIDTH, title, rightTitle));
        for (String row : body) {
            lines.add(panelBody(MAIN_WIDTH, row));
        }
        lines.add(panelBottom(MAIN_WIDTH));
    }

    /**
     * Appends one player's sidebar card to the right-column render buffer.
     *
     * @param lines target row buffer
     * @param player player to render
     * @param isCurrentPlayer whether this player is active
     */
    private void appendPlayerCard(List<String> lines, Player player, boolean isCurrentPlayer) {
        String borderColor = isCurrentPlayer ? GREEN : WHITE;
        String nameColor = isCurrentPlayer ? CYAN : playerAccent(player);
        String title = (isCurrentPlayer ? "► " : "") + player.getName() + (isCurrentPlayer ? " (you)" : "");
        String points = player.getPoints() + "pts";

        lines.add(panelTop(SIDEBAR_WIDTH, borderColor));
        lines.add(panelHeader(SIDEBAR_WIDTH, title, points, borderColor, nameColor, GOLD + BOLD));
        lines.add(panelBody(SIDEBAR_WIDTH, "Gems", borderColor));
        for (String row : splitStatsLines(player.getGems().asMap(), true, SIDEBAR_WIDTH - 4)) {
            lines.add(panelBody(SIDEBAR_WIDTH, "  " + row, borderColor));
        }
        lines.add(panelDivider(SIDEBAR_WIDTH, borderColor));
        lines.add(panelBody(SIDEBAR_WIDTH, "Bonus", borderColor));
        for (String row : splitStatsLines(player.calculateBonuses(), false, SIDEBAR_WIDTH - 4)) {
            lines.add(panelBody(SIDEBAR_WIDTH, "  " + row, borderColor));
        }
        lines.add(panelDivider(SIDEBAR_WIDTH, borderColor));
        lines.add(panelBody(SIDEBAR_WIDTH, "Reserved: " + formatReservedTokens(player.getReservedCards()), borderColor));
        lines.add(panelBottom(SIDEBAR_WIDTH, borderColor));
    }

    /**
     * Builds the noble section body rows.
     *
     * @param nobles nobles currently available
     * @return render rows for the noble panel
     */
    private List<String> buildNoblePanelLines(List<Noble> nobles) {
        if (nobles == null || nobles.isEmpty()) {
            return List.of(DIM + WHITE + "No nobles available." + RESET);
        }

        String[] topRow = new String[nobles.size()];
        String[] ptRow = new String[nobles.size()];
        String[] rqRow = new String[nobles.size()];
        String[] botRow = new String[nobles.size()];

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);
            String dash = rep('─', NOBLE_INNER);
            topRow[i] = WHITE + "┌" + dash + "┐" + RESET;
            botRow[i] = WHITE + "└" + dash + "┘" + RESET;
            ptRow[i] = WHITE + "│" + RESET + padTo(" " + GOLD + "★" + noble.getPoints() + RESET, NOBLE_INNER)
                    + WHITE + "│" + RESET;
            rqRow[i] = WHITE + "│" + RESET + padTo(" " + formatNobleRequirements(noble.getRequirements()), NOBLE_INNER)
                    + WHITE + "│" + RESET;
        }

        return List.of(
                " " + joinTiles(topRow, 1),
                " " + joinTiles(ptRow, 1),
                " " + joinTiles(rqRow, 1),
                " " + joinTiles(botRow, 1));
    }

    /**
     * Builds one tier section body from visible development cards.
     *
     * @param cards cards to render
     * @return render rows for that tier panel
     */
    private List<String> buildTierPanelLines(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return List.of(DIM + WHITE + "No cards visible." + RESET);
        }

        String[] topRow = new String[cards.size()];
        String[] labelRow = new String[cards.size()];
        String[] costRow = new String[cards.size()];
        String[] botRow = new String[cards.size()];

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            String dash = rep('─', CARD_INNER);
            String gemLabel = GEM_LABEL.getOrDefault(card.getBonus(), "?");
            String gemAnsi = GEM_ANSI.getOrDefault(card.getBonus(), WHITE);

            topRow[i] = WHITE + "┌" + dash + "┐" + RESET;
            botRow[i] = WHITE + "└" + dash + "┘" + RESET;
            labelRow[i] = WHITE + "│" + RESET
                    + padTo(" " + gemAnsi + BOLD + gemLabel + RESET + "  " + WHITE + card.getPoints() + RESET, CARD_INNER)
                    + WHITE + "│" + RESET;
            costRow[i] = WHITE + "│" + RESET
                    + padTo(" " + DIM + WHITE + formatCardCost(card) + RESET, CARD_INNER)
                    + WHITE + "│" + RESET;
        }

        return List.of(
                " " + joinTiles(topRow, 1),
                " " + joinTiles(labelRow, 1),
                " " + joinTiles(costRow, 1),
                " " + joinTiles(botRow, 1));
    }

    /**
     * Formats the bank gem counts into one colored row.
     *
     * @param bank bank to display
     * @return formatted bank row
     */
    private String formatBankLine(GemCollection bank) {
        StringBuilder sb = new StringBuilder();
        for (GemColor color : BANK_ORDER) {
            if (sb.length() > 0) {
                sb.append("   ");
            }
            sb.append(coloredToken(color, GEM_LABEL.get(color) + ":" + (bank == null ? 0 : bank.getCount(color))));
        }
        return sb.toString();
    }

    /**
     * Formats the recent action log as a single row.
     *
     * @return formatted log row
     */
    private List<String> formatLogLines() {
        List<String> source = readOnlyLogEntries != null ? readOnlyLogEntries : actionLog;
        if (source == null || source.isEmpty()) {
            return List.of(DIM + WHITE + "No actions yet" + RESET);
        }
        String latest = source.get(source.size() - 1);
        return List.of(WHITE + latest + RESET);
    }

    /**
     * Formats and wraps gem stats so they fit within a sidebar card.
     *
     * @param stats stats to render
     * @param includeGold whether to include gold
     * @param width visible row width for wrapping
     * @return wrapped stat rows
     */
    private List<String> splitStatsLines(Map<GemColor, Integer> stats, boolean includeGold, int width) {
        List<String> tokens = new ArrayList<>();
        List<GemColor> order = includeGold ? BANK_ORDER : CARD_ORDER;
        for (GemColor color : order) {
            if (!includeGold && color == GemColor.GOLD) {
                continue;
            }
            int value = stats == null ? 0 : stats.getOrDefault(color, 0);
            tokens.add(coloredToken(color, GEM_LABEL.get(color) + ":" + value));
        }
        return wrapTokens(tokens, width);
    }

    /**
     * Wraps preformatted tokens into rows that fit within a target width.
     *
     * @param tokens tokens to wrap
     * @param width visible row width
     * @return wrapped rows
     */
    private List<String> wrapTokens(List<String> tokens, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String token : tokens) {
            String candidate = current.length() == 0 ? token : current + " " + token;
            if (vlen(candidate) > width && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(token);
            } else {
                if (current.length() > 0) {
                    current.append(" ");
                }
                current.append(token);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines.isEmpty() ? List.of("") : lines;
    }

    /**
     * Formats reserved cards as compact sidebar slot markers.
     *
     * @param reservedCards reserved cards owned by a player
     * @return formatted reserved slot string
     */
    private String formatReservedTokens(List<Card> reservedCards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (reservedCards != null && i < reservedCards.size()) {
                Card card = reservedCards.get(i);
                sb.append("[")
                        .append(GEM_ANSI.getOrDefault(card.getBonus(), WHITE))
                        .append(GEM_LABEL.getOrDefault(card.getBonus(), "?"))
                        .append(WHITE)
                        .append(card.getPoints())
                        .append(RESET)
                        .append("]");
            } else {
                sb.append(DIM).append("[ ]").append(RESET);
            }
        }
        return sb.toString();
    }

    /**
     * Chooses a sidebar accent color based on turn order.
     *
     * @param player player whose accent is needed
     * @return ANSI color code for that player
     */
    private String playerAccent(Player player) {
        int index = Math.floorMod(player.getTurnOrder(), 4);
        return switch (index) {
            case 0 -> RED;
            case 1 -> BLUE;
            case 2 -> GOLD;
            default -> PURPLE;
        };
    }

    /**
     * Wraps one token in the ANSI color associated with a gem color.
     *
     * @param color gem color driving the styling
     * @param text token text to color
     * @return colored token
     */
    private String coloredToken(GemColor color, String text) {
        return GEM_ANSI.getOrDefault(color, WHITE) + BOLD + text + RESET;
    }

    /**
     * Builds the top border of a boxed panel using the default border color.
     *
     * @param width visible panel width
     * @return formatted top border row
     */
    private String panelTop(int width) {
        return panelTop(width, WHITE);
    }

    /**
     * Builds the top border of a boxed panel.
     *
     * @param width visible panel width
     * @param borderColor ANSI color for the border
     * @return formatted top border row
     */
    private String panelTop(int width, String borderColor) {
        return borderColor + "┌" + rep('─', width - 2) + "┐" + RESET;
    }

    /**
     * Builds the bottom border of a boxed panel using the default color.
     *
     * @param width visible panel width
     * @return formatted bottom border row
     */
    private String panelBottom(int width) {
        return panelBottom(width, WHITE);
    }

    /**
     * Builds the bottom border of a boxed panel.
     *
     * @param width visible panel width
     * @param borderColor ANSI color for the border
     * @return formatted bottom border row
     */
    private String panelBottom(int width, String borderColor) {
        return borderColor + "└" + rep('─', width - 2) + "┘" + RESET;
    }

    /**
     * Builds a horizontal divider row for a boxed panel.
     *
     * @param width visible panel width
     * @param borderColor ANSI color for the divider
     * @return formatted divider row
     */
    private String panelDivider(int width, String borderColor) {
        return borderColor + "├" + rep('─', width - 2) + "┤" + RESET;
    }

    /**
     * Builds a panel header row using default colors.
     *
     * @param width visible panel width
     * @param title left-aligned title
     * @param rightTitle right-aligned title
     * @return formatted header row
     */
    private String panelHeader(int width, String title, String rightTitle) {
        return panelHeader(width, title, rightTitle, WHITE, BOLD + WHITE, DIM + WHITE);
    }

    /**
     * Builds a panel header row with caller-supplied colors.
     *
     * @param width visible panel width
     * @param title left-aligned title
     * @param rightTitle right-aligned title
     * @param borderColor ANSI border color
     * @param titleColor ANSI title color
     * @param rightColor ANSI right-title color
     * @return formatted header row
     */
    private String panelHeader(int width, String title, String rightTitle, String borderColor,
            String titleColor, String rightColor) {
        String left = titleColor + title + RESET;
        String right = rightTitle.isBlank() ? "" : rightColor + rightTitle + RESET;
        int gap = (width - 2) - vlen(left) - vlen(right);
        if (gap < 1) {
            gap = 1;
        }
        return borderColor + "│" + RESET + left + sp(gap) + right + borderColor + "│" + RESET;
    }

    /**
     * Builds one boxed panel body row using the default border color.
     *
     * @param width visible panel width
     * @param content row content
     * @return formatted body row
     */
    private String panelBody(int width, String content) {
        return panelBody(width, content, WHITE);
    }

    /**
     * Builds one boxed panel body row.
     *
     * @param width visible panel width
     * @param content row content
     * @param borderColor ANSI border color
     * @return formatted body row
     */
    private String panelBody(int width, String content, String borderColor) {
        int bodyWidth = width - 2;
        int pad = bodyWidth - vlen(content);
        if (pad < 0) {
            pad = 0;
        }
        return borderColor + "│" + RESET + content + sp(pad) + borderColor + "│" + RESET;
    }

    /**
     * Updates the action status line with a success message.
     *
     * @param msg success message to display
     */
    private void ok(String msg) {
        actionStatus = GREEN + BOLD + msg + RESET;
    }

    /**
     * Updates the action status line with an error message and pauses briefly.
     *
     * @param msg error message to display
     */
    private void err(String msg) {
        actionStatus = RED + BOLD + msg + RESET;
        sleep(1000);
    }

    /**
     * Returns the current action status line contents.
     *
     * @return formatted action status string
     */
    private String formatActionStatus() {
        return actionStatus;
    }

    /**
     * Formats an inline status line for the action panel.
     *
     * @param message status text to show
     * @param color ANSI color to apply
     * @return formatted status text
     */
    private String formatInlineStatus(String message, String color) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String safeColor = color == null ? WHITE : color;
        return safeColor + message + RESET;
    }

    /**
     * Truncates a string based on visible width rather than ANSI length.
     *
     * @param text text to truncate
     * @param maxVisibleChars maximum visible characters allowed
     * @return original or truncated text
     */
    private String truncateVisible(String text, int maxVisibleChars) {
        if (vlen(text) <= maxVisibleChars) {
            return text;
        }
        return text.substring(0, Math.max(0, maxVisibleChars - 3)) + "...";
    }

    /**
     * Re-renders the board with a temporary inline status prompt and reads input.
     *
     * @param state current game state
     * @param message prompt text to show in the status row
     * @return trimmed user input entered for that prompt
     */
    private String promptActionStatus(GameState state, String message) {
        String visibleMessage = truncateVisible(message, MAIN_WIDTH - 2);
        actionStatus = RED + visibleMessage + RESET;
        clearScreen();
        render(state);
        System.out.print("\u001B[" + statusLinesFromBottom + "A\r\u001B[" + (4 + vlen(visibleMessage)) + "C\u001B[?25h");
        String input = scanner.nextLine().trim();
        System.out.print("\u001B[?25l");
        return input;
    }

    /**
     * Clears the terminal and moves the cursor to the home position.
     */
    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    /**
     * Sleeps for a short UI delay while preserving interruption status.
     *
     * @param ms sleep duration in milliseconds
     */
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

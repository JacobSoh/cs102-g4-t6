package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.cs102.g04t06.game.exception.NobleNotAvailableException;
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
    private static final int ACTION_CURSOR_OFFSET = 5 + ACTION_PROMPT.length(); // "  ║ " + prompt + 1 space
    private static final int MAX_LOG_ENTRIES = 2;
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
    private final List<String> actionLog = new ArrayList<>();
    private final GameRules rules = new GameRules();
    private final InputHandler inputHandler = new InputHandler();
    private int actionLinesFromBottom = 1;
    private int statusLinesFromBottom = 1;
    private String actionStatus = "";

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

    public GameBoardUI() {
        this(new Scanner(System.in));
    }

    GameBoardUI(Scanner scanner) {
        this.scanner = scanner;
    }

    // -------------------------------------------------------------------------
    // Public entry points
    // -------------------------------------------------------------------------
    public void show(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("GameState must not be null");
        }

        while (true) {
            clearScreen();
            render(state);
            if (state.isGameOver()) {
                break;
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
        clearScreen();
        render(state);
    }

    // -------------------------------------------------------------------------
    // Master render
    // -------------------------------------------------------------------------
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
    private void boardTop() {
        System.out.println(WHITE + "  ╔" + rep('═', FILL) + "╗" + RESET);
    }

    private void boardBottom() {
        System.out.print(WHITE + "  ╚" + rep('═', FILL) + "╝" + RESET);
    }

    /** One content line padded to INNER visible width. */
    private void line(String content) {
        int pad = INNER - vlen(content);
        if (pad < 0) {
            pad = 0;
        }
        System.out.println(WHITE + "  ║ " + RESET
                + content + sp(pad)
                + WHITE + " ║" + RESET);
    }

    private void blank() {
        line("");
    }

    private void divider() {
        System.out.println(WHITE + "  ╠" + rep('═', FILL) + "╣" + RESET);
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------
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
    private void printLog() {
        StringBuilder sb = new StringBuilder(DIM + WHITE + "LOG:  " + RESET);
        if (actionLog.isEmpty()) {
            sb.append(DIM).append(WHITE).append("No actions yet").append(RESET);
            line(sb.toString());
            return;
        }

        int start = Math.max(0, actionLog.size() - MAX_LOG_ENTRIES);
        List<String> recent = actionLog.subList(start, actionLog.size());

        for (int i = 0; i < recent.size(); i++) {
            sb.append(WHITE).append(recent.get(i)).append(RESET);
            if (i < recent.size() - 1) {
                sb.append(DIM).append(WHITE).append("   |   ").append(RESET);
            }
        }
        line(sb.toString());
    }

    // -------------------------------------------------------------------------
    // ACTION line + prompt
    // -------------------------------------------------------------------------
    private void printActionLine() {
        line(DIM + WHITE + "  ┌ Available Actions ────────────────────────────────────────────────┐" + RESET);
        line(DIM + WHITE + "  . take w r u  : take 3 diff gems    . buy t1 slot1 : buy visible card" + RESET);
        line(DIM + WHITE + "  . take w w    : take 2 same gems    . buy reserve  : buy reserve card" + RESET);
        line(DIM + WHITE + "  . reserve t1 slot1 : reserve + gold      . pass         : skip turn" + RESET);
        line(DIM + WHITE + "  └───────────────────────────────────────────────────────────────────┘" + RESET);
        line(GREEN + BOLD + ACTION_PROMPT + RESET);
    }

    private String promptAction() {
        // Move cursor into the ACTION row inside the board:
        System.out.print("\u001B[" + actionLinesFromBottom + "A\r\u001B[" + ACTION_CURSOR_OFFSET + "C\u001B[?25h");
        return scanner.nextLine().trim();
    }

    // -------------------------------------------------------------------------
    // Action dispatch
    // -------------------------------------------------------------------------
    private void handleAction(GameState state, String input) {
        String s = input.toLowerCase().trim();
        if (s.startsWith("take")) {
            handleTake(state, s);
        } else if (s.startsWith("buy")) {
            handleBuy(state, s);
        } else if (s.startsWith("reserve")) {
            handleReserve(state, s);
        } else if (s.equals("pass")) {
            handlePass(state);
        } else {
            err("Unknown command. Try: take, buy, reserve, pass, q");
        }
    }

    private void handleTake(GameState state, String s) {
        String payloadRaw = s.replaceFirst("^take", "").trim();
        if (payloadRaw.isEmpty()) {
            err("Usage: take <gem> [gem] [gem]   e.g.  take w r u");
            return;
        }

        List<GemColor> colors;
        try {
            colors = inputHandler.parseGemSequence(payloadRaw);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

        Player player = state.getCurrentPlayer();
        GemCollection bank = state.getGemBank();

        if (colors.size() == 3) {
            GemCollection requested;
            try {
                requested = inputHandler.promptGemSelection(3, colors);
            } catch (IllegalArgumentException e) {
                err(e.getMessage());
                return;
            }
            if (!rules.canTakeThreeDifferentGems(requested, bank)) {
                err("Invalid take: must be 3 different colors available in bank.");
                return;
            }

            applyTake(state, player, requested, "Took");

        } else if (colors.size() == 2) {
            if (colors.get(0) != colors.get(1)) {
                err("Invalid take: for 2 gems, both must be the same color.");
                return;
            }

            GemColor color = colors.get(0);
            if (!rules.canTakeTwoSameGems(color, bank)) {
                err("Invalid take: bank must have at least 4 of that color.");
                return;
            }

            GemCollection requested = new GemCollection().add(color, 2);
            applyTake(state, player, requested, "Took");

        } else {
            err("Invalid take: choose either 3 different gems or 2 of the same color.");
        }
    }

    private void handleBuy(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            err("Usage: buy <tier> <slot> or buy reserve <slot>");
            return;
        }

        Player player = state.getCurrentPlayer();
        boolean fromReserved;
        String purchaseLabel;
        Card card;

        try {
            if (isReservedBuyToken(p[1])) {
                int reservedSlotIndex = inputHandler.parseSlotToken(p[2]);
                if (reservedSlotIndex >= player.getReservedCards().size()) {
                    err("Reserved card does not exist at that slot.");
                    return;
                }
                int reservedSelection = state.getMarket().getVisibleCards(1).size() + reservedSlotIndex + 1;
                card = inputHandler.promptCardSelection(
                        state.getMarket(),
                        1,
                        true,
                        player.getReservedCards(),
                        reservedSelection);
                fromReserved = true;
                purchaseLabel = "reserved slot" + (reservedSlotIndex + 1);
            } else {
                int tier = inputHandler.parseTierToken(p[1]);
                int slotIndex = inputHandler.parseSlotToken(p[2]);
                card = inputHandler.promptCardSelection(
                        state.getMarket(),
                        tier,
                        false,
                        player.getReservedCards(),
                        slotIndex + 1);
                fromReserved = false;
                purchaseLabel = "t" + tier + " slot" + (slotIndex + 1);
            }
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

        if (!rules.canAffordCard(player, card)) {
            err("Cannot afford that card.");
            return;
        }

        GemCollection payment = buildPayment(player, rules.calculateActualCost(player, card));
        try {
            player.deductGems(payment);
            state.addGemsToBank(payment);
        } catch (IllegalStateException e) {
            err(e.getMessage());
            return;
        }

        player.addCard(card);
        if (!fromReserved) {
            state.getMarket().removeCard(card);
        }

        actionLog.add(player.getName() + " bought " + purchaseLabel);
        ok("Bought " + purchaseLabel);

        handleEndOfTurnNobleClaim(state, player);
        handleWinCheck(state, player);
        advanceTurn(state);
        handleGameEnd(state);
        sleep(800);
    }

    private void handleReserve(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            err("Usage: reserve <tier> <slot>   e.g.  reserve t1 slot3");
            return;
        }

        int tier;
        int slotIndex;
        try {
            tier = inputHandler.parseTierToken(p[1]);
            slotIndex = inputHandler.parseSlotToken(p[2]);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

        Player player = state.getCurrentPlayer();
        if (!rules.canReserveCard(player)) {
            err("You already have 3 reserved cards.");
            return;
        }

        Card card;
        try {
            card = state.getMarket().getVisibleCard(tier, slotIndex);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

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

    private boolean isReservedBuyToken(String token) {
        return "reserved".equalsIgnoreCase(token)
                || "reserve".equalsIgnoreCase(token)
                || "r".equalsIgnoreCase(token);
    }

    private void applyTake(GameState state, Player player, GemCollection requested, String verb) {
        try {
            state.removeGemsFromBank(requested);
            player.addGems(requested);
        } catch (IllegalArgumentException e) {
            err(e.getMessage());
            return;
        }

        actionLog.add(player.getName() + " took " + formatGemShort(requested));
        ok(verb + ": " + formatGemShort(requested));

        if (rules.mustReturnGems(player)) {
            handleReturnExcessGems(state, player);
        }

        handleEndOfTurnNobleClaim(state, player);
        advanceTurn(state);
        handleGameEnd(state);
        sleep(800);
    }

    private void handleReturnExcessGems(GameState state, Player player) {
        int excess = player.getGemCount() - 10;
        String promptMessage = "Return " + excess + " gem(s) [e.g. wr, uu]: ";
        while (excess > 0) {
            String input = promptActionStatus(state, promptMessage);

            List<GemColor> toReturnColors;
            try {
                toReturnColors = inputHandler.parseGemSequence(input);
            } catch (IllegalArgumentException e) {
                promptMessage = "Return " + excess + " gem(s) [invalid input, try again]: ";
                continue;
            }

            try {
                GemCollection toReturn = inputHandler.promptGemsToReturn(player, excess, toReturnColors);
                player.deductGems(toReturn);
                state.addGemsToBank(toReturn);
                actionLog.add(player.getName() + " returned " + formatGemShort(toReturn));
            } catch (IllegalArgumentException | IllegalStateException e) {
                promptMessage = "Return " + excess + " gem(s) [invalid input, try again]: ";
                continue;
            }

            excess = player.getGemCount() - 10;
            promptMessage = "Return " + excess + " gem(s) [e.g. wr, uu]: ";
        }
        actionStatus = "";
    }

    private void handleNobleClaim(GameState state, Player player) {
        List<Noble> claimable = rules.getClaimableNobles(player, state.getAvailableNobles());
        if (claimable.isEmpty()) {
            return;
        }

        Noble chosen = claimable.get(0);
        if (claimable.size() > 1) {
            System.out.println();
            System.out.println(WHITE + "Choose a noble to claim:" + RESET);
            for (int i = 0; i < claimable.size(); i++) {
                Noble n = claimable.get(i);
                System.out.println("  " + (i + 1) + ". " + n.getName() + " (" + n.getPoints() + " pts)");
            }
            System.out.print(GREEN + "  > " + RESET);
            String input = scanner.nextLine().trim();
            int idx;
            try {
                idx = Integer.parseInt(input) - 1;
            } catch (NumberFormatException e) {
                idx = 0;
            }
            if (idx >= 0 && idx < claimable.size()) {
                chosen = claimable.get(idx);
            }
        }

        try {
            Noble claimed = state.removeNoble(chosen);
            player.claimNoble(claimed);
            actionLog.add(player.getName() + " claimed noble " + claimed.getName());
            ok("Claimed noble: " + claimed.getName());
        } catch (NobleNotAvailableException e) {
            err(e.getMessage());
        }
    }

    private void handleEndOfTurnNobleClaim(GameState state, Player player) {
        int claimedBefore = player.getClaimedNobles().size();
        handleNobleClaim(state, player);
        if (player.getClaimedNobles().size() > claimedBefore) {
            handleWinCheck(state, player);
        }
    }

    private void handleWinCheck(GameState state, Player player) {
        if (!state.isFinalRoundTriggered() && rules.hasPlayerWon(player, state.getWinningThreshold())) {
            state.triggerFinalRound();
            actionLog.add(player.getName() + " reached " + player.getPoints() + " points. Final round triggered.");
            ok(player.getName() + " reached " + player.getPoints() + " points. Final round triggered.");
        }
    }

    private void handleGameEnd(GameState state) {
        if (!state.isGameOver()) {
            return;
        }

        Player winner = rules.getWinner(state.getPlayers(), state.getWinningThreshold());
        if (winner == null) {
            ok("Game over!");
            return;
        }

        actionLog.add("Final round complete. Winner: " + winner.getName() + " with " + winner.getPoints() + " points.");
        ok("Game over! " + winner.getName() + " wins with " + winner.getPoints() + " points.");
    }

    private void advanceTurn(GameState state) {
        state.advanceToNextPlayer();
    }

    private GemCollection buildPayment(Player player, GemCollection actualCost) {
        Map<GemColor, Integer> pay = new EnumMap<>(GemColor.class);
        int goldNeeded = 0;

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            int need = actualCost.getCount(color);
            int have = player.getGems().getCount(color);
            int payColor = Math.min(have, need);
            pay.put(color, payColor);
            goldNeeded += Math.max(0, need - payColor);
        }

        if (goldNeeded > 0) {
            pay.put(GemColor.GOLD, goldNeeded);
        }

        return new GemCollection(pay);
    }

    private String formatGemShort(GemCollection gems) {
        StringBuilder sb = new StringBuilder();
        for (GemColor color : GemColor.values()) {
            int count = gems.getCount(color);
            if (count > 0) {
                sb.append(gemCodeLower(color)).append(count).append(" ");
            }
        }
        return sb.toString().trim();
    }

    // -------------------------------------------------------------------------
    // Rendering utilities
    // -------------------------------------------------------------------------

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
        appendMarketPanel(lines, "LOG", "", List.of(formatLogLine()));

        List<String> actionBody = List.of(
                DIM + WHITE + ". take w r u  : take 3 diff gems    . buy t1 slot1 : buy visible card" + RESET,
                DIM + WHITE + ". take w w    : take 2 same gems    . buy reserve 1: buy reserve card" + RESET,
                DIM + WHITE + ". reserve t1 slot1 : reserve + gold      . pass         : skip turn" + RESET,
                "",
                GREEN + BOLD + ACTION_PROMPT + RESET,
                formatActionStatus()
        );
        int actionLineIndex = lines.size() + 2 + 4;
        int statusLineIndex = lines.size() + 2 + 5;
        appendMarketPanel(lines, "ACTIONS", "", actionBody);
        return new MainAreaRender(lines, actionLineIndex, statusLineIndex);
    }

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

    private void appendMarketPanel(List<String> lines, String title, String rightTitle, List<String> body) {
        lines.add(panelTop(MAIN_WIDTH));
        lines.add(panelHeader(MAIN_WIDTH, title, rightTitle));
        for (String row : body) {
            lines.add(panelBody(MAIN_WIDTH, row));
        }
        lines.add(panelBottom(MAIN_WIDTH));
    }

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

    private String formatLogLine() {
        if (actionLog.isEmpty()) {
            return DIM + WHITE + "No actions yet" + RESET;
        }

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, actionLog.size() - MAX_LOG_ENTRIES);
        List<String> recent = actionLog.subList(start, actionLog.size());
        for (int i = 0; i < recent.size(); i++) {
            if (i > 0) {
                sb.append(DIM).append(WHITE).append("   |   ").append(RESET);
            }
            sb.append(WHITE).append(recent.get(i)).append(RESET);
        }
        return sb.toString();
    }

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

    private String playerAccent(Player player) {
        int index = Math.floorMod(player.getTurnOrder(), 4);
        return switch (index) {
            case 0 -> RED;
            case 1 -> BLUE;
            case 2 -> GOLD;
            default -> PURPLE;
        };
    }

    private String coloredToken(GemColor color, String text) {
        return GEM_ANSI.getOrDefault(color, WHITE) + BOLD + text + RESET;
    }

    private String panelTop(int width) {
        return panelTop(width, WHITE);
    }

    private String panelTop(int width, String borderColor) {
        return borderColor + "┌" + rep('─', width - 2) + "┐" + RESET;
    }

    private String panelBottom(int width) {
        return panelBottom(width, WHITE);
    }

    private String panelBottom(int width, String borderColor) {
        return borderColor + "└" + rep('─', width - 2) + "┘" + RESET;
    }

    private String panelDivider(int width, String borderColor) {
        return borderColor + "├" + rep('─', width - 2) + "┤" + RESET;
    }

    private String panelHeader(int width, String title, String rightTitle) {
        return panelHeader(width, title, rightTitle, WHITE, BOLD + WHITE, DIM + WHITE);
    }

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

    private String panelBody(int width, String content) {
        return panelBody(width, content, WHITE);
    }

    private String panelBody(int width, String content, String borderColor) {
        int bodyWidth = width - 2;
        int pad = bodyWidth - vlen(content);
        if (pad < 0) {
            pad = 0;
        }
        return borderColor + "│" + RESET + content + sp(pad) + borderColor + "│" + RESET;
    }

    private void ok(String msg) {
        actionStatus = GREEN + BOLD + msg + RESET;
    }

    private void err(String msg) {
        actionStatus = RED + BOLD + msg + RESET;
        sleep(1000);
    }

    private String formatActionStatus() {
        return actionStatus;
    }

    private String truncateVisible(String text, int maxVisibleChars) {
        if (vlen(text) <= maxVisibleChars) {
            return text;
        }
        return text.substring(0, Math.max(0, maxVisibleChars - 3)) + "...";
    }

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

    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

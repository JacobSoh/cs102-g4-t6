package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
 * ANSI codes use \033 (octal 033 = decimal 27 = ESC), the most
 * reliable escape literal in Java string constants.
 */
public class GameBoardUI {

    // -------------------------------------------------------------------------
    // ANSI Colour Codes — all use \033 (octal ESC)
    // -------------------------------------------------------------------------
    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";
    private static final String GOLD   = "\033[38;5;220m";
    private static final String WHITE  = "\033[37m";
    private static final String GREEN  = "\033[32m";
    private static final String BLUE   = "\033[34m";
    private static final String RED    = "\033[31m";
    private static final String CYAN   = "\033[36m";
    private static final String PURPLE = "\033[35m";
    private static final String GREY   = "\033[90m";   // bright-black for K (black gems)

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
    private static final int INNER = 84;   // visible width between ║ borders
    private static final int FILL  = 86;   // ═ repeat count  (INNER + 2)

    // Tile inner widths (visible chars between ┌/┐)
    private static final int NOBLE_INNER = 16;   // ┌ + 16×─ + ┐  = 18 wide
    private static final int CARD_INNER  = 12;   // ┌ + 12×─ + ┐  = 14 wide

    // -------------------------------------------------------------------------
    // Scanner + UI-local session state
    // -------------------------------------------------------------------------
    private final Scanner scanner;
    private final List<String> actionLog = new ArrayList<>();
    private int roundNumber = 1;

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
            String input = promptAction();
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
        boardTop();
        printHeader();
        printNobles(state.getAvailableNobles());
        printTier("TIER 3", state.getMarket().getVisibleCards(3), state.getMarket().getDeckSize(3));
        printTier("TIER 2", state.getMarket().getVisibleCards(2), state.getMarket().getDeckSize(2));
        printTier("TIER 1", state.getMarket().getVisibleCards(1), state.getMarket().getDeckSize(1));
        printBank(state.getGemBank());
        printPlayers(state.getPlayers(), state.getCurrentPlayer());
        printLog();
        printActionLine();
        boardBottom();
    }

    // -------------------------------------------------------------------------
    // Board frame
    // -------------------------------------------------------------------------
    private void boardTop() {
        System.out.println(WHITE + "  ╔" + rep('═', FILL) + "╗" + RESET);
    }

    private void boardBottom() {
        System.out.println(WHITE + "  ╚" + rep('═', FILL) + "╝" + RESET);
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
    private void printHeader() {
        String left  = GOLD + BOLD + "SPLENDOR" + RESET;
        String mid   = WHITE + "Round " + roundNumber + RESET;
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

        int start = Math.max(0, actionLog.size() - 3);
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
        line(GREEN + BOLD + "ACTION >" + RESET);
    }

    private String promptAction() {
        System.out.print(WHITE + "  ║ " + RESET + GREEN + BOLD + "  > " + RESET);
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
        String[] p = s.split("\\s+");
        if (p.length < 2) {
            err("Usage: take <gem> [gem] [gem]   e.g.  take w r u");
            return;
        }

        String payload = String.join(" ", Arrays.copyOfRange(p, 1, p.length)).toUpperCase();
        actionLog.add(state.getCurrentPlayer().getName() + " requested take " + payload);
        ok("Took: " + payload + "  (pending rules integration)");
        sleep(800);
    }

    private void handleBuy(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            err("Usage: buy <tier> <slot>   e.g.  buy t2 slot1");
            return;
        }

        actionLog.add(state.getCurrentPlayer().getName() + " requested buy " + p[1] + " " + p[2]);
        ok("Bought " + p[1] + " " + p[2] + "  (pending rules integration)");
        sleep(800);
    }

    private void handleReserve(GameState state, String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) {
            err("Usage: reserve <tier> <slot>   e.g.  reserve t1 slot3");
            return;
        }

        actionLog.add(state.getCurrentPlayer().getName() + " requested reserve " + p[1] + " " + p[2]);
        ok("Reserved " + p[1] + " " + p[2] + "  (pending rules integration)");
        sleep(800);
    }

    private void handlePass(GameState state) {
        String current = state.getCurrentPlayer().getName();
        state.advanceToNextPlayer();
        if (state.getCurrentPlayerIndex() == 0) {
            roundNumber++;
        }
        actionLog.add(current + " passed. Current: " + state.getCurrentPlayer().getName());
        ok("Turn passed.");
        sleep(600);
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
     * Pattern: ESC (0x1B = \033) followed by [ then digits/semicolons then m.
     */
    private int vlen(String s) {
        return s.replaceAll("\033\\[[;\\d]*m", "").length();
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

    private void ok(String msg) {
        System.out.println("\n" + GREEN + "  ✔  " + msg + RESET);
    }

    private void err(String msg) {
        System.out.println("\n" + RED + "  ✖  " + msg + RESET);
        sleep(1000);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
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

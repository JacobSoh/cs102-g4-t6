package edu.cs102.g04t06.game.presentation.console;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.cs102.g04t06.game.rules.entities.GemColor;

/**
 * GameBoardUI
 *
 * Renders the full Splendor game board in the console.
 *
 * ANSI codes use \033 (octal 033 = decimal 27 = ESC), the most
 * reliable escape literal in Java string constants.
 *
 * Every section that needs to be wired to real GameState
 * is marked with a  // TODO: HOOK  comment.
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
    // Lightweight mock data  (replace with real GameState later)
    // -------------------------------------------------------------------------

    private static class MockCard {
        final String label, ansi, cost;
        final int    points;
        MockCard(String label, String ansi, int points, String cost) {
            this.label = label; this.ansi = ansi;
            this.points = points; this.cost = cost;
        }
    }

    private static class MockNoble {
        final int    points;
        final String req;
        MockNoble(int points, String req) { this.points = points; this.req = req; }
    }

    private static class MockPlayer {
        final String  name, cardStats, gemStats;
        final int     points, totalGems, reservedCount;
        final boolean isCurrent;
        MockPlayer(String name, int points, boolean isCurrent,
                   String cardStats, String gemStats,
                   int totalGems, int reserved) {
            this.name = name; this.points = points; this.isCurrent = isCurrent;
            this.cardStats = cardStats; this.gemStats = gemStats;
            this.totalGems = totalGems; this.reservedCount = reserved;
        }
    }

    // TODO: HOOK — replace all mock* fields with a GameState parameter

    private int mockRound = 5;

    private final List<MockNoble> mockNobles = Arrays.asList(
        new MockNoble(3, "u:3 g:3"),
        new MockNoble(3, "r:4 k:4"),
        new MockNoble(3, "w:3 r:3")
    );

    private final List<MockCard> tier3 = Arrays.asList(
        new MockCard("K", GREY,  5, "w3r3u3"),
        new MockCard("R", RED,   4, "w2u4k3"),
        new MockCard("U", BLUE,  4, "r3g3k3"),
        new MockCard("G", GREEN, 5, "w4r2k4")
    );
    private final int deck3 = 12;

    private final List<MockCard> tier2 = Arrays.asList(
        new MockCard("G", GREEN, 2, "u2g1k2"),
        new MockCard("W", WHITE, 3, "r2g2k3"),
        new MockCard("R", RED,   2, "w1u2g2"),
        new MockCard("U", BLUE,  3, "r3k2g1")
    );
    private final int deck2 = 18;

    private final List<MockCard> tier1 = Arrays.asList(
        new MockCard("K", GREY,  0, "r1g2k1"),
        new MockCard("W", WHITE, 1, "u1g1k2"),
        new MockCard("U", BLUE,  0, "r2u2"),
        new MockCard("R", RED,   0, "w1k2")
    );
    private final int deck1 = 32;

    private final Map<GemColor, Integer> bank = new LinkedHashMap<>();
    {
        bank.put(GemColor.WHITE, 3);
        bank.put(GemColor.RED,   2);
        bank.put(GemColor.BLUE,  4);
        bank.put(GemColor.GREEN, 1);
        bank.put(GemColor.BLACK, 3);
        bank.put(GemColor.GOLD,  2);
    }

    private final List<MockPlayer> players = Arrays.asList(
        new MockPlayer("ALICE", 9,  true,
            "W:2 R:1 U:3 G:0 K:2", "W:1 R:2 U:0 G:3 K:1 *:1", 8, 0),
        new MockPlayer("BOB",   4,  false,
            "W:0 R:3 U:1 G:1 K:0", "7", 7, 1),
        new MockPlayer("CAROL", 12, false,
            "W:2 R:2 U:2 G:2 K:1", "5", 5, 0)
    );

    private final List<String> log = Arrays.asList(
        "Bob took R U G",
        "Carol bought T2-slot2",
        "Alice took W W"
    );

    // -------------------------------------------------------------------------
    // Scanner
    // -------------------------------------------------------------------------
    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Public entry point
    // TODO: HOOK — accept GameState: public void show(GameState state)
    // -------------------------------------------------------------------------
    public void show() {
        while (true) {
            clearScreen();
            render();
            String input = promptAction();
            if (input.equalsIgnoreCase("q")) break;
            handleAction(input);
        }
    }

    // -------------------------------------------------------------------------
    // Master render
    // -------------------------------------------------------------------------
    private void render() {
        boardTop();
        printHeader();
        printNobles();
        printTier("TIER 3", tier3, deck3);
        printTier("TIER 2", tier2, deck2);
        printTier("TIER 1", tier1, deck1);
        printBank();
        printPlayers();
        printLog();
        printActionLine();
        boardBottom();
    }

    // -------------------------------------------------------------------------
    // Board frame
    // -------------------------------------------------------------------------
    private void boardTop() {
        System.out.println(WHITE + "  \u2554" + rep('\u2550', FILL) + "\u2557" + RESET);
    }

    private void boardBottom() {
        System.out.println(WHITE + "  \u255a" + rep('\u2550', FILL) + "\u255d" + RESET);
    }

    /** One content line padded to INNER visible width. */
    private void line(String content) {
        int pad = INNER - vlen(content);
        if (pad < 0) pad = 0;
        System.out.println(WHITE + "  \u2551 " + RESET
                + content + sp(pad)
                + WHITE + " \u2551" + RESET);
    }

    private void blank()   { line(""); }
    private void divider() {
        System.out.println(WHITE + "  \u2560" + rep('\u2550', FILL) + "\u2563" + RESET);
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------
    private void printHeader() {
        // TODO: HOOK — replace mockRound with state.getRoundNumber()
        String left  = GOLD + BOLD + "SPLENDOR" + RESET;
        String mid   = WHITE + "Round " + mockRound + RESET;
        String right = DIM + WHITE + "[?] Help    [Q] Quit" + RESET;

        int g1 = (INNER / 2) - vlen(left) - vlen(mid) / 2;
        int g2 = INNER - vlen(left) - vlen(mid) - vlen(right) - g1;
        if (g1 < 1) g1 = 1;
        if (g2 < 1) g2 = 1;
        line(left + sp(g1) + mid + sp(g2) + right);
        divider();
    }

    // -------------------------------------------------------------------------
    // Nobles
    // -------------------------------------------------------------------------
    private void printNobles() {
        // TODO: HOOK — replace mockNobles with state.getAvailableNobles()
        line(PURPLE + BOLD + "NOBLES" + RESET);

        // Build each row across all noble tiles
        String[] topRow = new String[mockNobles.size()];
        String[] ptRow  = new String[mockNobles.size()];
        String[] rqRow  = new String[mockNobles.size()];
        String[] botRow = new String[mockNobles.size()];

        for (int i = 0; i < mockNobles.size(); i++) {
            MockNoble n = mockNobles.get(i);
            String border = WHITE;
            String dash   = rep('\u2500', NOBLE_INNER);

            topRow[i] = border + "\u250c" + dash + "\u2510" + RESET;
            botRow[i] = border + "\u2514" + dash + "\u2518" + RESET;

            // points:  │ ★ 3              │
            String pContent = " " + GOLD + "\u2605 " + n.points + RESET;
            ptRow[i] = WHITE + "\u2502" + RESET
                     + padTo(pContent, NOBLE_INNER)
                     + WHITE + "\u2502" + RESET;

            // requirements — colour each token individually
            String rContent = " " + colorReq(n.req);
            rqRow[i] = WHITE + "\u2502" + RESET
                     + padTo(rContent, NOBLE_INNER)
                     + WHITE + "\u2502" + RESET;
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
    private void printTier(String label, List<MockCard> cards, int deckCount) {
        // TODO: HOOK — replace cards/deckCount with CardMarket tier data

        String hL = BOLD + WHITE + label + RESET;
        String hR = DIM + WHITE + "DECK: " + RESET + BOLD + WHITE + deckCount + RESET;
        int gap = INNER - vlen(hL) - vlen(hR);
        if (gap < 1) gap = 1;
        line(hL + sp(gap) + hR);

        String[] topRow   = new String[cards.size()];
        String[] labelRow = new String[cards.size()];
        String[] costRow  = new String[cards.size()];
        String[] botRow   = new String[cards.size()];

        for (int i = 0; i < cards.size(); i++) {
            MockCard c    = cards.get(i);
            String border = WHITE;
            String dash   = rep('\u2500', CARD_INNER);

            topRow[i] = border + "\u250c" + dash + "\u2510" + RESET;
            botRow[i] = border + "\u2514" + dash + "\u2518" + RESET;

            // colour label + points:  "K  5"
            String lContent = " " + c.ansi + BOLD + c.label + RESET
                            + "  " + WHITE + c.points + RESET;
            labelRow[i] = WHITE + "\u2502" + RESET
                        + padTo(lContent, CARD_INNER)
                        + WHITE + "\u2502" + RESET;

            // cost string:  "w3r3u3"
            String cContent = " " + DIM + WHITE + c.cost + RESET;
            costRow[i] = WHITE + "\u2502" + RESET
                       + padTo(cContent, CARD_INNER)
                       + WHITE + "\u2502" + RESET;
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
    private void printBank() {
        // TODO: HOOK — replace bank with state.getGemBank()
        line(BOLD + WHITE + "BANK GEMS:" + RESET);

        StringBuilder sb = new StringBuilder("  ");
        for (Map.Entry<GemColor, Integer> e : bank.entrySet()) {
            String a = GEM_ANSI.getOrDefault(e.getKey(), WHITE);
            String l = GEM_LABEL.getOrDefault(e.getKey(), "?");
            sb.append(a).append(BOLD).append(l).append(":").append(e.getValue())
              .append(RESET).append("   ");
        }
        line(sb.toString());
        divider();
    }

    // -------------------------------------------------------------------------
    // Player panels
    // -------------------------------------------------------------------------
    private void printPlayers() {
        // TODO: HOOK — replace players with state.getPlayers()
        for (MockPlayer p : players) {
            if (p.isCurrent) printCurrentPlayer(p);
            else             printOtherPlayer(p);
        }
        divider();
    }

    private void printCurrentPlayer(MockPlayer p) {
        String ind   = GREEN + BOLD + "\u25ba " + RESET;
        String name  = CYAN  + BOLD + p.name + " (you)" + RESET;
        String pts   = GOLD  + BOLD + p.points + " pts" + RESET;
        int gap = INNER - vlen(ind) - vlen(name) - vlen(pts);
        if (gap < 2) gap = 2;
        line(ind + name + sp(gap) + pts);

        line(WHITE + "  Cards :  " + RESET
                + colorStats(p.cardStats)
                + WHITE + "   Reserved: " + RESET
                + reservedSlots(p.reservedCount));

        line(WHITE + "  Gems  :  " + RESET
                + colorStats(p.gemStats)
                + DIM + WHITE + "   (Total: " + p.totalGems + ")" + RESET);
    }

    private void printOtherPlayer(MockPlayer p) {
        line(RED + BOLD + p.name + RESET
                + "  " + BOLD + WHITE + p.points + " pts" + RESET
                + DIM + WHITE
                + "   Cards: " + p.cardStats
                + "   Gems: "  + p.gemStats + " total"
                + "   Reserved: " + p.reservedCount
                + RESET);
    }

    // -------------------------------------------------------------------------
    // Log
    // -------------------------------------------------------------------------
    private void printLog() {
        // TODO: HOOK — replace log with real game event log
        StringBuilder sb = new StringBuilder(DIM + WHITE + "LOG:  " + RESET);
        for (int i = 0; i < log.size(); i++) {
            sb.append(WHITE).append(log.get(i)).append(RESET);
            if (i < log.size() - 1)
                sb.append(DIM).append(WHITE).append("   |   ").append(RESET);
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
        System.out.print(WHITE + "  \u2551 " + RESET + GREEN + BOLD + "  > " + RESET);
        return scanner.nextLine().trim();
    }

    // -------------------------------------------------------------------------
    // Action dispatch
    // TODO: HOOK — wire each handler to GameRules / GameState
    // -------------------------------------------------------------------------
    private void handleAction(String input) {
        String s = input.toLowerCase().trim();
        if      (s.startsWith("take"))    handleTake(s);
        else if (s.startsWith("buy"))     handleBuy(s);
        else if (s.startsWith("reserve")) handleReserve(s);
        else if (s.equals("pass"))        handlePass();
        else                              err("Unknown command. Try: take, buy, reserve, pass, q");
    }

    private void handleTake(String s) {
        String[] p = s.split("\\s+");
        if (p.length < 2) { err("Usage: take <gem> [gem] [gem]   e.g.  take w r u"); return; }
        ok("Took: " + String.join(" ", Arrays.copyOfRange(p, 1, p.length)).toUpperCase() + "  (stub)");
        sleep(1200);
    }

    private void handleBuy(String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) { err("Usage: buy <tier> <slot>   e.g.  buy t2 slot1"); return; }
        ok("Bought " + p[1] + " " + p[2] + "  (stub)");
        sleep(1200);
    }

    private void handleReserve(String s) {
        String[] p = s.split("\\s+");
        if (p.length < 3) { err("Usage: reserve <tier> <slot>   e.g.  reserve t1 slot3"); return; }
        ok("Reserved " + p[1] + " " + p[2] + "  (stub)");
        sleep(1200);
    }

    private void handlePass() {
        ok("Turn passed.  (stub)");
        sleep(1200);
    }

    // -------------------------------------------------------------------------
    // Rendering utilities
    // -------------------------------------------------------------------------

    /**
     * Colours a stats string like "W:2 R:1 U:3" token by token.
     * Each token is coloured by its first letter.
     */
    private String colorStats(String stats) {
        if (stats == null || stats.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String t : stats.split("\\s+")) {
            if (t.isEmpty()) continue;
            if (!first) sb.append("  ");
            sb.append(gemAnsi(t.charAt(0))).append(BOLD).append(t).append(RESET);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Colours a noble requirement string like "u:3 g:3".
     * Each token is coloured by its first letter (lowercase gem code).
     */
    private String colorReq(String req) {
        StringBuilder sb = new StringBuilder();
        for (String t : req.split("\\s+")) {
            sb.append(gemAnsiLower(t.charAt(0))).append(t).append(RESET).append(" ");
        }
        // trim trailing space
        String result = sb.toString();
        return result.endsWith(" ") ? result.substring(0, result.length() - 1) : result;
    }

    /** Maps uppercase gem stat letter (W/R/U/G/K/*) to ANSI colour. */
    private String gemAnsi(char c) {
        switch (c) {
            case 'W': return WHITE;
            case 'R': return RED;
            case 'U': return BLUE;
            case 'G': return GREEN;
            case 'K': return GREY;
            case '*': return GOLD;
            default:  return WHITE;
        }
    }

    /** Maps lowercase noble req letter (w/r/u/g/k) to ANSI colour. */
    private String gemAnsiLower(char c) {
        switch (c) {
            case 'w': return WHITE;
            case 'r': return RED;
            case 'u': return BLUE;
            case 'g': return GREEN;
            case 'k': return GREY;
            default:  return WHITE;
        }
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
            if (i > 0) sb.append(sp(gapSize));
            sb.append(tiles[i]);
        }
        return sb.toString();
    }

    /** Renders 3 reserved card slots: [ ][ ][ ] */
    private String reservedSlots(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++)
            sb.append(i < count ? CYAN + "[\u25a0]" + RESET : DIM + "[ ]" + RESET);
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
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private void ok(String msg) {
        System.out.println("\n" + GREEN + "  \u2714  " + msg + RESET);
    }

    private void err(String msg) {
        System.out.println("\n" + RED + "  \u2716  " + msg + RESET);
        sleep(1200);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // -------------------------------------------------------------------------
    // Temporary main — remove once wired into App.java
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        new GameBoardUI().show();
    }
}

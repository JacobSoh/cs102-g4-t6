package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * EndScreenUI
 *
 * Displayed when the game ends. Shows:
 *   - Winner announcement
 *   - Number of rounds played
 *   - Final scoreboard (points, cards, nobles per player)
 *   - "Return to Main Menu" prompt
 *
 * Currently runs on mock data.
 * Every section that needs to be wired to real GameState
 * is marked with a  // TODO: HOOK  comment.
 */
public class EndScreenUI {

    // -----------------------------------------------------------------------
    // ANSI codes
    // -----------------------------------------------------------------------
    private static final String R  = "\u001B[0m";
    private static final String B  = "\u001B[1m";
    private static final String D  = "\u001B[2m";
    private static final String WH = "\u001B[37m";
    private static final String GR = "\u001B[32m";
    private static final String BL = "\u001B[34m";
    private static final String RE = "\u001B[31m";
    private static final String CY = "\u001B[36m";
    private static final String PU = "\u001B[35m";
    private static final String GY = "\u001B[90m";
    private static final String GO = "\u001B[38;5;220m";

    // -----------------------------------------------------------------------
    // Board dimensions  (same as GameBoardUI for visual consistency)
    // -----------------------------------------------------------------------
    private static final int INNER = 84;
    private static final int FILL  = 86;

    // Column widths inside the scoreboard table
    private static final int COL_NAME   = 12;
    private static final int COL_PTS    = 8;
    private static final int COL_CARDS  = 32;
    private static final int COL_NOBLES = 18;

    // -----------------------------------------------------------------------
    // Mock result data  (replace with real GameState / result object)
    // -----------------------------------------------------------------------

    /** Holds the end-of-game summary for one player. */
    public static class PlayerResult {
        public final String name;
        public final int    points;
        public final int    cardCount;
        public final String cardBonuses;   // e.g. "W:2 R:1 U:3 G:0 K:2"
        public final int    nobleCount;
        public final int    noblePoints;
        public final boolean isWinner;

        public PlayerResult(String name, int points, int cardCount,
                            String cardBonuses, int nobleCount,
                            int noblePoints, boolean isWinner) {
            this.name        = name;
            this.points      = points;
            this.cardCount   = cardCount;
            this.cardBonuses = cardBonuses;
            this.nobleCount  = nobleCount;
            this.noblePoints = noblePoints;
            this.isWinner    = isWinner;
        }
    }

    // TODO: HOOK — replace mock data with real GameState / PlayerResult list

    private int mockRounds = 12;

    private final List<PlayerResult> mockResults = Arrays.asList(
        new PlayerResult("CAROL", 15, 10, "W:2 R:2 U:2 G:2 K:2", 3, 9,  true),
        new PlayerResult("ALICE", 12,  8, "W:2 R:1 U:3 G:0 K:2", 2, 6,  false),
        new PlayerResult("BOB",    9,  6, "W:0 R:3 U:1 G:1 K:1", 1, 3,  false)
    );

    // -----------------------------------------------------------------------
    // Public entry point
    // TODO: HOOK — accept results: public void show(List<PlayerResult> results, int rounds)
    // -----------------------------------------------------------------------

    /**
     * Displays the end screen and blocks until the player presses M to return
     * to the main menu.
     */
    public void show() {
        clearScreen();
        boardTop();
        printWinnerBanner();
        printRoundsPlayed();
        printScoreboard();
        printReturnPrompt();
        boardBottom();
        waitForReturn();
    }

    // -----------------------------------------------------------------------
    // Board frame
    // -----------------------------------------------------------------------
    private void boardTop() {
        System.out.println();
        System.out.println(WH + "  \u2554" + rep('\u2550', FILL) + "\u2557" + R);
    }

    private void boardBottom() {
        System.out.println(WH + "  \u255a" + rep('\u2550', FILL) + "\u255d" + R);
        System.out.println();
    }

    private void line(String content) {
        int pad = INNER - vlen(content);
        if (pad < 0) pad = 0;
        System.out.println(WH + "  \u2551 " + R
                + content + sp(pad)
                + WH + " \u2551" + R);
    }

    private void blank()   { line(""); }
    private void divider() {
        System.out.println(WH + "  \u2560" + rep('\u2550', FILL) + "\u2563" + R);
    }

    // -----------------------------------------------------------------------
    // Winner banner
    // -----------------------------------------------------------------------
    private void printWinnerBanner() {
        // TODO: HOOK — replace mockResults.get(0) with actual winner
        PlayerResult winner = mockResults.stream()
                .filter(p -> p.isWinner).findFirst().orElse(mockResults.get(0));

        // ASCII trophy art, centred
        String[] trophy = {
            "        .-=========-.        ",
            "        \\'-=======-'/        ",
            "        _|   .=.   |_        ",
            "       ((|  {{1}}  |))       ",
            "        \\|   /|\\   |/        ",
            "         \\__ '=' __/         ",
            "       ___) '---' (___       ",
            "      /______[_]______\\      "
        };

        blank();
        for (String row : trophy) {
            // Replace {1} placeholder with winner initial
            String rendered = row.replace("{1}", GO + B + winner.name.charAt(0) + R + GO);
            String centred  = centre(GO + rendered + R, INNER);
            line(centred);
        }
        blank();

        // "GAME OVER" heading
        line(centre(GO + B + "G A M E   O V E R" + R, INNER));
        blank();

        // Winner name
        line(centre(WH + "\uD83C\uDFC6  Winner: " + R
                + CY + B + winner.name + R
                + WH + "  with  " + R
                + GO + B + winner.points + " pts" + R, INNER));
        blank();
        divider();
    }

    // -----------------------------------------------------------------------
    // Rounds played
    // -----------------------------------------------------------------------
    private void printRoundsPlayed() {
        // TODO: HOOK — replace mockRounds with state.getRoundNumber() or result
        line(D + WH + "  Rounds played: " + R + B + WH + mockRounds + R);
        blank();
        divider();
    }

    // -----------------------------------------------------------------------
    // Scoreboard
    // -----------------------------------------------------------------------
    private void printScoreboard() {
        line(B + WH + "FINAL SCORES" + R);
        blank();

        // Table header
        String header = B + WH
                + padTo("  PLAYER",      COL_NAME)
                + padTo("PTS",           COL_PTS)
                + padTo("CARD BONUSES",  COL_CARDS)
                + padTo("NOBLES",        COL_NOBLES)
                + R;
        line(header);
        line(D + WH + "  " + rep('\u2500', INNER - 2) + R);

        // One row per player, sorted by points (descending) — winner first
        // TODO: HOOK — replace mockResults with real sorted result list
        List<PlayerResult> sorted = new ArrayList<>(mockResults);
        sorted.sort((a, b2) -> b2.points - a.points);

        for (int i = 0; i < sorted.size(); i++) {
            PlayerResult p = sorted.get(i);

            // Medal prefix
            String medal;
            switch (i) {
                case 0: medal = GO + B + "1. " + R; break;
                case 1: medal = WH + B + "2. " + R; break;
                case 2: medal = RE + B + "3. " + R; break;
                default: medal = D + WH + (i + 1) + ". " + R; break;
            }

            String nameCol   = padTo(medal + playerColour(p) + B + p.name + R, COL_NAME);
            String ptsCol    = padTo(GO + B + p.points + R, COL_PTS);
            String cardsCol  = padTo(colorStats(p.cardBonuses)
                                     + D + WH + " (" + p.cardCount + " cards)" + R, COL_CARDS);
            String noblesCol = padTo(PU + B + p.nobleCount + R
                                     + WH + " noble" + (p.nobleCount == 1 ? "" : "s")
                                     + " (" + p.noblePoints + " pts)" + R, COL_NOBLES);

            line("  " + nameCol + ptsCol + cardsCol + noblesCol);

            // Highlight winner row
            if (p.isWinner) {
                line(D + GR + "  " + rep('\u2500', INNER - 2) + R);
            }
        }

        blank();
        divider();
    }

    // -----------------------------------------------------------------------
    // Return to main menu prompt
    // -----------------------------------------------------------------------
    private void printReturnPrompt() {
        blank();
        line(centre(GR + B + "[ M ]  Return to Main Menu" + R, INNER));
        blank();
    }

    /** Blocks until the user presses M (case-insensitive). */
    private void waitForReturn() {
        try {
            new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                    .inheritIO().start().waitFor();
            while (true) {
                int ch = System.in.read();
                if (ch == 'm' || ch == 'M') break;
            }
        } catch (Exception e) {
            // fallback: wait for Enter
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    String s = sc.nextLine().trim();
                    if (s.equalsIgnoreCase("m")) break;
                }
            }
        } finally {
            try {
                new ProcessBuilder("sh", "-c", "stty sane </dev/tty")
                        .inheritIO().start().waitFor();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Rendering utilities
    // -----------------------------------------------------------------------

    /** Returns a colour based on the player's rank / winner status. */
    private String playerColour(PlayerResult p) {
        return p.isWinner ? CY : WH;
    }

    /** Colours a "W:2 R:1 U:3" stats string token by token. */
    private String colorStats(String stats) {
        StringBuilder sb = new StringBuilder();
        for (String t : stats.split("\\s+")) {
            sb.append(gemAnsi(t.charAt(0))).append(B).append(t).append(R).append(" ");
        }
        return sb.toString().trim();
    }

    private String gemAnsi(char c) {
        switch (c) {
            case 'W': return WH;
            case 'R': return RE;
            case 'U': return BL;
            case 'G': return GR;
            case 'K': return GY;
            case '*': return GO;
            default:  return WH;
        }
    }

    /**
     * Pads string s to exactly targetWidth visible characters.
     * ANSI codes are excluded from the width calculation.
     */
    private String padTo(String s, int w) {
        int pad = w - vlen(s);
        return pad > 0 ? s + sp(pad) : s;
    }

    /**
     * Centres a string (which may contain ANSI codes) within a field of
     * width w, padding with spaces on both sides.
     */
    private String centre(String s, int w) {
        int total = w - vlen(s);
        int left  = total / 2;
        int right = total - left;
        if (left  < 0) left  = 0;
        if (right < 0) right = 0;
        return sp(left) + s + sp(right);
    }

    /**
     * Visible length of s — strips ANSI escape sequences before measuring.
     * \\e is Java regex's ESC (0x1B), matching the \u001B in our constants.
     */
    private int vlen(String s) {
        return s.replaceAll("\\e\\[[;\\d]*m", "").length();
    }

    private String sp(int n)       { return n > 0 ? " ".repeat(n) : ""; }
    private String rep(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // -----------------------------------------------------------------------
    // Temporary main — remove once wired into App.java
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        new EndScreenUI().show();
        System.out.println("Returned to main menu.");
    }
}

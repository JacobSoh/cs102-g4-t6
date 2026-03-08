package edu.cs102.g04t06.game.presentation.console;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * GameRenderer
 *
 * Responsible for rendering all game-related output to the console.
 * Acts as the bridge between the game model ({@link GameState}, {@link Player},
 * {@link Card}, {@link Noble}, {@link GemCollection}) and the terminal display,
 * using ANSI colour codes for visual clarity.
 *
 * All output is written to the {@link PrintStream} supplied at construction
 * time, defaulting to {@code System.out}. Injecting a stream backed by a
 * {@code java.io.ByteArrayOutputStream} makes every method straightforward
 * to unit-test without touching the real terminal.
 *
 * Methods prefixed with {@code display} print directly to the stream.
 * Methods prefixed with {@code format} return a formatted {@code String} so
 * the caller can embed the result inside a larger output.
 */
public class GameRenderer {

    // ANSI colour codes
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String GOLD   = "\u001B[38;5;220m";
    private static final String WHITE  = "\u001B[37m";
    private static final String GREEN  = "\u001B[32m";
    private static final String BLUE   = "\u001B[34m";
    private static final String RED    = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";
    private static final String GREY   = "\u001B[90m";

    // Layout constants
    /** Visible content width between the left and right border characters. */
    private static final int INNER = 84;
    /** Number of '═' characters in the top/bottom border line. */
    private static final int FILL  = 86;

    // Output destination
    private final PrintStream out;

    // Constructorrs
    /**
     * Creates a {@code GameRenderer} that writes to {@code System.out}.
     */
    public GameRenderer() {
        this(System.out);
    }

    /**
     * Creates a {@code GameRenderer} that writes to the supplied stream.
     * Pass a {@code new PrintStream(byteArrayOutputStream)} in tests.
     *
     * @param out the stream to write all output to; must not be {@code null}
     */
    public GameRenderer(PrintStream out) {
        this.out = out;
    }

    // Core state display

    /**
     * Renders the complete game board for the given state to the output stream.
     *
     * The output includes, in order:
     *   A bordered header with the game title
     *   All available nobles with their point values and requirements
     *   All visible development cards from the card market, grouped by tier
     *   The current gem bank counts for every {@link GemColor}
     *   A summary row for every player showing name, points, card count, and gem count
     * 
     * @param state the current game state to display; must not be {@code null}
     */
    public void displayGameState(GameState state) {
        boardTop();
        line(GOLD + BOLD + "SPLENDOR  —  Game Board" + RESET);
        divider();

        // Nobles
        line(PURPLE + BOLD + "NOBLES" + RESET);
        List<Noble> nobles = state.getAvailableNobles();
        if (nobles == null || nobles.isEmpty()) {
            line(DIM + WHITE + "  No nobles remaining." + RESET);
        } else {
            for (Noble noble : nobles) {
                line("  " + formatNoble(noble));
            }
        }
        divider();

        // Visible development cards
        line(BOLD + WHITE + "DEVELOPMENT CARDS" + RESET);
        for (int tier = 3; tier >= 1; tier--) {
            line(DIM + WHITE + "  Tier " + tier + ":" + RESET);
            List<Card> tierCards = state.getMarket().getVisibleCards(tier);
            if (tierCards == null || tierCards.isEmpty()) {
                line(DIM + WHITE + "    No cards visible." + RESET);
            } else {
                for (Card card : tierCards) {
                    line("    " + formatCard(card));
                }
            }
        }
        divider();

        // Gem bank 
        line(BOLD + WHITE + "GEM BANK" + RESET);
        line("  " + formatGemCollection(state.getGemBank()));
        divider();

        // All players summary
        line(BOLD + WHITE + "PLAYERS" + RESET);
        for (Player player : state.getPlayers()) {
            boolean isCurrent = player == state.getCurrentPlayer();
            String indicator  = isCurrent ? GREEN + BOLD + "\u25ba " + RESET : "  ";
            line(indicator
                    + CYAN + BOLD + player.getName() + RESET
                    + "  " + GOLD + player.getPoints() + " pts" + RESET
                    + "  Cards: " + player.getPurchasedCards().size()
                    + "  Gems: "  + player.getGemCount());
        }

        boardBottom();
    }

    /**
     * Renders a detailed panel for the active player to the output stream.
     *
     * The output includes:
     *   The player's name, highlighted as the active player
     *   Total prestige points
     *   Individual gem counts for every {@link GemColor}
     *   Total number of purchased development cards and their bonus breakdown
     *   Number of reserved cards currently held (out of a maximum of 3)
     *   Number of nobles claimed
     * 
     * @param player the player whose status to display; must not be {@code null}
     */
    public void displayCurrentPlayer(Player player) {
        boardTop();

        // Name + points header
        line(CYAN + BOLD + player.getName() + " (your turn)" + RESET
                + "   " + GOLD + BOLD + player.getPoints() + " pts" + RESET);
        divider();

        // Gem counts
        line(WHITE + "  Gems  : " + RESET
                + formatGemCollection(player.getGems())
                + DIM + WHITE + "   (Total: " + player.getGemCount() + ")" + RESET);

        // Card counts + bonus breakdown
        line(WHITE + "  Cards : " + RESET
                + BOLD + player.getPurchasedCards().size() + RESET
                + " purchased   "
                + formatBonuses(player.calculateBonuses()));

        // Reserved cards
        line(WHITE + "  Reserved: " + RESET
                + BOLD + player.getReservedCards().size() + "/3" + RESET);

        // Nobles claimed
        line(WHITE + "  Nobles: " + RESET
                + PURPLE + BOLD + player.getClaimedNobles().size() + RESET);

        boardBottom();
    }

    // Menus, messages, and formatting

    /**
     * Prints the in-game action menu to the output stream.
     *
     * Lists all four actions a player may take on their turn:
     * Take three gems of different colour
     * Take two gems of the same colour
     * Purchase a development card
     * Reserve a development card
     */
    public void displayActionMenu() {
        out.println();
        out.println(WHITE + "  \u250c" + "\u2500".repeat(40) + "\u2510" + RESET);
        out.println(WHITE + "  \u2502" + BOLD + WHITE
                + centreInBox("ACTIONS", 40) + RESET + WHITE + "\u2502" + RESET);
        out.println(WHITE + "  \u251c" + "\u2500".repeat(40) + "\u2524" + RESET);
        out.println(WHITE + "  \u2502 " + GREEN + BOLD + "[ 1 ]" + RESET
                + WHITE + "  Take three different gems      " + WHITE + "\u2502" + RESET);
        out.println(WHITE + "  \u2502 " + GREEN + BOLD + "[ 2 ]" + RESET
                + WHITE + "  Take two gems of same colour   " + WHITE + "\u2502" + RESET);
        out.println(WHITE + "  \u2502 " + BLUE  + BOLD + "[ 3 ]" + RESET
                + WHITE + "  Purchase a development card    " + WHITE + "\u2502" + RESET);
        out.println(WHITE + "  \u2502 " + CYAN  + BOLD + "[ 4 ]" + RESET
                + WHITE + "  Reserve a development card     " + WHITE + "\u2502" + RESET);
        out.println(WHITE + "  \u2514" + "\u2500".repeat(40) + "\u2518" + RESET);
        out.println();
    }

    /**
     * Prints a formatted error message to the output stream.
     *
     * The message is prefixed with a red ✖ symbol so it stands out
     * clearly from regular output.
     *
     * @param message the error text to display; must not be {@code null}
     */
    public void displayError(String message) {
        out.println();
        out.println(RED + BOLD + "  \u2716  " + RESET + RED + message + RESET);
        out.println();
    }

    /**
     * Prints the end-of-game winner announcement and final scoreboard
     * to the output stream.
     *
     * The winner's name and score are highlighted with gold text, followed
     * by a ranked list of all players sorted by prestige points in descending
     * order.
     *
     * @param winner     the player who won the game; must not be {@code null}
     * @param allPlayers every player in the game, in any order; must not be {@code null}
     */
    public void displayWinner(Player winner, List<Player> allPlayers) {
        boardTop();
        line(GOLD + BOLD + "G A M E   O V E R" + RESET);
        blank();
        line("  " + WHITE + "Winner: " + RESET
                + CYAN + BOLD + winner.getName() + RESET
                + "  " + GOLD + BOLD + winner.getPoints() + " pts" + RESET);
        divider();

        line(BOLD + WHITE + "FINAL SCORES" + RESET);
        blank();

        List<Player> sorted = new ArrayList<>(allPlayers);
        sorted.sort((a, b) -> b.getPoints() - a.getPoints());

        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);
            String medal;
            switch (i) {
                case 0:  medal = GOLD  + BOLD + "1. " + RESET; break;
                case 1:  medal = WHITE + BOLD + "2. " + RESET; break;
                case 2:  medal = RED   + BOLD + "3. " + RESET; break;
                default: medal = DIM   + WHITE + (i + 1) + ". " + RESET;
            }
            line("  " + medal
                    + CYAN + p.getName() + RESET
                    + "  " + GOLD + p.getPoints() + " pts" + RESET
                    + "  Cards: "  + p.getPurchasedCards().size()
                    + "  Nobles: " + p.getClaimedNobles().size());
        }

        boardBottom();
    }

    // Helper formatting methods

    /**
     * Returns a single-line string representation of a development card.
     *
     * Format example: {@code [Tier 1]  WHITE  0 pts  Cost: R:2 K:1}
     * Includes the card's tier level, gem colour bonus, prestige point value,
     * and full gem cost.
     *
     * @param card the card to format; must not be {@code null}
     * @return a human-readable, colour-coded string describing the card
     */
    public String formatCard(Card card) {
        return DIM + WHITE + "[Tier " + card.getLevel() + "]" + RESET
                + "  " + gemAnsi(card.getBonus()) + BOLD + gemLabel(card.getBonus()) + RESET
                + "  " + GOLD + card.getPoints() + " pts" + RESET
                + "  Cost: " + card.getCost().toString();
    }

    /**
     * Returns a single-line string representation of a noble tile.
     *
     * Format example: {@code ★ 3 pts  Requires: WHITE:3 RED:3}
     * Includes the noble's prestige point value and each gem colour
     * requirement with its count.
     *
     * @param noble the noble to format; must not be {@code null}
     * @return a human-readable, colour-coded string describing the noble
     */
    public String formatNoble(Noble noble) {
        StringBuilder sb = new StringBuilder();
        sb.append(GOLD + BOLD + "\u2605 " + noble.getPoints() + " pts" + RESET);
        sb.append("  Requires: ");
        for (Map.Entry<GemColor, Integer> entry : noble.getRequirements().entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(gemAnsi(entry.getKey()))
                  .append(gemLabel(entry.getKey()))
                  .append(":").append(entry.getValue())
                  .append(RESET).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Returns a single-line string showing the count of every gem colour
     * in the supplied collection.
     *
     * Each colour is printed with its ANSI colour code in the order:
     * White, Blue, Green, Red, Black, Gold.
     * Example output: {@code W:3  U:1  G:0  R:2  K:4  *:1}
     *
     * @param gems the gem collection to format; must not be {@code null}
     * @return a colour-coded string of gem counts for all six {@link GemColor} values
     */
    public String formatGemCollection(GemCollection gems) {
        GemColor[] order = {
            GemColor.WHITE, GemColor.BLUE,  GemColor.GREEN,
            GemColor.RED,   GemColor.BLACK, GemColor.GOLD
        };
        StringBuilder sb = new StringBuilder();
        for (GemColor color : order) {
            sb.append(gemAnsi(color))
              .append(BOLD)
              .append(gemLabel(color)).append(":").append(gems.getCount(color))
              .append(RESET)
              .append("  ");
        }
        return sb.toString().trim();
    }

    // Private helpers

    /**
     * Formats a bonus map (from {@link Player#calculateBonuses()}) into a
     * compact colour-coded string, e.g. {@code Bonuses: W:2 U:1 G:0 R:3 K:1 *:0}.
     */
    private String formatBonuses(Map<GemColor, Integer> bonuses) {
        GemColor[] order = {
            GemColor.WHITE, GemColor.BLUE,  GemColor.GREEN,
            GemColor.RED,   GemColor.BLACK, GemColor.GOLD
        };
        StringBuilder sb = new StringBuilder("Bonuses: ");
        for (GemColor color : order) {
            int count = bonuses.getOrDefault(color, 0);
            sb.append(gemAnsi(color))
              .append(gemLabel(color)).append(":").append(count)
              .append(RESET).append(" ");
        }
        return sb.toString().trim();
    }

    // Board frame helpers

    private void boardTop() {
        out.println(WHITE + "  \u2554" + rep('\u2550', FILL) + "\u2557" + RESET);
    }

    private void boardBottom() {
        out.println(WHITE + "  \u255a" + rep('\u2550', FILL) + "\u255d" + RESET);
        out.println();
    }

    private void line(String content) {
        int pad = INNER - vlen(content);
        if (pad < 0) pad = 0;
        out.println(WHITE + "  \u2551 " + RESET
                + content + sp(pad)
                + WHITE + " \u2551" + RESET);
    }

    private void blank() { line(""); }

    private void divider() {
        out.println(WHITE + "  \u2560" + rep('\u2550', FILL) + "\u2563" + RESET);
    }

    // Gem colour helpers

    /** Maps a {@link GemColor} to its ANSI colour escape string. */
    private String gemAnsi(GemColor color) {
        switch (color) {
            case WHITE: return WHITE;
            case BLUE:  return BLUE;
            case GREEN: return GREEN;
            case RED:   return RED;
            case BLACK: return GREY;
            case GOLD:  return GOLD;
            default:    return WHITE;
        }
    }

    /** Maps a {@link GemColor} to its short display label (W, U, G, R, K, *). */
    private String gemLabel(GemColor color) {
        switch (color) {
            case WHITE: return "W";
            case BLUE:  return "U";
            case GREEN: return "G";
            case RED:   return "R";
            case BLACK: return "K";
            case GOLD:  return "*";
            default:    return "?";
        }
    }

    // String/ layout utilities

    /** Returns a string of {@code n} spaces, or {@code ""} when n ≤ 0. */
    private String sp(int n) {
        return n > 0 ? " ".repeat(n) : "";
    }

    /** Returns a string of {@code n} repetitions of character {@code c}. */
    private String rep(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    /**
     * Returns the visible (printable) length of {@code s} by stripping ANSI
     * escape sequences before measuring.
     */
    private int vlen(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "").length();
    }

    /** Centres plain text inside a fixed-width box, padding with spaces. */
    private String centreInBox(String text, int width) {
        int total = width - text.length();
        int left  = total / 2;
        int right = total - left;
        return sp(left) + text + sp(right);
    }
}

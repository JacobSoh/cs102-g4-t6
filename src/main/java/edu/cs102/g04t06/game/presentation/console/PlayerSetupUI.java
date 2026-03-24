package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * PlayerSetupUI
 *
 * Collects the local offline player configuration before the game starts:
 *   1. The local player's name
 *   2. Number of CPU opponents (1-3, so total players 2-4)
 *
 * Returns a PlayerSetupResult containing everything the game needs.
 */
public class PlayerSetupUI implements ThemeStyleSheet {

    private static final int BOX_WIDTH = 50;

    // -------------------------------------------------------------------------
    // Result object returned to the caller
    // -------------------------------------------------------------------------

    /**
     * Holds everything collected during player setup.
     * Pass this into your game initialisation logic.
     */
    public static class PlayerSetupResult {
        public final String       localPlayerName;   // the human at this machine
        public final boolean      isOnline;          // true = online, false = offline/CPU
        public final int          totalPlayers;      // 2–4
        public final List<Player> players;           // index 0 = local player, rest = friends/CPUs
        public final List<Boolean> isHuman;          // true = human, false = CPU

        public PlayerSetupResult(String localPlayerName,
                                 boolean isOnline,
                                 List<Player> players,
                                 List<Boolean> isHuman) {
            if (players.size() != isHuman.size()) {
                throw new IllegalArgumentException("players and isHuman size must match");
            }
            this.localPlayerName = localPlayerName;
            this.isOnline        = isOnline;
            this.players         = List.copyOf(players);
            this.totalPlayers    = this.players.size();
            this.isHuman         = List.copyOf(isHuman);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Mode: ").append(isOnline ? "Online" : "Offline").append("\n");
            sb.append("Players (").append(totalPlayers).append("):\n");
            for (int i = 0; i < players.size(); i++) {
                sb.append("  ").append(i + 1).append(". ")
                  .append(players.get(i).getName())
                  .append(isHuman.get(i) ? " [Human]" : " [CPU]")
                  .append("\n");
            }
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Scanner (shared across steps)
    // -------------------------------------------------------------------------
    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Runs the offline player setup flow and returns a populated PlayerSetupResult.
     */
    public PlayerSetupResult show() {
        clearScreen();
        printHeader("PLAYER SETUP", "Offline Game");
        System.out.println(WHITE + "  Mode: " + RESET
                + BLUE + "Offline (vs CPU)" + RESET);
        System.out.println();

        String localName = promptLocalName();
        int opponentCount = promptOpponentCount(localName);
        List<Player> players = new ArrayList<>();
        List<Boolean> humans = new ArrayList<>();

        players.add(new Player(localName, 0));
        humans.add(true);
        addCpuPlayers(opponentCount, players, humans);

        PlayerSetupResult result = new PlayerSetupResult(
                localName,
                false,
                players,
                humans
        );
        showSummary(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Inputs
    // -------------------------------------------------------------------------
    private String promptLocalName() {
        while (true) {
            System.out.print(WHITE + "  Your name: " + RESET);

            String name = scanner.nextLine().trim();

            if (name.isEmpty()) {
                printError("Name cannot be empty. Please try again.");
                sleep(1000);
                continue;
            }
            if (name.length() > 20) {
                printError("Name must be 20 characters or fewer.");
                sleep(1000);
                continue;
            }
            return name;
        }
    }

    private int promptOpponentCount(String localName) {
        while (true) {
            System.out.println(WHITE + "  You are playing as: " + GOLD + BOLD
                    + localName + RESET);
            System.out.println();
            System.out.println(WHITE + "  How many players total?"
                    + " " + DIM + "(2–4)" + RESET);
            System.out.println();
            System.out.print(WHITE + "  Total players (2-4): " + RESET);
            String input = scanner.nextLine().trim();

            try {
                int totalPlayers = Integer.parseInt(input);
                if (totalPlayers < 2 || totalPlayers > 4) {
                    throw new NumberFormatException();
                }
                return totalPlayers - 1;
            } catch (NumberFormatException e) {
                printError("Please enter a number between 2 and 4.");
                sleep(1000);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 3 — Auto-generate CPU players
    // -------------------------------------------------------------------------
    private void addCpuPlayers(int count, List<Player> players, List<Boolean> humans) {
        String[] cpuNames = {"CPU-1", "CPU-2", "CPU-3"};
        for (int i = 0; i < count; i++) {
            players.add(new Player(cpuNames[i], players.size()));
            humans.add(false);
        }
    }

    // -------------------------------------------------------------------------
    // Summary screen
    // -------------------------------------------------------------------------
    private void showSummary(PlayerSetupResult result) {
        clearScreen();
        printHeader("PLAYER SETUP", "Summary — Ready to Play!");

        System.out.println(WHITE + "  Mode: " + RESET
                + (result.isOnline
                    ? GREEN + BOLD + "Online" + RESET
                    : BLUE  + BOLD + "Offline (vs CPU)" + RESET));
        System.out.println();
        System.out.println(WHITE + "  Players:" + RESET);
        System.out.println();

        for (int i = 0; i < result.players.size(); i++) {
            String name    = result.players.get(i).getName();
            boolean human  = result.isHuman.get(i);
            String tag     = human
                    ? GREEN + "[Human]" + RESET
                    : BLUE  + "[CPU]"   + RESET;
            String youTag  = (i == 0) ? GOLD + " ← you" + RESET : "";
            System.out.println("    " + CYAN + (i + 1) + ". " + RESET
                    + WHITE + name + RESET + "  " + tag + youTag);
        }

        System.out.println();
        System.out.print(GREEN + BOLD + "  Press Enter to start the game... " + RESET);
        waitForEnter();
    }

    // -------------------------------------------------------------------------
    // Rendering helpers
    // -------------------------------------------------------------------------
    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    private void printHeader(String title, String subtitle) {
        System.out.println();
        System.out.println(GOLD + BOLD + "  " + title + RESET);
        System.out.println(DIM  + WHITE + "  " + subtitle + RESET);
        System.out.println(DIM  + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    private void printOptionBox(String[] options) {
        int contentWidth = BOX_WIDTH;
        for (String option : options) {
            contentWidth = Math.max(contentWidth, stripAnsi(option).length() + 1);
        }

        System.out.println(WHITE + "  ┌" + "─".repeat(contentWidth) + "┐" + RESET);
        for (String option : options) {
            int visibleLen = stripAnsi(option).length();
            int padding = contentWidth - visibleLen - 1;
            if (padding < 0) padding = 0;
            System.out.println(WHITE + "  │ " + RESET + option
                    + " ".repeat(padding) + WHITE + "│" + RESET);
        }
        System.out.println(WHITE + "  └" + "─".repeat(contentWidth) + "┘" + RESET);
        System.out.println();
    }

    private void printError(String msg) {
        System.out.println();
        System.out.println(RED + "  ✖  " + msg + RESET);
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------
    private void waitForEnter() {
        while (true) {
            String input = scanner.nextLine();
            if (input.isBlank()) {
                return;
            }
            System.out.println(RED + "  Please press Enter only to continue." + RESET);
            System.out.print(GREEN + "  > " + RESET);
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String stripAnsi(String s) {
        return s.replaceAll(ANSI_REGEX, "");
    }

    private boolean nameExistsIgnoreCase(List<Player> players, String candidateName) {
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(candidateName)) {
                return true;
            }
        }
        return false;
    }

}

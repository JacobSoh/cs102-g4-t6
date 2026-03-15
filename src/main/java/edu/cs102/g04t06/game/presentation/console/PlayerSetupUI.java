package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * PlayerSetupUI
 *
 * Collects all player configuration before the game starts:
 *   1. The local player's name
 *   2. Online (friends) or Offline (CPU) mode
 *   3. Number of opponents (1–3, so total players 2–4)
 *   4. Friend names (online) or auto-generated CPU names (offline)
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
    // Mode choice
    // -------------------------------------------------------------------------
    public enum ModeChoice {
        ONLINE,
        OFFLINE
    }

    // -------------------------------------------------------------------------
    // Scanner (shared across steps)
    // -------------------------------------------------------------------------
    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Runs the full player setup flow and returns a populated PlayerSetupResult.
     * Returns null when the user selects Back from mode selection.
     */
    public PlayerSetupResult show() {
        // Step 1 — local player name
        String localName = stepGetLocalName();

        // Step 2 — online or offline
        ModeChoice mode = stepChooseMode(localName);
        if (mode == null) {
            return null;
        }

        // Step 3 — number of opponents
        int opponentCount = stepChooseOpponentCount(localName, mode);

        // Step 4 — build player lists
        List<Player> players = new ArrayList<>();
        List<Boolean> humans = new ArrayList<>();

        players.add(new Player(localName, 0));
        humans.add(true); // local player is always human

        if (mode == ModeChoice.ONLINE) {
            collectFriendNames(opponentCount, players, humans);
        } else {
            addCpuPlayers(opponentCount, players, humans);
        }

        // Summary screen
        PlayerSetupResult result = new PlayerSetupResult(
                localName,
                mode == ModeChoice.ONLINE,
                players,
                humans
        );
        showSummary(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Step 1 — Get local player name
    // -------------------------------------------------------------------------
    private String stepGetLocalName() {
        while (true) {
            clearScreen();
            printHeader("PLAYER SETUP", "Step 1 of 3 — Your Name");

            System.out.println(WHITE + "  Enter your name: " + RESET);
            System.out.print(GREEN + "  > " + RESET);

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

    // -------------------------------------------------------------------------
    // Step 2 — Choose online or offline
    // -------------------------------------------------------------------------
    private ModeChoice stepChooseMode(String localName) {
        while (true) {
            clearScreen();
            printHeader("PLAYER SETUP", "Step 2 of 3 — Game Mode");

            System.out.println(WHITE + "  Hello, " + GOLD + BOLD + localName + RESET
                    + WHITE + "! How would you like to play?" + RESET);
            System.out.println();

            printOptionBox(new String[]{
                GREEN + BOLD + "[ O ]" + RESET + WHITE + "  Play Online  " + RESET
                        + DIM + "(with friends)" + RESET,
                BLUE  + BOLD + "[ F ]" + RESET + WHITE + "  Play Offline " + RESET
                        + DIM + "(vs CPU)" + RESET,
                WHITE +        "[ B ]" + RESET + WHITE + "  Back to Main Menu" + RESET
            });

            System.out.print(GREEN + "  > " + RESET);
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "o": return ModeChoice.ONLINE;
                case "f": return ModeChoice.OFFLINE;
                case "b": return null; // caller can handle back navigation if needed
                default:
                    printError("Invalid choice. Press O, F, or B.");
                    sleep(1000);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 3 — Number of opponents
    // -------------------------------------------------------------------------
    private int stepChooseOpponentCount(String localName, ModeChoice mode) {
        String modeLabel = (mode == ModeChoice.ONLINE) ? "friends" : "CPU opponents";

        while (true) {
            clearScreen();
            printHeader("PLAYER SETUP", "Step 3 of 3 — Number of Opponents");

            System.out.println(WHITE + "  You are playing as: " + GOLD + BOLD
                    + localName + RESET);
            System.out.println(WHITE + "  Mode: " + RESET
                    + (mode == ModeChoice.ONLINE
                        ? GREEN + "Online" + RESET
                        : BLUE  + "Offline (vs CPU)" + RESET));
            System.out.println();
            System.out.println(WHITE + "  How many " + modeLabel
                    + " would you like? " + DIM + "(1–3)" + RESET);
            System.out.println();

            printOptionBox(new String[]{
                GREEN + "[ 1 ]" + RESET + WHITE + "  2 players total" + RESET,
                GREEN + "[ 2 ]" + RESET + WHITE + "  3 players total" + RESET,
                GREEN + "[ 3 ]" + RESET + WHITE + "  4 players total" + RESET
            });

            System.out.print(GREEN + "  > " + RESET);
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": return 1;
                case "2": return 2;
                case "3": return 3;
                default:
                    printError("Please enter 1, 2, or 3.");
                    sleep(1000);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 4a — Collect friend names (online)
    // -------------------------------------------------------------------------
    private void collectFriendNames(int count, List<Player> players, List<Boolean> humans) {
        for (int i = 1; i <= count; i++) {
            while (true) {
                clearScreen();
                printHeader("PLAYER SETUP", "Online — Friend " + i + " of " + count);

                System.out.println(WHITE + "  Enter name for Friend " + i + ":" + RESET);
                System.out.print(GREEN + "  > " + RESET);

                String name = scanner.nextLine().trim();

                if (name.isEmpty()) {
                    printError("Name cannot be empty.");
                    sleep(1000);
                    continue;
                }
                if (name.length() > 20) {
                    printError("Name must be 20 characters or fewer.");
                    sleep(1000);
                    continue;
                }
                if (nameExistsIgnoreCase(players, name)) {
                    printError("That name is already taken. Choose a different name.");
                    sleep(1000);
                    continue;
                }

                players.add(new Player(name, players.size()));
                humans.add(true);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 4b — Auto-generate CPU players (offline)
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

    // -------------------------------------------------------------------------
    // Temporary main — remove once wired into App.java
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        PlayerSetupUI ui = new PlayerSetupUI();
        PlayerSetupResult result = ui.show();
        System.out.println("\nSetup complete!");
        System.out.println(result);
    }
}

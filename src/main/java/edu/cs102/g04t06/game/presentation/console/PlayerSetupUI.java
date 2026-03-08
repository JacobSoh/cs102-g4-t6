package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
public class PlayerSetupUI {

    // -------------------------------------------------------------------------
    // ANSI Colour Codes
    // -------------------------------------------------------------------------
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GOLD   = "\u001B[38;5;220m";
    private static final String WHITE  = "\u001B[37m";
    private static final String DIM    = "\u001B[2m";
    private static final String GREEN  = "\u001B[32m";
    private static final String BLUE   = "\u001B[34m";
    private static final String RED    = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

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
        public final List<String> allPlayerNames;    // index 0 = local player, rest = friends/CPUs
        public final List<Boolean> isHuman;          // true = human, false = CPU

        public PlayerSetupResult(String localPlayerName,
                                 boolean isOnline,
                                 int totalPlayers,
                                 List<String> allPlayerNames,
                                 List<Boolean> isHuman) {
            this.localPlayerName = localPlayerName;
            this.isOnline        = isOnline;
            this.totalPlayers    = totalPlayers;
            this.allPlayerNames  = allPlayerNames;
            this.isHuman         = isHuman;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Mode: ").append(isOnline ? "Online" : "Offline").append("\n");
            sb.append("Players (").append(totalPlayers).append("):\n");
            for (int i = 0; i < allPlayerNames.size(); i++) {
                sb.append("  ").append(i + 1).append(". ")
                  .append(allPlayerNames.get(i))
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
        List<String>  names   = new ArrayList<>();
        List<Boolean> humans  = new ArrayList<>();

        names.add(localName);
        humans.add(true); // local player is always human

        if (mode == ModeChoice.ONLINE) {
            collectFriendNames(localName, opponentCount, names, humans);
        } else {
            addCpuPlayers(opponentCount, names, humans);
        }

        // Summary screen
        PlayerSetupResult result = new PlayerSetupResult(
                localName,
                mode == ModeChoice.ONLINE,
                names.size(),
                names,
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
    private void collectFriendNames(String localName, int count,
                                    List<String> names, List<Boolean> humans) {
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
                if (name.equalsIgnoreCase(localName) || names.contains(name)) {
                    printError("That name is already taken. Choose a different name.");
                    sleep(1000);
                    continue;
                }

                names.add(name);
                humans.add(true);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step 4b — Auto-generate CPU players (offline)
    // -------------------------------------------------------------------------
    private void addCpuPlayers(int count, List<String> names, List<Boolean> humans) {
        String[] cpuNames = {"CPU-1", "CPU-2", "CPU-3"};
        for (int i = 0; i < count; i++) {
            names.add(cpuNames[i]);
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

        for (int i = 0; i < result.allPlayerNames.size(); i++) {
            String name    = result.allPlayerNames.get(i);
            boolean human  = result.isHuman.get(i);
            String tag     = human
                    ? GREEN + "[Human]" + RESET
                    : BLUE  + "[CPU]"   + RESET;
            String youTag  = (i == 0) ? GOLD + " ← you" + RESET : "";
            System.out.println("    " + CYAN + (i + 1) + ". " + RESET
                    + WHITE + name + RESET + "  " + tag + youTag);
        }

        System.out.println();
        System.out.println(GREEN + BOLD + "  Press any key to start the game..." + RESET);
        waitForKeyPress();
    }

    // -------------------------------------------------------------------------
    // Rendering helpers
    // -------------------------------------------------------------------------
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
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
        System.out.println(WHITE + "  ┌" + "─".repeat(BOX_WIDTH) + "┐" + RESET);
        for (String option : options) {
            int visibleLen = stripAnsi(option).length();
            int padding    = BOX_WIDTH - visibleLen - 2;
            if (padding < 0) padding = 0;
            System.out.println(WHITE + "  │ " + RESET + option
                    + " ".repeat(padding) + WHITE + "│" + RESET);
        }
        System.out.println(WHITE + "  └" + "─".repeat(BOX_WIDTH) + "┘" + RESET);
        System.out.println();
    }

    private void printError(String msg) {
        System.out.println();
        System.out.println(RED + "  ✖  " + msg + RESET);
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------
    private void waitForKeyPress() {
        try {
            new ProcessBuilder("sh", "-c", "stty raw -echo </dev/tty")
                    .inheritIO().start().waitFor();
            System.in.read();
        } catch (Exception e) {
            try { System.in.read(); } catch (Exception ex) { /* ignore */ }
        } finally {
            try {
                new ProcessBuilder("sh", "-c", "stty sane </dev/tty")
                        .inheritIO().start().waitFor();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String stripAnsi(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
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

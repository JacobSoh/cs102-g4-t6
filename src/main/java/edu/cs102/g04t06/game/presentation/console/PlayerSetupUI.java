package edu.cs102.g04t06.game.presentation.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.cs102.g04t06.game.presentation.shared.PlayerIdentityPrompts;
import edu.cs102.g04t06.game.rules.entities.Player;

/**
 * PlayerSetupUI
 *
 * Collects the local offline player configuration before the game starts:
 *   1. The local player's name
 *   2. The local player's birthday
 *   3. Number of opponents (1-3, so total players 2-4)
 *
 * Returns a PlayerSetupResult containing everything the game needs.
 */
public class PlayerSetupUI extends AbstractConsoleUI {

    private static final int BOX_WIDTH = 50;

    /**
     * Holds everything collected during player setup.
     * Pass this into your game initialisation logic.
     */
    public static class PlayerSetupResult {
        public final String       localPlayerName;
        public final boolean      isOnline;
        public final int          totalPlayers;
        public final List<Player> players;
        public final List<Boolean> isHuman;
        public final List<String> aiDifficulties;

        public PlayerSetupResult(String localPlayerName,
                                 boolean isOnline,
                                 List<Player> players,
                                 List<Boolean> isHuman,
                                 List<String> aiDifficulties) {
            if (players.size() != isHuman.size() || players.size() != aiDifficulties.size()) {
                throw new IllegalArgumentException("players, isHuman, and aiDifficulties size must match");
            }
            this.localPlayerName  = localPlayerName;
            this.isOnline         = isOnline;
            this.players          = List.copyOf(players);
            this.totalPlayers     = this.players.size();
            this.isHuman          = List.copyOf(isHuman);
            this.aiDifficulties   = Collections.unmodifiableList(new ArrayList<>(aiDifficulties));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Mode: ").append(isOnline ? "Online" : "Offline").append("\n");
            sb.append("Players (").append(totalPlayers).append("):\n");
            for (int i = 0; i < players.size(); i++) {
                sb.append("  ").append(i + 1).append(". ")
                  .append(players.get(i).getName())
                  .append(isHuman.get(i) ? " [Human]" : " [CPU - " + aiDifficulties.get(i) + "]")
                  .append("\n");
            }
            return sb.toString();
        }
    }

    private final PlayerIdentityPrompts identityPrompts = new PlayerIdentityPrompts(scanner);

    /**
     * Runs the offline player setup flow and returns a populated PlayerSetupResult.
     */
    public PlayerSetupResult show() {
        clearScreen();
        printHeader("PLAYER SETUP", "Offline Game");
        System.out.println(WHITE + "  Mode: " + RESET + BLUE + "Offline (vs CPU)" + RESET);
        System.out.println();

        String localName = identityPrompts.promptName("Your name");
        int localAge = identityPrompts.promptBirthdayAsAge(localName);
        int totalPlayers = promptTotalPlayers(localName);
        int opponentCount = totalPlayers - 1;
        List<PlayerSeed> playerSeeds = new ArrayList<>();

        playerSeeds.add(new PlayerSeed(localName, localAge, true));
        addCpuPlayers(opponentCount, localAge, playerSeeds);
        playerSeeds.sort(Comparator.comparingInt(PlayerSeed::age));

        List<Player> players = new ArrayList<>();
        List<Boolean> humans = new ArrayList<>();
        List<String> difficulties = new ArrayList<>();
        for (int i = 0; i < playerSeeds.size(); i++) {
            PlayerSeed seed = playerSeeds.get(i);
            players.add(new Player(seed.name(), i));
            humans.add(seed.isHuman());
            difficulties.add(seed.isHuman() ? null : promptDifficulty(seed.name()));
        }

        PlayerSetupResult result = new PlayerSetupResult(localName, false, players, humans, difficulties);
        showSummary(result);
        return result;
    }

    private int promptTotalPlayers(String localName) {
        System.out.println(WHITE + "  You are playing as: " + GOLD + BOLD + localName + RESET);
        System.out.println();
        return identityPrompts.promptTotalPlayers();
    }

    private void addCpuPlayers(int count, int referenceAge, List<PlayerSeed> playerSeeds) {
        String[] cpuNames = {"CPU-1", "CPU-2", "CPU-3"};
        for (int i = 0; i < count; i++) {
            playerSeeds.add(new PlayerSeed(cpuNames[i], randomNearbyAge(referenceAge), false));
        }
    }

    private String promptDifficulty(String cpuName) {
        while (true) {
            System.out.println(WHITE + "  Select difficulty for " + CYAN + cpuName + RESET + ":");
            System.out.println("    " + GREEN + "1" + RESET + "  Easy");
            System.out.println("    " + RED   + "2" + RESET + "  Hard");
            System.out.print(WHITE + "  Choice (1-2): " + RESET);
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": return "EASY";
                case "2": return "HARD";
                default:
                    printError("Please enter 1 or 2.");
                    sleep(800);
            }
        }
    }

    private int randomNearbyAge(int referenceAge) {
        int minAge = Math.max(1, referenceAge - 5);
        int maxAge = Math.min(120, referenceAge + 5);
        return java.util.concurrent.ThreadLocalRandom.current().nextInt(minAge, maxAge + 1);
    }

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
            String name   = result.players.get(i).getName();
            boolean human = result.isHuman.get(i);
            String diff   = result.aiDifficulties.get(i);
            String tag    = human ? GREEN + "[Human]" + RESET : BLUE + "[CPU - " + diff + "]" + RESET;
            String youTag = (i == 0) ? GOLD + " ← you" + RESET : "";
            System.out.println("    " + CYAN + (i + 1) + ". " + RESET + WHITE + name + RESET + "  " + tag + youTag);
        }

        System.out.println();
        System.out.print(GREEN + BOLD + "  Press Enter to start the game... " + RESET);
        waitForEnter();
    }

    private void printHeader(String title, String subtitle) {
        System.out.println();
        System.out.println(GOLD + BOLD + "  " + title + RESET);
        System.out.println(DIM  + WHITE + "  " + subtitle + RESET);
        System.out.println(DIM  + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    private record PlayerSeed(String name, int age, boolean isHuman) {}
}

package edu.cs102.g04t06.game.presentation.network;

import java.util.Scanner;

import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;
import edu.cs102.g04t06.game.presentation.shared.PlayerIdentityPrompts;

/**
 * Simple host/join prompts for LAN sessions.
 */
public class LanSetupUI implements ThemeStyleSheet {
    private final Scanner scanner = new Scanner(System.in);
    private final PlayerIdentityPrompts identityPrompts = new PlayerIdentityPrompts(scanner);
    private static final int BOX_WIDTH = 50;

    /**
     * Collected host-side setup values for a LAN session.
     */
    public static final class HostSetup {
        public final String hostPlayerName;
        public final int hostPlayerAge;
        public final int port;
        public final int totalPlayers;

        public HostSetup(String hostPlayerName, int hostPlayerAge, int port, int totalPlayers) {
            this.hostPlayerName = hostPlayerName;
            this.hostPlayerAge = hostPlayerAge;
            this.port = port;
            this.totalPlayers = totalPlayers;
        }
    }

    /**
     * Collected client-side setup values for joining a LAN session.
     */
    public static final class JoinSetup {
        public final String playerName;
        public final int playerAge;
        public final String hostAddress;
        public final int port;

        public JoinSetup(String playerName, int playerAge, String hostAddress, int port) {
            this.playerName = playerName;
            this.playerAge = playerAge;
            this.hostAddress = hostAddress;
            this.port = port;
        }
    }

    /**
     * Prompts for the settings required to host a LAN game.
     *
     * @return the chosen host setup values
     */
    public HostSetup promptHostSetup() {
        clearScreen();
        printHeader("LAN SETUP", "Host Game");
        System.out.println(WHITE + "  Mode: " + RESET + BLUE + "Online (Host)" + RESET);
        System.out.println();
        String name = identityPrompts.promptName("Host player name");
        int age = identityPrompts.promptBirthdayAsAge(name);
        int port = promptInt("Port", 1024, 65535);
        int totalPlayers = identityPrompts.promptTotalPlayers();
        return new HostSetup(name, age, port, totalPlayers);
    }

    /**
     * Prompts for the settings required to join a LAN game.
     *
     * @return the chosen join setup values
     */
    public JoinSetup promptJoinSetup() {
        clearScreen();
        printHeader("LAN SETUP", "Join Game");
        System.out.println(WHITE + "  Mode: " + RESET + BLUE + "Online (Client)" + RESET);
        System.out.println();
        String name = identityPrompts.promptName("Player name");
        String hostAddress = promptNonBlank("Host IP / hostname");
        int port = promptInt("Port", 1024, 65535);
        name = validateJoinPlayerName(hostAddress, port, name);
        int age = identityPrompts.promptBirthdayAsAge(name);
        return new JoinSetup(name, age, hostAddress, port);
    }

    /**
     * Prints a lobby status update to the console.
     *
     * @param message the status message to show
     */
    public void showLobbyStatus(String message) {
        System.out.println(CYAN + message + RESET);
    }

    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    private void printHeader(String title, String subtitle) {
        System.out.println();
        System.out.println(GOLD + BOLD + "  " + title + RESET);
        System.out.println(DIM + WHITE + "  " + subtitle + RESET);
        System.out.println(DIM + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    private String promptNonBlank(String label) {
        while (true) {
            System.out.print(WHITE + "  " + label + ": " + RESET);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) {
                return value;
            }
            System.out.println(RED + "  Value cannot be empty." + RESET);
        }
    }

    private int promptInt(String label, int min, int max) {
        while (true) {
            System.out.print(WHITE + "  " + label + ": " + RESET);
            String value = scanner.nextLine().trim();
            try {
                int parsed = Integer.parseInt(value);
                if (parsed < min || parsed > max) {
                    throw new NumberFormatException();
                }
                return parsed;
            } catch (NumberFormatException e) {
                System.out.println(RED + "  Enter a number between "
                        + min + " and " + max + "." + RESET);
            }
        }
    }

    private String validateJoinPlayerName(String hostAddress, int port, String name) {
        while (true) {
            String validationError = LanGameClient.validatePlayerName(hostAddress, port, name);
            if (validationError == null) {
                return name;
            }
            System.out.println(RED + "  ✖  " + validationError + RESET);
            name = identityPrompts.promptName("Player name");
        }
    }
}

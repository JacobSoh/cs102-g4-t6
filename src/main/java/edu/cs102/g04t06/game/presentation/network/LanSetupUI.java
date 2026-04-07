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

        /**
         * Creates the collected host-side setup values.
         *
         * @param hostPlayerName the local host player's name
         * @param hostPlayerAge the local host player's age
         * @param port the listening port to bind
         * @param totalPlayers the total player count for the match
         */
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

        /**
         * Creates the collected client-side join values.
         *
         * @param playerName the joining player's name
         * @param playerAge the joining player's age
         * @param hostAddress the host IP address or hostname
         * @param port the host port
         */
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
        String hostAddress = promptNonBlank("Host IP / hostname");
        int port = promptInt("Port", 1024, 65535);
        String name = identityPrompts.promptName("Player name");
        while (true) {
            LanGameClient.JoinValidationResult result =
                    LanGameClient.validateJoinRequest(hostAddress, port, name);
            if (result.status() == LanGameClient.JoinValidationStatus.OK) {
                break;
            }

            System.out.println(RED + "  ✖  " + result.message() + RESET);
            if (result.status() == LanGameClient.JoinValidationStatus.INVALID_HOST) {
                hostAddress = promptNonBlank("Host IP / hostname");
                port = promptInt("Port", 1024, 65535);
                continue;
            }
            name = identityPrompts.promptName("Player name");
        }
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

    /**
     * Clears the console before drawing the setup screen.
     */
    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    /**
     * Prints the shared setup header for the current mode.
     *
     * @param title the main title text
     * @param subtitle the contextual subtitle text
     */
    private void printHeader(String title, String subtitle) {
        System.out.println();
        System.out.println(GOLD + BOLD + "  " + title + RESET);
        System.out.println(DIM + WHITE + "  " + subtitle + RESET);
        System.out.println(DIM + WHITE + "  " + "─".repeat(BOX_WIDTH) + RESET);
        System.out.println();
    }

    /**
     * Prompts until a non-blank string is entered.
     *
     * @param label the field label to show
     * @return the trimmed non-blank value
     */
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

    /**
     * Prompts until a valid integer within the given range is entered.
     *
     * @param label the field label to show
     * @param min the inclusive minimum accepted value
     * @param max the inclusive maximum accepted value
     * @return the parsed integer
     */
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
}

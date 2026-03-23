package edu.cs102.g04t06.game.presentation.network;

import java.util.Scanner;

import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;

/**
 * Simple host/join prompts for LAN sessions.
 */
public class LanSetupUI implements ThemeStyleSheet {
    private final Scanner scanner = new Scanner(System.in);

    public static final class HostSetup {
        public final String hostPlayerName;
        public final int port;
        public final int totalPlayers;

        public HostSetup(String hostPlayerName, int port, int totalPlayers) {
            this.hostPlayerName = hostPlayerName;
            this.port = port;
            this.totalPlayers = totalPlayers;
        }
    }

    public static final class JoinSetup {
        public final String playerName;
        public final String hostAddress;
        public final int port;

        public JoinSetup(String playerName, String hostAddress, int port) {
            this.playerName = playerName;
            this.hostAddress = hostAddress;
            this.port = port;
        }
    }

    public HostSetup promptHostSetup() {
        clearScreen();
        printHeader("HOST LAN GAME");
        String name = promptNonBlank("Host player name");
        int port = promptInt("Port", 1024, 65535);
        int totalPlayers = promptInt("Total players (2-4)", 2, 4);
        return new HostSetup(name, port, totalPlayers);
    }

    public JoinSetup promptJoinSetup() {
        clearScreen();
        printHeader("JOIN LAN GAME");
        String name = promptNonBlank("Player name");
        String hostAddress = promptNonBlank("Host IP / hostname");
        int port = promptInt("Port", 1024, 65535);
        return new JoinSetup(name, hostAddress, port);
    }

    public void showLobbyStatus(String message) {
        System.out.println(CYAN + message + RESET);
    }

    private void clearScreen() {
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println(GOLD + BOLD + "  " + title + RESET);
        System.out.println(DIM + WHITE + "  " + "─".repeat(48) + RESET);
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
}

package edu.cs102.g04t06.game.presentation.network;

import java.util.Scanner;

import edu.cs102.g04t06.game.presentation.console.GameBoardUI;
import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;

/**
 * Console UI controller for a LAN client session.
 */
public class LanSessionUI implements ThemeStyleSheet {
    private final Scanner scanner = new Scanner(System.in);
    private final GameBoardUI boardUI = new GameBoardUI();

    public NetworkMessage handle(NetworkMessage message) {
        return switch (message.type) {
            case JOIN_ACCEPTED, LOBBY_STATE, START_GAME, GAME_STATE, INFO, ERROR, PLAYER_DISCONNECTED, GAME_OVER -> {
                render(message);
                yield null;
            }
            case REQUEST_COMMAND -> promptCommand(message);
            case REQUEST_RETURN_GEMS -> promptGemReturn(message);
            default -> null;
        };
    }

    public void showConnectionClosed() {
        System.out.println(RED + "Connection to host was closed." + RESET);
    }

    public void showJoinFailure(String errorMessage) {
        System.out.println(RED + "Failed to join LAN game: " + errorMessage + RESET);
    }

    private void render(NetworkMessage message) {
        if (message.state != null) {
            String statusMessage = (message.message != null && !message.message.isBlank())
                    ? message.message
                    : "Waiting for turn...";
            boardUI.displayReadOnlyState(message.state, statusMessage, message.logEntries);
            System.out.println();
        }

        if (message.state == null && message.message != null && !message.message.isBlank()) {
            printStatusLine(message);
        }
    }

    private NetworkMessage promptCommand(NetworkMessage message) {
        render(message);
        System.out.print(WHITE + "Command > " + RESET);
        NetworkMessage reply = NetworkMessage.of(MessageType.MOVE_SUBMIT, null);
        reply.command = scanner.nextLine().trim();
        return reply;
    }

    private NetworkMessage promptGemReturn(NetworkMessage message) {
        render(message);
        int excess = message.excessCount == null ? 0 : message.excessCount;
        System.out.print(WHITE + "Return " + excess + " gem(s) > " + RESET);
        NetworkMessage reply = NetworkMessage.of(MessageType.RETURN_GEMS, null);
        reply.command = scanner.nextLine().trim();
        return reply;
    }

    private void printStatusLine(NetworkMessage message) {
        String text = safeMessage(message);
        if (text.isBlank()) {
            return;
        }
        String color = switch (message.type) {
            case JOIN_ACCEPTED -> GREEN;
            case ERROR -> RED;
            case PLAYER_DISCONNECTED -> YELLOW;
            case GAME_OVER -> GOLD + BOLD;
            default -> CYAN;
        };
        System.out.println(color + text + RESET);
    }

    private String safeMessage(NetworkMessage message) {
        return message.message == null ? "" : message.message;
    }
}

package edu.cs102.g04t06.game.presentation.network;

import edu.cs102.g04t06.game.presentation.console.GameBoardUI;
import edu.cs102.g04t06.game.presentation.console.ThemeStyleSheet;
import edu.cs102.g04t06.game.rules.GameState;

/**
 * Console UI controller for a LAN client session.
 */
public class LanSessionUI implements ThemeStyleSheet {
    private final GameBoardUI boardUI = new GameBoardUI();
    private final String localPlayerName;
    private String inlineError = "";

    public LanSessionUI(String localPlayerName) {
        this.localPlayerName = localPlayerName;
        boardUI.setPerspectivePlayerName(localPlayerName);
    }

    public NetworkMessage handle(NetworkMessage message) {
        if (message.type == MessageType.ERROR && message.state != null) {
            inlineError = safeMessage(message);
            return null;
        }

        return switch (message.type) {
            case JOIN_ACCEPTED, LOBBY_STATE, START_GAME, GAME_STATE, INFO, ERROR, PLAYER_DISCONNECTED, GAME_OVER -> {
                render(message);
                yield null;
            }
            case REQUEST_COMMAND -> promptCommand(message);
            case REQUEST_RETURN_GEMS -> promptGemReturn(message);
            case REQUEST_NOBLE_SELECTION -> promptNobleSelection(message);
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
            String statusMessage = inlineError;
            String statusColor = RED + BOLD;
            if (statusMessage == null || statusMessage.isBlank()) {
                statusMessage = defaultStatusMessage(message);
                statusColor = CYAN;
            }
            boardUI.displayNetworkState(message.state, statusMessage, statusColor, message.logEntries);
        }

        if (message.state == null && message.message != null && !message.message.isBlank()) {
            printStatusLine(message);
        }
    }

    private NetworkMessage promptCommand(NetworkMessage message) {
        String statusMessage = inlineError;
        String statusColor = RED + BOLD;
        if (statusMessage == null || statusMessage.isBlank()) {
            statusMessage = message.message;
            statusColor = CYAN;
        }
        String input = boardUI.promptNetworkTurn(message.state, statusMessage, statusColor, message.logEntries);
        inlineError = "";
        if (isDisconnectCommand(input)) {
            return NetworkMessage.of(MessageType.DISCONNECT_REQUEST, "Client requested disconnect.");
        }
        NetworkMessage reply = NetworkMessage.of(MessageType.MOVE_SUBMIT, null);
        reply.command = input;
        return reply;
    }

    private NetworkMessage promptGemReturn(NetworkMessage message) {
        int excess = message.excessCount == null ? 0 : message.excessCount;
        String statusMessage = inlineError;
        String statusColor = RED + BOLD;
        if (statusMessage == null || statusMessage.isBlank()) {
            statusMessage = "Return " + excess + " gem(s).";
            statusColor = YELLOW;
        }
        String input = boardUI.promptNetworkTurn(message.state, statusMessage, statusColor, message.logEntries);
        inlineError = "";
        if (isDisconnectCommand(input)) {
            return NetworkMessage.of(MessageType.DISCONNECT_REQUEST, "Client requested disconnect.");
        }
        NetworkMessage reply = NetworkMessage.of(MessageType.RETURN_GEMS, null);
        reply.command = input;
        return reply;
    }

    private NetworkMessage promptNobleSelection(NetworkMessage message) {
        String statusMessage = inlineError;
        String statusColor = RED + BOLD;
        if (statusMessage == null || statusMessage.isBlank()) {
            statusMessage = safeMessage(message);
            statusColor = YELLOW;
        }
        String input = boardUI.promptNetworkTurn(message.state, statusMessage, statusColor, message.logEntries);
        inlineError = "";
        if (isDisconnectCommand(input)) {
            return NetworkMessage.of(MessageType.DISCONNECT_REQUEST, "Client requested disconnect.");
        }
        NetworkMessage reply = NetworkMessage.of(MessageType.NOBLE_SELECTION, null);
        reply.command = input;
        return reply;
    }

    private void printStatusLine(NetworkMessage message) {
        String text = safeMessage(message);
        if (text.isBlank()) {
            return;
        }
        if (message.type == MessageType.ERROR && message.state != null) {
            inlineError = text;
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

    private String defaultStatusMessage(NetworkMessage message) {
        if (message == null || message.state == null) {
            return "Waiting for turn...";
        }
        if (message.type == MessageType.GAME_OVER) {
            return safeMessage(message);
        }
        GameState state = message.state;
        String currentPlayerName = state.getCurrentPlayer().getName();
        if (currentPlayerName.equals(localPlayerName)) {
            return "Your turn.";
        }
        return "Waiting for " + currentPlayerName + " to play.";
    }

    private boolean isDisconnectCommand(String input) {
        return input != null && input.equalsIgnoreCase("q");
    }
}

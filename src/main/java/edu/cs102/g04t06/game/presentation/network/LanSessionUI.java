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

    /**
     * Creates the console controller for one joined LAN player.
     *
     * @param localPlayerName the player name to render from the local perspective
     */
    public LanSessionUI(String localPlayerName) {
        this.localPlayerName = localPlayerName;
        boardUI.setPerspectivePlayerName(localPlayerName);
    }

    /**
     * Handles a single inbound network message and optionally produces a reply.
     *
     * @param message the message received from the host
     * @return the reply to send back, or {@code null} when no reply is required
     */
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

    /**
     * Notifies the player that the host connection ended unexpectedly.
     */
    public void showConnectionClosed() {
        System.out.println(RED + "Connection to host was closed." + RESET);
    }

    /**
     * Displays a join failure message to the local player.
     *
     * @param errorMessage the failure reason reported by the connection attempt
     */
    public void showJoinFailure(String errorMessage) {
        System.out.println(RED + "Failed to join LAN game: " + errorMessage + RESET);
    }

    /**
     * Renders the latest stateful or status-only message from the host.
     *
     * @param message the message to display
     */
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

    /**
     * Prompts the local player for their main turn command.
     *
     * @param message the turn request from the host
     * @return the reply message containing either a move or a disconnect request
     */
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

    /**
     * Prompts the local player to return excess gems.
     *
     * @param message the gem-return request from the host
     * @return the reply message containing either returned gems input or a disconnect request
     */
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

    /**
     * Prompts the local player to choose a noble after a qualifying move.
     *
     * @param message the noble-selection request from the host
     * @return the reply message containing either the selection or a disconnect request
     */
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

    /**
     * Prints a status-only line for lobby, info, error, or game-over messages.
     *
     * @param message the message to print
     */
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

    /**
     * Returns a non-null version of the message text.
     *
     * @param message the network message to inspect
     * @return the message text, or an empty string when absent
     */
    private String safeMessage(NetworkMessage message) {
        return message.message == null ? "" : message.message;
    }

    /**
     * Builds the default status line shown above the rendered board.
     *
     * @param message the latest message received from the host
     * @return the status text appropriate for the local player's perspective
     */
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

    /**
     * Determines whether the local input requests leaving the LAN session.
     *
     * @param input the raw console input
     * @return {@code true} when the player entered the quit shortcut
     */
    private boolean isDisconnectCommand(String input) {
        return input != null && input.equalsIgnoreCase("q");
    }
}

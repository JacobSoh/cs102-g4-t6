package edu.cs102.g04t06.game.presentation.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Joiner-side LAN client.
 */
public class LanGameClient {
    /**
     * Outcome of a pre-join validation attempt against a host.
     */
    public enum JoinValidationStatus {
        OK,
        INVALID_HOST,
        INVALID_NAME
    }

    /**
     * Structured result returned by join validation.
     */
    public static final class JoinValidationResult {
        private final JoinValidationStatus status;
        private final String message;

        /**
         * Creates a validation result.
         *
         * @param status the validation outcome
         * @param message the user-facing explanation, or {@code null} on success
         */
        public JoinValidationResult(JoinValidationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        /**
         * Returns the validation outcome.
         *
         * @return the validation status
         */
        public JoinValidationStatus status() {
            return status;
        }

        /**
         * Returns the explanatory message for the validation outcome.
         *
         * @return the validation message, or {@code null} when validation succeeded
         */
        public String message() {
            return message;
        }
    }

    private final String playerName;
    private final int playerAge;
    private final String hostAddress;
    private final int port;
    private final LanSessionUI sessionUI;

    /**
     * Creates a LAN client for a remote player.
     *
     * @param playerName the joining player's display name
     * @param playerAge the joining player's age used for turn ordering
     * @param hostAddress the host IP address or hostname
     * @param port the host port
     */
    public LanGameClient(String playerName, int playerAge, String hostAddress, int port) {
        this.playerName = playerName;
        this.playerAge = playerAge;
        this.hostAddress = hostAddress;
        this.port = port;
        this.sessionUI = new LanSessionUI(playerName);
    }

    /**
     * Connects to the host, joins the lobby, and runs the client-side session loop.
     */
    public void run() {
        try (Socket socket = new Socket(hostAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            NetworkMessage join = NetworkMessage.of(MessageType.JOIN_REQUEST, null);
            join.playerName = playerName;
            join.playerAge = playerAge;
            NetworkProtocol.send(writer, join);

            NetworkMessage message;
            boolean joinedLobby = false;
            while ((message = NetworkProtocol.read(reader)) != null) {
                if (message.type == MessageType.JOIN_ACCEPTED) {
                    joinedLobby = true;
                }
                NetworkMessage response = sessionUI.handle(message);
                if (response != null) {
                    NetworkProtocol.send(writer, response);
                    if (response.type == MessageType.DISCONNECT_REQUEST) {
                        return;
                    }
                }
                if (message.type == MessageType.ERROR && !joinedLobby) {
                    return;
                }
                if (message.type == MessageType.GAME_OVER) {
                    break;
                }
            }

            if (message == null && joinedLobby) {
                sessionUI.showConnectionClosed();
            }
        } catch (IOException e) {
            sessionUI.showJoinFailure(e.getMessage());
        }
    }

    /**
     * Asks the host whether a player name is currently available.
     *
     * @param hostAddress the host address to contact
     * @param port the host port
     * @param playerName the proposed player name
     * @return null when the name is available, otherwise an error message
     */
    public static String validatePlayerName(String hostAddress, int port, String playerName) {
        JoinValidationResult result = validateJoinRequest(hostAddress, port, playerName);
        return result.status() == JoinValidationStatus.OK ? null : result.message();
    }

    /**
     * Validates that the host is reachable and the proposed player name is accepted.
     *
     * @param hostAddress the host address to contact
     * @param port the host port
     * @param playerName the proposed player name
     * @return a structured validation result
     */
    public static JoinValidationResult validateJoinRequest(String hostAddress, int port, String playerName) {
        try (Socket socket = new Socket(hostAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            NetworkMessage probe = NetworkMessage.of(MessageType.CHECK_NAME, null);
            probe.playerName = playerName;
            NetworkProtocol.send(writer, probe);

            NetworkMessage response = NetworkProtocol.read(reader);
            if (response == null) {
                // Some hosts close the short-lived validation probe immediately after replying.
                // Treat EOF here as a soft success and let the real join request remain authoritative.
                return new JoinValidationResult(JoinValidationStatus.OK, null);
            }
            if (response.type == MessageType.ERROR) {
                return new JoinValidationResult(JoinValidationStatus.INVALID_NAME,
                        response.message == null ? "Player name validation failed." : response.message);
            }
            return new JoinValidationResult(JoinValidationStatus.OK, null);
        } catch (IOException e) {
            return new JoinValidationResult(JoinValidationStatus.INVALID_HOST,
                    "Failed to contact host: " + e.getMessage());
        }
    }
}

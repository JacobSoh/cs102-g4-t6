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
    private final String playerName;
    private final int playerAge;
    private final String hostAddress;
    private final int port;
    private final LanSessionUI sessionUI;

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
        try (Socket socket = new Socket(hostAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            NetworkMessage probe = NetworkMessage.of(MessageType.CHECK_NAME, null);
            probe.playerName = playerName;
            NetworkProtocol.send(writer, probe);

            NetworkMessage response = NetworkProtocol.read(reader);
            if (response == null) {
                return "Host closed the connection while validating the player name.";
            }
            if (response.type == MessageType.ERROR) {
                return response.message == null ? "Player name validation failed." : response.message;
            }
            return null;
        } catch (IOException e) {
            return "Failed to contact host: " + e.getMessage();
        }
    }
}

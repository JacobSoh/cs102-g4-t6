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
    private final String hostAddress;
    private final int port;
    private final LanSessionUI sessionUI = new LanSessionUI();

    public LanGameClient(String playerName, String hostAddress, int port) {
        this.playerName = playerName;
        this.hostAddress = hostAddress;
        this.port = port;
    }

    public void run() {
        try (Socket socket = new Socket(hostAddress, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            NetworkMessage join = NetworkMessage.of(MessageType.JOIN_REQUEST, null);
            join.playerName = playerName;
            NetworkProtocol.send(writer, join);

            NetworkMessage message;
            while ((message = NetworkProtocol.read(reader)) != null) {
                NetworkMessage response = sessionUI.handle(message);
                if (response != null) {
                    NetworkProtocol.send(writer, response);
                }
                if (message.type == MessageType.GAME_OVER) {
                    break;
                }
            }

            if (message == null) {
                sessionUI.showConnectionClosed();
            }
        } catch (IOException e) {
            sessionUI.showJoinFailure(e.getMessage());
        }
    }
}

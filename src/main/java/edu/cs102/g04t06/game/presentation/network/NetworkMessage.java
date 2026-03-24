package edu.cs102.g04t06.game.presentation.network;

import java.util.List;

import edu.cs102.g04t06.game.rules.GameState;

/**
 * Generic transport envelope for LAN multiplayer messages.
 */
public class NetworkMessage {
    public MessageType type;
    public String message;
    public String logMessage;
    public List<String> logEntries;
    public String playerName;
    public String command;
    public Integer playerIndex;
    public Integer expectedPlayers;
    public Integer excessCount;
    public List<String> players;
    public GameState state;

    public static NetworkMessage of(MessageType type, String message) {
        NetworkMessage payload = new NetworkMessage();
        payload.type = type;
        payload.message = message;
        return payload;
    }
}

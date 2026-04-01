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
    public Integer playerAge;
    public String command;
    public Integer playerIndex;
    public Integer expectedPlayers;
    public Integer excessCount;
    public List<String> players;
    public GameState state;

    /**
     * Creates a message with the given type and human-readable message text.
     *
     * @param type the message type
     * @param message the message text
     * @return a new network message envelope
     */
    public static NetworkMessage of(MessageType type, String message) {
        NetworkMessage payload = new NetworkMessage();
        payload.type = type;
        payload.message = message;
        return payload;
    }
}

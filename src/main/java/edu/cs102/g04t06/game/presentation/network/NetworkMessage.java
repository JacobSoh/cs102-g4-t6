package edu.cs102.g04t06.game.presentation.network;

import java.util.List;

import edu.cs102.g04t06.game.rules.GameState;

/**
 * Generic transport envelope for LAN multiplayer messages.
 */
public class NetworkMessage {
    /** The protocol message discriminator. */
    public MessageType type;
    /** A short human-readable status or prompt. */
    public String message;
    /** The latest public action summary, when supplied. */
    public String logMessage;
    /** The recent shared event log shown in the UI. */
    public List<String> logEntries;
    /** The player name associated with the message. */
    public String playerName;
    /** The player's age used for initial turn ordering. */
    public Integer playerAge;
    /** Raw command input returned by a client. */
    public String command;
    /** The assigned player index for a newly joined client. */
    public Integer playerIndex;
    /** The expected total player count for the session. */
    public Integer expectedPlayers;
    /** The number of excess gems a player must return. */
    public Integer excessCount;
    /** The ordered player names currently in the lobby or match. */
    public List<String> players;
    /** The authoritative game state snapshot attached to the message. */
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

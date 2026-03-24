package edu.cs102.g04t06.game.presentation.network;

/**
 * Message types used by the LAN multiplayer JSON line protocol.
 */
public enum MessageType {
    JOIN_REQUEST,
    JOIN_ACCEPTED,
    LOBBY_STATE,
    START_GAME,
    GAME_STATE,
    REQUEST_COMMAND,
    REQUEST_RETURN_GEMS,
    MOVE_SUBMIT,
    RETURN_GEMS,
    PLAYER_DISCONNECTED,
    INFO,
    ERROR,
    GAME_OVER
}

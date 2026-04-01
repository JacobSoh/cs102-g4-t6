package edu.cs102.g04t06.game.presentation.network;

/**
 * Message types used by the LAN multiplayer JSON line protocol.
 */
public enum MessageType {
    CHECK_NAME,
    JOIN_REQUEST,
    JOIN_ACCEPTED,
    LOBBY_STATE,
    START_GAME,
    GAME_STATE,
    REQUEST_COMMAND,
    REQUEST_RETURN_GEMS,
    REQUEST_NOBLE_SELECTION,
    MOVE_SUBMIT,
    RETURN_GEMS,
    NOBLE_SELECTION,
    PLAYER_DISCONNECTED,
    INFO,
    ERROR,
    GAME_OVER
}

package edu.cs102.g04t06.game.presentation.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility for line-delimited JSON messaging.
 */
public final class NetworkProtocol {
    private static final Gson GSON = new GsonBuilder().create();

    private NetworkProtocol() {
    }

    /**
     * Serializes and sends a message on the provided writer.
     *
     * @param writer the destination writer
     * @param message the message to send
     */
    public static void send(PrintWriter writer, NetworkMessage message) {
        writer.println(GSON.toJson(message));
    }

    /**
     * Reads and deserializes a single line-delimited JSON message.
     *
     * @param reader the source reader
     * @return the parsed message, or null on end-of-stream
     * @throws IOException if the read fails
     */
    public static NetworkMessage read(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return GSON.fromJson(line, NetworkMessage.class);
    }
}

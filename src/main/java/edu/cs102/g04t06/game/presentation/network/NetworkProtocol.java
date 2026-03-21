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

    public static void send(PrintWriter writer, NetworkMessage message) {
        writer.println(GSON.toJson(message));
    }

    public static NetworkMessage read(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return GSON.fromJson(line, NetworkMessage.class);
    }
}

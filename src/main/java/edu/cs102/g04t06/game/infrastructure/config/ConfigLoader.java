package edu.cs102.g04t06.game.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Properties;

import edu.cs102.g04t06.game.rules.entities.GemColor;

public class ConfigLoader {
    private Properties properties;

    public ConfigLoader(String configPath) {
        this.properties = new Properties();
        try (InputStream cf = getClass().getClassLoader().getResourceAsStream(configPath)) {
            if (cf == null) {
                throw new RuntimeException("File does not exists: " + configPath);
            }
            this.properties.load(cf);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file", e);
        }
    }

    public int getWinningPoints() {
        return Integer.parseInt(this.properties.getProperty("game.winningPoints"));
    }

    public int getGemCount(int playerCount, GemColor color) {
        StringBuilder sb = new StringBuilder();
        Formatter sf = new Formatter(sb);
        sf.format("game.%dplayers.%s", playerCount, color.toString().toLowerCase());
        return Integer.parseInt(this.properties.getProperty(sf.toString()));
    }

    public int getMaxReservedCards(int playerCount, GemColor color) {
        return Integer.parseInt(this.properties.getProperty("game.maxReservedCards"));
    }

    public int getMaxGemsPerPlayer(int playerCount, GemColor color) {
        return Integer.parseInt(this.properties.getProperty("game.maxGemsPerPlayer"));
    }

    public HashMap<String, String> getDataFilePath() {
        HashMap<String, String> filePaths = new HashMap<>();
        String base = "data.file.";
        String cardPath = this.properties.getProperty(base + "card");
        String noblePath = this.properties.getProperty(base + "noble");
        filePaths.put("card", cardPath);
        filePaths.put("noblePath", noblePath);
        return filePaths;
    }
}
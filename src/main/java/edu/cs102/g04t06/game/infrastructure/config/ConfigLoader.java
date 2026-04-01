package edu.cs102.g04t06.game.infrastructure.config;

// Edited by GPT-5 (Codex)

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Properties;

import edu.cs102.g04t06.game.rules.entities.GemColor;

/**
 * Loads game configuration values from a classpath {@code .properties} resource.
 *
 * <p>Expected keys include:</p>
 * <ul>
 *   <li>{@code game.{N}players.{color}} where {@code N} is 2-4 and color is a
 *       lowercase {@link GemColor} name</li>
 *   <li>{@code data.file.card}</li>
 *   <li>{@code data.file.noble}</li>
 * </ul>
 */
public class ConfigLoader {
    private Properties properties;

    /**
     * Creates a loader for the given classpath config file.
     *
     * @param configPath classpath-relative path to the properties file
     * @throws RuntimeException if the resource is missing or cannot be read
     */
    public ConfigLoader(String configPath) {
        this.properties = new Properties();
        try (InputStream cf = getClass().getResourceAsStream("/" + configPath)) {
            if (cf == null) {
                throw new FileNotFoundException("File does not exists: " + configPath);
            }
            this.properties.load(cf);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file", e);
        }
    }

    /**
     * Returns the number of gems available for a given player count and color.
     *
     * <p>Player counts greater than 4 are clamped to 4.</p>
     *
     * @param playerCount number of players (2-4; values &gt; 4 are clamped)
     * @param color gem color to look up
     * @return gem count for the given player count and color
     */
    public int getGemCount(int playerCount, GemColor color) {
        StringBuilder sb = new StringBuilder();
        Formatter sf = new Formatter(sb);
        sf.format("game.%dplayers.%s", playerCount >= 4 ? 4 : playerCount, color.toString().toLowerCase());
        return Integer.parseInt(this.properties.getProperty(sf.toString()));
    }

    /**
     * Returns data file paths defined in the configuration.
     *
     * <p>Keys in the returned map:</p>
     * <ul>
     *   <li>{@code card}</li>
     *   <li>{@code noblePath}</li>
     * </ul>
     *
     * @return map of data file paths
     */
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

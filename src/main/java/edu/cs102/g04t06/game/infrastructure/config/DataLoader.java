package edu.cs102.g04t06.game.infrastructure.config;

import java.util.List;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.Noble;

/**
 * Interface for loading game data from an external source.
 * Implementations may read from CSV, JSON, databases, etc.
 */
public interface DataLoader {
    /**
     * Loads all level-1 development cards from the given file path.
     *
     * @param filePath path to the data file
     * @return list of level-1 cards
     */
    List<Card> loadLevel1Cards(String filePath);

    /**
     * Loads all level-2 development cards from the given file path.
     *
     * @param filePath path to the data file
     * @return list of level-2 cards
     */
    List<Card> loadLevel2Cards(String filePath);

    /**
     * Loads all level-3 development cards from the given file path.
     *
     * @param filePath path to the data file
     * @return list of level-3 cards
     */
    List<Card> loadLevel3Cards(String filePath);

    /**
     * Loads all nobles from the given file path.
     *
     * @param filePath path to the data file
     * @return list of nobles
     */
    List<Noble> loadNobles(String filePath);
}

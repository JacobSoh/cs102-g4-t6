package edu.cs102.g04t06.game.execution;

import java.util.HashMap;
import java.util.List;

import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.infrastructure.config.ExcelDataLoader;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.Noble;

/**
 * Builds fully initialised game state instances from configured data sources.
 */
public class GameStateFactory {

    public GameState createInitialGameState(int totalPlayers, List<String> playerNames) {
        ConfigLoader config = new ConfigLoader("config.properties");
        HashMap<String, String> paths = config.getDataFilePath();
        String cardPath = paths.get("card");
        String noblePath = paths.get("noblePath");

        List<Card> level1 = ExcelDataLoader.loadLevel1Cards(cardPath);
        List<Card> level2 = ExcelDataLoader.loadLevel2Cards(cardPath);
        List<Card> level3 = ExcelDataLoader.loadLevel3Cards(cardPath);
        List<Noble> allNobles = ExcelDataLoader.loadNobles(noblePath);

        return new GameEngine().initializeGame(
                totalPlayers, playerNames, config, level1, level2, level3, allNobles);
    }
}

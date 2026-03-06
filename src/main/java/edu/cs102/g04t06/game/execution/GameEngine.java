package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class GameEngine{

    private final GameRules gameRules;

    public GameEngine(){
        this.gameRules = new GameRules();
    }

    /**
     * initializeGame
     * creates a new gamestate 
     * - players in turn order
     * - shuffles and split cards into deck
     * - selects nobles
     * - sets up gem bank based on player count
     */

    /**
     * Initializes a new game state with shuffled decks, selected nobles,
     * and a gem bank sized for the number of players.
     *
     * @param playerCount  number of players (2-4)
     * @param playerNames  list of player names in turn order
     * @param config       config loader for gem counts
     * @param level1       full list of level 1 cards
     * @param level2       full list of level 2 cards
     * @param level3       full list of level 3 cards
     * @param allNobles    full list of all nobles
     * @return a fully initialized GameState ready to play
     */
    public GameState initializeGame(
        int playerCount,
        List<String> playerNames,
        ConfigLoader config,
        List<Card> level1,
        List<Card> level2,
        List<Card> level3,
        List<Noble> allNobles
    ){
        //create new players in turn order
        List<Player> players = new ArrayList<>();
        for(int i = 0; i < playerCount; i++){
            players.add(new Player(playerNames.get(i), i + 1));
        }

        //shuffle decks
        List<Card> shuffledLevel1 = new ArrayList<>(level1);
        List<Card> shuffledLevel2 = new ArrayList<>(level2);
        List<Card> shuffledLevel3 = new ArrayList<>(level3);
        Collections.shuffle(shuffledLevel1);
        Collections.shuffle(shuffledLevel2);
        Collections.shuffle(shuffledLevel3);

        //select nobles, nobleCount = playerCount + 1 
        List<Noble> selectedNobles = new ArrayList<>(allNobles);
        Collections.shuffle(selectedNobles);
        List<Noble> gameNobles = selectedNobles.subList(0, playerCount + 1);

        //build gemBank
        // 2 players: 4 , 3 players: 5, 4 players: 7
        // Always 5 GOLD regardless of player count
        int regularGemCount = getRegularGemCount(playerCount);
        Map<GemColor, Integer> bankMap = new EnumMap<>(GemColor.class);
        for(GemColor color : GemColor.values()){
            if(color == GemColor.GOLD){
                bankMap.put(color, 5);
            } else {
                bankMap.put(color, regularGemCount);
            }
        }
        GemCollection initialGems = new GemCollection(bankMap);

        //build cardmarket
        CardMarket market = new CardMarket(shuffledLevel1, shuffledLevel2, shuffledLevel3);

        //return gameState
        return new GameState(
            players,
            market,
            initialGems,
            new ArrayList<>(gameNobles),
            config.getWinningPoints()
        );
    }

    /**
     * check the win condition at the end of every turn
     * 
     * @param state current game state at the end of a turn
     * @param winningPoints the points needed to win a game
     * @return player who won the game, null if no winner yet
     */
    public Player checkWinCondition(GameState state, int winningPoints){
       return gameRules.getWinner(state.getPlayers(), winningPoints);
    }


    //moves the game to next player turn
    public List<Noble> advanceTurn(GameState state){
        Player currentPlayer = state.getCurrentPlayer();

        //find claimable nobles for current player
        List<Noble> claimableNobles = gameRules.getClaimableNobles(
            currentPlayer,
            state.getAvailableNobles()
        );

        //claim the nobles 
        //potential bug, if there is more than one claimable nobles 
        //player can choose in real game but needs to prompt UI
        //enforce player to claim the first noble for now
        if(claimableNobles.size() >= 1){
            Noble noble = claimableNobles.get(0);
            currentPlayer.claimNoble(noble);
            state.removeNoble(noble);
            claimableNobles.clear();
        }

        //wrap around if last player in a round
        boolean isLastPlayer = (state.getCurrentPlayerIndex() == state.getPlayers().size() - 1);
        if(isLastPlayer){
            Player winner = checkWinCondition(state, state.getWinningThreshold());
            if(winner != null){
                state.setGameOver(true);
                return claimableNobles; // return early when gameover
            }
        }

        state.advanceToNextPlayer();
        return claimableNobles;
    }


    //helper functions
    private int getRegularGemCount(int playerCount){
        switch (playerCount) {
            case 2: return 4;
            case 3: return 5;
            case 4: return 7;
            default: throw new IllegalArgumentException("Invalid player count!");
        }
    }

}
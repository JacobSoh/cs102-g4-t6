package edu.cs102.g04t06.game.rules;

import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;
import java.util.List;

public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private CardMarket market;
    private GemCollection gemBank;
    private List<Noble> availableNobles;

    // Constructor to set up the game
    public GameState(List<Player> players, CardMarket market, GemCollection initialGems, List<Noble> nobles) {
        this.players = players;
        this.market = market;
        this.gemBank = initialGems;
        this.availableNobles = nobles;
        this.currentPlayerIndex = 0;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public GemCollection getGemBank() {
        return gemBank;
    }

    public CardMarket getMarket() {
        return market;
    }

    // These methods allow ActionExecutor to change the bank's totals
    public void addGemsToBank(GemCollection gems) {
        this.gemBank = this.gemBank.add(gems);
    }

    public void removeGemsFromBank(GemCollection gems) {
        this.gemBank = this.gemBank.subtract(gems);
    }
}
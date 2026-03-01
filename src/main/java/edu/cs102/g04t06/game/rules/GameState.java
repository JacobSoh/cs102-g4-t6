package edu.cs102.g04t06.game.rules;

import java.util.List;

import edu.cs102.g04t06.game.exception.NobleNotAvailableException;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class GameState {

    private List<Player> players;
    private int currentPlayerIndex;
    private CardMarket market;
    private GemCollection gemBank;
    private List<Noble> availableNobles;
    private boolean gameOver;
    private int winningThreshold;

    public GameState(List<Player> players, CardMarket market, GemCollection gemBank, List<Noble> availableNobles, int winningThreshold) {
        this.players = players;
        this.market = market;
        this.gemBank = gemBank;
        this.availableNobles = availableNobles;
        this.winningThreshold = winningThreshold;
    }

    public Player getCurrentPlayer() {
        return this.players.get(this.currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public CardMarket getMarket() {
        return this.market;
    }

    public GemCollection getGemBank() {
        return this.gemBank;
    }

    public List<Noble> getAvailableNobles() {
        return this.availableNobles;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public int getWinningThreshold() {
        return this.winningThreshold;
    }

    public void advanceToNextPlayer() {
        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void removeNoble(Noble noble) throws NobleNotAvailableException {
        if (this.availableNobles.contains(noble)) this.availableNobles.remove(noble);
        else throw new NobleNotAvailableException(noble);
    }

    public void addGemsToBank(GemCollection gems) {
        this.gemBank = this.gemBank.add(gems);
    }

    public void removeGemsFromBank(GemCollection gems) {
        this.gemBank = this.gemBank.subtract(gems);
    }

}

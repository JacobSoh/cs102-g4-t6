package edu.cs102.g04t06.game.rules;

import java.util.List;

import edu.cs102.g04t06.game.exception.NobleNotAvailableException;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * represents the mutable game state for a Splendor match.
 */
public class GameState {

    private final List<Player> players;
    private int currentPlayerIndex;
    private final CardMarket market;
    private GemCollection gemBank;
    private final List<Noble> availableNobles;
    private boolean gameOver;
    private final int winningThreshold;

    /**
     * constructs a game state with players, shared resources, and victory settings.
     * @param players the players participating in the game
     * @param market the current card market
     * @param gemBank the current gems available in the bank
     * @param availableNobles the nobles currently available to be claimed
     * @param winningThreshold the score required to trigger game end
     */
    public GameState(List<Player> players, CardMarket market, GemCollection gemBank, List<Noble> availableNobles, int winningThreshold) {
        this.players = players;
        this.market = market;
        this.gemBank = gemBank;
        this.availableNobles = availableNobles;
        this.winningThreshold = winningThreshold;
    }

    /**
     * gets the player whose turn is currently active.
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return this.players.get(this.currentPlayerIndex);
    }

    /**
     * gets the index of the current player in the players list.
     * @return the current player index
     */
    public int getCurrentPlayerIndex() {
        return this.currentPlayerIndex;
    }

    /**
     * gets all players in turn order.
     * @return the players in this game state
     */
    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * gets the current card market.
     * @return the card market
     */
    public CardMarket getMarket() {
        return this.market;
    }

    /**
     * gets the current gems in the bank.
     * @return the gem bank
     */
    public GemCollection getGemBank() {
        return this.gemBank;
    }

    /**
     * gets nobles that are still available to claim.
     * @return the list of available nobles
     */
    public List<Noble> getAvailableNobles() {
        return this.availableNobles;
    }

    /**
     * checks whether the game has ended.
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return this.gameOver;
    }

    /**
     * gets the points needed to trigger game end.
     * @return the winning threshold
     */
    public int getWinningThreshold() {
        return this.winningThreshold;
    }

    /**
     * advances turn control to the next player, wrapping to index 0 when needed.
     */
    public void advanceToNextPlayer() {
        this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.players.size();
    }

    /**
     * sets whether the game is over.
     * @param gameOver true to mark the game as over, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * removes a noble from the available nobles list.
     * @param noble the noble to remove
     * @return the removed noble instance
     * @throws NobleNotAvailableException if the noble is not present in the available nobles list
     */
    public Noble removeNoble(Noble noble) throws NobleNotAvailableException {
        if (this.availableNobles.contains(noble)) {
            int indexToRemove = this.availableNobles.indexOf(noble);
            Noble nobleToRemove = this.availableNobles.get(indexToRemove);
            this.availableNobles.remove(indexToRemove);
            return nobleToRemove;
        } else throw new NobleNotAvailableException(noble);
    }

    /**
     * adds gems back into the bank.
     * @param gems the gems to add
     */
    public void addGemsToBank(GemCollection gems) {
        this.gemBank = this.gemBank.add(gems);
    }

    /**
     * removes gems from the bank.
     * @param gems the gems to remove
     */
    public void removeGemsFromBank(GemCollection gems) {
        this.gemBank = this.gemBank.subtract(gems);
    }

}

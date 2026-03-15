package edu.cs102.g04t06.game.rules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.exception.NobleNotAvailableException;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * represents the mutable game state for a Splendor match.
 */
public class GameState {

    private final List<Player> players;
    private int currentPlayerIndex;
    private int roundNumber;
    private final CardMarket market;
    private GemCollection gemBank;
    private final List<Noble> availableNobles;
    private boolean gameOver;
    private boolean finalRoundTriggered;
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
        this.roundNumber = 1;
        this.market = market != null ? market : new CardMarket(
                // level 1
                new ArrayList<>() {{
                    GemColor[] bonuses = {GemColor.WHITE, GemColor.BLUE, GemColor.GREEN, GemColor.RED, GemColor.BLACK};
                    for (int i = 0; i < 40; i++) {
                        GemColor bonus = bonuses[(i + 1) % bonuses.length];
                        int points = (i % 8 == 0) ? 1 : 0;
                        EnumMap<GemColor, Integer> req = new EnumMap<>(GemColor.class);
                        GemColor[] colors = {GemColor.WHITE, GemColor.RED, GemColor.BLUE, GemColor.GREEN, GemColor.BLACK};
                        for (int j = 0; j < 3; j++) {
                            GemColor color = colors[(i + j) % colors.length];
                            req.put(color, 1 + ((i + j) % 2));
                        }
                        add(new Card(1, points, bonus, new Cost(req)));
                    }
                }},
                // level 2
                new ArrayList<>() {{
                    GemColor[] bonuses = {GemColor.WHITE, GemColor.BLUE, GemColor.GREEN, GemColor.RED, GemColor.BLACK};
                    for (int i = 0; i < 30; i++) {
                        GemColor bonus = bonuses[(i + 2) % bonuses.length];
                        int points = 1 + (i % 3);
                        EnumMap<GemColor, Integer> req = new EnumMap<>(GemColor.class);
                        GemColor[] colors = {GemColor.WHITE, GemColor.RED, GemColor.BLUE, GemColor.GREEN, GemColor.BLACK};
                        for (int j = 0; j < 3; j++) {
                            GemColor color = colors[(i + j) % colors.length];
                            req.put(color, 2 + ((i + j) % 2));
                        }
                        add(new Card(2, points, bonus, new Cost(req)));
                    }
                }},
                // level 3
                new ArrayList<>() {{
                    GemColor[] bonuses = {GemColor.WHITE, GemColor.BLUE, GemColor.GREEN, GemColor.RED, GemColor.BLACK};
                    for (int i = 0; i < 20; i++) {
                        GemColor bonus = bonuses[(i + 3) % bonuses.length];
                        int points = 3 + (i % 3);
                        EnumMap<GemColor, Integer> req = new EnumMap<>(GemColor.class);
                        GemColor[] colors = {GemColor.WHITE, GemColor.RED, GemColor.BLUE, GemColor.GREEN, GemColor.BLACK};
                        for (int j = 0; j < 3; j++) {
                            GemColor color = colors[(i + j) % colors.length];
                            req.put(color, 3 + ((i + j) % 2));
                        }
                        add(new Card(3, points, bonus, new Cost(req)));
                    }
                }}
        );

        if (gemBank != null) {
            this.gemBank = gemBank;
        } else {
            int playerCount = players == null ? 2 : players.size();
            int base = switch (playerCount) {
                case 2 -> 4;
                case 3 -> 5;
                default -> 7;
            };
            this.gemBank = new GemCollection()
                    .add(GemColor.WHITE, base)
                    .add(GemColor.RED, base)
                    .add(GemColor.BLUE, base)
                    .add(GemColor.GREEN, base)
                    .add(GemColor.BLACK, base)
                    .add(GemColor.GOLD, 5);
        }

        if (availableNobles != null) {
            this.availableNobles = availableNobles;
        } else {
            this.availableNobles = new ArrayList<>(List.of(
                    new Noble(1, "Anne of Green", Map.of(GemColor.WHITE, 3, GemColor.GREEN, 3)),
                    new Noble(2, "Red Regent", Map.of(GemColor.RED, 4, GemColor.BLACK, 4)),
                    new Noble(3, "Azure Court", Map.of(GemColor.BLUE, 4, GemColor.WHITE, 4))
            ));
        }
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
     * gets the current round number (1-based).
     * @return the current round number
     */
    public int getRoundNumber() {
        return this.roundNumber;
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

    public boolean isFinalRoundTriggered() {
        return this.finalRoundTriggered;
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
        if (this.currentPlayerIndex == 0) {
            this.roundNumber++;
            if (this.finalRoundTriggered) {
                this.gameOver = true;
            }
        }
    }

    /**
     * sets whether the game is over.
     * @param gameOver true to mark the game as over, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void triggerFinalRound() {
        this.finalRoundTriggered = true;
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

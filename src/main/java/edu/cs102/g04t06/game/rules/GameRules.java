package edu.cs102.g04t06.game.rules;

// Edited by GPT-5 (Codex)

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Validates core Splendor rules such as affordability, gem-taking, and victory checks.
 */
public class GameRules{

    // Universal Splendor rule constants.
    private static final int WINNING_POINTS = 15;
    private static final int MAX_GEMS_IN_HAND = 10;
    private static final int MAX_RESERVED_CARDS = 3;
    private static final int MIN_GEMS_IN_BANK_FOR_DOUBLE = 4;

    /**
     * Returns the fixed victory threshold for the game.
     *
     * @return points required to trigger the final round
     */
    public static int getWinningPoints() {
        return WINNING_POINTS;
    }

    /**
     * Returns the fixed reserved-card limit for the game.
     *
     * @return maximum reserved cards allowed per player
     */
    public static int getMaxReservedCards() {
        return MAX_RESERVED_CARDS;
    }

    /**
     * Returns the fixed gem-hand limit for the game.
     *
     * @return maximum gems allowed in hand
     */
    public static int getMaxGemsPerPlayer() {
        return MAX_GEMS_IN_HAND;
    }

    /**
     * Returns the fixed bank threshold required to take two of the same gem color.
     *
     * @return minimum gems needed in bank for a double take
     */
    public static int getMinGemsInBankForDouble() {
        return MIN_GEMS_IN_BANK_FOR_DOUBLE;
    }

    /**
     * checks if player fella can afford the card
     * A player can afford a card if their actual cost (after card bonuses) 
     * can be covered by their gems + gold (wildcards)
     * 
     * @param player the player attempting the purchase
     * @param card the card to be bought
     * @return true if the player can afford the card
     */
    public boolean canAffordCard(Player player, Card card){
        GemCollection actualcost = calculateActualCost(player, card);
        GemCollection playerGems = player.getGems();
        int goldAvailable = playerGems.getCount(GemColor.GOLD);

        for(GemColor color : GemColor.values()){
            if (color == GemColor.GOLD) continue;
            int shortfall = actualcost.getCount(color) - playerGems.getCount(color);
            if (shortfall > 0) {
                goldAvailable -= shortfall;
                if (goldAvailable < 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * calculate the actual cost of a card after subtracting bonuses
     * via Cost.afterBonuses()
     * 
     * @param player the player attempting the purchase
     * @param card the card to be purchased
     * @return a GemCollection representing the remaining cost after bonuses
     */
    public GemCollection calculateActualCost(Player player, Card card){
        Map<GemColor, Integer> bonuses = player.calculateBonuses();
        Cost actualcost = card.getCost().afterBonuses(bonuses);
        return new GemCollection(actualcost.asMap());
    }

    /**
    * checks if a player can take three different gems 
    * each requested color must appear exactly once and exist in the bank
    * 
    * @param requested the GemCollection the player wanted to take
    * @param bank the remaining gem bank 
    * @return true if player takes valid action
    * */
    public boolean canTakeThreeDifferentGems(GemCollection requested, GemCollection bank){
            if(requested.getTotalCount() < 3 || requested.getTotalCount() > 3){
                return false;
            }
            if (requested.getCount(GemColor.GOLD) > 0) {
                return false;
            }       
            for (Map.Entry<GemColor, Integer> entry : requested.asMap().entrySet()){
                int count = entry.getValue();

                if (count == 0) continue; 

                if (count > 1) return false; 

                if (bank.getCount(entry.getKey()) < 1) return false; 
            }
            return true;
    }

    /**
     * checks if a player can take two gems of the same color from the existing bank
     * the bank must have at least 4 gems of that color available
     * 
     * @param color the color of the gem player wants to take two of
     * @param bank the remaining gem bank
     * @return true if the action is valid
     */
    public boolean canTakeTwoSameGems(GemColor color, GemCollection bank){
        if (color == GemColor.GOLD) {
            return false;
        }
        return bank.getCount(color) >= MIN_GEMS_IN_BANK_FOR_DOUBLE;
    }

    /**
     * checks if player must return the gems back into bank
     * 
     * @param player the player to check
     * @return true if players has more than 10 gems
     */
    public boolean mustReturnGems(Player player){
        return player.getGemCount() > MAX_GEMS_IN_HAND;
    }

    /**
     * returns all nobles the player is eligible to claim based on 
     * purchased card bonuses meeting each noble's requirements
     * 
     * @param player the player to check
     * @param nobles the available nobles to be claimed
     * @return the list of nobles player can claim
     */
    public List<Noble> getClaimableNobles(Player player, List<Noble> nobles){
        List<Noble> result = new ArrayList<>();
        for(Noble noble : nobles){
            if(Noble.canBeClaimed(noble, player.calculateBonuses())){
                result.add(noble);
            }
        }
        return result;
    }

    /**
     * checks whether a player has won the game
     * win condition: points > win threshold
     * 
     * @param player the player to check
     * @param threshold the number of points needed to win 
     * @return true if a player exceed or reach the threshold
     */
    public boolean hasPlayerWon(Player player, int threshold){
        return player.getPoints() >= threshold;
    }

    /**
     * checks if player is able to reserve a card
     * a player can only reserve max of 3 cards
     * 
     * @param player the player to check
     * @return true if the player has less than 3 reserved cards
     */
    public boolean canReserveCard(Player player){
        return player.getReservedCards().size() < MAX_RESERVED_CARDS;
    }

    /**
     * checks the winner of a game 
     * players with the most point wins
     * tiebreak: the player with fewer purchased cards win
     * 
     * @param players list of all players in a game
     * @param threshold the amount of points needed to win
     * @return the winning player
     */
    public Player getWinner(List<Player> players, int threshold){
        Player winner = null;
        boolean unresolvedTie = false;
        for(Player player : players){
            if(hasPlayerWon(player, threshold)){
                if(winner == null || player.getPoints() > winner.getPoints()){
                    winner = player;
                    unresolvedTie = false;
                } else if(player.getPoints() == winner.getPoints()){
                    if(player.getPurchasedCards().size() < winner.getPurchasedCards().size()){
                        winner = player;
                        unresolvedTie = false;
                    } else if (player.getPurchasedCards().size() == winner.getPurchasedCards().size()) {
                        unresolvedTie = true;
                    }
                }
            }
        }
        return unresolvedTie ? null : winner;
    }
}

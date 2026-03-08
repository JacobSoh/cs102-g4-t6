package edu.cs102.g04t06.game.rules.entities;

import java.util.*;

import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;


/**
 * Represents a player and the state they own during a game.
 */
public class Player {

    // attributes
    private final String name;
    private int turnOrder;
    private final List<Card> purchasedCards;
    private final List<Card> reservedCards;
    private GemCollection gems;
    private final List<Noble> claimedNobles;

    /**
     * Creates a player with empty cards, gems, and nobles.
     *
     * @param name player display name
     * @param turnOrder zero-based play order
     */
    public Player(String name, int turnOrder){      
        this.name = name;
        this.turnOrder = turnOrder;
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.gems = new GemCollection();
        this.claimedNobles = new ArrayList<>();
    }

    /**
     * Returns the player's display name.
     *
     * @return player name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the player's turn order.
     *
     * @return zero-based turn order
     */
    public int getTurnOrder(){
        return turnOrder;
    }

    /**
     * Returns purchased development cards.
     *
     * @return list of purchased cards
     */
    public List<Card>getPurchasedCards(){
        return purchasedCards;
    }

    /**
     * Returns currently reserved cards.
     *
     * @return list of reserved cards
     */
    public List<Card>getReservedCards(){
        return reservedCards;
    }

    /**
     * Returns the player's current gem collection.
     *
     * @return gem collection
     */
    public GemCollection getGems(){
        return gems;
    }

    /**
     * Returns nobles already claimed by this player.
     *
     * @return list of claimed nobles
     */
    public List<Noble> getClaimedNobles(){
        return claimedNobles;
    }

    /**
     * Returns total gems currently held.
     *
     * @return total gem count
     */
    public int getGemCount(){
        return gems.getTotalCount();
    }

    /**
     * retutn the total points for this player
     * summing all points from the cards and noble
     * @return total prestige points from cards and nobles
     */
    public int getPoints(){
        int total = 0;
        for(Card card : purchasedCards){
            total += card.getPoints();
        }
        for(Noble noble : claimedNobles){
            total += noble.getPoints();
        }
        return total;
    }

    /**
    * Calculate the combined gem bonuses produced by all purchased cards.
    * One card give one bonus gem of its bonus color.
    * (the gem discount when owning one card)
    *
    * @return a map of {@code GemColor -> bonus count}
    */
    public Map<GemColor, Integer> calculateBonuses(){
        Map<GemColor, Integer> bonuses = new EnumMap<>(GemColor.class);
        for(GemColor color : GemColor.values()){
            bonuses.put(color, 0);
        }
        for(Card card: purchasedCards){
            GemColor bonus = card.getBonus();
            bonuses.put(bonus, bonuses.get(bonus) + 1);
        }
        return bonuses;
    }

    /**
     * add card into the player's purchased card list
     * remove the card from the reserved list if its reserved
     * 
     * @param card the card to be purchased
     */
    public void addCard(Card card){
        reservedCards.remove(card);
        purchasedCards.add(card);
    }

    /**
     * Add a card into the player/s reserved card list
     * player can hold maxmimum of 3 cards
     * 
     * @param card the card to be reserved 
     * @throws IllegalStateException when the player already has 3 reserved cards
     */
    public void addReservedCard(Card card){
        if(reservedCards.size() >= 3){
            throw new IllegalStateException(
                "Cannot reserve more than 3 cards"
            );
        }
        reservedCards.add(card);
    }

    /**
    * removes a card from the player's reserved cards.
    * 
    * @param card the card to remove
    */
    public void removeReservedCard(Card card) {
        reservedCards.remove(card);
    }

    /**
     * add gems into the player's gem collection
     * 
     * @param gems gems to be added
     */
    public void addGems(GemCollection gems){
        for (GemColor color : GemColor.values()) {
        this.gems = this.gems.add(color, gems.getCount(color));
        }
    }

    /**
     * deduct gems from the player's gem collection
     * 
     * @param gems gems to be added
     */

    public void deductGems(GemCollection gems){
        if(!this.gems.contains(gems)){
            throw new IllegalStateException(
                "There is not enough gems"
            );
        }
        this.gems = this.gems.subtract(gems);
    }

    /**
     * claims a noble for this player
     * 
     * @param noble the noble to be claimed
     */
    public void claimNoble(Noble noble){
        claimedNobles.add(noble);
    }
}

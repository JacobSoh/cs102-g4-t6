package edu.cs102.g04t06.game.rules.entities;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;

/**
 * represents an immutable Development card 
 */
public class Card {

    private final int level;
    private final int points;
    private final GemColor bonus;
    private final Cost cost;

    /**
     * constructs a new immutable Development card
     * @param level the tier of the card (1-3)
     * @param points the prestige points the card is worth
     * @param bonus the gem color bonus provided by this card
     * @param cost the cost required to purchase this card
     */
    public Card (int level, int points, GemColor bonus, Cost cost) {
        this.level = level;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
    }

    /**
     * gets the development level of the card
     * @return the card level (1, 2, or 3)
     */
    public int getLevel() {
        return level;
    }

    /**
     * gets the prestige points awarded by this card
     * @return the number of prestige points 
     */
    public int getPoints() {
        return points;
    }

    /**
     * gets the gem color bonus this card provides
     * @return the GemColor bonus
     */
    public GemColor getBonus() {
        return bonus;
    }

    /**
     * gets the cost requirement to buy this card
     * @return the Cost of the object
     */
    public Cost getCost() {
        return cost;
    }
}

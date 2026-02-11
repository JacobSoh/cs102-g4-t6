package edu.cs102.g04t06.game.rules.valueobjects;

import edu.cs102.g04t06.game.rules.entities.GemColor;
import java.util.*;

/**
 * Immutable Collection of gems used in Splendor Operations return new
 * GemCollection instances rather than modifying state
 */
public class GemCollection {

    private Map<GemColor, Integer> gems;

    /**
     * Creates empty GemCollection with all gem colors intialised to 0
     */
    public GemCollection() {
        this.gems = new EnumMap<>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            gems.put(color, 0);
        }
    }

    /**
     * Creats a GemCollection from an existing map of gem counts
     *
     * @param gems map of gemColor to Integer counts
     */
    public GemCollection(Map<GemColor, Integer> gems) {
        this.gems = gems;
    }

    /**
     * Returns the count of a specific gem colour
     *
     * @param color the gem color to check
     * @return the number of gems of that colour
     */
    public int getCount(GemColor color) {
        return gems.getOrDefault(color, 0);
    }

    /**
     * Returns total number of gems across all colours
     *
     * @return total gem count
     */
    public int getTotalCount() {
        int sum = 0;
        for (int amount : gems.values()) {
            sum += amount;
        }
        return sum;
    }

    /**
     * Checks if this collection has no gems of any colour
     *
     * @return true if all gem counts are 0
     */
    public boolean isEmpty() {
        return this.getTotalCount() == 0;
    }

    /**
     * Returns a defensive copy of the internal gem map Modifications to the
     * returned map do not affect this GemCollection
     *
     * @return a new map containing the gem counts
     */
    public Map<GemColor, Integer> asMap() {
        return new EnumMap<>(this.gems);
    }

    /**
     * Adds a specified amount of a gem colour to this collection Returns a new
     * GemCollection with the updated count
     *
     * @param color the gem colour to add
     * @param amount the number of gems to add
     * @return a new GemCollection with the added gems
     */
    public GemCollection add(GemColor color, int amount) {
        Map<GemColor, Integer> temp = this.asMap();
        temp.merge(color, amount, Integer::sum);
        return new GemCollection(temp);
    }

    /**
     * Subtract a specified amount of a gem colour to this collection Returns a
     * new GemCollection with the updated count
     *
     * @param color the gem colour to subtract
     * @param amount the number of gems to subtract
     * @return a new GemCollection with the reduced gems
     * @throws IllegalArgumentException if result is negative
     */
    public GemCollection subtract(GemColor color, int amount) {
        int result = gems.getOrDefault(color, 0) - amount;
        if (result < 0) {
            throw new IllegalArgumentException("Cannot have negative gems");
        }

        Map<GemColor, Integer> temp = this.asMap();
        temp.put(color, result);

        return new GemCollection(temp);

    }

    /**
     * Subtracts all gem counts of another GemCollection from this one. Returns
     * a new GemCollection with the resulting counts.
     *
     * @param other the GemCollection to subtract
     * @return a new GemCollection with the reduced gems
     * @throws IllegalArgumentException if any resulting count is negative
     */
    public GemCollection subtract(GemCollection other) {
        if (!(this.contains(other))) {
            throw new IllegalArgumentException("Cannot have negative gems");
        }

        GemCollection temp = new GemCollection();
        for (GemColor color : GemColor.values()) {
            temp = temp.subtract(color, other.getCount(color));
        }

        return temp;
    }

    /**
     * Checks if this collection has at least as many gems of every color as the
     * other collection. Useful for checking if a player can afford a cost.
     *
     * @param other the GemCollection to compare against
     * @return true if this collection contains enough gems for every color
     */
    public boolean contains(GemCollection other) {
        for (GemColor color : GemColor.values()) {
            if (this.getCount(color) < other.getCount(color)) {
                return false;
            }
        }

        return true;
    }

}

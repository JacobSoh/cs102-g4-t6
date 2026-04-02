package edu.cs102.g04t06.game.rules.entities;

import java.util.HashMap; // Map refers to the concept of a dictionary -> with Key and Value pairs
import java.util.Map; // the specific dictionary we are using  
import java.util.Objects;

/**
 * represents an immutable Noble card
 */
public class Noble implements GameEntity {

    private final int points = 3;
    private final Map<GemColor, Integer> requirements; // the variable "requirements" will hold the cost to get the Noble (eg. Key: Red -> Value: 4)
    private final String name;
    private final int id;
    
    /**
     * constructs a new immutable Noble card
     * @param points the prestige points the noble provides
     * @param requirements the specific gem bonuses a player needs to purchase this noble
     */
    public Noble(int id, String name, Map<GemColor, Integer> requirements) {
        this.id = id;
        this.name = name;
        this.requirements = new HashMap<>(requirements); // defensive (separate from original) copy of HashMap
        
    }

    /**
     * gets the prestige points awarded by this noble
     * @return the number of prestige points 
     */
    public int getPoints() {
        return points;
    }

    /**
     * gets a copy of the gem requirements for this noble
     * @return a defensive copy of the requirements map
     */
    public Map<GemColor, Integer> getRequirements() {
        return new HashMap<>(requirements);
    }

    /**
     * determines if a noble can be claimed based on a players bonuses
     * @param noble the noble to check
     * @param bonuses the current bonuses the player holds
     * @return true if the player meets all requirements, false otherwise
     */
    public static boolean canBeClaimed(Noble noble, Map<GemColor, Integer> bonuses) {
        Map<GemColor, Integer> reqs = noble.getRequirements(); // gets the cost of the noble 

        for (Map.Entry<GemColor, Integer> entry : reqs.entrySet()) { // loop through every color required by the noble
            GemColor color = entry.getKey();
            int requiredAmount = entry.getValue();

            // get players bonus for this color, defaulting to 0 if they have none
            int playerAmount = bonuses.getOrDefault(color, 0);

            if (playerAmount < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    /**
     * computes a hash code consistent with {@link #equals(Object)}.
     * two nobles that have the same points and requirements produce the same hash.
     * @return a hash code based on points and requirements
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.points, this.requirements);
    }

    /**
     * compares this noble with another object for logical equality.
     * two nobles are equal when both their points and requirements are equal.
     * @param o the object to compare with this noble
     * @return true if the given object is a noble with equal points and requirements, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Noble)) return false;
        Noble n = (Noble) o;
        return this.points == n.points && this.requirements.equals(n.requirements);
    }

    /**
     * returns a String representation of this Noble card.
     * the returned String includes the Noble's id and name.
     */
    @Override
    public String toString() {
        return "Noble{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
        }   

    /**
     * Returns the display name of this noble.
     *
     * @return the noble name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the identifier of this noble.
     *
     * @return the noble id
     */
    public int getId() {
        return id;
    }
}

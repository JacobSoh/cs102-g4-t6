package edu.cs102.g04t06.game.rules.entities;

import java.util.Map; // Map refers to the concept of a dictionary -> with Key and Value pairs 
import java.util.HashMap; // the specific dictionary we are using  

/**
 * represents an immutable Noble card 
 */
public class Noble {

    private final int points;
    private final Map<GemColor, Integer> requirements; // the variable "requirements" will hold the cost to get the Noble (eg. Key: Red -> Value: 4)
    
    /**
     * constructs a new immutable Noble card
     * @param points the prestige points the noble provides
     * @param requirements the specific gem bonuses a player needs to purchase this noble
     */
    public Noble(int points, Map<GemColor, Integer> requirements) {
        this.points = points;
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
}

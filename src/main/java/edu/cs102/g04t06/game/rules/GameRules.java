package edu.cs102.g04t06.game.rules;

import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class GameRules {

    // Placeholder for the rule you need right now
    public static boolean canTakeTwoSameGems(GemColor color, GemCollection bank) {
        // Splendor Rule: You can take 2 of the same color if the bank has at least 4
        return bank.getCount(color) >= 4;
    }

    // Placeholder for the next rule
    public static boolean canTakeThreeDifferentGems(GemCollection requested, GemCollection bank) {
        // Logic for checking 3 different gems will go here later
        return true; 
    }
}
package edu.cs102.g04t06.game.rules;

import java.util.ArrayList;
import java.util.List;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
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

    public static boolean canAffordCard(Player player, Card card) {
        // This is a placeholder for the actual affordability logic.
        // For now, we return true so you can test your execution logic!
        return true; 
    }

    /**
 * Checks which nobles are eligible to visit the player based on their card bonuses.
 * This is a stub for EXEC-1 to compile. Logic will be finalized in RULES-1.
 */
    public static List<Noble> getClaimableNobles(Player player, List<Noble> nobles) {
        List<Noble> claimable = new ArrayList<>();
        
        for (Noble noble : nobles) {
            // We call the canBeClaimed method we just reviewed in your Noble class!
            if (Noble.canBeClaimed(noble, player.calculateBonuses())) {
                claimable.add(noble);
            }
        }
        
        return claimable;
    }
}
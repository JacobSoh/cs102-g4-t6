package edu.cs102.g04t06.game.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class GameRules {

    // Placeholder for the rule you need right now
    public static boolean canTakeTwoSameGems(GemColor color, GemCollection bank) {
        // Splendor Rule: Bank must have at least 4 of that color left to take 2
        return bank.getCount(color) >= 4;
    }

    // stub
    public static boolean canTakeThreeDifferentGems(GemCollection selection, GemCollection bank) {
        // Splendor Rule: Check if the bank has at least 1 of every color requested
        for (GemColor color : GemColor.values()) {
            if (selection.getCount(color) > 0 && bank.getCount(color) < 1) {
                return false; 
            }
        }
        return true;
    }

    public static boolean canAffordCard(Player player, Card card) {
        // 1. Calculate the cost after applying the player's permanent card bonuses
        GemCollection bonuses = new GemCollection(player.calculateBonuses());
        GemCollection discountedCost = card.getCost().afterBonuses(bonuses);
        
        // 2. See how many missing gems need to be covered by GOLD
        int goldNeeded = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            
            int required = discountedCost.getCount(color);
            int playerHas = player.getGems().getCount(color);
            
            if (playerHas < required) {
                goldNeeded += (required - playerHas);
            }
        }
        
        // 3. The move is legal if they have enough GOLD to cover the shortfall
        return player.getGems().getCount(GemColor.GOLD) >= goldNeeded;
    }

    /**
 * Checks which nobles are eligible to visit the player based on their card bonuses.
 * This is a stub for EXEC-1 to compile. Logic will be finalized in RULES-1.
 */
    public static List<Noble> getClaimableNobles(Player player, List<Noble> availableNobles) {
        List<Noble> claimable = new ArrayList<>();
        Map<GemColor, Integer> playerBonuses = player.calculateBonuses();
        
        for (Noble noble : availableNobles) {
            boolean canClaim = true;
            // Assuming your Noble class has a getRequirements() method!
            Map<GemColor, Integer> reqs = noble.getRequirements(); 
            
            for (Map.Entry<GemColor, Integer> entry : reqs.entrySet()) {
                int playerHas = playerBonuses.getOrDefault(entry.getKey(), 0);
                if (playerHas < entry.getValue()) {
                    canClaim = false;
                    break;
                }
            }
            if (canClaim) {
                claimable.add(noble);
            }
        }
        return claimable;
    }
}
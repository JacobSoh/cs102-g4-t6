package edu.cs102.g04t06.game.execution.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Utility methods for evaluating card affordability and strategic value for AI players.
 */
public class CardEvaluator{

    /**
     * returns all cards in the visible card market and the player's reserved
     * that they can afford
     * 
     * @param state the current state of the board
     * @param player the player whose gems and bonuses are checked
     * @return a list of afforable cards: empty list if none
     */
    public static List<Card> findAllAffordableCards(GameState state, Player player){
        GameRules rules = new GameRules();
        List<Card> affordable = new ArrayList<>();

        //check afforable cards of each level in the current card market
        for(int level = 1; level <= 3; level++){
            List<Card> visibleCards = state.getMarket().getVisibleCards(level);
            for(Card card : visibleCards){
                if(card != null && rules.canAffordCard(player, card)){
                    affordable.add(card);
                }
            }
            
        }

        //check the affordable cards on the player reserved hand
        for(Card card : player.getReservedCards()){
            if(card != null && rules.canAffordCard(player, card)){
                    affordable.add(card);
            }
        }

        return affordable;
    }

    /**
     * returns a map of gem colors to the number of additional gems needed
     * to afford a given card
     * 
     * @param player the player whose gems and bonuses are checked
     * @param card the card to be checked with
     * @return a map of Gemcolor and deficit (only colours with deficit > 0)
     */
    public static Map<GemColor, Integer> calculateDeficit(Player player, Card card){
        GameRules rules = new GameRules();
        Cost actualCost = new Cost(rules.calculateActualCost(player, card).asMap());

        Map<GemColor, Integer> deficit = new EnumMap<>(GemColor.class);

        for(GemColor color : GemColor.values()){
            if(color == GemColor.GOLD) continue;

            int needed = actualCost.getRequired(color);
            int held = player.getGems().getCount(color);
            int gap = needed - held;

            if(gap > 0){
                deficit.put(color, gap);
            }
        }
        return deficit;
    }

    /**
     * returns the total number of gems needed acorss all colors 
     * for the player to afford a given card
     * 
     * @param player the player to be checked with gems or bonuses
     * @param card the card to be evalauted
     * @return the total deficit as a single int
     */
    public static int totalDeficit(Player player, Card card){
        Map<GemColor, Integer> deficit = calculateDeficit(player, card);
        int sum = 0;

        for(Map.Entry<GemColor, Integer> entry : deficit.entrySet()){
            sum += entry.getValue();
        }
        return sum;
    }


    /**
     * estimates the number of gem-taking turns required before the player can 
     * afford the given card, given the current bank state
     * 
     * if 3 colors have deficit -> reduce the top 3 by 1 each
     * else if any color has deficit >= 2 and bank has >= 4 -> reduce by 2
     * else reduce all avaiable deficit by 1 each
     * 
     * @param player the player evaluating the card 
     * @param card the card being evaluated
     * @param bank the current gem bank
     * @return an estimated number of turn 
     */
    public static int estimateTurns(Player player, Card card, GemCollection bank){
        Map<GemColor, Integer> deficit = new EnumMap<>(calculateDeficit(player, card));

        int turns = 0;
        while(!deficit.isEmpty()){
            turns++;

            List<GemColor> colorsWithDeficit = new ArrayList<>(deficit.keySet());
            colorsWithDeficit.sort((a,b) -> deficit.get(b) - deficit.get(a)); //descending order

            if(colorsWithDeficit.size() >= 3){
                for(int i = 0; i < 3; i++){
                    GemColor color = colorsWithDeficit.get(i);
                    int newValue = deficit.get(color) - 1;
                    if(newValue <= 0){
                        deficit.remove(color);
                    } else {
                        deficit.put(color, newValue);
                    }
                }
            } else {
                boolean tookTwo = false;
                for(GemColor color : colorsWithDeficit){
                    if(deficit.get(color) >= 2 && bank.getCount(color) >= 4){
                        int newValue = deficit.get(color) - 2;
                        if(newValue <= 0){
                            deficit.remove(color);
                        } else {
                            deficit.put(color, newValue);
                        }
                        tookTwo = true;
                    }
                    break;
                }

                if(!tookTwo){
                    for (GemColor color : new ArrayList<>(colorsWithDeficit)) {
                        int newVal = deficit.get(color) - 1;
                        if (newVal <= 0) {
                            deficit.remove(color);
                        } else {
                            deficit.put(color, newVal);
                        }
                    }
                }
            }
        }
        return turns;
    }


    /**
     * Returns a score representing how much buying this card would contribute
     * toward claiming available nobles.
     *
     * For each noble, if the card's bonus colour is still needed, this card
     * fills (1 / remaining count for that colour) of the gap. Scores are summed
     * across all nobles.
     * 
     * e.g if 3 nobles require 3 blue bonus and 2 nobles require 3 black bonus
     * buying a blue card will give a score of 1.0 while buying a black card gives a score of 2/3
     * 
     * 
     * @param card   the card being evaluated
     * @param player the player evaluating the card
     * @param nobles list of nobles still available in the game
     * @return total noble-progress score (higher = better)
     */
    public static double nobleProgress(Card card, Player player, List<Noble> nobles){
        GemColor cardBonus = card.getBonus();

        Map<GemColor, Integer> currentBonuses = player.calculateBonuses();
        double progress = 0.0;

        for(Noble noble : nobles){
            Map<GemColor, Integer> requirements = noble.getRequirements();

            if(!requirements.containsKey(cardBonus)) continue;

            int required = requirements.get(cardBonus);
            int alreadyHas = currentBonuses.getOrDefault(cardBonus, 0);
            int remaining = required - alreadyHas;

            if(remaining > 0){
                progress += 1.0/remaining;
            }
        }

        return progress;
    }

    /**
     * @param card the card being evaluated
     * @param state the current gamestate to scan visible market
     * @return discount utility score 
     */
    public static double discountUtility(Card card, GameState state) {
        GemColor cardBonus = card.getBonus();
        
        int matchingCards = 0;
        int totalCards = 0;

        for(int level = 1; level <= 3; level++){
            List<Card> visible = state.getMarket().getVisibleCards(level);
            for(Card visibleCard : visible){
                totalCards++;
                if(visibleCard.getCost().getRequired(cardBonus) > 0){
                    matchingCards++;
                }
            }
        }

        if(totalCards == 0){
            return 0.0;
        }

        return (double) matchingCards/totalCards;
    }
}

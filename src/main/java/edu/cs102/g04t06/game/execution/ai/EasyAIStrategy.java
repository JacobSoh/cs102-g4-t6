package edu.cs102.g04t06.game.execution.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.ActionType;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;


public class EasyAIStrategy implements AIStrategy {

    private final GameRules rules = new GameRules();

    /**
     * Decides the AI's main turn action using a simple priority chain.
     *
     * Step 1: Try to purchase the best affordable card from the market or
     *         reserved hand (highest points, tie-break lowest total cost).
     * Step 2: If nothing affordable and the AI build some gems collection (gems > 3),
     *         reserve the highest-value visible card with >= 3 prestige points
     * Step 3: Otherwise, take gems toward the closest-to-affordable card.
     *
     * @param state the current game state
     * @param self  the AI's Player entity
     * @return an AIAction describing what to do this turn
     */
    @Override
    public AIAction decideAction(GameState state, Player self){

        Card bestCard = findBestAffordableCard(state, self);
        if(bestCard != null){
            boolean fromReserved = self.getReservedCards().contains(bestCard);
            String desc = String.format("Purchase a card (Level %d, %d pts)",
             bestCard.getLevel(), bestCard.getPoints());
            return new AIAction(ActionType.PURCHASE_CARD, bestCard,
                fromReserved, null, desc);
        }

        if (rules.canReserveCard(self) && self.getGems().getTotalCount() >= 3) {
            Card bestReserve = findBestReservableCard(state);
            if (bestReserve != null) {
                String desc = String.format("Reserve a card");
                return new AIAction(ActionType.RESERVE_CARD, bestReserve,
                    false, null, desc);
            }
        }


        Card target = findClosestCard(state, self);
        return buildGemRequest(self, target, state.getGemBank());
    }

    /**
     * Picks the first claimable noble from the list. No scoring or
     * comparison is performed — this is the simplest possible selection.
     *
     * @param claimable list of nobles the player is eligible to claim
     * @param state     current game state (unused by Easy AI)
     * @param self      the AI's Player entity (unused by Easy AI)
     * @return the first Noble in the claimable list
     */
    @Override
    public Noble chooseNoble(List<Noble> claimable, GameState state, Player self) {
        return claimable.get(0);
    }

    /**
     * chooses which gem to return when the AI exceeds 10 gems
     * repeatedly choose the color that the AI holds the most of and returns 
     * as many as needed , until excessCount = 0
     * 
     * uses a simulated copy of the player's gem count so the real 
     * Player is not mutated during selection
     * 
     * @param self the AI player
     * @param excessCount number of gems needed to be returned
     * @param state current game state 
     * @return a GemCollection of exactly excessCount gem to return
     */
    @Override
    public GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state) {
        Map<GemColor, Integer> simulated = new EnumMap<>(GemColor.class);
        for(GemColor color : GemColor.values()){
            int count = self.getGems().getCount(color);
            if (count > 0) {
                simulated.put(color, count);
            }
        }

        GemCollection toReturn = new GemCollection();
        int remaining = excessCount;

        while(remaining > 0){
            GemColor best = findMostHeldColour(simulated);
            if(best == null) break;

            int available = simulated.getOrDefault(best, 0);
            int amount = Math.min(remaining, available);

            toReturn = toReturn.add(best, amount);
            remaining -= amount;

            //remove the simulated gems in the player hand
            int newCount = available - amount;
            if (newCount <= 0) {
                simulated.remove(best);
            } else {
                simulated.put(best, newCount);
            }
        }

        return toReturn;
    }


    //private helpers 

    /**
     * tie-break: when two cards are afforable, we want to choose the one with lower cost
     * 
     * @param candidate the card to be compared with
     * @param current the initial card
     * @return true if candidate cost less than the current card cost
     */
    private boolean isBetterPurchase(Card candidate, Card current) {
        if (current == null) return true;
        if (candidate.getPoints() > current.getPoints()) return true;
        if (candidate.getPoints() < current.getPoints()) return false;

        return candidate.getCost().getTotalGems() < current.getCost().getTotalGems();
    }

    /**
     * finds the best afforable card from the visible market
     * choose the one with the most points
     * (tie-break): choose lowest total gem cost
     * 
     * @param state the current gamestate
     * @param self the AI's player
     * @return the best afforable card ; null if none are affordable
     */
    private Card findBestAffordableCard(GameState state, Player self){
        List<Card> affordable = CardEvaluator.findAllAffordableCards(state, self);

        Card bestCard = null;
        for(Card card : affordable){
            if(isBetterPurchase(card, bestCard)){
                bestCard = card;
            }
        }
        return bestCard;
    }


    /**
     * scans all visible market cards for card worth reserving
     * a card is worth reserving when it has >= 3 points
     * returns best candidate (highest points, tie-break -> lowest cost)
     * or null if no card qualifies 
     * 
     * @param state the current game state
     * @return the best reservable card, or null
     */
    private Card findBestReservableCard(GameState state){
        Card bestCard = null;
        
        for(int level = 1; level <= 3; level++){
            List<Card> visibleCards = state.getMarket().getVisibleCards(level);
            for(Card card : visibleCards){
                if(card != null && card.getPoints() >= 3){
                    if(isBetterPurchase(card, bestCard)){
                        bestCard = card;
                    }
                }
            }
        }
        return bestCard;
    }

    /**
     * finds all visible card across all visible market with the 
     * smallest total gem deficit for this player
     * 
     * @param state the current game state
     * @param self the AI player entity
     * @return the closest card, null if no visible card
     */
    private Card findClosestCard(GameState state, Player self){
        Card closest = null;
        int smallestDeficit = Integer.MAX_VALUE;
        for(int level = 1; level <= 3; level++){
            List<Card> visibleCards = state.getMarket().getVisibleCards(level);
            for(Card card : visibleCards){
                int deficit = CardEvaluator.totalDeficit(self, card);
                if(card != null && deficit < smallestDeficit){
                    smallestDeficit = deficit;
                    closest = card;
                }
            }
            
        }
        return closest;
    }

    /**
     * returns the non-GOLD gemColor the player holds the most from a gem count map
     * GOLD is skipped as it is the most valuable (joker can replace any gems)
     * GOLD can max be hold at 5, in a case to return gem, there will always be other colors to return
     * 
     * @param gems a mutable map of GemColor to count
     * @return the most held non GOLD color
     */
    private GemColor findMostHeldColour(Map<GemColor, Integer> gems){
        GemColor best = null;
        int bestCount = -1;

        for(Map.Entry<GemColor, Integer> entry : gems.entrySet()){
            GemColor color = entry.getKey();
            int count = entry.getValue();
            if(count <= 0 || color == GemColor.GOLD) continue;
            if (count > bestCount){
                bestCount = count;
                best = color;
            }
        }
        return best;
    }

    
    /**
     * builds a gem request targeting a specigfic card's deficit
     * tries these condition in order 
     *  1. take 3 different gems from the colours with the highest deficit
     *  2. take 2 of the same color (highest deficit where bank >= 4) 
     *  3. take any 3 available gems in the bank
     *  4. take 2 of the same color for any color possible
     *  5. take whatever individual gem are available
     *  6. return an empty list if the bank is completely depleted
     * 
     * 
     * @param self the AI's player identity
     * @param target the card being targeted
     * @param bank the current gem bank
     * @return AIAction for gem taking (always return a valid one)
     */
    private AIAction buildGemRequest(Player self, Card target, GemCollection bank){
        Map<GemColor, Integer> deficit = CardEvaluator.calculateDeficit(self, target);

        //sort deficit in descending order of needed gems
        List<GemColor> colorsWithDeficit = new ArrayList<>(deficit.keySet());
        colorsWithDeficit.sort((a,b) -> deficit.get(b) - deficit.get(a));

        //try 1 : 3 different gems from deficit colors 
        List<GemColor> pickable = new ArrayList<>();
        for(GemColor color : colorsWithDeficit){
            if(bank.getCount(color) >= 1) pickable.add(color);
            if(pickable.size() == 3) break;
        }
        if(pickable.size() == 3){
            GemCollection request = new GemCollection();
            for(GemColor c : pickable) request = request.add(c, 1); //GemCollection is immutable
            if(rules.canTakeThreeDifferentGems(request, bank)){
                return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false, request, 
                String.format("Take 3 different: %s", pickable));
            }
        }

        //try 2 : 2 same gems from highest deficit colors where bank >= 4
        for(GemColor color : colorsWithDeficit){
            if(rules.canTakeTwoSameGems(color, bank)){
                GemCollection request = new GemCollection().add(color, 2);
                return new AIAction(ActionType.TAKE_TWO_SAME, null, false, request,
                String.format("Take 2 %s gems", color));
            }
        }

        //try 3 : any 3 different gems from bank
        List<GemColor> available = new ArrayList<>();
        for(GemColor color : GemColor.values()){
            if(color == GemColor.GOLD) continue;
            if(bank.getCount(color) >= 1) available.add(color);
            if(available.size() == 3) break;
        }
        if(available.size() == 3){

            GemCollection request = new GemCollection();
            for (GemColor c : available) request = request.add(c, 1);
            if (rules.canTakeThreeDifferentGems(request, bank)) {
                return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false, request,
                    String.format("Take 3 different (fallback): %s", available));
            }
        }

        //try 4 : any 2 same
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (rules.canTakeTwoSameGems(color, bank)) {
                GemCollection request = new GemCollection().add(color, 2);
                return new AIAction(ActionType.TAKE_TWO_SAME, null, false, request,
                    String.format("Take 2 %s gems", color));
            }
        }

        //try 5 : whatever gems are available (up to three)
        GemCollection request = new GemCollection();
        List<GemColor> taken = new ArrayList<>();
        for (GemColor color : GemColor.values()){
            if (color == GemColor.GOLD) continue;
            if (bank.getCount(color) >= 1) {
                request = request.add(color, 1);
                taken.add(color);
            }
            if (taken.size() == 3) break;
        }

        if (!taken.isEmpty()) {
            return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false, request,
                String.format("Take %d gem(s) (last resort): %s", taken.size(), taken));
        }

        //bank completely empty - should not be a common occurence
        return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false,
            new GemCollection(), "No gems available");

    }
}
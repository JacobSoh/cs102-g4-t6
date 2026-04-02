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

/**
 * Baseline AI strategy that favors straightforward card scoring and gem collection.
 */
public class EasyAIStrategy implements AIStrategy {

    private final GameRules rules = new GameRules();

    // hyperparamter tuning 
    // adjust these to change how aggressively the AI weights each factor

    /** Multiplier for prestige points when scoring a card. */
    private static final double WEIGHT_POINTS        = 5.0; //4.0

    /** Multiplier for how useful a card's bonus color for getting nobles. */
    private static final double WEIGHT_NOBLE_SYNERGY  = 1.5; //3.0

    /** Multiplier for how useful a card's bonus is toward future purchases. */
    private static final double WEIGHT_ENGINE_VALUE   = 0.5; //1.5

    /** Penalty multiplier for each gem of deficit (how far away from buying). */
    private static final double WEIGHT_DEFICIT_PENALTY = 1.5; //1.0

    /** Minimum points for a card to be considered worth reserving. */
    private static final int RESERVE_MIN_POINTS = 3; //3.0

    /** Number of top target cards considered when picking gems. */
    private static final int GEM_TARGET_COUNT = 2; //3.0


    /**
     * Main decision loop
     *
     *  1. Buy the best affordable card 
     *  (scored by points + noble synergy + engine value, penalised by cost).
     *  2. Reserve a high-value card if:
     *       the gold token would let us buy it next turn, or
     *       it is a high-point card worth reserving away from the other player.
     *  3. Take gems that overlap the deficits of our top target cards.
     * 
     * @param state the current gamestate
     * @param self the AI player 
     * @return an AIAction deciding the action of the bot
     */
    @Override
    public AIAction decideAction(GameState state, Player self) {

        //step 1: purchase
        Card bestBuy = findBestAffordableCard(state, self);
        if (bestBuy != null) {
            boolean fromReserved = self.getReservedCards().contains(bestBuy);
            String desc = String.format("Purchase card (Lv%d, %dpts, %s bonus)",
                    bestBuy.getLevel(), bestBuy.getPoints(), bestBuy.getBonus());
            return new AIAction(ActionType.PURCHASE_CARD, bestBuy,
                    fromReserved, null, desc);
        }

        //step 2: reserve
        if (rules.canReserveCard(self)) {
            Card reserveTarget = findBestReservableCard(state, self);
            if (reserveTarget != null) {
                String desc = String.format("Reserve card (Lv%d, %dpts)",
                        reserveTarget.getLevel(), reserveTarget.getPoints());
                return new AIAction(ActionType.RESERVE_CARD, reserveTarget,
                        false, null, desc);
            }
        }

        //step 3: take gems
        List<Card> targets = findTopTargetCards(state, self, GEM_TARGET_COUNT);
        return buildGemRequest(self, targets, state.getGemBank());
    }

    /**
     * choose the first noble
     * 
     * @param claimable the list of noble that are claiamble
     * @param state the current gamestate
     * @param self the AI player
     * @return the chosen Noble
     */
    @Override
    public Noble chooseNoble(List<Noble> claimable, GameState state, Player self) {
        return claimable.getFirst();
    }

    /**
     * Returns gems the AI does not need for its current target cards.
     * Falls back to most-held color if everything is needed.
     * 
     * @param self the AI player
     * @param excessCount the extra gems needed to be returned
     * @param state the current gamestate
     * @return a GemCollection of what to return
     */
    @Override
    public GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state) {
        // compute combined deficit across top targets, get colors we need
        List<Card> targets = findTopTargetCards(state, self, GEM_TARGET_COUNT);
        Map<GemColor, Integer> need = combinedDeficit(self, targets);

        //build a mutable snapshot of the player's gems
        Map<GemColor, Integer> held = snapshotGems(self);

        GemCollection toReturn = new GemCollection();
        int remaining = excessCount;

        //pass 1: return gems we dont need
        List<GemColor> unneeded = new ArrayList<>();
        for (GemColor c : held.keySet()) {
            if (c == GemColor.GOLD) continue;
            if (!need.containsKey(c) || need.get(c) <= 0) unneeded.add(c);
        }

        //sort unneeded by descending count so we clear the biggest pile first
        unneeded.sort((a, b) -> held.getOrDefault(b, 0) - held.getOrDefault(a, 0));

        for (GemColor c : unneeded) {
            if (remaining <= 0) break;
            int give = Math.min(remaining, held.getOrDefault(c, 0));
            if (give <= 0) continue;
            toReturn = toReturn.add(c, give);
            remaining -= give;
            held.put(c, held.getOrDefault(c, 0) - give);
        }

        //pass 2: if still over, return the most-held color (excluding GOLD)
        while (remaining > 0) {
            GemColor most = findMostHeldColor(held);
            if (most == null) break;
            int give = Math.min(remaining, held.getOrDefault(most, 0));
            toReturn = toReturn.add(most, give);
            remaining -= give;
            held.put(most, held.getOrDefault(most, 0) - give);
            if (held.get(most) <= 0) held.remove(most);
        }
        return toReturn;
    }



    /**
     * Scores a card considering four factors:
     *   + raw prestige points
     *   + noble synergy  (does its bonus help us get a noble)
     *   + engine value   (does its bonus reduce cost of other visible cards)
     *   − deficit penalty (how many gems are we short)
     * 
     * @param card the card to be evaluated
     * @param state the current gamestate
     * @param self the AI player
     * @return a float value of the overall score of a card (higher is better)
     */
    private double scoreCard(Card card, GameState state, Player self) {
        double pts    = card.getPoints() * WEIGHT_POINTS;
        double noble  = nobleSynergy(card, state, self) * WEIGHT_NOBLE_SYNERGY;
        double engine = engineValue(card, state, self) * WEIGHT_ENGINE_VALUE;
        double cost   = CardEvaluator.totalDeficit(self, card) * WEIGHT_DEFICIT_PENALTY;
        return pts + noble + engine - cost;
    }

    /**
     * determine how much does this card's bonus color help toward claimable nobles
     * Returns the count of nobles on the board that still need this color.
     * 
     * @param card the card to be evaluated
     * @param state the current gamestate
     * @param self the AI player
     * @return a float value determining how useful is 
     * this card bonus color for claiming nobles (higher is better)
     */
    private double nobleSynergy(Card card, GameState state, Player self) {
        GemColor bonus = card.getBonus();
        if (bonus == null || bonus == GemColor.GOLD) return 0;

        int synergy = 0;
        for (Noble noble : state.getAvailableNobles()) {
            int required = noble.getRequirements().getOrDefault(bonus, 0);
            int have     = self.calculateBonuses().getOrDefault(bonus, 0);
            if (required > have) {
                synergy++;  // this card helps close the gap
            }
        }
        return synergy;
    }

    /**
     * determine ow useful is this card's bonus color for buying other visible cards
     * Counts how many visible cards list this color in their cost and the
     * player cannot yet cover it with bonuses alone.
     * 
     * @param card the card to be evaluated
     * @param state the current gamestate
     * @param self the AI player
     * @return a float value determining how useful is 
     * this card bonus color for buying other visible cards (higher is better)
     */
    private double engineValue(Card card, GameState state, Player self) {
        GemColor bonus = card.getBonus();
        if (bonus == null || bonus == GemColor.GOLD) return 0;

        int count = 0;
        for (int level = 1; level <= 3; level++) {
            for (Card c : state.getMarket().getVisibleCards(level)) {
                if (c == null || c.equals(card)) continue;
                int costInColor = c.getCost().getRequired(bonus);  
                int covered     = self.calculateBonuses().getOrDefault(bonus, 0);
                if (costInColor > covered) count++;
            }
        }
        return count;
    }


    /**
     * Finds the best affordable card by composite score.
     * Considers both visible market cards and reserved hand.
     * 
     * @param state the current gamestate
     * @param self the AI player
     * @return the best afforadable card
     */
    private Card findBestAffordableCard(GameState state, Player self) {
        List<Card> affordable = CardEvaluator.findAllAffordableCards(state, self);

        Card best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Card card : affordable) {
            double score = scoreCard(card, state, self);
            if (score > bestScore) {
                bestScore = score;
                best = card;
            }
        }
        return best;
    }

    /**
     * Improved reserve logic — a card is worth reserving when:
     *   (a) it has high points and the gold token would close the gap
     *       (deficit <= 1 after receiving gold), or
     *   (b) it is a high-value card (>= RESERVE_MIN_POINTS) worth reserving
     *
     * @param state the current gamestate
     * @param self the AI player
     * @return the best reservable card
     */
    private Card findBestReservableCard(GameState state, Player self) {
        Card best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // How developed is our engine? Count total bonuses.
        int totalBonuses = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            totalBonuses += self.calculateBonuses().getOrDefault(color, 0);
        }

        for (int level = 1; level <= 3; level++) {
            for (Card card : state.getMarket().getVisibleCards(level)) {
                if (card == null) continue;

                int deficit = CardEvaluator.totalDeficit(self, card);

                //if gold closes the gap always reserve
                boolean goldCloses = deficit <= 1;

                //if we have some bonus and the card is reachable (deficit <= 4)
                boolean highValue = card.getPoints() >= RESERVE_MIN_POINTS
                        && totalBonuses >= 2
                        && deficit <= 4;

                if (!goldCloses && !highValue) continue;

                double score = scoreCard(card, state, self);
                if (goldCloses) score += 5.0;

                if (score > bestScore) {
                    bestScore = score;
                    best = card;
                }
            }
        }
        return best;
    }

    /**
     * returns the top n target cards (not yet affordable) sorted by
     * composite score. Used to guide gem-taking decisions.
     * 
     * @param state the current gamestate
     * @param self the AI player to be evaluated
     * @param n the number of candidates card we are choosing as a target
     * @return the list of target cards
     */
    private List<Card> findTopTargetCards(GameState state, Player self, int n) {
        List<Card> candidates = new ArrayList<>();

        //add all visible market cards
        for (int level = 1; level <= 3; level++) {
            for (Card card : state.getMarket().getVisibleCards(level)) {
                if (card != null) candidates.add(card);
            }
        }
        //add reserved cards
        for (Card card : self.getReservedCards()) {
            if (card != null) candidates.add(card);
        }

        //sort by score descending
        candidates.sort((a, b) -> Double.compare(
                scoreCard(b, state, self),
                scoreCard(a, state, self)));

        //return top n cards of the deck
        List<Card> top = new ArrayList<>();
        for (int i = 0; i < Math.min(n, candidates.size()); i++) {
            top.add(candidates.get(i));
        }
        return top;
    }


    //  Gem-taking logic (multi-target)

    /**
     * Builds a gem request considering deficits across multiple target cards.
     * Colors are prioritized by their combined deficit across all targets.
     *
     * try these in order:
     *  1. Three different gems from highest combined-deficit colors
     *  2. Two same gems from highest combined-deficit color (bank >= 4)
     *  3. Three different gems from any available bank color
     *  4. Two same gems from any available color
     *  5. Whatever gems remain
     *  6. Empty
     * 
     * @param self the AI player to be evaluated
     * @param targets a list of target cards to be purchased
     * @param the current gem bank
     * @return an AIAction to take the gem
     */
    private AIAction buildGemRequest(Player self, List<Card> targets, GemCollection bank) {
        //sums deficits across all targets
        Map<GemColor, Integer> combined = combinedDeficit(self, targets);

        //sort colors by descending combined deficit
        List<GemColor> prioritized = new ArrayList<>(combined.keySet());
        prioritized.sort((a, b) -> combined.get(b) - combined.get(a));

        //try 1: 3 different gems from deficit colors
        List<GemColor> pickable = new ArrayList<>();
        for (GemColor color : prioritized) {
            if (bank.getCount(color) >= 1) pickable.add(color);
            if (pickable.size() == 3) break;
        }
        if (pickable.size() == 3) {
            GemCollection request = new GemCollection();
            for (GemColor c : pickable) request = request.add(c, 1);
            if (rules.canTakeThreeDifferentGems(request, bank)) {
                return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false, request,
                        String.format("Take 3 different (targeted): %s", pickable));
            }
        }

        //try 2: 2 same gems from highest deficit (bank >= 4)
        for (GemColor color : prioritized) {
            if (rules.canTakeTwoSameGems(color, bank)) {
                GemCollection request = new GemCollection().add(color, 2);
                return new AIAction(ActionType.TAKE_TWO_SAME, null, false, request,
                        String.format("Take 2 %s gems (targeted)", color));
            }
        }

        //try 3: any 3 different gems from bank
        List<GemColor> available = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (bank.getCount(color) >= 1) available.add(color);
            if (available.size() == 3) break;
        }
        if (available.size() == 3) {
            GemCollection request = new GemCollection();
            for (GemColor c : available) request = request.add(c, 1);
            if (rules.canTakeThreeDifferentGems(request, bank)) {
                return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false, request,
                        String.format("Take 3 different (fallback): %s", available));
            }
        }

        //try 4: any 2 same
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (rules.canTakeTwoSameGems(color, bank)) {
                GemCollection request = new GemCollection().add(color, 2);
                return new AIAction(ActionType.TAKE_TWO_SAME, null, false, request,
                        String.format("Take 2 %s gems (fallback)", color));
            }
        }

        //try 5: whatever individual gems remain
        GemCollection request = new GemCollection();
        List<GemColor> taken = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
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

        //try 6: empty bank 
        return new AIAction(ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "No gems available");
    }


    //helpers

    /**
     * Sum the gem deficit across multiple target cards.
     * For each non-GOLD color, sums how many more gems the player needs
     * across all targets. This guides gem selection toward colors that
     * help the most cards simultaneously.
     * 
     * @param self the AI player
     * @param targets the list of targeted cards
     * @returns a map of deficit of each color across multiple target cards
     */
    private Map<GemColor, Integer> combinedDeficit(Player self, List<Card> targets) {
        Map<GemColor, Integer> combined = new EnumMap<>(GemColor.class);
        for (Card card : targets) {
            Map<GemColor, Integer> deficit = CardEvaluator.calculateDeficit(self, card);
            for (Map.Entry<GemColor, Integer> entry : deficit.entrySet()) {
                combined.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return combined;
    }


    /** recreate the player's gem counts into a mutable map (excludes zero counts). 
     *  a helper for gem return logic
     * 
     * @param self the AI player self
     * @return a map of the player's gem count
    */
    private Map<GemColor, Integer> snapshotGems(Player self) {
        Map<GemColor, Integer> snapshot = new EnumMap<>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            int count = self.getGems().getCount(color);
            if (count > 0) snapshot.put(color, count);
        }
        return snapshot;
    }


    /** returns the non-GOLD color with the highest count in the map. 
     * 
     * @param gems a map of current held gems
     * @return the most held color in that map
    */
    private GemColor findMostHeldColor(Map<GemColor, Integer> gems) {
        GemColor best = null;
        int bestCount = -1;
        for (Map.Entry<GemColor, Integer> entry : gems.entrySet()) {
            if (entry.getKey() == GemColor.GOLD) continue;
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }

        return best;
    }
}

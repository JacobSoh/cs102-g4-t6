package edu.cs102.g04t06.game.execution.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.ActionType;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Turn-weighted scoring AI strategy.
 * Evaluates every possible action by score and picks the highest.
 * Adapts weights to the current game phase (early / mid / late).
 */
public class HardAIStrategy implements AIStrategy {

    private static final GameRules RULES = new GameRules();
    private static final GemColor[] NON_GOLD = {
        GemColor.WHITE, GemColor.BLUE, GemColor.GREEN, GemColor.RED, GemColor.BLACK
    };

    // =========================================================
    // AIStrategy interface
    // =========================================================

    /**
     * Scores every purchasable card, reservable card, and gem-take combination,
     * then returns the highest-scored action.
     * @param current gamestate
     * @param the AI Player
     * @return Best Action to take
     */
    @Override
    public AIAction decideAction(GameState state, Player self) {
        double bestScore = Double.NEGATIVE_INFINITY;
        AIAction bestAction = null;

        // 1. Score all affordable purchase actions
        for (Card card : getAllVisibleCards(state)) {
            if (RULES.canAffordCard(self, card)) {
                double score = scoreCard(card, self, state);
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = new AIAction(ActionType.PURCHASE_CARD, card, false, null,
                            "AI purchases " + card.getBonus() + " card (level " + card.getLevel() + ")");
                }
            }
        }
        for (Card card : self.getReservedCards()) {
            if (RULES.canAffordCard(self, card)) {
                double score = scoreCard(card, self, state);
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = new AIAction(ActionType.PURCHASE_CARD, card, true, null,
                            "AI purchases reserved " + card.getBonus() + " card");
                }
            }
        }

        // 2. Score all reserve actions (0.7x — card secured but not yet owned)
        if (RULES.canReserveCard(self)) {
            for (Card card : getAllVisibleCards(state)) {
                double score = scoreCard(card, self, state) * 0.7;
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = new AIAction(ActionType.RESERVE_CARD, card, false, null,
                            "AI reserves " + card.getBonus() + " card (level " + card.getLevel() + ")");
                }
            }
        }

        // 3. Score best gem-take combination
        GemCollection bestGems = findBestGemTake(state, self);
        if (bestGems != null) {
            double gemScore = scoreGemTake(bestGems, state, self);
            if (bestAction == null || gemScore > bestScore) {
                bestAction = buildGemAction(bestGems);
            }
        }

        // Fallback: take any available gems (nearly-empty bank edge case)
        if (bestAction == null) {
            GemCollection fallback = takeSomeGems(state);
            if (fallback != null) {
                bestAction = buildGemAction(fallback);
            }
        }

        return bestAction;
    }

    /**
     * Picks the noble that opponents are closest to claiming (blocking strategy).
     * Since all nobles are worth the same 3 points, the tiebreaker is denying the most urgent opponent
     *
     * Score per candidate = sum over opponents of 1 / (deficit + 1)
     * where deficit = total bonus cards the opponent still needs for that noble.
     * A deficit of 0 means the opponent can already claim it next turn → highest urgency.
     * @param list of claimable nobles
     * @param current game state
     * @param AI player
     * @return the noble that opponents are closest to taking
     */
    @Override
    public Noble chooseNoble(List<Noble> claimable, GameState state, Player self) {
        Noble best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Noble candidate : claimable) {
            double blockingScore = 0.0;
            for (Player opponent : state.getPlayers()) {
                if (opponent == self) continue;
                int deficit = opponentNobleDeficit(candidate, opponent);
                blockingScore += 1.0 / (deficit + 1);
            }
            if (blockingScore > bestScore) {
                bestScore = blockingScore;
                best = candidate;
            }
        }

        return best != null ? best : claimable.get(0);
    }

    /**
     * Returns exactly excessCount gems to give back to the bank.
     * Keeps gems needed for the highest-valued target card; returns the rest first.
     * @param AI Player
     * @param number of extra gems
     * @param current game state
     * @return the gem collection of gems to be returned
     */
    @Override
    public GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state) {
        Card target = findBestTargetCard(state, self);

        // Build per-color deficit map for the target card
        Map<GemColor, Integer> deficit = new EnumMap<>(GemColor.class);
        if (target != null) {
            Cost reduced = target.getCost().afterBonuses(self.calculateBonuses());
            for (GemColor c : NON_GOLD) {
                int def = Math.max(0, reduced.getRequired(c) - self.getGems().getCount(c));
                deficit.put(c, def);
            }
        }

        // LOW priority = no deficit for this color (safe to return first)
        // HIGH priority = covers a deficit (keep if possible)
        List<GemColor> low = new ArrayList<>();
        List<GemColor> high = new ArrayList<>();
        for (GemColor c : NON_GOLD) {
            if (self.getGems().getCount(c) <= 0) continue;
            if (deficit.getOrDefault(c, 0) > 0) {
                high.add(c);
            } else {
                low.add(c);
            }
        }

        // Within each priority group return the most-held color first
        Comparator<GemColor> byCountDesc = (a, b) ->
                self.getGems().getCount(b) - self.getGems().getCount(a);
        low.sort(byCountDesc);
        high.sort(byCountDesc);

        List<GemColor> returnOrder = new ArrayList<>(low);
        returnOrder.addAll(high);

        GemCollection toReturn = new GemCollection();
        int remaining = excessCount;
        for (GemColor c : returnOrder) {
            if (remaining <= 0) break;
            int available = self.getGems().getCount(c) - toReturn.getCount(c);
            int amount = Math.min(remaining, available);
            if (amount > 0) {
                toReturn = toReturn.add(c, amount);
                remaining -= amount;
            }
        }

        // Last resort: return gold if still short
        if (remaining > 0) {
            int goldAmount = Math.min(remaining, self.getGems().getCount(GemColor.GOLD));
            if (goldAmount > 0) {
                toReturn = toReturn.add(GemColor.GOLD, goldAmount);
            }
        }

        return toReturn;
    }

    // =========================================================
    // Card scoring sub-methods
    // =========================================================

    /**
     * Returns how many more bonus cards {@code opponent} needs to claim {@code noble}.
     * A value of 0 means the opponent already meets all requirements.
     */
    private int opponentNobleDeficit(Noble noble, Player opponent) {
        Map<GemColor, Integer> bonuses = opponent.calculateBonuses();
        int deficit = 0;
        for (Map.Entry<GemColor, Integer> entry : noble.getRequirements().entrySet()) {
            deficit += Math.max(0, entry.getValue() - bonuses.getOrDefault(entry.getKey(), 0));
        }
        return deficit;
    }

    /**
     * Central scoring formula:
     * (Prestige x W1 + NobleProgress x W2 + DiscountUtility x W3) / turnsToAcquire
     * plus a blocking bonus for opponents near winning.
     */
    private double scoreCard(Card card, Player self, GameState state) {
        double[] w = getPhaseWeights(self, state);
        double prestige      = card.getPoints();
        double nobleProgress = calculateNobleProgress(card, self, state.getAvailableNobles());
        double discountUtil  = calculateDiscountUtility(card, state);
        int turns            = calculateTurnsToAcquire(card, self, state.getGemBank());
        double base = (prestige * w[0] + nobleProgress * w[1] + discountUtil * w[2]) / turns;
        return base + calculateBlockingBonus(card, state, self);
    }

    /**
     * Returns how much purchasing this card advances the AI toward any available noble.
     * Contribution = 1 / remaining-needed for each noble that requires this card's bonus.
     */
    private double calculateNobleProgress(Card card, Player self, List<Noble> nobles) {
        Map<GemColor, Integer> bonuses = self.calculateBonuses();
        GemColor bonus = card.getBonus();
        double progress = 0.0;
        for (Noble noble : nobles) {
            int required  = noble.getRequirements().getOrDefault(bonus, 0);
            int have      = bonuses.getOrDefault(bonus, 0);
            int remaining = required - have;
            if (remaining > 0) {
                progress += 1.0 / remaining;
            }
        }
        return progress;
    }

    /**
     * Returns the fraction of currently visible cards that require this card's bonus color.
     * Higher = buying this card produces a widely-useful discount.
     */
    private double calculateDiscountUtility(Card card, GameState state) {
        GemColor bonus = card.getBonus();
        int matching = 0;
        int total = 0;
        for (int level = 1; level <= 3; level++) {
            for (Card visible : state.getMarket().getVisibleCards(level)) {
                total++;
                if (visible.getCost().getRequired(bonus) > 0) {
                    matching++;
                }
            }
        }
        return total == 0 ? 0.0 : (double) matching / total;
    }

    /**
     * Estimates how many gem-take turns are needed before the AI can afford this card.
     * Returns at least 1 to prevent division by zero in scoreCard.
     */
    private int calculateTurnsToAcquire(Card card, Player self, GemCollection bank) {
        return calculateTurnsInternal(card, self.calculateBonuses(), self.getGems(), bank);
    }

    /**
     * Internal overload accepting an explicit gem state so gem-take simulations
     * can compute turns-after without mutating the real Player.
     */
    private int calculateTurnsInternal(Card card, Map<GemColor, Integer> bonuses,
                                        GemCollection playerGems, GemCollection bank) {
        Cost actualCost = card.getCost().afterBonuses(bonuses);

        Map<GemColor, Integer> deficit = new EnumMap<>(GemColor.class);
        for (GemColor c : NON_GOLD) {
            int def = Math.max(0, actualCost.getRequired(c) - playerGems.getCount(c));
            if (def > 0) {
                deficit.put(c, def);
            }
        }

        // Gold wildcards reduce the effective deficit
        int gold     = playerGems.getCount(GemColor.GOLD);
        int totalDef = deficit.values().stream().mapToInt(Integer::intValue).sum();
        totalDef     = Math.max(0, totalDef - gold);
        if (totalDef == 0) return 1;

        // Simulate turns by greedily reducing deficits each turn
        Map<GemColor, Integer> sim = new EnumMap<>(deficit);
        int turns = 0;
        while (sim.values().stream().mapToInt(Integer::intValue).sum() > 0) {
            turns++;
            List<GemColor> colors = new ArrayList<>();
            for (Map.Entry<GemColor, Integer> e : sim.entrySet()) {
                if (e.getValue() > 0) colors.add(e.getKey());
            }

            if (colors.size() >= 3) {
                // Take 3 different: reduce the top-3 deficit colors by 1
                colors.sort((a, b) -> sim.get(b) - sim.get(a));
                for (int i = 0; i < 3; i++) {
                    GemColor c = colors.get(i);
                    sim.put(c, sim.get(c) - 1);
                }
            } else if (colors.size() == 1) {
                GemColor c = colors.get(0);
                int def = sim.get(c);
                if (def >= 2 && bank.getCount(c) >= 4) {
                    sim.put(c, def - 2);   // Take 2 same
                } else {
                    sim.put(c, def - 1);
                }
            } else {
                // 2 colors: reduce both by 1
                for (GemColor c : colors) {
                    sim.put(c, sim.get(c) - 1);
                }
            }
        }
        return Math.max(1, turns);
    }

    /**
     * Returns [W1, W2, W3] tuned to the current game phase.
     * Early: favor discount building. Mid: balanced. Late: favor prestige points.
     */
    private double[] getPhaseWeights(Player self, GameState state) {
        int totalBonuses = self.calculateBonuses().values().stream()
                .mapToInt(Integer::intValue).sum();
        boolean anyNearWin = state.getPlayers().stream()
                .anyMatch(p -> p.getPoints() >= 10);

        if (totalBonuses < 5) {
            return new double[]{0.1, 0.3, 0.6};    // Early
        } else if (totalBonuses <= 10 && !anyNearWin) {
            return new double[]{0.4, 0.4, 0.2};    // Mid
        } else {
            return new double[]{0.7, 0.2, 0.1};    // Late
        }
    }

    /**
     * Adds extra score for buying/reserving a card that blocks an opponent
     * who is close to winning.
     */
    private double calculateBlockingBonus(Card card, GameState state, Player self) {
        double blockScore = 0.0;
        int threshold = state.getWinningThreshold();

        for (Player opponent : state.getPlayers()) {
            if (opponent == self) continue;
            if (opponent.getPoints() < 12) continue;

            double urgency = (double) opponent.getPoints() / threshold;
            Map<GemColor, Integer> opponentBonuses = opponent.calculateBonuses();

            // Block opponent's noble progress if this card's bonus is needed
            for (Noble noble : state.getAvailableNobles()) {
                int remaining = Math.max(0,
                        noble.getRequirements().getOrDefault(card.getBonus(), 0)
                        - opponentBonuses.getOrDefault(card.getBonus(), 0));
                if (remaining > 0) {
                    blockScore += urgency * 2.0;
                }
            }

            // Block opponent from buying a card that would win the game
            if (RULES.canAffordCard(opponent, card)) {
                if (opponent.getPoints() + card.getPoints() >= threshold) {
                    blockScore += urgency * 5.0;
                }
            }
        }
        return blockScore;
    }

    // =========================================================
    // Gem-take helpers
    // =========================================================

    /**
     * Enumerates all valid 3-different and 2-same gem combinations,
     * scores each by how much it reduces turns-to-acquire for the top 3 target cards,
     * and returns the best combination.
     */
    private GemCollection findBestGemTake(GameState state, Player self) {
        List<Card> topCards    = getTopCards(state, self, 3);
        GemCollection bank     = state.getGemBank();

        // Pre-compute card scores to avoid re-scoring in inner loops
        Map<Card, Double> cardScores = new LinkedHashMap<>();
        for (Card c : topCards) cardScores.put(c, scoreCard(c, self, state));

        GemCollection best = null;
        double bestScore   = Double.NEGATIVE_INFINITY;

        // All C(5,3) = 10 three-different combinations
        for (int i = 0; i < NON_GOLD.length; i++) {
            for (int j = i + 1; j < NON_GOLD.length; j++) {
                for (int k = j + 1; k < NON_GOLD.length; k++) {
                    GemCollection combo = new GemCollection()
                            .add(NON_GOLD[i], 1).add(NON_GOLD[j], 1).add(NON_GOLD[k], 1);
                    if (!RULES.canTakeThreeDifferentGems(combo, bank)) continue;
                    double score = scoreGemCombo(combo, topCards, cardScores, self, bank);
                    if (score > bestScore) { bestScore = score; best = combo; }
                }
            }
        }

        // All 5 two-same combinations
        for (GemColor c : NON_GOLD) {
            if (!RULES.canTakeTwoSameGems(c, bank)) continue;
            GemCollection combo = new GemCollection().add(c, 2);
            double score = scoreGemCombo(combo, topCards, cardScores, self, bank);
            if (score > bestScore) { bestScore = score; best = combo; }
        }

        return best != null ? best : takeSomeGems(state);
    }

    /** Public-facing gem-take scorer used in decideAction for comparison. */
    private double scoreGemTake(GemCollection gems, GameState state, Player self) {
        List<Card> topCards = getTopCards(state, self, 3);
        Map<Card, Double> cardScores = new LinkedHashMap<>();
        for (Card c : topCards) cardScores.put(c, scoreCard(c, self, state));
        return scoreGemCombo(gems, topCards, cardScores, self, state.getGemBank());
    }

    /**
     * Scores a gem combination: sum of (cardScore x turnsReduced) across top cards.
     * Simulates taking the gems and recalculates turns-to-acquire for each target.
     */
    private double scoreGemCombo(GemCollection combo, List<Card> topCards, Map<Card, Double> cardScores, Player self, GemCollection bank) {
        GemCollection simGems      = self.getGems().add(combo);
        Map<GemColor, Integer> bonuses = self.calculateBonuses();
        double score = 0.0;
        for (Card card : topCards) {
            int before  = calculateTurnsInternal(card, bonuses, self.getGems(), bank);
            int after   = calculateTurnsInternal(card, bonuses, simGems, bank);
            int reduced = before - after;
            if (reduced > 0) {
                score += cardScores.get(card) * reduced;
            }
        }
        return score;
    }

    // =========================================================
    // Utility helpers
    // =========================================================

    private List<Card> getAllVisibleCards(GameState state) {
        List<Card> all = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            all.addAll(state.getMarket().getVisibleCards(level));
        }
        return all;
    }

    /** Returns up to n cards from market + reserved hand, sorted by score descending. */
    private List<Card> getTopCards(GameState state, Player self, int n) {
        List<Card> all = new ArrayList<>(getAllVisibleCards(state));
        all.addAll(self.getReservedCards());
        all.sort((a, b) -> Double.compare(scoreCard(b, self, state), scoreCard(a, self, state)));
        return all.subList(0, Math.min(n, all.size()));
    }

    private Card findBestTargetCard(GameState state, Player self) {
        List<Card> top = getTopCards(state, self, 1);
        return top.isEmpty() ? null : top.get(0);
    }

    /** Counts how many bonus colors are shared in both nobles' requirements. */
    private int countRequirementOverlap(Noble a, Noble b) {
        Map<GemColor, Integer> reqA = a.getRequirements();
        Map<GemColor, Integer> reqB = b.getRequirements();
        int overlap = 0;
        for (GemColor c : reqA.keySet()) {
            if (reqA.get(c) > 0 && reqB.getOrDefault(c, 0) > 0) overlap++;
        }
        return overlap;
    }

    /** Wraps a GemCollection in the correct TAKE AIAction type. */
    private AIAction buildGemAction(GemCollection gems) {
        int nonZero = 0;
        GemColor onlyColor = null;
        for (GemColor c : GemColor.values()) {
            if (gems.getCount(c) > 0) { nonZero++; onlyColor = c; }
        }
        boolean twoSame = (gems.getTotalCount() == 2 && nonZero == 1);
        ActionType type = twoSame ? ActionType.TAKE_TWO_SAME : ActionType.TAKE_THREE_DIFFERENT;
        String desc = twoSame ? "AI takes 2 " + onlyColor : "AI takes 3 different gems";
        return new AIAction(type, null, false, gems, desc);
    }

    /**
     * Fallback for a nearly-empty bank: take up to 3 different available gems.
     * Returns null only if the bank is completely empty.
     */
    private GemCollection takeSomeGems(GameState state) {
        GemCollection bank  = state.getGemBank();
        GemCollection combo = new GemCollection();
        int count = 0;
        for (GemColor c : NON_GOLD) {
            if (count >= 3) break;
            if (bank.getCount(c) >= 1) { combo = combo.add(c, 1); count++; }
        }
        return combo.getTotalCount() > 0 ? combo : null;
    }
}

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
 * Adapts weights to the current game phase (bonus-chase / early / mid / late).
 */
public class HardAIStrategy implements AIStrategy {

    private static final GameRules RULES = new GameRules();
    private static final GemColor[] NON_GOLD = {
        GemColor.WHITE, GemColor.BLUE, GemColor.GREEN, GemColor.RED, GemColor.BLACK
    };

    /**
     * Scores every purchasable card, reservable card, and gem-take combination,
     * then returns the highest-scored action.
     * @param current gamestate
     * @param the AI Player
     * @return Best action to take
     */
    @Override
    public AIAction decideAction(GameState state, Player self) {
        // 0. Winning move: if any affordable card pushes score to winning threshold, take it immediately
        AIAction winningMove = findWinningMove(state, self);
        if (winningMove != null) return winningMove;

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

        // 2. Reserve ONLY to block an opponent who is about to win or claim a noble
        if (RULES.canReserveCard(self)) {
            for (Card card : getAllVisibleCards(state)) {
                if (!isBlockingReserve(card, state, self)) continue;
                double score = scoreCard(card, self, state) * 0.7;
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = new AIAction(ActionType.RESERVE_CARD, card, false, null,
                            "AI blocks " + card.getBonus() + " card (level " + card.getLevel() + ")");
                }
            }
        }

        // 3. Score best gem-take combination; if no good combo exists, fall back to
        //    reserving the highest-scored visible card (best value / turns trade-off)
        GemCollection bestGems = findBestGemTake(state, self);
        if (bestGems != null) {
            double gemScore = scoreGemTake(bestGems, state, self);
            if (bestAction == null || gemScore > bestScore) {
                bestAction = buildGemAction(bestGems);
            }
        } else if (RULES.canReserveCard(self)) {
            Card bestToReserve = getAllVisibleCards(state).stream()
                    .max(Comparator.comparingDouble(c -> scoreCard(c, self, state)))
                    .orElse(null);
            if (bestToReserve != null) {
                double score = scoreCard(bestToReserve, self, state);
                if (bestAction == null || score > bestScore) {
                    bestAction = new AIAction(ActionType.RESERVE_CARD, bestToReserve, false, null,
                            "AI reserves best card: " + bestToReserve.getBonus()
                                    + " (level " + bestToReserve.getLevel() + ")");
                }
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
     *
     * Score per noble = sum over opponents of 1 / (deficit + 1)
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
        return toReturn;
    }

    // =========================================================
    // Card scoring sub-methods
    // =========================================================

    /**
     * Returns how many more bonus cards the opponent needs to claim noble
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
     *
     *   base = (Prestige×W1 + NobleProgress×W2 + DiscountUtility×W3)
     *          / turns^1.3
     *          × diversityFactor
     *
     * plus a blocking bonus for opponents near winning.
     *
     * turns^1.3 (instead of linear turns) penalises expensive cards progressively
     * more, pushing the AI toward affordable buys in mid game.
     *
     * diversityFactor = 1 / (1 + existingSameColorBonuses × 0.3) gives diminishing
     * returns for stacking the same bonus color, naturally spreading acquisitions.
     *
     * In the bonus-chase phase (W1=0, W2=0, W3=1) this reduces to:
     *   DiscountUtility / turns^1.3 × diversityFactor
     * — pick the cheapest, most-useful-discount card that does not stack a color
     * the AI already holds heavily.
     */
    private double scoreCard(Card card, Player self, GameState state) {
        double[] w = getPhaseWeights(self);
        double prestige      = card.getPoints();
        double nobleProgress = calculateNobleProgress(card, self, state.getAvailableNobles());
        double discountUtil  = calculateDiscountUtility(card, state);
        int turns            = calculateTurnsInternal(card, self.calculateBonuses(), self.getGems());

        // Diminishing-returns penalty for stacking the same bonus colour
        int existing = self.calculateBonuses().getOrDefault(card.getBonus(), 0);
        double diversityFactor = 1.0 / (1.0 + existing * 0.3);

        double base = (prestige * w[0] + nobleProgress * w[1] + discountUtil * w[2])
                / Math.pow(turns, 1.3)
                * diversityFactor;
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
     * Formula: turns = max(ceil(netDeficit / 3), ceil(maxColorDeficit / 2)), where
     *   netDeficit      = sum of per-color deficits minus gold wildcards.
     *   maxColorDeficit = largest single-color deficit after applying gold to that color.
     *
     * The net formula captures overall throughput (3 gems/turn max).
     * The color bottleneck captures that at most 2 of one color can be taken per turn,
     * so a single high-deficit color can be the real limiting factor even when the
     * total looks reachable quickly.
     */
    private int calculateTurnsInternal(Card card, Map<GemColor, Integer> bonuses,
                                        GemCollection playerGems) {
        int totalDeficit = 0;
        int maxColorDeficit = 0;
        for (GemColor c : NON_GOLD) {
            int actual = Math.max(0, card.getCost().getRequired(c) - bonuses.getOrDefault(c, 0));
            int colorDeficit = Math.max(0, actual - playerGems.getCount(c));
            totalDeficit += colorDeficit;
            maxColorDeficit = Math.max(maxColorDeficit, colorDeficit);
        }
        int gold = playerGems.getCount(GemColor.GOLD);
        int netDeficit = Math.max(0, totalDeficit - gold);
        if (netDeficit == 0) {
            return 1;
        }

        return Math.max((netDeficit + 2) / 3, maxColorDeficit);
    }

    /**
     * Returns [W1, W2, W3] tuned to the current game phase.
     *
     * Bonus-chase (bonuses < 5):                  W=[0, 0, 1]   — pure discount utility.
     * Early       (bonuses >= 5, prestige < 5):   W=[0.1, 0.3, 0.6] — discount + noble progress.
     * Mid         (5 < prestige < 10):             W=[0.4, 0.4, 0.2] — balanced.
     * Late        (prestige >= 10):                W=[0.7, 0.2, 0.1] — prestige focused.
     */
    private double[] getPhaseWeights(Player self) {
        int totalBonuses = self.calculateBonuses().values().stream()
                .mapToInt(Integer::intValue).sum();
        int prestige = self.getPoints();

        if (totalBonuses < 5) {
            return new double[]{0.0, 0.0, 1.0};    // Bonus-chase: pure discount utility
        } else if (prestige < 5) {
            return new double[]{0.1, 0.3, 0.6};    // Early: discount + noble progress
        } else if (prestige < 10) {
            return new double[]{0.4, 0.4, 0.2};    // Mid: balanced
        } else {
            return new double[]{0.7, 0.2, 0.1};    // Late: prestige focused
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

    /**
     * Returns true if reserving this card serves a blocking purpose:
     * (a) an opponent can afford it and buying it would reach the winning threshold, or
     * (b) an opponent with >= 10 points needs exactly one more of this card's bonus color
     *     to claim an available noble.
     */
    private boolean isBlockingReserve(Card card, GameState state, Player self) {
        int threshold = state.getWinningThreshold();
        for (Player opponent : state.getPlayers()) {
            if (opponent == self) continue;
            if (RULES.canAffordCard(opponent, card)
                    && opponent.getPoints() + card.getPoints() >= threshold) {
                return true;
            }
            if (opponent.getPoints() >= 10) {
                Map<GemColor, Integer> opBonuses = opponent.calculateBonuses();
                for (Noble noble : state.getAvailableNobles()) {
                    int req = noble.getRequirements().getOrDefault(card.getBonus(), 0);
                    int has = opBonuses.getOrDefault(card.getBonus(), 0);
                    if (req > 0 && req - has == 1) {
                        return true;
                    }
                }
            }
        }
        return false;
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
                    double score = scoreGemCombo(combo, topCards, cardScores, self);
                    if (score > bestScore) { bestScore = score; best = combo; }
                }
            }
        }

        // All 5 two-same combinations
        for (GemColor c : NON_GOLD) {
            if (!RULES.canTakeTwoSameGems(c, bank)) continue;
            GemCollection combo = new GemCollection().add(c, 2);
            double score = scoreGemCombo(combo, topCards, cardScores, self);
            if (score > bestScore) { bestScore = score; best = combo; }
        }

        return best != null ? best : takeSomeGems(state);
    }

    /**
     * Scores a gem-take for comparison against purchases in decideAction.
     * Applies a saturation penalty when the player already holds many gems so
     * that buying (if possible) beats endlessly collecting more gems.
     *
     * Saturation factor: max(0.1, 1.0 - max(0, held-5) x 0.15)
     *   held <= 5: 1.00 (no penalty)
     *   held  = 7: 0.70
     *   held  = 9: 0.40
     *   held  = 10: 0.25
     *
     * Overflow penalty (applied on top of saturation): if taking these gems would
     * push the total above 10, a severe additional multiplier is applied.
     *   1 over limit: × 0.50
     *   2 over limit: × 0.25
     *   3 over limit: × 0.02
     * This is to penalise any gem combination that would make the AI have to return gems
     */
    private double scoreGemTake(GemCollection gems, GameState state, Player self) {
        List<Card> topCards = getTopCards(state, self, 3);
        Map<Card, Double> cardScores = new LinkedHashMap<>();
        for (Card c : topCards) cardScores.put(c, scoreCard(c, self, state));
        double raw = scoreGemCombo(gems, topCards, cardScores, self);
        int held = self.getGemCount();
        double saturation = Math.max(0.1, 1.0 - Math.max(0, held - 5) * 0.15);

        int heldAfter = held + gems.getTotalCount();
        double overflowPenalty = 1.0;
        if (heldAfter > 10) {
            int excess = heldAfter - 10;
            overflowPenalty = Math.max(0.01, Math.pow(0.2, excess));
        }

        return raw * saturation * overflowPenalty;
    }

    /**
     * Scores a gem combination: sum of (cardScore x turnsReduced) across top cards.
     * Simulates taking the gems and recalculates turns-to-acquire for each target.
     *
     * Also applies an overflow penalty when the combo would push the player above
     * 10 gems, so findBestGemTake naturally prefers non-overflowing combinations
     * when scoring alternatives against each other.
     */
    private double scoreGemCombo(GemCollection combo, List<Card> topCards, Map<Card, Double> cardScores, Player self) {
        GemCollection simGems      = self.getGems().add(combo);
        Map<GemColor, Integer> bonuses = self.calculateBonuses();
        double score = 0.0;
        for (Card card : topCards) {
            int before  = calculateTurnsInternal(card, bonuses, self.getGems());
            int after   = calculateTurnsInternal(card, bonuses, simGems);
            int reduced = before - after;
            if (reduced > 0) {
                score += cardScores.get(card) * reduced;
            }
        }

        int heldAfter = self.getGemCount() + combo.getTotalCount();
        if (heldAfter > 10) {
            int excess = heldAfter - 10;
            score *= Math.max(0.01, Math.pow(0.2, excess));
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


    /** Wraps a GemCollection in the correct TAKE AIAction type. */
    /**
     * Returns a PURCHASE_CARD action for the first affordable card that would bring
     * self's prestige to the winning threshold or above
     * Checks visible cards first, then reserved cards.
     */
    private AIAction findWinningMove(GameState state, Player self) {
        int threshold = state.getWinningThreshold();
        for (Card card : getAllVisibleCards(state)) {
            if (RULES.canAffordCard(self, card) && winsWithCard(card, self, state, threshold)) {
                return new AIAction(ActionType.PURCHASE_CARD, card, false, null,
                        "AI wins by purchasing " + card.getBonus() + " card (level " + card.getLevel() + ")");
            }
        }
        for (Card card : self.getReservedCards()) {
            if (RULES.canAffordCard(self, card) && winsWithCard(card, self, state, threshold)) {
                return new AIAction(ActionType.PURCHASE_CARD, card, true, null,
                        "AI wins by purchasing reserved " + card.getBonus() + " card");
            }
        }
        return null;
    }

    private boolean winsWithCard(Card card, Player self, GameState state, int threshold) {
        int pointsAfterCard = self.getPoints() + card.getPoints();
        if (pointsAfterCard >= threshold) return true;

        // Check if buying this card unlocks a noble that completes the win
        Map<GemColor, Integer> simBonuses = new EnumMap<>(self.calculateBonuses());
        simBonuses.merge(card.getBonus(), 1, Integer::sum);
        for (Noble noble : state.getAvailableNobles()) {
            if (Noble.canBeClaimed(noble, simBonuses)
                    && pointsAfterCard + noble.getPoints() >= threshold) {
                return true;
            }
        }
        return false;
    }

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

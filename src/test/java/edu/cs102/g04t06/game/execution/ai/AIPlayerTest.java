package edu.cs102.g04t06.game.execution.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.ActionType;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

@DisplayName("AIPlayer Tests")
class AIPlayerTest {

    // =========================================================================
    // Stub strategies
    //
    // AIPlayer is a thin delegation wrapper — it forwards every call to its
    // AIStrategy.  To test delegation without needing real game logic, we use
    // stubs that record which method was called and return pre-configured
    // responses.  This keeps tests fast, deterministic, and independent of
    // EasyAIStrategy / HardAIStrategy internals.
    // =========================================================================

    /**
     * A spy / stub strategy that records every call and returns pre-set values.
     * Each decideAction / chooseNoble / chooseGemsToReturn call increments a
     * counter and captures the arguments for later assertions.
     */
    static class SpyStrategy implements AIStrategy {

        // --- Pre-configured return values ---
        AIAction actionToReturn;
        Noble nobleToReturn;
        GemCollection gemsToReturn;

        // --- Call counters ---
        int decideActionCallCount = 0;
        int chooseNobleCallCount = 0;
        int chooseGemsToReturnCallCount = 0;

        // --- Captured arguments ---
        GameState lastDecideState;
        Player lastDecidePlayer;

        List<Noble> lastClaimableNobles;
        GameState lastNobleState;
        Player lastNoblePlayer;

        Player lastGemsPlayer;
        int lastExcessCount;
        GameState lastGemsState;

        @Override
        public AIAction decideAction(GameState state, Player self) {
            decideActionCallCount++;
            lastDecideState = state;
            lastDecidePlayer = self;
            return actionToReturn;
        }

        @Override
        public Noble chooseNoble(List<Noble> claimable, GameState state, Player self) {
            chooseNobleCallCount++;
            lastClaimableNobles = claimable;
            lastNobleState = state;
            lastNoblePlayer = self;
            return nobleToReturn;
        }

        @Override
        public GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state) {
            chooseGemsToReturnCallCount++;
            lastGemsPlayer = self;
            lastExcessCount = excessCount;
            lastGemsState = state;
            return gemsToReturn;
        }
    }

    /**
     * A second stub strategy used to verify that setStrategy() actually swaps
     * the delegation target.  Returns a distinct AIAction so tests can tell
     * which strategy was invoked.
     */
    static class AlternateStrategy implements AIStrategy {

        AIAction actionToReturn;
        Noble nobleToReturn;
        GemCollection gemsToReturn;

        @Override
        public AIAction decideAction(GameState state, Player self) {
            return actionToReturn;
        }

        @Override
        public Noble chooseNoble(List<Noble> claimable, GameState state, Player self) {
            return nobleToReturn;
        }

        @Override
        public GemCollection chooseGemsToReturn(Player self, int excessCount, GameState state) {
            return gemsToReturn;
        }
    }

    // =========================================================================
    // Shared fixtures
    // =========================================================================

    private Player player;
    private SpyStrategy spyStrategy;
    private AIPlayer aiPlayer;
    private GameState dummyState;

    /** Builds a Cost with a single colour requirement. */
    private Cost cost(GemColor color, int amount) {
        Map<GemColor, Integer> map = new EnumMap<>(GemColor.class);
        map.put(color, amount);
        return new Cost(map);
    }

    /** Builds a Noble with given colour requirements. */
    private Noble noble(int id, GemColor color, int amount) {
        Map<GemColor, Integer> req = new EnumMap<>(GemColor.class);
        req.put(color, amount);
        return new Noble(id, "Noble-" + id, req);
    }

    /**
     * Builds a minimal GameState for delegation tests.
     * We only need a valid GameState reference — the AI logic itself
     * lives in the strategy, not in AIPlayer.
     */
    private GameState buildDummyState() {
        // Pad each tier to 20 cards as required by CardMarket
        Map<GemColor, Integer> fillerCost = new EnumMap<>(GemColor.class);
        fillerCost.put(GemColor.WHITE, 1);
        Cost cheap = new Cost(fillerCost);
        List<Card> filler = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            filler.add(new Card(1, 0, GemColor.WHITE, cheap));
        }

        GemCollection bank = new GemCollection()
            .add(GemColor.RED,   7)
            .add(GemColor.BLUE,  7)
            .add(GemColor.GREEN, 7)
            .add(GemColor.WHITE, 7)
            .add(GemColor.BLACK, 7)
            .add(GemColor.GOLD,  5);

        CardMarket market = new CardMarket(
            new ArrayList<>(filler),
            new ArrayList<>(filler),
            new ArrayList<>(filler)
        );
        Player dummy = new Player("Dummy", 1);
        return new GameState(
            List.of(dummy),
            market,
            bank,
            Collections.emptyList(),
            15
        );
    }

    @BeforeEach
    void setUp() {
        player = new Player("TestAI", 1);
        spyStrategy = new SpyStrategy();
        aiPlayer = new AIPlayer(player, spyStrategy);
        dummyState = buildDummyState();
    }

    // =========================================================================
    // 1. Constructor & getPlayer
    // =========================================================================

    @Nested
    @DisplayName("Constructor and getPlayer")
    class ConstructorTests {

        @Test
        @DisplayName("getPlayer returns the Player passed to the constructor")
        void getPlayer_returnsSamePlayer() {
            assertSame(player, aiPlayer.getPlayer(),
                "getPlayer() must return the exact Player object passed to the constructor");
        }

        @Test
        @DisplayName("getPlayer returns correct player after construction with different player")
        void getPlayer_differentPlayer() {
            Player other = new Player("OtherAI", 2);
            AIPlayer otherAI = new AIPlayer(other, spyStrategy);
            assertSame(other, otherAI.getPlayer());
        }

        @Test
        @DisplayName("Two AIPlayers wrapping different players return different players")
        void twoAIPlayers_differentPlayers() {
            Player p1 = new Player("AI-1", 1);
            Player p2 = new Player("AI-2", 2);
            AIPlayer ai1 = new AIPlayer(p1, spyStrategy);
            AIPlayer ai2 = new AIPlayer(p2, spyStrategy);
            assertNotSame(ai1.getPlayer(), ai2.getPlayer());
        }
    }

    // =========================================================================
    // 2. decideAction — delegation
    // =========================================================================

    @Nested
    @DisplayName("decideAction delegation")
    class DecideActionTests {

        @Test
        @DisplayName("decideAction returns exactly what the strategy returns")
        void returnsStrategyResult() {
            Card card = new Card(1, 1, GemColor.GREEN, cost(GemColor.RED, 2));
            AIAction expected = new AIAction(
                ActionType.PURCHASE_CARD, card, false, null, "Buy green card"
            );
            spyStrategy.actionToReturn = expected;

            AIAction result = aiPlayer.decideAction(dummyState);
            assertSame(expected, result,
                "decideAction must return the exact AIAction from the strategy");
        }

        @Test
        @DisplayName("decideAction passes the correct GameState to the strategy")
        void passesCorrectState() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "take gems"
            );
            aiPlayer.decideAction(dummyState);
            assertSame(dummyState, spyStrategy.lastDecideState,
                "Strategy must receive the exact GameState passed to decideAction");
        }

        @Test
        @DisplayName("decideAction passes the wrapped Player to the strategy")
        void passesWrappedPlayer() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "take gems"
            );
            aiPlayer.decideAction(dummyState);
            assertSame(player, spyStrategy.lastDecidePlayer,
                "Strategy must receive the AIPlayer's wrapped Player");
        }

        @Test
        @DisplayName("decideAction calls the strategy exactly once per invocation")
        void callsStrategyOnce() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "take gems"
            );
            aiPlayer.decideAction(dummyState);
            assertEquals(1, spyStrategy.decideActionCallCount,
                "Strategy.decideAction should be called exactly once");
        }

        @Test
        @DisplayName("Multiple decideAction calls each delegate to the strategy")
        void multipleCallsAllDelegate() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "take gems"
            );
            aiPlayer.decideAction(dummyState);
            aiPlayer.decideAction(dummyState);
            aiPlayer.decideAction(dummyState);
            assertEquals(3, spyStrategy.decideActionCallCount);
        }

        @Test
        @DisplayName("decideAction can return a PURCHASE_CARD action")
        void canReturnPurchaseAction() {
            Card card = new Card(2, 3, GemColor.WHITE, cost(GemColor.BLUE, 3));
            spyStrategy.actionToReturn = new AIAction(
                ActionType.PURCHASE_CARD, card, false, null, "Buy card"
            );
            AIAction result = aiPlayer.decideAction(dummyState);
            assertEquals(ActionType.PURCHASE_CARD, result.getActionType());
            assertSame(card, result.getTargetCard());
        }

        @Test
        @DisplayName("decideAction can return a RESERVE_CARD action")
        void canReturnReserveAction() {
            Card card = new Card(3, 5, GemColor.BLACK, cost(GemColor.RED, 4));
            spyStrategy.actionToReturn = new AIAction(
                ActionType.RESERVE_CARD, card, false, null, "Reserve high-value card"
            );
            AIAction result = aiPlayer.decideAction(dummyState);
            assertEquals(ActionType.RESERVE_CARD, result.getActionType());
            assertSame(card, result.getTargetCard());
        }

        @Test
        @DisplayName("decideAction can return a TAKE_TWO_SAME action")
        void canReturnTakeTwoSameAction() {
            GemCollection gems = new GemCollection().add(GemColor.RED, 2);
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_TWO_SAME, null, false, gems, "Take 2 red"
            );
            AIAction result = aiPlayer.decideAction(dummyState);
            assertEquals(ActionType.TAKE_TWO_SAME, result.getActionType());
            assertNull(result.getTargetCard());
        }

        @Test
        @DisplayName("decideAction can return a fromReserved purchase")
        void canReturnFromReservedPurchase() {
            Card card = new Card(1, 1, GemColor.GREEN, cost(GemColor.RED, 2));
            spyStrategy.actionToReturn = new AIAction(
                ActionType.PURCHASE_CARD, card, true, null, "Buy from reserved"
            );
            AIAction result = aiPlayer.decideAction(dummyState);
            assertTrue(result.isFromReserved());
        }
    }

    // =========================================================================
    // 3. chooseNoble — delegation
    // =========================================================================

    @Nested
    @DisplayName("chooseNoble delegation")
    class ChooseNobleTests {

        @Test
        @DisplayName("chooseNoble returns exactly what the strategy returns")
        void returnsStrategyResult() {
            Noble expected = noble(1, GemColor.GREEN, 3);
            spyStrategy.nobleToReturn = expected;

            Noble result = aiPlayer.chooseNoble(List.of(expected), dummyState);
            assertSame(expected, result,
                "chooseNoble must return the exact Noble from the strategy");
        }

        @Test
        @DisplayName("chooseNoble passes the claimable list to the strategy")
        void passesClaimableList() {
            Noble n1 = noble(1, GemColor.GREEN, 3);
            Noble n2 = noble(2, GemColor.BLUE, 4);
            List<Noble> claimable = List.of(n1, n2);
            spyStrategy.nobleToReturn = n1;

            aiPlayer.chooseNoble(claimable, dummyState);
            assertSame(claimable, spyStrategy.lastClaimableNobles,
                "Strategy must receive the exact claimable list");
        }

        @Test
        @DisplayName("chooseNoble passes the correct GameState to the strategy")
        void passesCorrectState() {
            Noble n = noble(1, GemColor.RED, 3);
            spyStrategy.nobleToReturn = n;

            aiPlayer.chooseNoble(List.of(n), dummyState);
            assertSame(dummyState, spyStrategy.lastNobleState);
        }

        @Test
        @DisplayName("chooseNoble passes the wrapped Player to the strategy")
        void passesWrappedPlayer() {
            Noble n = noble(1, GemColor.RED, 3);
            spyStrategy.nobleToReturn = n;

            aiPlayer.chooseNoble(List.of(n), dummyState);
            assertSame(player, spyStrategy.lastNoblePlayer);
        }

        @Test
        @DisplayName("chooseNoble calls the strategy exactly once")
        void callsStrategyOnce() {
            Noble n = noble(1, GemColor.RED, 3);
            spyStrategy.nobleToReturn = n;

            aiPlayer.chooseNoble(List.of(n), dummyState);
            assertEquals(1, spyStrategy.chooseNobleCallCount);
        }

        @Test
        @DisplayName("chooseNoble works with a single-element claimable list")
        void singleClaimable() {
            Noble only = noble(1, GemColor.WHITE, 2);
            spyStrategy.nobleToReturn = only;

            Noble result = aiPlayer.chooseNoble(List.of(only), dummyState);
            assertSame(only, result);
        }

        @Test
        @DisplayName("chooseNoble works with multiple claimable nobles")
        void multipleClaimable() {
            Noble n1 = noble(1, GemColor.GREEN, 3);
            Noble n2 = noble(2, GemColor.BLUE, 4);
            Noble n3 = noble(3, GemColor.RED, 3);
            spyStrategy.nobleToReturn = n2; // strategy picks the second one

            Noble result = aiPlayer.chooseNoble(List.of(n1, n2, n3), dummyState);
            assertSame(n2, result, "Strategy chose n2, AIPlayer should return n2");
        }
    }

    // =========================================================================
    // 4. chooseGemsToReturn — delegation
    // =========================================================================

    @Nested
    @DisplayName("chooseGemsToReturn delegation")
    class ChooseGemsToReturnTests {

        @Test
        @DisplayName("chooseGemsToReturn returns exactly what the strategy returns")
        void returnsStrategyResult() {
            GemCollection expected = new GemCollection().add(GemColor.RED, 2);
            spyStrategy.gemsToReturn = expected;

            GemCollection result = aiPlayer.chooseGemsToReturn(2, dummyState);
            assertSame(expected, result,
                "chooseGemsToReturn must return the exact GemCollection from the strategy");
        }

        @Test
        @DisplayName("chooseGemsToReturn passes the wrapped Player to the strategy")
        void passesWrappedPlayer() {
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.BLUE, 1);

            aiPlayer.chooseGemsToReturn(1, dummyState);
            assertSame(player, spyStrategy.lastGemsPlayer);
        }

        @Test
        @DisplayName("chooseGemsToReturn passes the correct excessCount to the strategy")
        void passesExcessCount() {
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.GREEN, 3);

            aiPlayer.chooseGemsToReturn(3, dummyState);
            assertEquals(3, spyStrategy.lastExcessCount);
        }

        @Test
        @DisplayName("chooseGemsToReturn passes the correct GameState to the strategy")
        void passesCorrectState() {
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.RED, 1);

            aiPlayer.chooseGemsToReturn(1, dummyState);
            assertSame(dummyState, spyStrategy.lastGemsState);
        }

        @Test
        @DisplayName("chooseGemsToReturn calls the strategy exactly once")
        void callsStrategyOnce() {
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.RED, 1);

            aiPlayer.chooseGemsToReturn(1, dummyState);
            assertEquals(1, spyStrategy.chooseGemsToReturnCallCount);
        }

        @Test
        @DisplayName("chooseGemsToReturn works with excessCount of 1")
        void excessCountOne() {
            GemCollection oneGem = new GemCollection().add(GemColor.WHITE, 1);
            spyStrategy.gemsToReturn = oneGem;

            GemCollection result = aiPlayer.chooseGemsToReturn(1, dummyState);
            assertSame(oneGem, result);
        }

        @Test
        @DisplayName("chooseGemsToReturn works with larger excessCount")
        void largerExcessCount() {
            GemCollection multiGems = new GemCollection()
                .add(GemColor.RED, 2)
                .add(GemColor.BLUE, 1);
            spyStrategy.gemsToReturn = multiGems;

            GemCollection result = aiPlayer.chooseGemsToReturn(3, dummyState);
            assertSame(multiGems, result);
        }
    }

    // =========================================================================
    // 5. setStrategy — swapping behaviour
    // =========================================================================

    @Nested
    @DisplayName("setStrategy")
    class SetStrategyTests {

        @Test
        @DisplayName("After setStrategy, decideAction delegates to the new strategy")
        void decideAction_usesNewStrategy() {
            // Original strategy returns a TAKE_THREE_DIFFERENT action
            GemCollection gems = new GemCollection().add(GemColor.RED, 1);
            AIAction originalAction = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false, gems, "original"
            );
            spyStrategy.actionToReturn = originalAction;

            AIAction firstResult = aiPlayer.decideAction(dummyState);
            assertSame(originalAction, firstResult);

            // Swap to alternate strategy returning a PURCHASE_CARD action
            AlternateStrategy alternate = new AlternateStrategy();
            Card card = new Card(1, 2, GemColor.BLUE, cost(GemColor.RED, 1));
            AIAction newAction = new AIAction(
                ActionType.PURCHASE_CARD, card, false, null, "alternate"
            );
            alternate.actionToReturn = newAction;

            aiPlayer.setStrategy(alternate);
            AIAction secondResult = aiPlayer.decideAction(dummyState);
            assertSame(newAction, secondResult,
                "After setStrategy, decideAction must delegate to the new strategy");
        }

        @Test
        @DisplayName("After setStrategy, chooseNoble delegates to the new strategy")
        void chooseNoble_usesNewStrategy() {
            Noble n1 = noble(1, GemColor.GREEN, 3);
            Noble n2 = noble(2, GemColor.BLUE, 4);

            spyStrategy.nobleToReturn = n1;
            assertSame(n1, aiPlayer.chooseNoble(List.of(n1, n2), dummyState));

            AlternateStrategy alternate = new AlternateStrategy();
            alternate.nobleToReturn = n2;
            aiPlayer.setStrategy(alternate);

            assertSame(n2, aiPlayer.chooseNoble(List.of(n1, n2), dummyState),
                "After setStrategy, chooseNoble must delegate to the new strategy");
        }

        @Test
        @DisplayName("After setStrategy, chooseGemsToReturn delegates to the new strategy")
        void chooseGemsToReturn_usesNewStrategy() {
            GemCollection gems1 = new GemCollection().add(GemColor.RED, 1);
            GemCollection gems2 = new GemCollection().add(GemColor.BLUE, 2);

            spyStrategy.gemsToReturn = gems1;
            assertSame(gems1, aiPlayer.chooseGemsToReturn(1, dummyState));

            AlternateStrategy alternate = new AlternateStrategy();
            alternate.gemsToReturn = gems2;
            aiPlayer.setStrategy(alternate);

            assertSame(gems2, aiPlayer.chooseGemsToReturn(2, dummyState),
                "After setStrategy, chooseGemsToReturn must delegate to the new strategy");
        }

        @Test
        @DisplayName("setStrategy does not change the wrapped Player")
        void doesNotChangePlayer() {
            AlternateStrategy alternate = new AlternateStrategy();
            aiPlayer.setStrategy(alternate);
            assertSame(player, aiPlayer.getPlayer(),
                "Swapping strategy must not affect the wrapped Player");
        }

        @Test
        @DisplayName("Original strategy is no longer called after setStrategy")
        void originalStrategyNoLongerCalled() {
            AlternateStrategy alternate = new AlternateStrategy();
            alternate.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "alt"
            );
            aiPlayer.setStrategy(alternate);

            // Reset spy counter to verify no further calls
            spyStrategy.decideActionCallCount = 0;
            aiPlayer.decideAction(dummyState);
            assertEquals(0, spyStrategy.decideActionCallCount,
                "Original strategy must not be called after setStrategy");
        }

        @Test
        @DisplayName("Can swap strategy multiple times")
        void multipleSwaps() {
            // Swap 1: to alternate
            AlternateStrategy alt1 = new AlternateStrategy();
            Card card1 = new Card(1, 1, GemColor.RED, cost(GemColor.WHITE, 1));
            alt1.actionToReturn = new AIAction(
                ActionType.PURCHASE_CARD, card1, false, null, "alt1"
            );
            aiPlayer.setStrategy(alt1);
            assertSame(card1, aiPlayer.decideAction(dummyState).getTargetCard());

            // Swap 2: back to spy
            Card card2 = new Card(2, 3, GemColor.BLUE, cost(GemColor.GREEN, 2));
            spyStrategy.actionToReturn = new AIAction(
                ActionType.RESERVE_CARD, card2, false, null, "spy"
            );
            aiPlayer.setStrategy(spyStrategy);
            assertSame(card2, aiPlayer.decideAction(dummyState).getTargetCard());

            // Swap 3: to a fresh alternate
            AlternateStrategy alt2 = new AlternateStrategy();
            alt2.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "alt2"
            );
            aiPlayer.setStrategy(alt2);
            assertEquals(ActionType.TAKE_THREE_DIFFERENT,
                aiPlayer.decideAction(dummyState).getActionType());
        }
    }

    // =========================================================================
    // 6. Cross-cutting: no method interaction leaks
    // =========================================================================

    @Nested
    @DisplayName("Method independence")
    class MethodIndependenceTests {

        @Test
        @DisplayName("Calling decideAction does not invoke chooseNoble or chooseGemsToReturn")
        void decideAction_noSideEffects() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "gems"
            );
            aiPlayer.decideAction(dummyState);
            assertEquals(0, spyStrategy.chooseNobleCallCount,
                "decideAction must not call chooseNoble");
            assertEquals(0, spyStrategy.chooseGemsToReturnCallCount,
                "decideAction must not call chooseGemsToReturn");
        }

        @Test
        @DisplayName("Calling chooseNoble does not invoke decideAction or chooseGemsToReturn")
        void chooseNoble_noSideEffects() {
            Noble n = noble(1, GemColor.RED, 3);
            spyStrategy.nobleToReturn = n;
            aiPlayer.chooseNoble(List.of(n), dummyState);
            assertEquals(0, spyStrategy.decideActionCallCount,
                "chooseNoble must not call decideAction");
            assertEquals(0, spyStrategy.chooseGemsToReturnCallCount,
                "chooseNoble must not call chooseGemsToReturn");
        }

        @Test
        @DisplayName("Calling chooseGemsToReturn does not invoke decideAction or chooseNoble")
        void chooseGemsToReturn_noSideEffects() {
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.RED, 1);
            aiPlayer.chooseGemsToReturn(1, dummyState);
            assertEquals(0, spyStrategy.decideActionCallCount,
                "chooseGemsToReturn must not call decideAction");
            assertEquals(0, spyStrategy.chooseNobleCallCount,
                "chooseGemsToReturn must not call chooseNoble");
        }

        @Test
        @DisplayName("All three methods can be called in sequence without interference")
        void allThreeMethodsInSequence() {
            spyStrategy.actionToReturn = new AIAction(
                ActionType.TAKE_THREE_DIFFERENT, null, false,
                new GemCollection(), "gems"
            );
            Noble n = noble(1, GemColor.GREEN, 3);
            spyStrategy.nobleToReturn = n;
            spyStrategy.gemsToReturn = new GemCollection().add(GemColor.RED, 1);

            AIAction action = aiPlayer.decideAction(dummyState);
            Noble noble = aiPlayer.chooseNoble(List.of(n), dummyState);
            GemCollection gems = aiPlayer.chooseGemsToReturn(1, dummyState);

            // Each method was called exactly once
            assertEquals(1, spyStrategy.decideActionCallCount);
            assertEquals(1, spyStrategy.chooseNobleCallCount);
            assertEquals(1, spyStrategy.chooseGemsToReturnCallCount);

            // Each returned the correct pre-set value
            assertSame(spyStrategy.actionToReturn, action);
            assertSame(n, noble);
            assertSame(spyStrategy.gemsToReturn, gems);
        }
    }
}
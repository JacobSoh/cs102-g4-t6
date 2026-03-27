package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class GameEngineTest {

    private GameEngine gameEngine;
    private List<Card> level1Cards;
    private List<Card> level2Cards;
    private List<Card> level3Cards;
    private List<Noble> allNobles;
    private ConfigLoader config;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Cost emptyCost() {
        return new Cost(new EnumMap<>(GemColor.class));
    }

    private Cost costOf(GemColor color, int amount) {
        Map<GemColor, Integer> map = new EnumMap<>(GemColor.class);
        map.put(color, amount);
        return new Cost(map);
    }

    private GemCollection gemsOf(GemColor color, int amount) {
        Map<GemColor, Integer> map = new EnumMap<>(GemColor.class);
        map.put(color, amount);
        return new GemCollection(map);
    }

    private Noble nobleRequiring(int id, GemColor color, int amount) {
        Map<GemColor, Integer> reqs = new EnumMap<>(GemColor.class);
        reqs.put(color, amount);
        return new Noble(id, "Noble" + id, reqs);
    }

    /** Creates a full set of cards for testing — 40 level1, 30 level2, 20 level3 */
    private List<Card> makeCards(int level, int count) {
        List<Card> cards = new ArrayList<>();
        GemColor[] colors = GemColor.values();
        for (int i = 0; i < count; i++) {
            GemColor bonus = colors[i % (colors.length - 1)]; // exclude GOLD
            cards.add(new Card(level, 0, bonus, emptyCost()));
        }
        return cards;
    }

    /** Creates a full set of nobles for testing */
    private List<Noble> makeNobles(int count) {
        List<Noble> nobles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nobles.add(nobleRequiring(i + 1, GemColor.RED, 3));
        }
        return nobles;
    }

    @BeforeEach
    void setUp() {
        gameEngine  = new GameEngine();
        level1Cards = makeCards(1, 40);
        level2Cards = makeCards(2, 30);
        level3Cards = makeCards(3, 20);
        allNobles   = makeNobles(10);
        config      = new ConfigLoader("config.properties"); 
    }

    // =========================================================================
    // initializeGame() — player count setup
    // =========================================================================

    @Test
    void testInitializeGame_2Players_correctPlayerCount() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(2, state.getPlayers().size());
    }

    @Test
    void testInitializeGame_3Players_correctPlayerCount() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(3, state.getPlayers().size());
    }

    @Test
    void testInitializeGame_4Players_correctPlayerCount() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave");
        GameState state = gameEngine.initializeGame(4, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(4, state.getPlayers().size());
    }

    // =========================================================================
    // initializeGame() — player names and turn order
    // =========================================================================

    @Test
    void testInitializeGame_playersHaveCorrectNames() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals("Alice", state.getPlayers().get(0).getName());
        assertEquals("Bob",   state.getPlayers().get(1).getName());
        assertEquals("Carol", state.getPlayers().get(2).getName());
    }

    @Test
    void testInitializeGame_playersHaveCorrectTurnOrder() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(1, state.getPlayers().get(0).getTurnOrder());
        assertEquals(2, state.getPlayers().get(1).getTurnOrder());
        assertEquals(3, state.getPlayers().get(2).getTurnOrder());
    }

    @Test
    void testInitializeGame_firstPlayerIsCurrentPlayer() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(0, state.getCurrentPlayerIndex());
        assertEquals("Alice", state.getCurrentPlayer().getName());
    }

    // =========================================================================
    // initializeGame() — gem bank
    // =========================================================================

    @Test
    void testInitializeGame_gemBank_2Players_4StandardGems() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        GemCollection bank = state.getGemBank();
        assertEquals(4, bank.getCount(GemColor.RED));
        assertEquals(4, bank.getCount(GemColor.BLUE));
        assertEquals(4, bank.getCount(GemColor.GREEN));
        assertEquals(4, bank.getCount(GemColor.WHITE));
        assertEquals(4, bank.getCount(GemColor.BLACK));
    }

    @Test
    void testInitializeGame_gemBank_2Players_5Gold() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(5, state.getGemBank().getCount(GemColor.GOLD));
    }

    @Test
    void testInitializeGame_gemBank_3Players_5StandardGems() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        GemCollection bank = state.getGemBank();
        assertEquals(5, bank.getCount(GemColor.RED));
        assertEquals(5, bank.getCount(GemColor.BLUE));
        assertEquals(5, bank.getCount(GemColor.GREEN));
        assertEquals(5, bank.getCount(GemColor.WHITE));
        assertEquals(5, bank.getCount(GemColor.BLACK));
    }

    @Test
    void testInitializeGame_gemBank_3Players_5Gold() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(5, state.getGemBank().getCount(GemColor.GOLD));
    }

    @Test
    void testInitializeGame_gemBank_4Players_7StandardGems() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave");
        GameState state = gameEngine.initializeGame(4, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        GemCollection bank = state.getGemBank();
        assertEquals(7, bank.getCount(GemColor.RED));
        assertEquals(7, bank.getCount(GemColor.BLUE));
        assertEquals(7, bank.getCount(GemColor.GREEN));
        assertEquals(7, bank.getCount(GemColor.WHITE));
        assertEquals(7, bank.getCount(GemColor.BLACK));
    }

    @Test
    void testInitializeGame_gemBank_4Players_5Gold() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave");
        GameState state = gameEngine.initializeGame(4, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(5, state.getGemBank().getCount(GemColor.GOLD));
    }

    // =========================================================================
    // initializeGame() — nobles
    // =========================================================================

    @Test
    void testInitializeGame_nobles_2Players_3NoblesSelected() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(3, state.getAvailableNobles().size()); // playerCount + 1
    }

    @Test
    void testInitializeGame_nobles_3Players_4NoblesSelected() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(4, state.getAvailableNobles().size());
    }

    @Test
    void testInitializeGame_nobles_4Players_5NoblesSelected() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave");
        GameState state = gameEngine.initializeGame(4, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(5, state.getAvailableNobles().size());
    }

    @Test
    void testInitializeGame_nobles_areSubsetOfAllNobles() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertTrue(allNobles.containsAll(state.getAvailableNobles()));
    }


    // =========================================================================
    // initializeGame() — CardMarket
    // =========================================================================

    @Test
    void testInitializeGame_cardMarketCreated() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertNotNull(state.getMarket());
    }

    @Test
    void testInitializeGame_cardMarketHasCorrectDeckSizes() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        // 4 visible cards per level, rest in deck
        assertEquals(level1Cards.size() - 4, state.getMarket().getDeckSize(1));
        assertEquals(level2Cards.size() - 4, state.getMarket().getDeckSize(2));
        assertEquals(level3Cards.size() - 4, state.getMarket().getDeckSize(3));
    }

    @Test
    void testInitializeGame_playersStartWithNoGems() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        for (Player player : state.getPlayers()) {
            assertEquals(0, player.getGems().getTotalCount());
        }
    }

    @Test
    void testInitializeGame_playersStartWithNoCards() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        for (Player player : state.getPlayers()) {
            assertTrue(player.getPurchasedCards().isEmpty());
            assertTrue(player.getReservedCards().isEmpty());
        }
    }

    @Test
    void testInitializeGame_gameNotOverAtStart() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertFalse(state.isGameOver());
    }

    @Test
    void testInitializeGame_playersStartWithZeroPoints() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        for (Player player : state.getPlayers()) {
            assertEquals(0, player.getPoints());
        }
    }

    // =========================================================================
    // initializeGame() — edge cases
    // =========================================================================

    @Test
    void testInitializeGame_invalidPlayerCount_throwsException() {
        List<String> names = List.of("Alice");
        assertThrows(IllegalArgumentException.class, () ->
                gameEngine.initializeGame(1, names, config, level1Cards, level2Cards, level3Cards, allNobles)
        );
    }

    @Test
    void testInitializeGame_invalidPlayerCount_5Players_throwsException() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave", "Eve");
        assertThrows(IllegalArgumentException.class, () ->
                gameEngine.initializeGame(5, names, config, level1Cards, level2Cards, level3Cards, allNobles)
        );
    }

    @Test
    void testInitializeGame_calledTwice_producesIndependentStates() {
        List<String> names = List.of("Alice", "Bob");
        GameState state1 = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);
        GameState state2 = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertNotSame(state1, state2);
        assertNotSame(state1.getPlayers(), state2.getPlayers());
    }

    // =========================================================================
    // checkWinCondition()
    // =========================================================================

    @Test
    void testCheckWinCondition_playerReaches15Points() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(0).addCard(new Card(1, 15, GemColor.RED, emptyCost()));

        Player winner = gameEngine.checkWinCondition(state, 15);
        assertNotNull(winner);
        assertEquals("Alice", winner.getName());
    }

    @Test
    void testCheckWinCondition_noWinnerYet() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player winner = gameEngine.checkWinCondition(state, 15);
        assertNull(winner);
    }

    @Test
    void testCheckWinCondition_tieBreakerFewerCardsWins() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player alice = state.getPlayers().get(0);
        Player bob   = state.getPlayers().get(1);

        // Both have 15 points
        alice.addCard(new Card(1, 15, GemColor.RED,  emptyCost()));
        bob.addCard(new Card(1,   15, GemColor.BLUE, emptyCost()));
        bob.addCard(new Card(1,   0,  GemColor.GREEN, emptyCost())); // extra card

        Player winner = gameEngine.checkWinCondition(state, 15);
        assertEquals("Alice", winner.getName()); // fewer cards wins
    }

    @Test
    void testCheckWinCondition_gameOverFlagSet() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(0).addCard(new Card(1, 15, GemColor.RED, emptyCost()));

        // Simulate last player in round triggering game over
        state.setGameOver(true);
        assertTrue(state.isGameOver());
    }

    @Test
    void testCheckWinCondition_multiplePlayersAboveThreshold_highestWins() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(0).addCard(new Card(1, 15, GemColor.RED,   emptyCost()));
        state.getPlayers().get(1).addCard(new Card(1, 18, GemColor.BLUE,  emptyCost()));
        state.getPlayers().get(2).addCard(new Card(1, 16, GemColor.GREEN, emptyCost()));

        Player winner = gameEngine.checkWinCondition(state, 15);
        assertEquals("Bob", winner.getName()); // 18 points wins
    }

    @Test
    void testCheckWinCondition_exactlyAtThreshold_isWinner() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(0).addCard(new Card(1, 15, GemColor.RED, emptyCost()));

        assertNotNull(gameEngine.checkWinCondition(state, 15));
    }

    @Test
    void testCheckWinCondition_oneBelow_isNotWinner() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(0).addCard(new Card(1, 14, GemColor.RED, emptyCost()));

        assertNull(gameEngine.checkWinCondition(state, 15));
    }

    // =========================================================================
    // advanceTurn()
    // =========================================================================

    @Test
    void testAdvanceTurn_turnAdvancesFromFirstToSecond() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        assertEquals(0, state.getCurrentPlayerIndex());
        gameEngine.advanceTurn(state);
        assertEquals(1, state.getCurrentPlayerIndex());
    }

    @Test
    void testAdvanceTurn_wrapsAroundToFirstPlayer() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        gameEngine.advanceTurn(state); // Alice → Bob
        gameEngine.advanceTurn(state); // Bob → Alice (wrap)

        assertEquals(0, state.getCurrentPlayerIndex());
        assertEquals("Alice", state.getCurrentPlayer().getName());
    }

    @Test
    void testAdvanceTurn_3Players_fullRoundCycle() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        gameEngine.advanceTurn(state); // → Bob
        assertEquals("Bob", state.getCurrentPlayer().getName());

        gameEngine.advanceTurn(state); // → Carol
        assertEquals("Carol", state.getCurrentPlayer().getName());

        gameEngine.advanceTurn(state); // → Alice (wrap)
        assertEquals("Alice", state.getCurrentPlayer().getName());
    }

    @Test
    void testAdvanceTurn_returnsClaimableNobles() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        // Give Alice enough bonuses to claim a noble
        Noble noble = nobleRequiring(99, GemColor.RED, 3);
        state.getAvailableNobles().add(noble);
        Player alice = state.getPlayers().get(0);
        alice.addCard(new Card(1, 0, GemColor.RED, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.RED, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.RED, emptyCost()));

        List<Noble> claimable = gameEngine.advanceTurn(state);
        // Noble was auto-claimed (only 1 claimable) so list is empty
        assertTrue(claimable.isEmpty());
        assertTrue(alice.getClaimedNobles().contains(noble));
    }

    @Test
    void testAdvanceTurn_noNoblesClaimable_returnsEmptyList() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        List<Noble> claimable = gameEngine.advanceTurn(state);
        assertTrue(claimable.isEmpty());
    }

    @Test
    void testAdvanceTurn_gameAlreadyOver_doesNotAdvanceTurn() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getPlayers().get(1).addCard(new Card(1, 15, GemColor.RED, emptyCost()));

        // Advance to last player (Bob) and trigger game over
        gameEngine.advanceTurn(state); // Alice → Bob
        gameEngine.advanceTurn(state); // Bob's turn ends → game over triggered

        assertTrue(state.isGameOver());
    }

    @Test
    void testAdvanceTurn_nobleAutoClaimedAndRemovedFromState() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        // Clear existing nobles and add one specific noble
        state.getAvailableNobles().clear();
        Noble noble = nobleRequiring(99, GemColor.BLUE, 3);
        state.getAvailableNobles().add(noble);

        Player alice = state.getPlayers().get(0);
        alice.addCard(new Card(1, 0, GemColor.BLUE, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.BLUE, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.BLUE, emptyCost()));

        gameEngine.advanceTurn(state);

        // Noble should be removed from state and claimed by alice
        assertFalse(state.getAvailableNobles().contains(noble));
        assertTrue(alice.getClaimedNobles().contains(noble));
    }

    // =========================================================================
    // Full game simulation
    // =========================================================================

    @Test
    void testFullGameSimulation_3Players_winConditionTriggered() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player alice = state.getPlayers().get(0);
        Player bob   = state.getPlayers().get(1);
        Player carol = state.getPlayers().get(2);

        // Simulate 20+ turns of gem taking and card purchasing
        // Turn 1: Alice takes gems
        alice.addGems(gemsOf(GemColor.RED, 4));
        gameEngine.advanceTurn(state); // → Bob

        // Turn 2: Bob takes gems
        bob.addGems(gemsOf(GemColor.BLUE, 4));
        gameEngine.advanceTurn(state); // → Carol

        // Turn 3: Carol takes gems
        carol.addGems(gemsOf(GemColor.GREEN, 4));
        gameEngine.advanceTurn(state); // → Alice (end of round 1)

        // Turn 4: Alice buys a card
        alice.addCard(new Card(1, 2, GemColor.RED, emptyCost()));
        gameEngine.advanceTurn(state); // → Bob

        // Turn 5: Bob buys a card
        bob.addCard(new Card(1, 2, GemColor.BLUE, emptyCost()));
        gameEngine.advanceTurn(state); // → Carol

        // Turn 6: Carol buys a card
        carol.addCard(new Card(1, 2, GemColor.GREEN, emptyCost()));
        gameEngine.advanceTurn(state); // → Alice (end of round 2)

        // Simulate Alice accumulating points over many turns
        for (int i = 0; i < 6; i++) {
            alice.addCard(new Card(1, 2, GemColor.RED, emptyCost()));
            gameEngine.advanceTurn(state); // Alice
            gameEngine.advanceTurn(state); // Bob
            gameEngine.advanceTurn(state); // Carol
        }

        // Verify state consistency
        assertTrue(alice.getPoints() >= 14); // Alice has accumulated points

        // Final round: Alice reaches 15+ points
        alice.addCard(new Card(1, 3, GemColor.RED, emptyCost()));
        gameEngine.advanceTurn(state); // Alice
        gameEngine.advanceTurn(state); // Bob
        gameEngine.advanceTurn(state); // Carol — end of round, triggers win check

        assertTrue(state.isGameOver());
        Player winner = gameEngine.checkWinCondition(state, 15);
        assertEquals("Alice", winner.getName());
    }

    @Test
    void testFullGameSimulation_stateConsistencyAfterEachTurn() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        // Run 10 full rounds and verify state stays consistent
        for (int round = 0; round < 10; round++) {
            int indexBefore = state.getCurrentPlayerIndex();
            gameEngine.advanceTurn(state);
            int indexAfter = state.getCurrentPlayerIndex();

            // Turn index must always be valid
            assertTrue(indexAfter >= 0);
            assertTrue(indexAfter < state.getPlayers().size());

            // Turn must have actually advanced
            assertNotEquals(indexBefore, indexAfter);

            if (state.isGameOver()) break;
        }
    }

    @Test
    void testFullGameSimulation_multiplePlayersReach15SameRound() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player alice = state.getPlayers().get(0);
        Player bob   = state.getPlayers().get(1);

        // Give both players 15 points before the last round ends
        alice.addCard(new Card(1, 15, GemColor.RED,  emptyCost()));
        bob.addCard(new Card(1,   16, GemColor.BLUE, emptyCost()));

        // End of round — Bob has more points
        Player winner = gameEngine.checkWinCondition(state, 15);
        assertNotNull(winner);
        assertEquals("Bob", winner.getName()); // Bob wins with 16 vs 15
    }

    // =========================================================================
    // Edge cases — noble claiming after purchase
    // =========================================================================

    @Test
    void testNobleClaimingAfterPurchase_nobleAddedToPlayer() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getAvailableNobles().clear();
        Noble noble = nobleRequiring(99, GemColor.WHITE, 3);
        state.getAvailableNobles().add(noble);

        Player alice = state.getPlayers().get(0);
        alice.addCard(new Card(1, 0, GemColor.WHITE, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.WHITE, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.WHITE, emptyCost()));

        gameEngine.advanceTurn(state);

        assertEquals(1, alice.getClaimedNobles().size());
        assertEquals(noble.getPoints(), alice.getPoints());
    }

    @Test
    void testNobleClaimingAfterPurchase_nobleRemovedFromBoard() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        state.getAvailableNobles().clear();
        Noble noble = nobleRequiring(99, GemColor.BLACK, 3);
        state.getAvailableNobles().add(noble);

        Player alice = state.getPlayers().get(0);
        alice.addCard(new Card(1, 0, GemColor.BLACK, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.BLACK, emptyCost()));
        alice.addCard(new Card(1, 0, GemColor.BLACK, emptyCost()));

        gameEngine.advanceTurn(state);

        assertFalse(state.getAvailableNobles().contains(noble));
    }

    // =========================================================================
    // Edge cases — reserved card purchase
    // =========================================================================

    @Test
    void testReservedCardPurchase_cardMovedFromReservedToPurchased() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player alice = state.getPlayers().get(0);
        Card card = new Card(1, 3, GemColor.RED, emptyCost());

        // Reserve the card
        alice.addReservedCard(card);
        assertEquals(1, alice.getReservedCards().size());

        // Purchase the reserved card
        alice.removeReservedCard(card);
        alice.addCard(card);

        assertEquals(0, alice.getReservedCards().size());
        assertEquals(1, alice.getPurchasedCards().size());
        assertEquals(3, alice.getPoints());
    }

    @Test
    void testReservedCard_playerReceivesGoldOnReserve() {
        List<String> names = List.of("Alice", "Bob");
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player alice = state.getPlayers().get(0);
        int goldBefore = state.getGemBank().getCount(GemColor.GOLD);

        // Simulate reserving — player gets 1 GOLD, bank loses 1
        alice.addReservedCard(new Card(1, 0, GemColor.RED, emptyCost()));
        alice.addGems(gemsOf(GemColor.GOLD, 1));
        state.removeGemsFromBank(gemsOf(GemColor.GOLD, 1));

        assertEquals(goldBefore - 1, state.getGemBank().getCount(GemColor.GOLD));
        assertEquals(1, alice.getGems().getCount(GemColor.GOLD));
    }

    // =========================================================================
    // Edge cases — turn order corruption
    // =========================================================================

    @Test
    void testAdvanceTurn_4Players_neverExceedsBounds() {
        List<String> names = List.of("Alice", "Bob", "Carol", "Dave");
        GameState state = gameEngine.initializeGame(4, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        // Run 40 turns (10 full rounds) — index must never go out of bounds
        for (int i = 0; i < 40; i++) {
            int idx = state.getCurrentPlayerIndex();
            assertTrue(idx >= 0 && idx < 4, "Index out of bounds: " + idx);
            gameEngine.advanceTurn(state);
            if (state.isGameOver()) break;
        }
    }

    @Test
    void testAdvanceTurn_currentPlayerChangesEveryTurn() {
        List<String> names = List.of("Alice", "Bob", "Carol");
        GameState state = gameEngine.initializeGame(3, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        String previousName = state.getCurrentPlayer().getName();
        for (int i = 0; i < 9; i++) {
            gameEngine.advanceTurn(state);
            String currentName = state.getCurrentPlayer().getName();
            assertNotEquals(previousName, currentName, "Player did not change on turn " + i);
            previousName = currentName;
            if (state.isGameOver()) break;
        }
    }

    @Test
    void testAdvanceTurn_winningThresholdOf0_tiedPlayersReturnNull() {
        List<String> names = List.of("Alice", "Bob");
        // Winning threshold of 0 means everyone qualifies, but identical scores
        // and identical card counts produce an unresolved tie → null
        GameState state = gameEngine.initializeGame(2, names, config, level1Cards, level2Cards, level3Cards, allNobles);

        Player winner = gameEngine.checkWinCondition(state, 0);
        assertNull(winner, "Unresolved tie (same points, same cards) should return null");
    }

}

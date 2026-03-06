package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.GameState;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class ActionExecutorNobleTest {

    private GameState state;
    private Player player;
    private Noble affordableNoble;
    private Noble secondAffordableNoble;
    private Noble expensiveNoble;

    @BeforeEach
    public void setUp() {
        // 1. Create Player
        List<Player> players = new ArrayList<>();
        players.add(new Player("Zyik", 0));

        // 2. Setup Dummy CardMarket (Bypassing the 40/30/20 size rule)
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0);
        Cost emptyCost = new Cost(freeMap);
        
        List<Card> level1Deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) level1Deck.add(new Card(1, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level2Deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) level2Deck.add(new Card(2, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level3Deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) level3Deck.add(new Card(3, 0, GemColor.WHITE, emptyCost));

        // 3. Setup Nobles 
        Map<GemColor, Integer> req1 = new HashMap<>();
        req1.put(GemColor.RED, 3);
        affordableNoble = new Noble(3, req1); // Needs 3 Red bonuses

        Map<GemColor, Integer> req2 = new HashMap<>();
        req2.put(GemColor.BLUE, 3);
        secondAffordableNoble = new Noble(3, req2); // Needs 3 Blue bonuses

        Map<GemColor, Integer> req3 = new HashMap<>();
        req3.put(GemColor.BLACK, 5);
        expensiveNoble = new Noble(3, req3); // Needs 5 Black bonuses

        List<Noble> nobles = new ArrayList<>();
        nobles.add(affordableNoble);
        nobles.add(secondAffordableNoble);
        nobles.add(expensiveNoble);

        // 4. Initialize GameState
        state = new GameState(players, new CardMarket(level1Deck, level2Deck, level3Deck), new GemCollection(), nobles);
        player = state.getCurrentPlayer();
    }

    // ==========================================================
    // TESTS FOR: executeClaimNoble()
    // ==========================================================

    @Test
    public void testExecuteClaimNoble_NobleAddedToPlayer() {
        // Give player 3 RED cards to afford the noble
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0);
        Cost emptyCost = new Cost(freeMap);
        
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));

        ActionResult result = ActionExecutor.executeClaimNoble(state, affordableNoble);

        assertTrue(result.isSuccess(), "Action should succeed since player has 3 Red bonuses");
        assertTrue(player.getClaimedNobles().contains(affordableNoble), "Noble added to player");
    }

    @Test
    public void testExecuteClaimNoble_NobleRemovedFromAvailable() {
        // Give player 3 RED cards
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0);
        Cost emptyCost = new Cost(freeMap);
        
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));

        ActionExecutor.executeClaimNoble(state, affordableNoble);

        assertFalse(state.getNobles().contains(affordableNoble), "Noble removed from available GameState list");
    }

    @Test
    public void testExecuteClaimNoble_CanClaimMultipleNobles() {
        // Give player 3 RED cards AND 3 BLUE cards
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0);
        Cost emptyCost = new Cost(freeMap);
        
        for (int i = 0; i < 3; i++) {
            player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
            player.addCard(new Card(1, 0, GemColor.BLUE, emptyCost));
        }

        ActionResult result1 = ActionExecutor.executeClaimNoble(state, affordableNoble);
        ActionResult result2 = ActionExecutor.executeClaimNoble(state, secondAffordableNoble);

        assertTrue(result1.isSuccess() && result2.isSuccess(), "Both claims should succeed");
        assertEquals(2, player.getClaimedNobles().size(), "Player should have exactly 2 nobles");
    }
}
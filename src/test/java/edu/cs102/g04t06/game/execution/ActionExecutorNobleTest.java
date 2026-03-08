package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.EnumMap;
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
        // 1. Create Players
        List<Player> players = new ArrayList<>();
        players.add(new Player("Zyik", 0));
        players.add(new Player("Dong En", 1));

        // 2. Setup Bank (Give it 5 of every standard gem for a healthy start)
        GemCollection startingBank = new GemCollection()
                .add(GemColor.RED, 5).add(GemColor.BLUE, 5)
                .add(GemColor.GREEN, 5).add(GemColor.BLACK, 5)
                .add(GemColor.WHITE, 5).add(GemColor.GOLD, 5); // Added gold just in case!

        // 3. Setup Dummy Market (Required to build GameState)
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0); 
        Cost emptyCost = new Cost(freeMap);
        
        List<Card> level1Deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) level1Deck.add(new Card(1, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level2Deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) level2Deck.add(new Card(2, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level3Deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) level3Deck.add(new Card(3, 0, GemColor.WHITE, emptyCost));

        CardMarket market = new CardMarket(level1Deck, level2Deck, level3Deck);

        // 4. Setup Nobles (THIS IS THE IMPORTANT PART)
        // 1. Create the LOCAL list of nobles
        List<Noble> noblesList = new ArrayList<>();
        
        // 2. Assign values to your CLASS variables so the tests can use them!
        Map<GemColor, Integer> redReq = new EnumMap<>(GemColor.class);
        redReq.put(GemColor.RED, 3);
        // Assigning to the class variable 'affordableNoble'
        this.affordableNoble = new Noble(1, "Red Noble", redReq);
        
        Map<GemColor, Integer> blueReq = new EnumMap<>(GemColor.class);
        blueReq.put(GemColor.BLUE, 3);
        // Assigning to the class variable 'secondAffordableNoble'
        this.secondAffordableNoble = new Noble(2, "Blue Noble", blueReq);

        // 3. Add them both to the list
        noblesList.add(affordableNoble);
        noblesList.add(secondAffordableNoble);

        // 4. Initialize GameState
        this.state = new GameState(players, market, startingBank, noblesList, 15);
        this.player = state.getCurrentPlayer();
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

        assertFalse(state.getAvailableNobles().contains(affordableNoble), "Noble removed from available GameState list");
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
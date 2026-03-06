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

public class ActionExecutorIntegrationTest {

    private GameState state;
    private Player player;
    private Cost emptyCost;

    @BeforeEach
    public void setUp() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Zyik", 0));

        GemCollection startingBank = new GemCollection()
                .add(GemColor.RED, 5).add(GemColor.BLUE, 5)
                .add(GemColor.GREEN, 5).add(GemColor.BLACK, 5)
                .add(GemColor.WHITE, 5).add(GemColor.GOLD, 5);

        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0);
        emptyCost = new Cost(freeMap);
        
        List<Card> level1Deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) level1Deck.add(new Card(1, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level2Deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) level2Deck.add(new Card(2, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level3Deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) level3Deck.add(new Card(3, 0, GemColor.WHITE, emptyCost));

        state = new GameState(players, new CardMarket(level1Deck, level2Deck, level3Deck), startingBank, new ArrayList<>());
        player = state.getCurrentPlayer();
    }

    @Test
    public void testSequence_TakeGems_Purchase_ClaimNoble() {
        // 1. Setup specific target items
        Map<GemColor, Integer> costMap = new HashMap<>();
        costMap.put(GemColor.RED, 1);
        costMap.put(GemColor.BLUE, 1);
        costMap.put(GemColor.GREEN, 1);
        Card targetCard = new Card(1, 0, GemColor.BLACK, new Cost(costMap));

        Map<GemColor, Integer> nobleReq = new HashMap<>();
        nobleReq.put(GemColor.BLACK, 1);
        Noble targetNoble = new Noble(3, nobleReq);
        state.getNobles().add(targetNoble); // Put noble on the board

        // STEP 1: Take Gems
        GemCollection takeSelection = new GemCollection().add(GemColor.RED, 1).add(GemColor.BLUE, 1).add(GemColor.GREEN, 1);
        ActionResult step1 = ActionExecutor.executeTakeThreeDifferentGems(state, takeSelection);
        assertTrue(step1.isSuccess(), "Step 1: Take gems should succeed");

        // STEP 2: Purchase Card using those gems
        ActionResult step2 = ActionExecutor.executePurchaseCard(state, targetCard, false);
        assertTrue(step2.isSuccess(), "Step 2: Purchase card should succeed");

        // STEP 3: Claim Noble using the new card's BLACK bonus
        ActionResult step3 = ActionExecutor.executeClaimNoble(state, targetNoble);
        assertTrue(step3.isSuccess(), "Step 3: Claim noble should succeed");
        
        // Assert State Consistency
        assertTrue(player.getClaimedNobles().contains(targetNoble), "Player owns noble");
        assertTrue(player.getPurchasedCards().contains(targetCard), "Player owns card");
        assertEquals(0, player.getGemCount(), "Player spent all their gems"); // Spent them all
    }

    @Test
    public void testSequence_Reserve_LaterPurchaseFromReserved() {
        // Target card costs 2 RED
        Map<GemColor, Integer> costMap = new HashMap<>();
        costMap.put(GemColor.RED, 2);
        Card targetCard = new Card(1, 0, GemColor.WHITE, new Cost(costMap));

        // STEP 1: Reserve the card
        ActionResult step1 = ActionExecutor.executeReserveCard(state, targetCard);
        assertTrue(step1.isSuccess(), "Step 1: Reserve should succeed");
        assertEquals(1, player.getGems().getCount(GemColor.GOLD), "Got 1 gold from reserving");

        // STEP 2: Take Gems (Take 2 RED)
        ActionResult step2 = ActionExecutor.executeTakeTwoSameGems(state, GemColor.RED);
        assertTrue(step2.isSuccess(), "Step 2: Take gems should succeed");

        // STEP 3: Purchase from reserved
        ActionResult step3 = ActionExecutor.executePurchaseCard(state, targetCard, true);
        assertTrue(step3.isSuccess(), "Step 3: Purchase reserved card should succeed");

        // Assert State Consistency
        assertFalse(player.getReservedCards().contains(targetCard), "Card removed from reserved");
        assertTrue(player.getPurchasedCards().contains(targetCard), "Card added to purchased");
    }

    @Test
    public void testSequence_TakeGemsUntilExceed10_ReturnGems() {
        // Give player 8 gems to start
        player.addGems(new GemCollection().add(GemColor.WHITE, 8));

        // STEP 1: Take 3 different gems (8 + 3 = 11 gems)
        GemCollection takeSelection = new GemCollection().add(GemColor.RED, 1).add(GemColor.BLUE, 1).add(GemColor.GREEN, 1);
        ActionResult step1 = ActionExecutor.executeTakeThreeDifferentGems(state, takeSelection);
        
        assertTrue(step1.isSuccess());
        assertEquals(11, player.getGemCount(), "Player temporarily has 11 gems");

        // STEP 2: Return 1 gem to get back to the limit
        GemCollection returnSelection = new GemCollection().add(GemColor.WHITE, 1);
        ActionResult step2 = ActionExecutor.executeReturnGems(state, returnSelection);
        
        assertTrue(step2.isSuccess(), "Return action should succeed");
        assertEquals(10, player.getGemCount(), "Player is back to exactly 10 gems");
        assertEquals(6, state.getGemBank().getCount(GemColor.WHITE), "Bank received the returned gem");
    }
}
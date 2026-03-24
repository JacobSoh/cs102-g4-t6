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
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

public class ActionExecutorCardTest {

    private GameState state;
    private Player player;
    private Card affordableCard;
    private Card expensiveCard;
    private Cost emptyCost;

    @BeforeEach
    public void setUp() {
        // 1. Create Player
        List<Player> players = new ArrayList<>();
        players.add(new Player("Zyik", 0));

        // 2. Setup Bank
        GemCollection startingBank = new GemCollection()
                .add(GemColor.RED, 5).add(GemColor.BLUE, 5)
                .add(GemColor.GREEN, 5).add(GemColor.BLACK, 5)
                .add(GemColor.WHITE, 5).add(GemColor.GOLD, 5);

        // 3. Setup Market
        Map<GemColor, Integer> freeMap = new HashMap<>();
        freeMap.put(GemColor.WHITE, 0); 
        emptyCost = new Cost(freeMap);
        
        List<Card> level1Deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) level1Deck.add(new Card(1, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level2Deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) level2Deck.add(new Card(2, 0, GemColor.WHITE, emptyCost));
        
        List<Card> level3Deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) level3Deck.add(new Card(3, 0, GemColor.WHITE, emptyCost));

        // Actually create the CardMarket object!
        CardMarket market = new CardMarket(level1Deck, level2Deck, level3Deck);

        // 4. Setup Nobles (Empty list is fine for card tests)
        List<edu.cs102.g04t06.game.rules.entities.Noble> nobles = new ArrayList<>();

        // 5. Initialize GameState
        // Notice we are passing startingBank, not "bank"
        state = new GameState(players, market, startingBank, nobles, 15);
        player = state.getCurrentPlayer();

        // 6. Create dummy costs and cards for our specific tests
        Map<GemColor, Integer> cheapMap = new HashMap<>();
        cheapMap.put(GemColor.RED, 2);
        Cost cheapCost = new Cost(cheapMap);
        
        Map<GemColor, Integer> priceyMap = new HashMap<>();
        priceyMap.put(GemColor.BLACK, 5);
        Cost priceyCost = new Cost(priceyMap);

        affordableCard = new Card(1, 0, GemColor.BLUE, cheapCost); 
        expensiveCard = new Card(2, 2, GemColor.RED, priceyCost); 

        // --- ADD THESE TWO LINES ---
        // Force our specific test cards onto the visible game board so they can be removed!
        state.getMarket().getVisibleCards(1).set(0, affordableCard);
        state.getMarket().getVisibleCards(2).set(0, expensiveCard);
        
    }
    // ==========================================================
    // TESTS FOR: executePurchaseCard()
    // ==========================================================

    @Test
    public void testExecutePurchaseCard_CanAfford() {
        // Give player exactly 2 RED gems to afford the card
        player.addGems(new GemCollection().add(GemColor.RED, 2));
        
        ActionResult result = ActionExecutor.executePurchaseCard(state, affordableCard, false);

        assertTrue(result.isSuccess(), "Action should succeed since player can afford it");
        assertTrue(player.getPurchasedCards().contains(affordableCard), "Card added to player");
        assertEquals(0, player.getGems().getCount(GemColor.RED), "Gems deducted correctly");
        assertEquals(7, state.getGemBank().getCount(GemColor.RED), "Gems returned to bank");
    }

    @Test
    public void testExecutePurchaseCard_CannotAfford() {
        // Player has no gems
        ActionResult result = ActionExecutor.executePurchaseCard(state, expensiveCard, false);

        assertFalse(result.isSuccess(), "Action should fail since player cannot afford it");
        assertFalse(player.getPurchasedCards().contains(expensiveCard), "Card NOT added to player");
    }

    @Test
    public void testExecutePurchaseCard_BonusesAppliedToCost() {
        // Give player a card that provides a RED bonus
        player.addCard(new Card(1, 0, GemColor.RED, emptyCost));
        
        // Give player only 1 RED gem. They need 2, but the 1 RED bonus should cover the difference!
        player.addGems(new GemCollection().add(GemColor.RED, 1));

        ActionResult result = ActionExecutor.executePurchaseCard(state, affordableCard, false);

        assertTrue(result.isSuccess(), "Action should succeed using the card bonus discount");
        assertEquals(0, player.getGems().getCount(GemColor.RED), "Only 1 Red gem actually deducted");
    }

    // ==========================================================
    // TESTS FOR: executeReserveCard()
    // ==========================================================

    @Test
    public void testExecuteReserveCard_ValidAndGetsGold() {
        ActionResult result = ActionExecutor.executeReserveCard(state, affordableCard);

        assertTrue(result.isSuccess(), "Reserve action should succeed");
        assertTrue(player.getReservedCards().contains(affordableCard), "Card added to reserved list");
        assertEquals(1, player.getGems().getCount(GemColor.GOLD), "Player should receive 1 Gold gem");
        assertEquals(4, state.getGemBank().getCount(GemColor.GOLD), "Gold gem removed from bank");
    }

    @Test
    public void testExecuteReserveCard_InvalidMaxReserved() {
        // Fill up the player's reserve limit (3 cards)
        player.addReservedCard(new Card(1, 0, GemColor.WHITE, emptyCost));
        player.addReservedCard(new Card(1, 0, GemColor.WHITE, emptyCost));
        player.addReservedCard(new Card(1, 0, GemColor.WHITE, emptyCost));

        ActionResult result = ActionExecutor.executeReserveCard(state, affordableCard);

        assertFalse(result.isSuccess(), "Should fail because player already has 3 reserved cards");
        assertFalse(player.getReservedCards().contains(affordableCard), "4th card should NOT be added");
    }

    @Test
    public void testExecuteReserveTopCard_ValidAndGetsGold() {
        int deckSizeBefore = state.getMarket().getDeckSize(1);

        ActionResult result = ActionExecutor.executeReserveTopCard(state, 1);

        assertTrue(result.isSuccess(), "Reserve-top action should succeed");
        assertEquals(1, player.getReservedCards().size(), "Hidden top card added to reserved list");
        assertEquals(deckSizeBefore - 1, state.getMarket().getDeckSize(1), "Deck size should shrink by one");
        assertEquals(1, player.getGems().getCount(GemColor.GOLD), "Player should receive 1 Gold gem");
    }
}

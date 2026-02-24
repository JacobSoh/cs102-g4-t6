package edu.cs102.g04t06.game.rules.valueobjects;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;

/**
 * Unit tests for CardMarket value object.
 * 
 * Original test cases by GPT-5 (Codex).
 * Integrated, revised, and expanded with assertThrows by Claude Opus 4.6 (Anthropic).
 * 
 * @author CS102 Team G6
 * @version 1.1
 */
@DisplayName("CardMarket Tests")
class CardMarketTest {

    private static final int MIN_DECK_SIZE = 20;
    private CardMarket market;
    private Cost standardCost;

    @BeforeEach
    void setUp() {
        standardCost = createStandardCost();
        market = createStandardMarket();
    }

    // ==================== Helper Methods ====================

    private CardMarket createStandardMarket() {
        return new CardMarket(
                createCards(1, MIN_DECK_SIZE),
                createCards(2, MIN_DECK_SIZE),
                createCards(3, MIN_DECK_SIZE)
        );
    }

    private List<Card> createCards(int level, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Card(level, i % 3, GemColor.WHITE, standardCost));
        }
        return cards;
    }

    private Cost createStandardCost() {
        Map<GemColor, Integer> costMap = new EnumMap<>(GemColor.class);
        costMap.put(GemColor.WHITE, 2);
        return new Cost(costMap);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor should split visible and hidden cards correctly")
    void constructorShouldCreateVisibleAndDeckCards() {
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(16, market.getDeckSize(1));
        assertEquals(4, market.getVisibleCards(2).size());
        assertEquals(16, market.getDeckSize(2));
        assertEquals(4, market.getVisibleCards(3).size());
        assertEquals(16, market.getDeckSize(3));
    }

    @Test
    @DisplayName("Constructor should throw for fewer than 20 cards")
    void constructorShouldThrowForFewerThan20Cards() {
        List<Card> level1Cards = createCards(1, 19);
        List<Card> level2Cards = createCards(2, MIN_DECK_SIZE);
        List<Card> level3Cards = createCards(3, MIN_DECK_SIZE);

        assertThrows(IllegalArgumentException.class,
                () -> new CardMarket(level1Cards, level2Cards, level3Cards));
    }

    @Test
    @DisplayName("Constructor should throw for empty card list")
    void constructorShouldThrowForEmptyCardList() {
        List<Card> empty = new ArrayList<>();
        List<Card> level2 = createCards(2, MIN_DECK_SIZE);
        List<Card> level3 = createCards(3, MIN_DECK_SIZE);

        assertThrows(IllegalArgumentException.class,
                () -> new CardMarket(empty, level2, level3));
    }

    @Test
    @DisplayName("Constructor should shuffle cards (different arrangements across instances)")
    void constructorShouldShuffleCards() {
        List<Card> fresh1 = createCards(1, MIN_DECK_SIZE);
        List<Card> fresh2 = createCards(2, MIN_DECK_SIZE);
        List<Card> fresh3 = createCards(3, MIN_DECK_SIZE);

        CardMarket market1 = new CardMarket(
                new ArrayList<>(fresh1),
                new ArrayList<>(fresh2),
                new ArrayList<>(fresh3)
        );

        CardMarket market2 = new CardMarket(
                new ArrayList<>(fresh1),
                new ArrayList<>(fresh2),
                new ArrayList<>(fresh3)
        );

        CardMarket market3 = new CardMarket(
                new ArrayList<>(fresh1),
                new ArrayList<>(fresh2),
                new ArrayList<>(fresh3)
        );

        Card market1Card = market1.getVisibleCard(1, 0);
        Card market2Card = market2.getVisibleCard(1, 0);
        Card market3Card = market3.getVisibleCard(1, 0);

        boolean atLeastOneDifferent =
                market1Card != market2Card ||
                market2Card != market3Card ||
                market1Card != market3Card;

        assertTrue(atLeastOneDifferent,
                "Shuffle should produce different arrangements across multiple market instances");
    }

    // ==================== getVisibleCards() Tests ====================

    @Test
    @DisplayName("getVisibleCards should return 4 cards for each level")
    void getVisibleCardsShouldReturn4CardsPerLevel() {
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(4, market.getVisibleCards(2).size());
        assertEquals(4, market.getVisibleCards(3).size());
    }

    @Test
    @DisplayName("getVisibleCards should return non-null list")
    void getVisibleCardsShouldReturnNonNull() {
        assertNotNull(market.getVisibleCards(1));
        assertNotNull(market.getVisibleCards(2));
        assertNotNull(market.getVisibleCards(3));
    }

    @Test
    @DisplayName("getVisibleCards should throw for invalid level")
    void getVisibleCardsShouldThrowForInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCards(0));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCards(4));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCards(-1));
    }

    @Test
    @DisplayName("Visible cards should match requested level")
    void visibleCardsShouldBelongToRequestedLevel() {
        List<Card> level2VisibleCards = market.getVisibleCards(2);
        assertEquals(4, level2VisibleCards.size());
        assertTrue(level2VisibleCards.stream().allMatch(card -> card.getLevel() == 2));
    }

    // ==================== getVisibleCard() Tests ====================

    @Test
    @DisplayName("getVisibleCard should return correct card at index")
    void getVisibleCardShouldReturnCorrectCard() {
        Card card0 = market.getVisibleCard(1, 0);
        Card card1 = market.getVisibleCard(1, 1);
        Card card2 = market.getVisibleCard(1, 2);
        Card card3 = market.getVisibleCard(1, 3);

        assertNotNull(card0);
        assertNotNull(card1);
        assertNotNull(card2);
        assertNotNull(card3);

        assertNotSame(card0, card1);
        assertNotSame(card1, card2);
        assertNotSame(card2, card3);
    }

    @Test
    @DisplayName("getVisibleCard should throw for invalid index")
    void getVisibleCardShouldThrowForInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(1, -1));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(1, 4));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(1, 100));
    }

    @Test
    @DisplayName("getVisibleCard should throw for invalid level")
    void getVisibleCardShouldThrowForInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(0, 0));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(4, 0));
        assertThrows(IllegalArgumentException.class, () -> market.getVisibleCard(-1, 0));
    }

    @Test
    @DisplayName("getVisibleCard should handle all valid indices (0-3)")
    void getVisibleCardShouldHandleAllValidIndices() {
        for (int i = 0; i < 4; i++) {
            assertNotNull(market.getVisibleCard(1, i));
            assertNotNull(market.getVisibleCard(2, i));
            assertNotNull(market.getVisibleCard(3, i));
        }
    }

    // ==================== getDeckSize() Tests ====================

    @Test
    @DisplayName("getDeckSize should return correct size for each level")
    void getDeckSizeShouldReturnCorrectSize() {
        assertEquals(16, market.getDeckSize(1));
        assertEquals(16, market.getDeckSize(2));
        assertEquals(16, market.getDeckSize(3));
    }

    @Test
    @DisplayName("getDeckSize should throw for invalid level")
    void getDeckSizeShouldThrowForInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> market.getDeckSize(0));
        assertThrows(IllegalArgumentException.class, () -> market.getDeckSize(4));
        assertThrows(IllegalArgumentException.class, () -> market.getDeckSize(-1));
    }

    @Test
    @DisplayName("getDeckSize should decrease after removeCard triggers refill")
    void getDeckSizeShouldDecreaseAfterRemoveCard() {
        int initialDeckSize = market.getDeckSize(1);

        market.removeCard(1, 0);

        assertEquals(initialDeckSize - 1, market.getDeckSize(1));
        assertEquals(4, market.getVisibleCards(1).size());
    }

    // ==================== drawCard() Tests ====================

    @Test
    @DisplayName("drawCard should return a card and decrease deck size")
    void drawCardShouldReturnCardAndDecreaseDeck() {
        int initialSize = market.getDeckSize(1);

        Card drawn = market.drawCard(1);

        assertNotNull(drawn);
        assertEquals(initialSize - 1, market.getDeckSize(1));
    }

    @Test
    @DisplayName("drawCard should throw when deck is empty")
    void drawCardShouldThrowWhenDeckEmpty() {
        int deckSize = market.getDeckSize(1);
        for (int i = 0; i < deckSize; i++) {
            assertNotNull(market.drawCard(1));
        }

        assertEquals(0, market.getDeckSize(1));
        assertThrows(IllegalArgumentException.class, () -> market.drawCard(1));
    }

    @Test
    @DisplayName("drawCard should throw for invalid level")
    void drawCardShouldThrowForInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> market.drawCard(0));
        assertThrows(IllegalArgumentException.class, () -> market.drawCard(4));
        assertThrows(IllegalArgumentException.class, () -> market.drawCard(-1));
    }

    @Test
    @DisplayName("drawCard should draw different cards each time")
    void drawCardShouldDrawDifferentCards() {
        Card card1 = market.drawCard(1);
        Card card2 = market.drawCard(1);
        Card card3 = market.drawCard(1);

        assertNotNull(card1);
        assertNotNull(card2);
        assertNotNull(card3);

        assertNotSame(card1, card2);
        assertNotSame(card2, card3);
        assertNotSame(card1, card3);
    }

    @Test
    @DisplayName("drawCard should not affect visible cards")
    void drawCardShouldNotAffectVisible() {
        int visibleBefore = market.getVisibleCards(1).size();

        market.drawCard(1);

        assertEquals(visibleBefore, market.getVisibleCards(1).size());
    }

    // ==================== removeCard() Tests (with Auto-Refill) ====================

    @Test
    @DisplayName("removeCard should maintain 4 visible cards by auto-refilling")
    void removeCardShouldMaintain4VisibleCards() {
        market.removeCard(1, 0);

        assertEquals(4, market.getVisibleCards(1).size());
    }

    @Test
    @DisplayName("removeCard should decrease deck size when refilling")
    void removeCardShouldDecreaseDeckSize() {
        int initialDeckSize = market.getDeckSize(1);

        market.removeCard(1, 0);

        assertEquals(initialDeckSize - 1, market.getDeckSize(1));
    }

    @Test
    @DisplayName("removeCard should handle multiple consecutive removals")
    void removeCardShouldHandleMultipleRemovals() {
        int initialDeckSize = market.getDeckSize(1);

        market.removeCard(1, 0);
        market.removeCard(1, 0);
        market.removeCard(1, 0);

        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(initialDeckSize - 3, market.getDeckSize(1));
    }

    @Test
    @DisplayName("removeCard should not refill when deck is empty")
    void removeCardShouldNotRefillWhenDeckEmpty() {
        // Drain the deck first
        int deckSize = market.getDeckSize(1);
        for (int i = 0; i < deckSize; i++) {
            market.drawCard(1);
        }
        assertEquals(0, market.getDeckSize(1));

        // Remove a card (no deck to refill from)
        market.removeCard(1, 0);

        assertEquals(3, market.getVisibleCards(1).size());
        assertEquals(0, market.getDeckSize(1));
    }

    @Test
    @DisplayName("removeCard should handle partial refill when deck runs low")
    void removeCardShouldHandlePartialRefill() {
        // Drain all but 1 deck card
        int deckSize = market.getDeckSize(1);
        for (int i = 0; i < deckSize - 1; i++) {
            market.drawCard(1);
        }
        assertEquals(1, market.getDeckSize(1));

        // Remove 1 card (refills with last deck card)
        market.removeCard(1, 0);
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(0, market.getDeckSize(1));

        // Remove another (no refill possible)
        market.removeCard(1, 0);
        assertEquals(3, market.getVisibleCards(1).size());
        assertEquals(0, market.getDeckSize(1));
    }

    @Test
    @DisplayName("removeCard should throw for invalid index")
    void removeCardShouldThrowForInvalidIndex() {
        assertThrows(IllegalArgumentException.class, () -> market.removeCard(1, -1));
        assertThrows(IllegalArgumentException.class, () -> market.removeCard(1, 5));
        assertThrows(IllegalArgumentException.class, () -> market.removeCard(1, 100));
    }

    @Test
    @DisplayName("removeCard should throw for invalid level")
    void removeCardShouldThrowForInvalidLevel() {
        assertThrows(IllegalArgumentException.class, () -> market.removeCard(0, 0));
        assertThrows(IllegalArgumentException.class, () -> market.removeCard(4, 0));
    }

    @Test
    @DisplayName("removeCard should work independently for each level")
    void removeCardShouldWorkIndependentlyPerLevel() {
        int deck1Before = market.getDeckSize(1);
        int deck2Before = market.getDeckSize(2);
        int deck3Before = market.getDeckSize(3);

        market.removeCard(1, 0);

        assertEquals(deck1Before - 1, market.getDeckSize(1));
        assertEquals(deck2Before, market.getDeckSize(2));
        assertEquals(deck3Before, market.getDeckSize(3));

        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(4, market.getVisibleCards(2).size());
        assertEquals(4, market.getVisibleCards(3).size());
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Full workflow: remove card (auto-refills), draw, repeat")
    void fullWorkflowRemoveAndDraw() {
        int initialDeckSize = market.getDeckSize(1);

        market.removeCard(1, 0);
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(initialDeckSize - 1, market.getDeckSize(1));

        Card drawn = market.drawCard(1);
        assertNotNull(drawn);
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(initialDeckSize - 2, market.getDeckSize(1));

        market.removeCard(1, 2);
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(initialDeckSize - 3, market.getDeckSize(1));
    }

    @Test
    @DisplayName("Market should handle complete depletion with auto-refill")
    void marketShouldHandleCompleteDepletionWithAutoRefill() {
        int deckSize = market.getDeckSize(1);

        // Remove cards until deck is depleted (each refills from deck)
        for (int i = 0; i < deckSize; i++) {
            market.removeCard(1, 0);
            assertEquals(4, market.getVisibleCards(1).size());
        }

        assertEquals(0, market.getDeckSize(1));
        assertEquals(4, market.getVisibleCards(1).size());

        // Remove remaining visible cards (no refill possible)
        market.removeCard(1, 0);
        assertEquals(3, market.getVisibleCards(1).size());

        market.removeCard(1, 0);
        assertEquals(2, market.getVisibleCards(1).size());

        market.removeCard(1, 0);
        assertEquals(1, market.getVisibleCards(1).size());

        market.removeCard(1, 0);
        assertEquals(0, market.getVisibleCards(1).size());
    }

    @Test
    @DisplayName("All three levels should operate independently with auto-refill")
    void allLevelsShouldOperateIndependentlyWithAutoRefill() {
        int deck1Before = market.getDeckSize(1);
        int deck2Before = market.getDeckSize(2);
        int deck3Before = market.getDeckSize(3);

        market.removeCard(1, 0);

        market.drawCard(2);
        market.drawCard(2);

        market.removeCard(3, 1);
        market.removeCard(3, 1);

        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(deck1Before - 1, market.getDeckSize(1));

        assertEquals(4, market.getVisibleCards(2).size());
        assertEquals(deck2Before - 2, market.getDeckSize(2));

        assertEquals(4, market.getVisibleCards(3).size());
        assertEquals(deck3Before - 2, market.getDeckSize(3));
    }

    @Test
    @DisplayName("Removing all 4 visible cards rapidly should maintain refill")
    void removingAll4VisibleShouldMaintainRefill() {
        int initialDeck = market.getDeckSize(1);

        market.removeCard(1, 0);
        market.removeCard(1, 0);
        market.removeCard(1, 0);
        market.removeCard(1, 0);

        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(initialDeck - 4, market.getDeckSize(1));
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should handle large number of cards with auto-refill")
    void shouldHandleLargeNumberOfCardsWithAutoRefill() {
        List<Card> large1 = createCards(1, 100);
        List<Card> large2 = createCards(2, 100);
        List<Card> large3 = createCards(3, 100);

        CardMarket largeMarket = new CardMarket(large1, large2, large3);

        assertEquals(4, largeMarket.getVisibleCards(1).size());
        assertEquals(96, largeMarket.getDeckSize(1));

        for (int i = 0; i < 50; i++) {
            largeMarket.removeCard(1, 0);
        }

        assertEquals(4, largeMarket.getVisibleCards(1).size());
        assertEquals(46, largeMarket.getDeckSize(1));
    }
}
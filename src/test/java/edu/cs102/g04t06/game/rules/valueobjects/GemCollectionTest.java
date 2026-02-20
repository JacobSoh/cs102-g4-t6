package edu.cs102.g04t06.game.rules.valueobjects;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.cs102.g04t06.game.rules.entities.GemColor;

/**
 * Unit tests for GemCollection value object.
 *
 * Original test cases by GPT-5 (Codex).
 * Expanded and revised with additional assertThrows cases by Claude Opus 4.6 (Anthropic).
 *
 * @author CS102 Team G6
 * @version 1.1
 */
@DisplayName("GemCollection Tests")
class GemCollectionTest {

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Default constructor creates empty collection with all colors at 0")
    void defaultConstructorCreatesEmptyCollection() {
        GemCollection gems = new GemCollection();

        assertTrue(gems.isEmpty());
        assertEquals(0, gems.getTotalCount());
        for (GemColor color : GemColor.values()) {
            assertEquals(0, gems.getCount(color));
        }
    }

    @Test
    @DisplayName("Map constructor reads provided values correctly")
    void mapConstructorReadsProvidedValues() {
        Map<GemColor, Integer> seed = new EnumMap<>(GemColor.class);
        seed.put(GemColor.WHITE, 2);
        seed.put(GemColor.BLUE, 1);

        GemCollection gems = new GemCollection(seed);

        assertEquals(2, gems.getCount(GemColor.WHITE));
        assertEquals(1, gems.getCount(GemColor.BLUE));
        assertEquals(3, gems.getTotalCount());
    }

    @Test
    @DisplayName("Map constructor returns 0 for colors not in provided map")
    void mapConstructorReturnsZeroForMissingColors() {
        Map<GemColor, Integer> seed = new EnumMap<>(GemColor.class);
        seed.put(GemColor.WHITE, 5);

        GemCollection gems = new GemCollection(seed);

        assertEquals(0, gems.getCount(GemColor.RED));
        assertEquals(0, gems.getCount(GemColor.GOLD));
    }

    // ==================== getCount() Tests ====================

    @Test
    @DisplayName("getCount returns correct value after add")
    void getCountReturnsCorrectValueAfterAdd() {
        GemCollection gems = new GemCollection().add(GemColor.GREEN, 7);

        assertEquals(7, gems.getCount(GemColor.GREEN));
    }

    @Test
    @DisplayName("getCount returns 0 for colors never added")
    void getCountReturnsZeroForUntouchedColor() {
        GemCollection gems = new GemCollection().add(GemColor.WHITE, 3);

        assertEquals(0, gems.getCount(GemColor.BLACK));
    }

    // ==================== getTotalCount() Tests ====================

    @Test
    @DisplayName("getTotalCount sums all colors correctly")
    void getTotalCountSumsAllColors() {
        GemCollection gems = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.BLUE, 3)
                .add(GemColor.RED, 1);

        assertEquals(6, gems.getTotalCount());
    }

    @Test
    @DisplayName("getTotalCount returns 0 for empty collection")
    void getTotalCountReturnsZeroForEmpty() {
        GemCollection gems = new GemCollection();

        assertEquals(0, gems.getTotalCount());
    }

    // ==================== isEmpty() Tests ====================

    @Test
    @DisplayName("isEmpty returns true for default constructor")
    void isEmptyReturnsTrueForDefault() {
        GemCollection gems = new GemCollection();

        assertTrue(gems.isEmpty());
    }

    @Test
    @DisplayName("isEmpty returns false after adding gems")
    void isEmptyReturnsFalseAfterAdd() {
        GemCollection gems = new GemCollection().add(GemColor.GOLD, 1);

        assertFalse(gems.isEmpty());
    }

    @Test
    @DisplayName("isEmpty returns true after adding and subtracting same amount")
    void isEmptyReturnsTrueAfterAddAndSubtract() {
        GemCollection gems = new GemCollection()
                .add(GemColor.WHITE, 3)
                .subtract(GemColor.WHITE, 3);

        assertTrue(gems.isEmpty());
    }

    // ==================== asMap() Tests ====================

    @Test
    @DisplayName("asMap returns defensive copy that does not affect original")
    void asMapReturnsCopy() {
        GemCollection gems = new GemCollection().add(GemColor.WHITE, 2);

        Map<GemColor, Integer> copy = gems.asMap();
        copy.put(GemColor.WHITE, 99);

        assertEquals(2, gems.getCount(GemColor.WHITE));
    }

    @Test
    @DisplayName("asMap returns correct values")
    void asMapReturnsCorrectValues() {
        GemCollection gems = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.RED, 1);

        Map<GemColor, Integer> map = gems.asMap();

        assertEquals(3, map.get(GemColor.WHITE));
        assertEquals(1, map.get(GemColor.RED));
    }

    // ==================== add() Tests ====================

    @Test
    @DisplayName("add returns new collection without mutating original")
    void addReturnsUpdatedCollectionWithoutMutatingOriginal() {
        GemCollection original = new GemCollection();
        GemCollection updated = original.add(GemColor.RED, 3);

        assertEquals(0, original.getCount(GemColor.RED));
        assertEquals(3, updated.getCount(GemColor.RED));
    }

    @Test
    @DisplayName("add returns different instance from original")
    void addReturnsDifferentInstance() {
        GemCollection original = new GemCollection();
        GemCollection updated = original.add(GemColor.BLUE, 1);

        assertNotSame(original, updated);
    }

    @Test
    @DisplayName("add can be chained multiple times")
    void addCanBeChained() {
        GemCollection gems = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.BLUE, 3)
                .add(GemColor.GREEN, 1);

        assertEquals(2, gems.getCount(GemColor.WHITE));
        assertEquals(3, gems.getCount(GemColor.BLUE));
        assertEquals(1, gems.getCount(GemColor.GREEN));
        assertEquals(6, gems.getTotalCount());
    }

    @Test
    @DisplayName("add to same color accumulates correctly")
    void addToSameColorAccumulates() {
        GemCollection gems = new GemCollection()
                .add(GemColor.RED, 2)
                .add(GemColor.RED, 3);

        assertEquals(5, gems.getCount(GemColor.RED));
    }

    @Test
    @DisplayName("add with zero amount does not change collection")
    void addZeroDoesNotChange() {
        GemCollection gems = new GemCollection().add(GemColor.WHITE, 5);
        GemCollection same = gems.add(GemColor.WHITE, 0);

        assertEquals(5, same.getCount(GemColor.WHITE));
    }

    // ==================== subtract(GemColor, int) Tests ====================

    @Test
    @DisplayName("subtract by color returns correct result")
    void subtractByColorReturnsCorrectResult() {
        GemCollection gems = new GemCollection().add(GemColor.WHITE, 5);
        GemCollection result = gems.subtract(GemColor.WHITE, 3);

        assertEquals(2, result.getCount(GemColor.WHITE));
    }

    @Test
    @DisplayName("subtract by color does not mutate original")
    void subtractByColorDoesNotMutateOriginal() {
        GemCollection original = new GemCollection().add(GemColor.BLUE, 4);
        GemCollection result = original.subtract(GemColor.BLUE, 2);

        assertEquals(4, original.getCount(GemColor.BLUE));
        assertEquals(2, result.getCount(GemColor.BLUE));
    }

    @Test
    @DisplayName("subtract by color throws for negative result")
    void subtractByColorThrowsForNegativeResult() {
        GemCollection gems = new GemCollection();

        assertThrows(IllegalArgumentException.class,
                () -> gems.subtract(GemColor.BLACK, 1));
    }

    @Test
    @DisplayName("subtract by color throws when amount exceeds available")
    void subtractByColorThrowsWhenAmountExceedsAvailable() {
        GemCollection gems = new GemCollection().add(GemColor.RED, 2);

        assertThrows(IllegalArgumentException.class,
                () -> gems.subtract(GemColor.RED, 3));
    }

    @Test
    @DisplayName("subtract exact amount results in zero")
    void subtractExactAmountResultsInZero() {
        GemCollection gems = new GemCollection().add(GemColor.GREEN, 4);
        GemCollection result = gems.subtract(GemColor.GREEN, 4);

        assertEquals(0, result.getCount(GemColor.GREEN));
    }

    @Test
    @DisplayName("subtract by color with zero amount does not change collection")
    void subtractZeroDoesNotChange() {
        GemCollection gems = new GemCollection().add(GemColor.WHITE, 3);
        GemCollection same = gems.subtract(GemColor.WHITE, 0);

        assertEquals(3, same.getCount(GemColor.WHITE));
    }

    // ==================== subtract(GemCollection) Tests ====================

    @Test
    @DisplayName("subtract collection returns correct difference")
    void subtractCollectionReturnsDifference() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 4)
                .add(GemColor.BLUE, 2);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 1)
                .add(GemColor.BLUE, 2);

        GemCollection remaining = wallet.subtract(cost);

        assertEquals(3, remaining.getCount(GemColor.WHITE));
        assertEquals(0, remaining.getCount(GemColor.BLUE));
        assertEquals(3, remaining.getTotalCount());
    }

    @Test
    @DisplayName("subtract collection throws when insufficient gems")
    void subtractCollectionThrowsWhenInsufficient() {
        GemCollection wallet = new GemCollection().add(GemColor.WHITE, 1);
        GemCollection cost = new GemCollection().add(GemColor.WHITE, 2);

        assertThrows(IllegalArgumentException.class,
                () -> wallet.subtract(cost));
    }

    @Test
    @DisplayName("subtract collection throws when one color is insufficient")
    void subtractCollectionThrowsWhenOneColorInsufficient() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 5)
                .add(GemColor.BLUE, 1);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.BLUE, 3);

        assertThrows(IllegalArgumentException.class,
                () -> wallet.subtract(cost));
    }

    @Test
    @DisplayName("subtract collection does not mutate original")
    void subtractCollectionDoesNotMutateOriginal() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 5)
                .add(GemColor.RED, 3);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.RED, 1);

        GemCollection remaining = wallet.subtract(cost);

        assertEquals(5, wallet.getCount(GemColor.WHITE));
        assertEquals(3, wallet.getCount(GemColor.RED));
        assertEquals(3, remaining.getCount(GemColor.WHITE));
        assertEquals(2, remaining.getCount(GemColor.RED));
    }

    @Test
    @DisplayName("subtract empty collection returns equivalent collection")
    void subtractEmptyCollectionReturnsEquivalent() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2);
        GemCollection empty = new GemCollection();

        GemCollection result = wallet.subtract(empty);

        assertEquals(3, result.getCount(GemColor.WHITE));
        assertEquals(2, result.getCount(GemColor.BLUE));
    }

    @Test
    @DisplayName("subtract collection with multiple colors all at exact amounts")
    void subtractCollectionExactAmounts() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2)
                .add(GemColor.RED, 1);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2)
                .add(GemColor.RED, 1);

        GemCollection result = wallet.subtract(cost);

        assertTrue(result.isEmpty());
    }

    // ==================== contains() Tests ====================

    @Test
    @DisplayName("contains returns true when all colors are sufficient")
    void containsChecksAllColors() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.GREEN, 1);

        GemCollection affordable = new GemCollection().add(GemColor.WHITE, 2);
        GemCollection notAffordable = new GemCollection().add(GemColor.GREEN, 2);

        assertTrue(wallet.contains(affordable));
        assertFalse(wallet.contains(notAffordable));
    }

    @Test
    @DisplayName("contains returns true for empty other collection")
    void containsReturnsTrueForEmptyOther() {
        GemCollection wallet = new GemCollection().add(GemColor.WHITE, 1);
        GemCollection empty = new GemCollection();

        assertTrue(wallet.contains(empty));
    }

    @Test
    @DisplayName("empty collection contains empty collection")
    void emptyContainsEmpty() {
        GemCollection empty1 = new GemCollection();
        GemCollection empty2 = new GemCollection();

        assertTrue(empty1.contains(empty2));
    }

    @Test
    @DisplayName("empty collection does not contain non-empty collection")
    void emptyDoesNotContainNonEmpty() {
        GemCollection empty = new GemCollection();
        GemCollection nonEmpty = new GemCollection().add(GemColor.RED, 1);

        assertFalse(empty.contains(nonEmpty));
    }

    @Test
    @DisplayName("contains returns true when wallet has more than needed")
    void containsReturnsTrueWhenExcess() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 5)
                .add(GemColor.BLUE, 3);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.BLUE, 1);

        assertTrue(wallet.contains(cost));
    }

    @Test
    @DisplayName("contains returns true when wallet has exact amount")
    void containsReturnsTrueWhenExact() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2);

        assertTrue(wallet.contains(cost));
    }

    @Test
    @DisplayName("contains returns false when one color is short by 1")
    void containsReturnsFalseWhenShortByOne() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 1);
        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 3)
                .add(GemColor.BLUE, 2);

        assertFalse(wallet.contains(cost));
    }

    @Test
    @DisplayName("contains checks GOLD color as well")
    void containsChecksGoldColor() {
        GemCollection wallet = new GemCollection().add(GemColor.GOLD, 2);
        GemCollection cost = new GemCollection().add(GemColor.GOLD, 3);

        assertFalse(wallet.contains(cost));
    }

    // ==================== Immutability Integration Tests ====================

    @Test
    @DisplayName("Chained operations do not mutate any intermediate collections")
    void chainedOperationsDoNotMutateIntermediates() {
        GemCollection step0 = new GemCollection();
        GemCollection step1 = step0.add(GemColor.WHITE, 5);
        GemCollection step2 = step1.add(GemColor.BLUE, 3);
        GemCollection step3 = step2.subtract(GemColor.WHITE, 2);

        assertEquals(0, step0.getTotalCount());
        assertEquals(5, step1.getTotalCount());
        assertEquals(8, step2.getTotalCount());
        assertEquals(6, step3.getTotalCount());

        assertEquals(0, step0.getCount(GemColor.WHITE));
        assertEquals(5, step1.getCount(GemColor.WHITE));
        assertEquals(5, step2.getCount(GemColor.WHITE));
        assertEquals(3, step3.getCount(GemColor.WHITE));
    }

    @Test
    @DisplayName("Full workflow: add, check contains, subtract, verify result")
    void fullWorkflowAddContainsSubtract() {
        GemCollection wallet = new GemCollection()
                .add(GemColor.WHITE, 4)
                .add(GemColor.BLUE, 3)
                .add(GemColor.RED, 2);

        GemCollection cost = new GemCollection()
                .add(GemColor.WHITE, 2)
                .add(GemColor.BLUE, 1);

        assertTrue(wallet.contains(cost));

        GemCollection remaining = wallet.subtract(cost);

        assertEquals(2, remaining.getCount(GemColor.WHITE));
        assertEquals(2, remaining.getCount(GemColor.BLUE));
        assertEquals(2, remaining.getCount(GemColor.RED));
        assertEquals(6, remaining.getTotalCount());
    }
}
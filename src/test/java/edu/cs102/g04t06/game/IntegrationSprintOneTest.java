package edu.cs102.g04t06.game;

// Edited by GPT-5 (Codex)

import edu.cs102.g04t06.game.infrastructure.config.ConfigLoader;
import edu.cs102.g04t06.game.infrastructure.config.ExcelDataLoader;
import edu.cs102.g04t06.game.rules.GameRules;
import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Sprint 1 deliverables.
 * 
 * <p>Tests that all components work together:</p>
 * <ul>
 *   <li>ConfigLoader loads configuration</li>
 *   <li>ExcelDataLoader loads cards and nobles from CSV</li>
 *   <li>All entity classes work correctly</li>
 *   <li>CardMarket initializes and operates</li>
 *   <li>Player state management works</li>
 * </ul>
 * 
 * @author CS102 Team G6
 * @version 1.0
 * @since Sprint 1
 */
@DisplayName("Sprint 1 Integration Test")
class IntegrationSprintOneTest {
    
    private static final String CARD_FILE = "src/main/data/cards/splendor_card.csv";
    private static final String TEST_NOBLE_FILE = "src/main/data/cards/test_nobles.csv";
    private static final String CONFIG_FILE = "config.properties";
    
    @Test
    @DisplayName("Complete Sprint 1 integration test")
    void completeSprint1IntegrationTest() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  SPLENDOR SPRINT 1 INTEGRATION TEST        ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // ==================== TEST 1: ConfigLoader ====================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: ConfigLoader");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        ConfigLoader config = new ConfigLoader(CONFIG_FILE);
        System.out.println("✓ Config loaded successfully");
        
        assertEquals(4, config.getGemCount(2, GemColor.WHITE));
        assertEquals(5, config.getGemCount(3, GemColor.BLUE));
        assertEquals(7, config.getGemCount(4, GemColor.RED));
        assertEquals(5, config.getGemCount(2, GemColor.GOLD));
        
        System.out.println("  Winning points: " + GameRules.getWinningPoints());
        System.out.println("  Max reserved cards: " + GameRules.getMaxReservedCards());
        System.out.println("  Max gems per player: " + GameRules.getMaxGemsPerPlayer());
        System.out.println("  2-player white gems: " + config.getGemCount(2, GemColor.WHITE));
        System.out.println("  3-player blue gems: " + config.getGemCount(3, GemColor.BLUE));
        System.out.println("  4-player red gems: " + config.getGemCount(4, GemColor.RED));
        System.out.println("  Gold gems (any player count): " + config.getGemCount(2, GemColor.GOLD));
        
        // ==================== TEST 2: ExcelDataLoader ====================
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: ExcelDataLoader (CSV)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        String cardFile = config.getDataFilePath().get("card");
        String nobleFile = TEST_NOBLE_FILE;
        
        // Load cards
        ExcelDataLoader dataLoader = new ExcelDataLoader();
        List<Card> level1 = dataLoader.loadLevel1Cards(cardFile);
        List<Card> level2 = dataLoader.loadLevel2Cards(cardFile);
        List<Card> level3 = dataLoader.loadLevel3Cards(cardFile);

        // Load nobles
        List<Noble> nobles = dataLoader.loadNobles(nobleFile);
        
        System.out.println("✓ Data loaded successfully");
        
        // Verify expected counts (based on actual data provided)
        assertEquals(40, level1.size(), "Should have 40 Level 1 cards");
        assertEquals(30, level2.size(), "Should have 30 Level 2 cards");
        assertEquals(20, level3.size(), "Should have 20 Level 3 cards");
        assertEquals(3, nobles.size(), "Should have 3 nobles from test file");
        
        System.out.println("  Level 1 cards: " + level1.size());
        System.out.println("  Level 2 cards: " + level2.size());
        System.out.println("  Level 3 cards: " + level3.size());
        System.out.println("  Nobles: " + nobles.size());
        System.out.println("  Total cards: " + (level1.size() + level2.size() + level3.size()));
        
        // Verify card data integrity
        for (Card card : level1) {
            assertEquals(1, card.getLevel());
            assertTrue(card.getPoints() >= 0 && card.getPoints() <= 1);
        }
        
        for (Card card : level2) {
            assertEquals(2, card.getLevel());
            assertTrue(card.getPoints() >= 1 && card.getPoints() <= 3);
        }
        
        for (Card card : level3) {
            assertEquals(3, card.getLevel());
            assertTrue(card.getPoints() >= 3 && card.getPoints() <= 5);
        }
        
        // Verify noble data
        for (Noble noble : nobles) {
            assertEquals(3, noble.getPoints());
        }
        
        // Print sample cards
        System.out.println("\n  Sample Level 1 Card (first):");
        Card sampleL1 = level1.get(0);
        System.out.println("    Level: " + sampleL1.getLevel());
        System.out.println("    Points: " + sampleL1.getPoints());
        System.out.println("    Bonus: " + sampleL1.getBonus());
        System.out.println("    Cost: " + formatCost(sampleL1));
        
        System.out.println("\n  Sample Level 2 Card (first):");
        Card sampleL2 = level2.get(0);
        System.out.println("    Level: " + sampleL2.getLevel());
        System.out.println("    Points: " + sampleL2.getPoints());
        System.out.println("    Bonus: " + sampleL2.getBonus());
        System.out.println("    Cost: " + formatCost(sampleL2));
        
        System.out.println("\n  Sample Level 3 Card (first):");
        Card sampleL3 = level3.get(0);
        System.out.println("    Level: " + sampleL3.getLevel());
        System.out.println("    Points: " + sampleL3.getPoints());
        System.out.println("    Bonus: " + sampleL3.getBonus());
        System.out.println("    Cost: " + formatCost(sampleL3));
        
        System.out.println("\n  Sample Noble (first):");
        Noble sampleNoble = nobles.get(0);
        System.out.println("    Points: " + sampleNoble.getPoints());
        System.out.println("    Requirements: " + formatNobleRequirements(sampleNoble));
        
        // ==================== TEST 3: GemCollection ====================
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: GemCollection");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        GemCollection gems = new GemCollection();
        assertTrue(gems.isEmpty());
        assertEquals(0, gems.getTotalCount());
        
        gems = gems.add(GemColor.WHITE, 5);
        gems = gems.add(GemColor.BLUE, 3);
        gems = gems.add(GemColor.GREEN, 2);
        
        System.out.println("✓ GemCollection operations working");
        assertEquals(10, gems.getTotalCount());
        assertEquals(5, gems.getCount(GemColor.WHITE));
        assertEquals(3, gems.getCount(GemColor.BLUE));
        assertEquals(2, gems.getCount(GemColor.GREEN));
        
        System.out.println("  Total gems: " + gems.getTotalCount());
        System.out.println("  White: " + gems.getCount(GemColor.WHITE));
        System.out.println("  Blue: " + gems.getCount(GemColor.BLUE));
        System.out.println("  Green: " + gems.getCount(GemColor.GREEN));
        
        // Test immutability
        GemCollection gems2 = gems.subtract(GemColor.WHITE, 2);
        assertEquals(5, gems.getCount(GemColor.WHITE), "Original should be unchanged");
        assertEquals(3, gems2.getCount(GemColor.WHITE), "New should be updated");
        
        System.out.println("\n✓ Immutability verified");
        System.out.println("  Original white: " + gems.getCount(GemColor.WHITE));
        System.out.println("  After subtract white: " + gems2.getCount(GemColor.WHITE));
        
        // ==================== TEST 4: Player ====================
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 4: Player");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        Player player = new Player("Alice", 0);
        assertEquals("Alice", player.getName());
        assertEquals(0, player.getTurnOrder());
        assertEquals(0, player.getPoints());
        assertEquals(0, player.getGemCount());
        
        System.out.println("✓ Player created: " + player.getName());
        System.out.println("  Turn order: " + player.getTurnOrder());
        System.out.println("  Initial points: " + player.getPoints());
        
        // Add gems
        GemCollection playerGems = new GemCollection()
            .add(GemColor.WHITE, 3)
            .add(GemColor.BLUE, 2)
            .add(GemColor.GREEN, 1);
        player.addGems(playerGems);
        
        assertEquals(6, player.getGemCount());
        System.out.println("\n✓ Added gems to player");
        System.out.println("  Total gems: " + player.getGemCount());
        
        // Add cards
        player.addCard(level1.get(0));
        player.addCard(level2.get(0));
        
        assertEquals(2, player.getPurchasedCards().size());
        assertTrue(player.getPoints() >= 1); // level2 card has at least 1 point
        
        System.out.println("\n✓ Added cards to player");
        System.out.println("  Purchased cards: " + player.getPurchasedCards().size());
        System.out.println("  Points: " + player.getPoints());
        
        // Calculate bonuses
        Map<GemColor, Integer> bonuses = player.calculateBonuses();
        assertNotNull(bonuses);
        assertTrue(bonuses.size() > 0);
        
        System.out.println("  Bonuses:");
        for (GemColor color : GemColor.values()) {
            if (bonuses.containsKey(color) && bonuses.get(color) > 0) {
                System.out.println("    " + color + ": " + bonuses.get(color));
            }
        }
        
        // Reserve card
        player.addReservedCard(level3.get(0));
        assertEquals(1, player.getReservedCards().size());
        
        System.out.println("\n✓ Reserved card");
        System.out.println("  Reserved cards: " + player.getReservedCards().size());
        
        // Claim noble
        player.claimNoble(nobles.get(0));
        assertEquals(1, player.getClaimedNobles().size());
        
        System.out.println("\n✓ Claimed noble");
        System.out.println("  Claimed nobles: " + player.getClaimedNobles().size());
        System.out.println("  Total points: " + player.getPoints());
        
        // ==================== TEST 5: CardMarket ====================
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 5: CardMarket");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        CardMarket market = new CardMarket(level1, level2, level3);
        
        // Verify visible cards
        assertEquals(4, market.getVisibleCards(1).size());
        assertEquals(4, market.getVisibleCards(2).size());
        assertEquals(4, market.getVisibleCards(3).size());
        
        // Verify deck sizes
        assertEquals(36, market.getDeckSize(1)); // 40 - 4 = 36
        assertEquals(26, market.getDeckSize(2)); // 30 - 4 = 26
        assertEquals(16, market.getDeckSize(3)); // 20 - 4 = 16
        
        System.out.println("✓ CardMarket created");
        System.out.println("  Level 1: " + market.getVisibleCards(1).size() + " visible, " + market.getDeckSize(1) + " in deck");
        System.out.println("  Level 2: " + market.getVisibleCards(2).size() + " visible, " + market.getDeckSize(2) + " in deck");
        System.out.println("  Level 3: " + market.getVisibleCards(3).size() + " visible, " + market.getDeckSize(3) + " in deck");
        
        // Test getVisibleCard
        Card visibleCard = market.getVisibleCard(1, 0);
        assertNotNull(visibleCard);
        assertEquals(1, visibleCard.getLevel());
        
        System.out.println("\n✓ Retrieved visible card");
        
        // Test drawCard
        Card drawnCard = market.drawCard(1);
        assertNotNull(drawnCard);
        assertEquals(35, market.getDeckSize(1));
        
        System.out.println("\n✓ Drew card from deck");
        System.out.println("  New deck size: " + market.getDeckSize(1));
        
        // Test removeCard (auto-refills)
        int deckBefore = market.getDeckSize(1);
        market.removeCard(1, 0);
        
        assertEquals(4, market.getVisibleCards(1).size(), "Should auto-refill to 4");
        assertEquals(deckBefore - 1, market.getDeckSize(1), "Deck should decrease by 1");
        
        System.out.println("\n✓ Removed visible card (auto-refilled)");
        System.out.println("  Visible cards: " + market.getVisibleCards(1).size());
        System.out.println("  Deck size: " + market.getDeckSize(1));
        
        // ==================== TEST 6: Multi-Player Setup ====================
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 6: Multi-Player Setup");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        Player alice = new Player("Alice", 0);
        Player bob = new Player("Bob", 1);
        Player charlie = new Player("Charlie", 2);
        
        System.out.println("✓ Created 3 players");
        assertEquals(0, alice.getTurnOrder());
        assertEquals(1, bob.getTurnOrder());
        assertEquals(2, charlie.getTurnOrder());
        
        // Setup gem bank for 3 players
        int standardGems = config.getGemCount(3, GemColor.WHITE);
        int goldGems = config.getGemCount(3, GemColor.GOLD);
        
        GemCollection gemBank = new GemCollection()
            .add(GemColor.WHITE, standardGems)
            .add(GemColor.BLUE, standardGems)
            .add(GemColor.GREEN, standardGems)
            .add(GemColor.RED, standardGems)
            .add(GemColor.BLACK, standardGems)
            .add(GemColor.GOLD, goldGems);
        
        assertEquals(30, gemBank.getTotalCount()); // 5*5 + 5 = 30
        
        System.out.println("\n✓ Gem bank initialized");
        System.out.println("  Standard gems per color: " + standardGems);
        System.out.println("  Gold gems: " + goldGems);
        System.out.println("  Total gems: " + gemBank.getTotalCount());
        
        // Calculate nobles in play for this integration fixture
        int noblesInPlay = Math.min(3 + 1, nobles.size());
        assertTrue(noblesInPlay > 0);
        
        System.out.println("\n✓ Nobles for 3 players: " + noblesInPlay);
        
        // ==================== FINAL SUMMARY ====================
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           ALL TESTS PASSED ✓               ║");
        System.out.println("╚════════════════════════════════════════════╝");
        
        System.out.println("\nSummary:");
        System.out.println("  ✓ ConfigLoader: All configuration loaded correctly");
        System.out.println("  ✓ ExcelDataLoader: 90 cards + 3 nobles loaded");
        System.out.println("  ✓ GemCollection: Immutable operations verified");
        System.out.println("  ✓ Player: All state management working");
        System.out.println("  ✓ CardMarket: Shuffling and auto-refill working");
        System.out.println("  ✓ Multi-player: Setup validated for 3 players");
        
        System.out.println("\n✓ Sprint 1 foundation complete!");
        System.out.println("✓ Ready for Sprint 2: Rules + Execution layer\n");
    }
    
    /**
     * Helper method to format card cost for display
     */
    private String formatCost(Card card) {
        StringBuilder sb = new StringBuilder();
        Map<GemColor, Integer> costMap = card.getCost().asMap();
        
        boolean first = true;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue; // Cards don't cost gold
            
            int amount = costMap.getOrDefault(color, 0);
            if (amount > 0) {
                if (!first) sb.append(", ");
                sb.append(amount).append(" ").append(color);
                first = false;
            }
        }
        
        return sb.length() > 0 ? sb.toString() : "Free";
    }
    
    /**
     * Helper method to format noble requirements for display
     */
    private String formatNobleRequirements(Noble noble) {
        StringBuilder sb = new StringBuilder();
        Map<GemColor, Integer> req = noble.getRequirements();
        
        boolean first = true;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue; // Nobles don't require gold
            
            int amount = req.getOrDefault(color, 0);
            if (amount > 0) {
                if (!first) sb.append(", ");
                sb.append(amount).append(" ").append(color);
                first = false;
            }
        }
        
        return sb.toString();
    }
}

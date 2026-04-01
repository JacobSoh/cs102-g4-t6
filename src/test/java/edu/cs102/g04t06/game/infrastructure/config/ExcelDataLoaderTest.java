package edu.cs102.g04t06.game.infrastructure.config;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExcelDataLoader (CSV loader).
 * 
 * @author CS102 Team G6
 * @version 1.0
 */
@DisplayName("ExcelDataLoader Tests")
class ExcelDataLoaderTest {

    private static final ExcelDataLoader loader = new ExcelDataLoader();

    private static final String TEST_CARD_FILE = "src/main/data/cards/test_cards.csv";
    private static final String TEST_NOBLE_FILE = "src/main/data/cards/test_nobles.csv";
    private static final String ACTUAL_CARD_FILE = "src/main/data/cards/splendor_card.csv";
    private static final String ACTUAL_NOBLE_FILE = "src/main/data/cards/splendor_noble.csv";
    
    @BeforeAll
    static void setupTestFiles() throws IOException {
        // Create test card CSV
        try (FileWriter writer = new FileWriter(TEST_CARD_FILE)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,BLACK,0,0,1,1,1,1\n");
            writer.write("1,BLUE,0,1,0,1,1,1\n");
            writer.write("2,GREEN,2,2,0,0,3,0\n");
            writer.write("2,RED,1,0,0,2,0,2\n");
            writer.write("3,WHITE,5,3,3,3,0,0\n");
        }
        
        // Create test noble CSV
        try (FileWriter writer = new FileWriter(TEST_NOBLE_FILE)) {
            writer.write("ID,Name,Black,Blue,Green,Red,White\n");
            writer.write("1,Caroline,3,0,0,3,3\n");
            writer.write("2,Henriette,0,3,3,3,0\n");
            writer.write("3,Mary Stuart,4,4,0,0,0\n");
        }
    }
    
    // ==================== Card Loading Tests ====================
    
    @Test
    @DisplayName("loadLevel1Cards should load only level 1 cards")
    void loadLevel1CardsShouldLoadOnlyLevel1() {
        List<Card> cards = loader.loadLevel1Cards(TEST_CARD_FILE);
        
        assertEquals(2, cards.size());
        
        for (Card card : cards) {
            assertEquals(1, card.getLevel());
        }
    }
    
    @Test
    @DisplayName("loadLevel2Cards should load only level 2 cards")
    void loadLevel2CardsShouldLoadOnlyLevel2() {
        List<Card> cards = loader.loadLevel2Cards(TEST_CARD_FILE);
        
        assertEquals(2, cards.size());
        
        for (Card card : cards) {
            assertEquals(2, card.getLevel());
        }
    }
    
    @Test
    @DisplayName("loadLevel3Cards should load only level 3 cards")
    void loadLevel3CardsShouldLoadOnlyLevel3() {
        List<Card> cards = loader.loadLevel3Cards(TEST_CARD_FILE);
        
        assertEquals(1, cards.size());
        
        for (Card card : cards) {
            assertEquals(3, card.getLevel());
        }
    }
    
    @Test
    @DisplayName("loadLevel1Cards should parse card data correctly")
    void loadLevel1CardsShouldParseDataCorrectly() {
        List<Card> cards = loader.loadLevel1Cards(TEST_CARD_FILE);
        
        // First card: 1,BLACK,0,0,1,1,1,1
        Card card1 = cards.get(0);
        assertEquals(1, card1.getLevel());
        assertEquals(0, card1.getPoints());
        assertEquals(GemColor.BLACK, card1.getBonus());
        assertEquals(4, card1.getCost().getTotalGems()); // 1+1+1+1
        assertEquals(0, card1.getCost().getRequired(GemColor.BLACK));
        assertEquals(1, card1.getCost().getRequired(GemColor.BLUE));
        assertEquals(1, card1.getCost().getRequired(GemColor.GREEN));
        assertEquals(1, card1.getCost().getRequired(GemColor.RED));
        assertEquals(1, card1.getCost().getRequired(GemColor.WHITE));
        
        // Second card: 1,BLUE,0,1,0,1,1,1
        Card card2 = cards.get(1);
        assertEquals(1, card2.getLevel());
        assertEquals(0, card2.getPoints());
        assertEquals(GemColor.BLUE, card2.getBonus());
        assertEquals(4, card2.getCost().getTotalGems());
        assertEquals(1, card2.getCost().getRequired(GemColor.BLACK));
        assertEquals(0, card2.getCost().getRequired(GemColor.BLUE));
    }
    
    @Test
    @DisplayName("loadLevel2Cards should parse points correctly")
    void loadLevel2CardsShouldParsePoints() {
        List<Card> cards = loader.loadLevel2Cards(TEST_CARD_FILE);
        
        // First level 2 card: 2,GREEN,2,...
        Card card1 = cards.get(0);
        assertEquals(2, card1.getPoints());
        
        // Second level 2 card: 2,RED,1,...
        Card card2 = cards.get(1);
        assertEquals(1, card2.getPoints());
    }
    
    @Test
    @DisplayName("loadLevel3Cards should parse high-value cards correctly")
    void loadLevel3CardsShouldParseHighValueCards() {
        List<Card> cards = loader.loadLevel3Cards(TEST_CARD_FILE);
        
        // 3,WHITE,5,3,3,3,0,0
        Card card = cards.get(0);
        assertEquals(3, card.getLevel());
        assertEquals(5, card.getPoints());
        assertEquals(GemColor.WHITE, card.getBonus());
        assertEquals(9, card.getCost().getTotalGems());
    }
    
    @Test
    @DisplayName("Card loading should handle zero costs")
    void cardLoadingShouldHandleZeroCosts() {
        List<Card> cards = loader.loadLevel3Cards(TEST_CARD_FILE);
        
        // 3,WHITE,5,3,3,3,0,0 (zero red and white)
        Card card = cards.get(0);
        assertEquals(0, card.getCost().getRequired(GemColor.RED));
        assertEquals(0, card.getCost().getRequired(GemColor.WHITE));
    }
    
    @Test
    @DisplayName("Card loading should handle all gem colors")
    void cardLoadingShouldHandleAllGemColors() {
        List<Card> level1 = loader.loadLevel1Cards(TEST_CARD_FILE);
        List<Card> level2 = loader.loadLevel2Cards(TEST_CARD_FILE);
        List<Card> level3 = loader.loadLevel3Cards(TEST_CARD_FILE);
        
        List<Card> allCards = new java.util.ArrayList<>();
        allCards.addAll(level1);
        allCards.addAll(level2);
        allCards.addAll(level3);
        
        // Check we have cards with different bonuses
        boolean hasBlack = allCards.stream().anyMatch(c -> c.getBonus() == GemColor.BLACK);
        boolean hasBlue = allCards.stream().anyMatch(c -> c.getBonus() == GemColor.BLUE);
        boolean hasGreen = allCards.stream().anyMatch(c -> c.getBonus() == GemColor.GREEN);
        boolean hasRed = allCards.stream().anyMatch(c -> c.getBonus() == GemColor.RED);
        boolean hasWhite = allCards.stream().anyMatch(c -> c.getBonus() == GemColor.WHITE);
        
        assertTrue(hasBlack);
        assertTrue(hasBlue);
        assertTrue(hasGreen);
        assertTrue(hasRed);
        assertTrue(hasWhite);
    }
    
    // ==================== Noble Loading Tests ====================
    
    @Test
    @DisplayName("loadNobles should load all nobles")
    void loadNoblesShouldLoadAllNobles() {
        List<Noble> nobles = loader.loadNobles(TEST_NOBLE_FILE);
        
        assertEquals(3, nobles.size());
    }
    
    @Test
    @DisplayName("loadNobles should parse noble data correctly")
    void loadNoblesShouldParseDataCorrectly() {
        List<Noble> nobles = loader.loadNobles(TEST_NOBLE_FILE);
        
        // First noble: 1,Caroline,3,0,0,3,3
        Noble noble1 = nobles.get(0);
        assertEquals(1, noble1.getId());
        assertEquals("Caroline", noble1.getName());
        assertEquals(3, noble1.getPoints());
        assertEquals(3, noble1.getRequirements().get(GemColor.BLACK));
        assertEquals(0, noble1.getRequirements().getOrDefault(GemColor.BLUE, 0));
        assertEquals(0, noble1.getRequirements().getOrDefault(GemColor.GREEN, 0));
        assertEquals(3, noble1.getRequirements().get(GemColor.RED));
        assertEquals(3, noble1.getRequirements().get(GemColor.WHITE));
        
        // Second noble: 2,Henriette,0,3,3,3,0
        Noble noble2 = nobles.get(1);
        assertEquals(2, noble2.getId());
        assertEquals("Henriette", noble2.getName());
        assertEquals(3, noble2.getPoints());
        assertEquals(0, noble2.getRequirements().getOrDefault(GemColor.BLACK, 0));
        assertEquals(3, noble2.getRequirements().get(GemColor.BLUE));
        
        // Third noble: 3,Mary Stuart,4,4,0,0,0
        Noble noble3 = nobles.get(2);
        assertEquals(3, noble3.getId());
        assertEquals("Mary Stuart", noble3.getName());
        assertEquals(3, noble3.getPoints());
        assertEquals(4, noble3.getRequirements().get(GemColor.BLACK));
        assertEquals(4, noble3.getRequirements().get(GemColor.BLUE));
    }
    
    @Test
    @DisplayName("loadNobles should handle zero requirements")
    void loadNoblesShouldHandleZeroRequirements() {
        List<Noble> nobles = loader.loadNobles(TEST_NOBLE_FILE);
        
        // Third noble has zeros for green, red, white
        Noble noble = nobles.get(2);
        assertEquals(0, noble.getRequirements().getOrDefault(GemColor.GREEN, 0));
        assertEquals(0, noble.getRequirements().getOrDefault(GemColor.RED, 0));
        assertEquals(0, noble.getRequirements().getOrDefault(GemColor.WHITE, 0));
    }
    
    // ==================== Error Handling Tests ====================
    
    @Test
    @DisplayName("loadLevel1Cards should throw exception for non-existent file")
    void loadLevel1CardsShouldThrowExceptionForNonExistentFile() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.loadLevel1Cards("nonexistent.csv");
        });
        
        assertTrue(exception.getMessage().contains("Failed to load cards"));
    }
    
    @Test
    @DisplayName("loadNobles should throw exception for non-existent file")
    void loadNoblesShouldThrowExceptionForNonExistentFile() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.loadNobles("nonexistent.csv");
        });
        
        assertTrue(exception.getMessage().contains("Failed to load nobles"));
    }
    
    @Test
    @DisplayName("Card loading should handle empty lines")
    void cardLoadingShouldHandleEmptyLines() throws IOException {
        String emptyLineFile = "test_empty_lines.csv";
        
        try (FileWriter writer = new FileWriter(emptyLineFile)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,BLACK,0,0,1,1,1,1\n");
            writer.write("\n"); // Empty line
            writer.write("1,BLUE,0,1,0,1,1,1\n");
            writer.write("  \n"); // Whitespace line
        }
        
        List<Card> cards = loader.loadLevel1Cards(emptyLineFile);
        
        assertEquals(2, cards.size());
        
        // Clean up
        new File(emptyLineFile).delete();
    }
    
    @Test
    @DisplayName("Noble loading should handle empty lines")
    void nobleLoadingShouldHandleEmptyLines() throws IOException {
        String emptyLineFile = "test_empty_nobles.csv";
        
        try (FileWriter writer = new FileWriter(emptyLineFile)) {
            writer.write("ID,Name,Black,Blue,Green,Red,White\n");
            writer.write("1,Caroline,3,0,0,3,3\n");
            writer.write("\n");
            writer.write("2,Mary Stuart,4,4,0,0,0\n");
        }
        
        List<Noble> nobles = loader.loadNobles(emptyLineFile);
        
        assertEquals(2, nobles.size());
        
        // Clean up
        new File(emptyLineFile).delete();
    }
    
    @Test
    @DisplayName("Card loading should handle trailing commas")
    void cardLoadingShouldHandleTrailingCommas() throws IOException {
        String trailingCommaFile = "test_trailing_comma.csv";
        
        try (FileWriter writer = new FileWriter(trailingCommaFile)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,BLACK,0,0,1,1,1,1,\n"); // Trailing comma
        }
        
        List<Card> cards = loader.loadLevel1Cards(trailingCommaFile);
        
        assertEquals(1, cards.size());
        
        // Clean up
        new File(trailingCommaFile).delete();
    }
    
    @Test
    @DisplayName("Card loading should throw exception for invalid gem color")
    void cardLoadingShouldThrowExceptionForInvalidGemColor() throws IOException {
        String invalidColorFile = "test_invalid_color.csv";
        
        try (FileWriter writer = new FileWriter(invalidColorFile)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,YELLOW,0,0,1,1,1,1\n"); // Invalid color
        }
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.loadLevel1Cards(invalidColorFile);
        });
        
        assertTrue(exception.getMessage().contains("Invalid gem color"));
        
        // Clean up
        new File(invalidColorFile).delete();
    }
    
    @Test
    @DisplayName("Card loading should throw exception for invalid number format")
    void cardLoadingShouldThrowExceptionForInvalidNumberFormat() throws IOException {
        String invalidNumberFile = "test_invalid_number.csv";
        
        try (FileWriter writer = new FileWriter(invalidNumberFile)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,BLACK,abc,0,1,1,1,1\n"); // Invalid PV
        }
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.loadLevel1Cards(invalidNumberFile);
        });
        
        assertTrue(exception.getMessage().contains("Invalid integer value"));
        
        // Clean up
        new File(invalidNumberFile).delete();
    }
    
    @Test
    @DisplayName("Card loading should throw exception for insufficient columns")
    void cardLoadingShouldThrowExceptionForInsufficientColumns() throws IOException {
        String insufficientColumnsFile = "test_insufficient_columns.csv";
        
        try (FileWriter writer = new FileWriter(insufficientColumnsFile)) {
            writer.write("Level,Color,PV,Black,Blue,Green,Red,White\n");
            writer.write("1,BLACK,0,0\n"); // Only 4 columns instead of 8
        }
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loader.loadLevel1Cards(insufficientColumnsFile);
        });
        
        assertTrue(exception.getMessage().contains("Invalid card format"));
        
        // Clean up
        new File(insufficientColumnsFile).delete();
    }
    
    // ==================== Actual File Tests ====================
    
    @Test
    @DisplayName("Should successfully load actual card file")
    void shouldSuccessfullyLoadActualCardFile() {
        File cardFile = new File(ACTUAL_CARD_FILE);
        
        if (cardFile.exists()) {
            assertDoesNotThrow(() -> {
                List<Card> level1 = loader.loadLevel1Cards(ACTUAL_CARD_FILE);
                List<Card> level2 = loader.loadLevel2Cards(ACTUAL_CARD_FILE);
                List<Card> level3 = loader.loadLevel3Cards(ACTUAL_CARD_FILE);
                
                assertTrue(level1.size() > 0, "Level 1 should have cards");
                assertTrue(level2.size() > 0, "Level 2 should have cards");
                assertTrue(level3.size() > 0, "Level 3 should have cards");
            });
        }
    }
    
    @Test
    @DisplayName("Actual noble file with quoted commas should load successfully")
    void actualNobleFileWithQuotedCommasShouldLoadSuccessfully() {
        File nobleFile = new File(ACTUAL_NOBLE_FILE);

        if (nobleFile.exists()) {
            List<Noble> nobles = loader.loadNobles(ACTUAL_NOBLE_FILE);
            assertFalse(nobles.isEmpty(), "Nobles should be loaded from file");
            assertTrue(nobles.stream().anyMatch(n -> n.getName().contains(",")),
                    "At least one noble name should contain a comma");
        }
    }
    
    @Test
    @DisplayName("Actual cards should have valid data ranges")
    void actualCardsShouldHaveValidDataRanges() {
        File cardFile = new File(ACTUAL_CARD_FILE);
        
        if (cardFile.exists()) {
            List<Card> level1 = loader.loadLevel1Cards(ACTUAL_CARD_FILE);
            List<Card> level2 = loader.loadLevel2Cards(ACTUAL_CARD_FILE);
            List<Card> level3 = loader.loadLevel3Cards(ACTUAL_CARD_FILE);
            
            // Level 1 cards typically have 0-1 points
            for (Card card : level1) {
                assertTrue(card.getPoints() >= 0 && card.getPoints() <= 1,
                    "Level 1 card should have 0-1 points");
                assertEquals(1, card.getLevel());
            }
            
            // Level 2 cards typically have 1-3 points
            for (Card card : level2) {
                assertTrue(card.getPoints() >= 1 && card.getPoints() <= 3,
                    "Level 2 card should have 1-3 points");
                assertEquals(2, card.getLevel());
            }
            
            // Level 3 cards typically have 3-5 points
            for (Card card : level3) {
                assertTrue(card.getPoints() >= 3 && card.getPoints() <= 5,
                    "Level 3 card should have 3-5 points");
                assertEquals(3, card.getLevel());
            }
        }
    }
}

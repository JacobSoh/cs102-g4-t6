package edu.cs102.g04t06.game.infrastructure.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.valueobjects.Cost;

/**
 * Utility class for loading game data from CSV files.
 * 
 * <p>Reads card and noble data from CSV files.</p>
 * 
 * <p>CSV format for cards:</p>
 * <pre>
 * Level,Color,PV,Black,Blue,Green,Red,White
 * </pre>
 * 
 * <p>CSV format for nobles:</p>
 * <pre>
 * PV,Black,Blue,Green,Red,White
 * </pre>
 * 
 */
public class ExcelDataLoader {
    
    // Private constructor - utility class should not be instantiated
    private ExcelDataLoader() {
        throw new AssertionError("ExcelDataLoader is a utility class and should not be instantiated");
    }
    
    /**
     * Loads Level 1 cards from CSV file.
     * 
     * @param filePath Path to the CSV file containing card data
     * @return List of Level 1 cards
     * @throws RuntimeException if file cannot be read or data is invalid
     */
    public static List<Card> loadLevel1Cards(String filePath) {
        return loadCardsFromFile(filePath, 1);
    }
    
    /**
     * Loads Level 2 cards from CSV file.
     * 
     * @param filePath Path to the CSV file containing card data
     * @return List of Level 2 cards
     * @throws RuntimeException if file cannot be read or data is invalid
     */
    public static List<Card> loadLevel2Cards(String filePath) {
        return loadCardsFromFile(filePath, 2);
    }
    
    /**
     * Loads Level 3 cards from CSV file.
     * 
     * @param filePath Path to the CSV file containing card data
     * @return List of Level 3 cards
     * @throws RuntimeException if file cannot be read or data is invalid
     */
    public static List<Card> loadLevel3Cards(String filePath) {
        return loadCardsFromFile(filePath, 3);
    }
    
    /**
     * Loads nobles from CSV file.
     * 
     * @param filePath Path to the CSV file containing noble data
     * @return List of nobles
     * @throws RuntimeException if file cannot be read or data is invalid
     */
    public static List<Noble> loadNobles(String filePath) {
        List<Noble> nobles = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            // Read and skip header line
            String header = br.readLine();
            lineNumber++;
            
            if (header == null) {
                throw new RuntimeException("CSV file is empty: " + filePath);
            }
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    Noble noble = parseNobleFromCsvLine(line);
                    nobles.add(noble);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing noble at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load nobles from file: " + filePath + " - " + e.getMessage(), e);
        }
        
        return nobles;
    }
    
    /**
     * Private helper method to load cards from CSV file and filter by level.
     * 
     * @param filePath Path to CSV file
     * @param targetLevel Card level to filter (1, 2, or 3)
     * @return List of cards with the specified level
     */
    private static List<Card> loadCardsFromFile(String filePath, int targetLevel) {
        List<Card> cards = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            // Read and skip header line
            String header = br.readLine();
            lineNumber++;
            
            if (header == null) {
                throw new RuntimeException("CSV file is empty: " + filePath);
            }
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                lineNumber++;
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    Card card = parseCardFromCsvLine(line);
                    
                    // Only add cards matching the target level
                    if (card.getLevel() == targetLevel) {
                        cards.add(card);
                    }
                    
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing card at line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cards from file: " + filePath + " - " + e.getMessage(), e);
        }
        
        return cards;
    }
    
    /**
     * Parses a Card from a CSV line.
     * 
     * Expected format: Level,Color,PV,Black,Blue,Green,Red,White
     * 
     * @param line CSV line containing card data
     * @return Parsed Card object
     */
    private static Card parseCardFromCsvLine(String line) {
        String[] parts = line.split(",", -1); // -1 to keep trailing empty strings
        
        if (parts.length < 8) {
            throw new RuntimeException("Invalid card format. Expected 8 columns, got " + parts.length);
        }
        
        // Column indices (0-based)
        // 0: Level
        // 1: Color (bonus)
        // 2: PV (points)
        // 3: Black cost
        // 4: Blue cost
        // 5: Green cost
        // 6: Red cost
        // 7: White cost
        
        int level = parseIntValue(parts[0], "Level");
        String bonusStr = parts[1].trim().toUpperCase();
        int points = parseIntValue(parts[2], "PV");
        
        GemColor bonus = parseGemColor(bonusStr);
        
        // Parse costs
        int costBlack = parseIntValue(parts[3], "Black cost");
        int costBlue = parseIntValue(parts[4], "Blue cost");
        int costGreen = parseIntValue(parts[5], "Green cost");
        int costRed = parseIntValue(parts[6], "Red cost");
        int costWhite = parseIntValue(parts[7], "White cost");
        
        Map<GemColor, Integer> costMap = new EnumMap<>(GemColor.class);
        if (costWhite > 0) costMap.put(GemColor.WHITE, costWhite);
        if (costBlue > 0) costMap.put(GemColor.BLUE, costBlue);
        if (costGreen > 0) costMap.put(GemColor.GREEN, costGreen);
        if (costRed > 0) costMap.put(GemColor.RED, costRed);
        if (costBlack > 0) costMap.put(GemColor.BLACK, costBlack);
        
        Cost cost = new Cost(costMap);
        
        return new Card(level, points, bonus, cost);
    }
    
    /**
     * Parses a Noble from a CSV line.
     * 
     * Expected format: PV,Black,Blue,Green,Red,White
     * 
     * @param line CSV line containing noble data
     * @return Parsed Noble object
     */
    private static Noble parseNobleFromCsvLine(String line) {
        String[] parts = splitCsvLine(line);

        if (parts.length < 7) {
            throw new RuntimeException("Invalid noble format. Expected 7 columns, got " + parts.length);
        }

        // Column indices (0-based)
        // 0: ID
        // 1: Name
        // 2: Black requirement
        // 3: Blue requirement
        // 4: Green requirement
        // 5: Red requirement
        // 6: White requirement

        int id = parseIntValue(parts[0], "ID");
        String name = parts[1].trim();

        int reqBlack = parseIntValue(parts[2], "Black requirement");
        int reqBlue = parseIntValue(parts[3], "Blue requirement");
        int reqGreen = parseIntValue(parts[4], "Green requirement");
        int reqRed = parseIntValue(parts[5], "Red requirement");
        int reqWhite = parseIntValue(parts[6], "White requirement");
        
        Map<GemColor, Integer> requirements = new EnumMap<>(GemColor.class);
        if (reqWhite > 0) requirements.put(GemColor.WHITE, reqWhite);
        if (reqBlue > 0) requirements.put(GemColor.BLUE, reqBlue);
        if (reqGreen > 0) requirements.put(GemColor.GREEN, reqGreen);
        if (reqRed > 0) requirements.put(GemColor.RED, reqRed);
        if (reqBlack > 0) requirements.put(GemColor.BLACK, reqBlack);
        
        return new Noble(id, name, requirements);
    }
    
    /**
     * Splits a CSV line into fields, respecting double-quoted fields that may
     * contain commas. Surrounding quotes are stripped from each field.
     *
     * @param line raw CSV line
     * @return array of field values
     */
    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Safely parses an integer value from a string.
     * 
     * @param value String value to parse
     * @param fieldName Name of the field (for error messages)
     * @return Parsed integer value, or 0 if empty/null
     */
    private static int parseIntValue(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid integer value for " + fieldName + ": " + value);
        }
    }
    
    /**
     * Parses a GemColor from a string.
     * 
     * @param colorStr String representation of gem color
     * @return GemColor enum value
     * @throws IllegalArgumentException if color is invalid
     */
    private static GemColor parseGemColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            throw new IllegalArgumentException("Gem color cannot be empty");
        }
        
        try {
            return GemColor.valueOf(colorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gem color: " + colorStr + ". Expected: WHITE, BLUE, GREEN, RED, or BLACK");
        }
    }
}
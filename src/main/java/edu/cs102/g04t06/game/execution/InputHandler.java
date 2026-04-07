package edu.cs102.g04t06.game.execution;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import edu.cs102.g04t06.game.rules.entities.Card;
import edu.cs102.g04t06.game.rules.entities.GemColor;
import edu.cs102.g04t06.game.rules.entities.Noble;
import edu.cs102.g04t06.game.rules.entities.Player;
import edu.cs102.g04t06.game.rules.valueobjects.CardMarket;
import edu.cs102.g04t06.game.rules.valueobjects.GemCollection;

/**
 * Validates and parses command input values for the Splendor game.
 */
public class InputHandler {

    public InputHandler() {}

    @Deprecated
    public int promptActionChoice(int choice) {
        if (choice < 1 || choice > 4) {
            throw new IllegalArgumentException(
                "Action choice must be between 1 and 4 but was " + choice + ".");
        }
        return choice;
    }

    public int parseTierToken(String token) {
        String t = token.toLowerCase().trim();
        t = t.replace("tier", "");
        t = t.startsWith("t") ? t.substring(1) : t;
        try {
            int tier = Integer.parseInt(t);
            if (tier < 1 || tier > 3) {
                throw new IllegalArgumentException("Tier must be 1, 2, or 3.");
            }
            return tier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid tier. Use t1, t2, or t3.");
        }
    }

    public int parseSlotToken(String token) {
        String t = token.toLowerCase().trim();
        t = t.replace("slot", "");
        t = t.startsWith("s") ? t.substring(1) : t;
        try {
            int slot = Integer.parseInt(t);
            if (slot < 1 || slot > 4) {
                throw new IllegalArgumentException("Slot must be 1 to 4.");
            }
            return slot - 1;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid slot. Use slot1..slot4.");
        }
    }

    public List<GemColor> parseGemSequence(String raw) {
        String cleaned = raw.toUpperCase().replaceAll("[^A-Z*]", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("No gem codes provided.");
        }

        List<GemColor> result = new ArrayList<>();
        for (char c : cleaned.toCharArray()) {
            result.add(parseGemCode(c));
        }
        return result;
    }

    private GemColor parseGemCode(char c) {
        return switch (c) {
            case 'W' -> GemColor.WHITE;
            case 'B' -> GemColor.BLUE;
            case 'G' -> GemColor.GREEN;
            case 'R' -> GemColor.RED;
            case 'D' -> GemColor.BLACK;
            case '*' -> GemColor.GOLD;
            default -> throw new IllegalArgumentException("Invalid gem code: " + c);
        };
    }

    @Deprecated
    public Card promptCardSelection(CardMarket market, int level,
            boolean includeReserved, List<Card> reservedCards, int selection) {

        List<Card> options = new ArrayList<>(market.getVisibleCards(level));

        if (includeReserved && reservedCards != null) {
            options.addAll(reservedCards);
        }

        if (selection < 1 || selection > options.size()) {
            throw new IllegalArgumentException(
                "Card selection must be between 1 and " + options.size() + " but was " + selection + ".");
        }

        return options.get(selection - 1);
    }

    public GemCollection promptGemSelection(int count, List<GemColor> colors) {
        if (colors.size() != count) {
            throw new IllegalArgumentException(
                "Expected " + count + " gem colour(s) but received " + colors.size() + ".");
        }

        Map<GemColor, Integer> selected = new EnumMap<>(GemColor.class);
        for (GemColor color : colors) {
            if (color == GemColor.GOLD) {
                throw new IllegalArgumentException(
                    "GOLD cannot be selected directly; it is obtained only by reserving a card.");
            }
            selected.merge(color, 1, Integer::sum);
        }

        return new GemCollection(selected);
    }

    public GemCollection promptGemsToReturn(Player player, int excessCount,
            List<GemColor> gemsToReturn) {

        if (gemsToReturn.size() != excessCount) {
            throw new IllegalArgumentException(
                "Expected " + excessCount + " gem(s) to return but received "
                + gemsToReturn.size() + ".");
        }

        Map<GemColor, Integer> toReturn = new EnumMap<>(GemColor.class);
        for (GemColor color : gemsToReturn) {
            toReturn.merge(color, 1, Integer::sum);
        }

        GemCollection returnCollection = new GemCollection(toReturn);
        if (!player.getGems().contains(returnCollection)) {
            throw new IllegalArgumentException(
                "Player does not hold enough of the specified gems to return.");
        }

        return returnCollection;
    }

    public Noble promptNobleSelection(List<Noble> claimable, int selection) {
        if (selection < 1 || selection > claimable.size()) {
            throw new IllegalArgumentException(
                "Noble selection must be between 1 and " + claimable.size()
                + " but was " + selection + ".");
        }
        return claimable.get(selection - 1);
    }

    @Deprecated
    public int promptPlayerCount(int count) {
        if (count < 2 || count > 4) {
            throw new IllegalArgumentException(
                "Player count must be 2, 3, or 4 but was " + count + ".");
        }
        return count;
    }
    
    @Deprecated
    public List<String> promptPlayerNames(List<String> names) {
        for (String name : names) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Player name cannot be null or blank.");
            }
        }
        return new ArrayList<>(names);
    }
}

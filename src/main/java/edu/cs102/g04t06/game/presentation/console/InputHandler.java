package edu.cs102.g04t06.game.presentation.console;

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
 * Validates and processes player input for the Splendor game.
 * Input values are supplied as parameters (e.g. from a GUI event handler)
 * rather than read from standard input. Each method validates its parameters
 * and throws {@link IllegalArgumentException} on invalid input.
 */
public class InputHandler {

    /**
     * Creates a new InputHandler 
    */
    public InputHandler() {}

    /**
     * Validates a player's chosen action number.
     *
     * @param choice the action number supplied by the player (must be 1–4):
     *               1 = Take 3 different gems,
     *               2 = Take 2 gems of the same colour,
     *               3 = Purchase a card,
     *               4 = Reserve a card
     * @return the validated choice
     * @throws IllegalArgumentException if {@code choice} is not in [1, 4]
     */
    public int promptActionChoice(int choice) {
        if (choice < 1 || choice > 4) {
            throw new IllegalArgumentException(
                "Action choice must be between 1 and 4 but was " + choice + ".");
        }
        return choice;
    }

    /**
     * Parses a tier token such as "t1", "tier2", or "3".
     *
     * @param token raw tier token
     * @return parsed tier in range [1, 3]
     * @throws IllegalArgumentException if token cannot be parsed or out of range
     */
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

    /**
     * Parses a slot token such as "slot1", "s2", or "4".
     *
     * @param token raw slot token
     * @return zero-based slot index in range [0, 3]
     * @throws IllegalArgumentException if token cannot be parsed or out of range
     */
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

    /**
     * Parses a gem-code sequence into gem colors.
     * Supported codes: W, U, G, R, K, *
     *
     * @param raw raw input sequence (e.g. "w r u" or "WRU")
     * @return parsed gem colors in order entered
     * @throws IllegalArgumentException if no codes or any code is invalid
     */
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

    /**
     * Resolves a card selection from the visible market and, optionally, the
     * player's reserved hand into the corresponding {@link Card} object.
     * Visible cards at the given level are listed first (indices 1–4), followed
     * by reserved cards if {@code includeReserved} is {@code true}.
     *
     * @param market          the current card market
     * @param level           the market level to draw visible cards from (1, 2, or 3)
     * @param includeReserved if {@code true}, reserved cards are appended as additional options
     * @param reservedCards   the player's currently reserved cards; may be {@code null} or empty
     * @param selection       1-based index of the card chosen by the player
     * @return the {@link Card} at the given selection index
     * @throws IllegalArgumentException if {@code selection} is out of the valid range
     */
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

    /**
     * Builds and validates a {@link GemCollection} from a list of gem colours
     * chosen by the player. GOLD may not be selected through this method.
     *
     * @param count  the exact number of gems that must be selected
     * @param colors the gem colours chosen by the player; size must equal {@code count}
     * @return a {@link GemCollection} containing the chosen gems
     * @throws IllegalArgumentException if {@code colors.size()} does not equal {@code count},
     *                                  or any colour is {@link GemColor#GOLD}
     */
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

    /**
     * Validates the gems a player wishes to return to the supply and builds
     * the corresponding {@link GemCollection}. The player must actually hold
     * all gems in {@code gemsToReturn}.
     *
     * @param player       the player who must return gems
     * @param excessCount  the exact number of gems that must be returned
     * @param gemsToReturn the gem colours the player chose to return;
     *                     size must equal {@code excessCount}
     * @return a {@link GemCollection} representing the gems to return
     * @throws IllegalArgumentException if {@code gemsToReturn.size()} does not equal
     *                                  {@code excessCount}, or the player does not hold
     *                                  the specified gems
     */
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

    /**
     * Resolves a noble selection from the list of claimable nobles into the
     * corresponding {@link Noble} object.
     *
     * @param claimable the non-empty list of nobles the player may claim
     * @param selection 1-based index of the noble chosen by the player
     * @return the {@link Noble} at the given selection index
     * @throws IllegalArgumentException if {@code selection} is out of the valid range
     */
    public Noble promptNobleSelection(List<Noble> claimable, int selection) {
        if (selection < 1 || selection > claimable.size()) {
            throw new IllegalArgumentException(
                "Noble selection must be between 1 and " + claimable.size()
                + " but was " + selection + ".");
        }
        return claimable.get(selection - 1);
    }

    /**
     * Validates the number of players for a game session.
     * Splendor supports 2 to 4 players.
     *
     * @param count the player count supplied (must be 2–4)
     * @return the validated player count
     * @throws IllegalArgumentException if {@code count} is not in [2, 4]
     */
    public int promptPlayerCount(int count) {
        if (count < 2 || count > 4) {
            throw new IllegalArgumentException(
                "Player count must be 2, 3, or 4 but was " + count + ".");
        }
        return count;
    }

    /**
     * Validates a list of player names and returns a defensive copy.
     * Every name must be non-null and non-blank after trimming.
     *
     * @param names the player names supplied in turn order
     * @return a new list containing the validated names
     * @throws IllegalArgumentException if any name is {@code null} or blank
     */
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

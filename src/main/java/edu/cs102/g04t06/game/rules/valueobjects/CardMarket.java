package edu.cs102.g04t06.game.rules.valueobjects;

import edu.cs102.g04t06.game.rules.entities.Card;
import java.util.*;

public class CardMarket {
    private final List<Card> level1Visible;
    private final List<Card> level1Deck;
    private final List<Card> level2Visible;
    private final List<Card> level2Deck;
    private final List<Card> level3Visible;
    private final List<Card> level3Deck;

    private List<Card> splitVisible(List<Card> cards) {
        return new ArrayList<>(cards.subList(0, 4));
    }

    private List<Card> splitDeck(List<Card> cards) {
        return new ArrayList<>(cards.subList(4, cards.size()));
    }

    /**
     * Creates a shuffled deck for each of the level cards
     * Split these decks into visible and hidden decks respecitively
     * 
     * @param level1Cards   List of level 1 cards
     * @param level2Cards   List of level 2 cards
     * @param level3Cards   List of level 3 cards
     */
    public CardMarket(List<Card> level1Cards, List<Card> level2Cards, List<Card> level3Cards) {
        Collections.shuffle(level1Cards);
        Collections.shuffle(level2Cards);
        Collections.shuffle(level3Cards);
        
        this.level1Visible = splitVisible(level1Cards);
        this.level1Deck = splitDeck(level1Cards);
        this.level2Visible = splitVisible(level2Cards);
        this.level2Deck = splitDeck(level2Cards);
        this.level3Visible = splitVisible(level3Cards);
        this.level3Deck = splitDeck(level3Cards);
    }

    /**
     * Returns the Visible list of cards of a particular level on the CardMarket
     * 
     * @param level     the level of the deck which we are interested in
     * @return deck corresponding to that level
     * @throws IllegalArgumentException if level is invalid value
     */
    public List<Card> getVisibleCards (int level) {
        if (!(level <= 3 && level >= 1)) {
            throw new IllegalArgumentException("Deck Level can only be 1, 2 or 3!");
        }
        switch(level) {
            case 1:
                return level1Visible;
            case 2:
                return level2Visible;
            case 3:
                return level3Visible;
            default:
                return null; // wont reach here, just adding for compiler
        }
    }

    /**
     * Returns the Visible card of a particular level, corresponding to its index
     * 
     * @param level     the level of the card we are interested in
     * @param index     the index of the card we are interested in
     * @return the visible card, or null if no such card exists
     */
    public Card getVisibleCard (int level, int index) {
        if (this.getVisibleCards(level).size() <= index) {
            return null;
        }
        return this.getVisibleCards(level).get(index);
    }

    /**
     * Returns the size of the non-visible deck of a particular level
     * 
     * @param level     the level of the deck we are interested in
     * @return the size of the non-visible deck of that level
     */
    public int getDeckSize (int level) {
        if (!(level <= 3 && level >= 1)) {
            throw new IllegalArgumentException("Deck Level can only be 1, 2 or 3!");
        }
        switch(level) {
            case 1:
                return level1Deck.size();
            case 2:
                return level2Deck.size();
            case 3:
                return level3Deck.size();
            default:
                return 0; // wont reach here, just adding for compiler
        }
    }

    /**
     * Returns the top card in the deck with index 0, removes it from the main deck
     * 
     * @param level     the level of the deck which we are interested in
     * @return the Card that was removed
     * @throws IllegalArgumentException if deck size is less than or equals to zero
     */
    public Card drawCard (int level) {
        if (this.getDeckSize(level) <= 0) {
            throw new IllegalArgumentException("Deck size must be greater than zero for a draw!");
        }
        switch(level) {
            case 1:
                return level1Deck.remove(0);
            case 2:
                return level2Deck.remove(0);
            case 3:
                return level3Deck.remove(0);
            default:
                return null; // wont reach here, just adding for compiler
        }
    }

    /**
     * Removes the card from visible deck
     * Checks if remaining deck is empty, and fills visible cards up to 4
     * 
     * @param level     the level of the deck which we are interested in
     * @param index     the index of the card in the deck which we are interested in
     * @return void
     * @throws IllegalArgumentException if deck size is less than or equals to zero
     */
    public void removeCard (int level, int index) {
        List<Card> visible = getVisibleCards(level);
        if (index < 0 || index >= visible.size()) {
            throw new IllegalArgumentException("Index not present in Visible Deck");
        }
        visible.remove(index);

        if (this.getDeckSize(level) > 0 && visible.size() < 4) {
            visible.add(this.drawCard(level));
        }
    }

}
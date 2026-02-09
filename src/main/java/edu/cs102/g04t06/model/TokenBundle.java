package edu.cs102.g04t06.model;

import edu.cs102.g04t06.model.type.TokenColor;

/**
 * Tracks counts of tokens by {@link TokenColor}.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class TokenBundle {
    private final int[] counts = new int[7];

    /**
     * Creates an empty token bundle with all counts set to zero.
     */
    public TokenBundle() {
        // Default constructor.
    }

    /**
     * Returns the count for the given token color.
     *
     * @param c the token color to query
     * @return the current count for {@code c}
     */
    public int get(TokenColor c) {
        return this.counts[c.ordinal()];
    }

    /**
     * Adds {@code n} tokens of the given color.
     *
     * @param c the token color to increment
     * @param n the number of tokens to add
     */
    public void add(TokenColor c, int n) {
        this.counts[c.ordinal()] += n;
    }

    /**
     * Removes {@code n} tokens of the given color.
     *
     * @param c the token color to decrement
     * @param n the number of tokens to remove
     */
    public void remove(TokenColor c, int n) {
        this.counts[c.ordinal()] -= n;
    }

    /**
     * Computes the total number of tokens across all colors.
     *
     * @return the total token count
     */
    public int total() {
        int total = 0;
        for (int i = 0; i < counts.length; i++) {
            total += counts[i];
        }
        return total;
    }
}

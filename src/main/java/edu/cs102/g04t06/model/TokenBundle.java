package edu.cs102.g04t06.model;

import edu.cs102.g04t06.model.type.TokenColor;

public class TokenBundle {
    private final int[] counts = new int[7];

    public int get(TokenColor c) {
        return this.counts[c.ordinal()];
    }

    public void add(TokenColor c, int n) {
        this.counts[c.ordinal()] += n;
    }

    public int total() {
        int total = 0;
        for (int i = 0; i < counts.length; i++) {
            total += counts[i];
        }
        return total;
    }
}
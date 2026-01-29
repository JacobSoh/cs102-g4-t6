package edu.cs102.g04t06.model;

import java.util.ArrayList;
import java.util.Map;

public class Character {

    private int id;
    private Map<Type.TokenType, Integer> tokens;
    private ArrayList<Card> cards;
    private ArrayList<Noble> nobles;
    private int prestigePoints;

    public void addToken(Type.TokenType tokenType, int amt) {
        this.tokens.put(tokenType, this.tokens.get(tokenType) + amt);
    }

    public void removeToken(Type.TokenType tokenType, int amt) {
        this.tokens.put(tokenType, this.tokens.get(tokenType) - amt);
    }

    public int getPrestigePoints() {
        return this.prestigePoints;
    }
}

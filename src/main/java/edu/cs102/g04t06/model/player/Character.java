package edu.cs102.g04t06.model.player;

import java.util.ArrayList;
import java.util.Map;

import edu.cs102.g04t06.model.card.Card;
import edu.cs102.g04t06.model.card.Noble;
import edu.cs102.g04t06.model.token.TokenType;

public class Character {

    private int id;
    private Map<TokenType, Integer> tokens;
    private ArrayList<Card> cards;
    private ArrayList<Noble> nobles;
    private int prestigePoints;

    public void addToken(TokenType tokenType, int amt) {
        this.tokens.put(tokenType, this.tokens.get(tokenType) + amt);
    }

    public void removeToken(TokenType tokenType, int amt) {
        this.tokens.put(tokenType, this.tokens.get(tokenType) - amt);
    }

    public int getPrestigePoints() {
        return this.prestigePoints;
    }
}

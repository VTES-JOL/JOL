/*
 * DeckParams.java
 *
 * Created on December 24, 2004, 7:35 PM
 */

package deckserver.util;

import cards.local.NormalizeDeck;
import cards.model.CardEntry;

/**
 *
 * @author  Joe User
 */
public class DeckParams {
    
    String name;
    CardEntry[] cards;
    String type;
    String query;
    NormalizeDeck nd;
    
    public DeckParams(String name, String type, String query, CardEntry[] cards, NormalizeDeck nd) {
        this.name = name;
        this.type = type;
        this.query = query;
        this.cards = cards;
        this.nd = nd;
    }
    
    public String getName() {
        if(name == null) return "";
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getQuery() {
        return query;
    }
    
    public CardEntry[] getCards() {
        return cards;
    }
    
    public String[] getErrors() {
        return nd.getErrorLines();
    }
    
    public NormalizeDeck getDeckObj() {
        return nd;
    }
    
}    

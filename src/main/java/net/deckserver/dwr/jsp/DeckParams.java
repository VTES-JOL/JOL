/*
 * DeckParams.java
 *
 * Created on December 24, 2004, 7:35 PM
 */

package net.deckserver.dwr.jsp;

import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.Deck;

/**
 * @author Joe User
 */
public class DeckParams {

    String name;
    CardEntry[] cards;
    String type;
    String query;
    Deck nd;

    public DeckParams(String name, String type, String query, CardEntry[] cards, Deck nd) {
        this.name = name;
        this.type = type;
        this.query = query;
        this.cards = cards;
        this.nd = nd;
    }

    public String getName() {
        if (name == null) return "";
        return name;
    }

    public String getType() {
        return type;
    }

    public CardEntry[] getCards() {
        return cards;
    }

    public Deck getDeckObj() {
        return nd;
    }

}    

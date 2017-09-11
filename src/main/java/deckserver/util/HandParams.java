/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import deckserver.client.JolGame;
import deckserver.game.state.Card;

/**
 * @author Joe User
 */
public class HandParams {

    private Card[] cards;
    private String text;

    public HandParams(JolGame game, String player, String text, String region) {
        cards = game.getState().getPlayerLocation(player, region).getCards();
        this.text = text;
    }

    public int getSize() {
        return cards.length;
    }

    public String getText() {
        return text;
    }

    public CardParams getCardParam(int i) {
        return new CardParams(cards[i]);
    }
}    

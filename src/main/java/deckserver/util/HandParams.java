/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import nbclient.model.Card;
import nbclient.vtesmodel.JolGame;

/**
 * @author Joe User
 */
public class HandParams {

    Card[] cards;
    String color;
    String text;

    public HandParams(JolGame game, String player, String color, String text, String region) {
        cards = (Card[]) game.getState().getPlayerLocation(player, region).getCards();
        this.color = color;
        this.text = text;
    }

    public int getSize() {
        return cards.length;
    }

    public String getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public CardParams getCardParam(int i) {
        CardParams ret = new CardParams(cards[i]);
        return ret;
    }
}    

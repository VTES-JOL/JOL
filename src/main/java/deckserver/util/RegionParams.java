/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import deckserver.interfaces.Card;
import deckserver.JolGame;

/**
 * @author Joe User
 */
public class RegionParams {

    String text;
    String color;
    String index;
    Card[] cards;
    boolean hidden;

    public RegionParams(JolGame game, String player, int index, String color, String text, String region, boolean hidden) {
        cards = (Card[]) game.getState().getPlayerLocation(player, region).getCards();
        this.color = color;
        this.text = text;
        this.hidden = hidden;
        this.index = region.substring(0, 1).toLowerCase() + index;
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
        return new CardParams(cards[i], hidden);
    }

    public String getIndex() {
        return index;
    }
}    

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
public class RegionParams {

    private String text;
    private String index;
    private Card[] cards;
    private boolean hidden;

    public RegionParams(JolGame game, String player, int index, String text, String region, boolean hidden) {
        cards = (Card[]) game.getState().getPlayerLocation(player, region).getCards();
        this.text = text;
        this.hidden = hidden;
        this.index = region.substring(0, 1).toLowerCase() + index;
    }

    public int getSize() {
        return cards.length;
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

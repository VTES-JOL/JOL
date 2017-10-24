/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package net.deckserver.dwr.jsp;

import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.interfaces.state.Card;

/**
 * @author Joe User
 */
public class RegionParams {

    private String text;
    private String index;
    private Card[] cards;
    private boolean hidden;

    public RegionParams(JolGame game, String player, int index, String text, String region, String id, boolean hidden) {
        cards = game.getState().getPlayerLocation(player, region).getCards();
        this.text = text;
        this.hidden = hidden;
        this.index = id + index;
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
